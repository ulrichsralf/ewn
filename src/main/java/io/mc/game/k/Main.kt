package io.mc.game.k

import io.mc.game.k.util.generateStartPosition
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.SendChannel
import kotlinx.coroutines.experimental.channels.actor
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket
import java.util.*


fun main(args: Array<String>) {
    runGame()
}


fun runGame(name: String = "ralf") = runBlocking {
    val g = Game()
    connect("localhost").let { (netOut, netIn) ->
        Runtime.getRuntime().addShutdownHook(Thread {
            launch { netOut.send(Logout) }
            Thread.sleep(200)
        })
        val cl = launch {
            Scanner(System.`in`).useDelimiter("\n")
                    .asSequence()
                    .forEach { netOut.send(ConsoleCommand(it)) }
        }
        launch {

            for (msg in netIn) {
                msg.parseMessage().let {
                    println(it)
                    when (msg.parseResponseCode()) {
                        R.Success -> when {
                            it.isConnected() -> netOut.send(Login(name))
                            it.isLoggedIn() ->{
                                netOut.send(PlayerList)
                                fsm.fire(FSMEvent.LOGIN, mapOf("name" to name))
                            }
                            it.isListe() -> {
                                println(it.getPlayerListe())
                            }
                            else -> Unit
                        }
                        R.Message -> when {
                            it.isStart() -> g.reset()
                            it.isEnd() -> {
                                fsm.fire(FSMEvent.FINISH, mapOf("key" to "peter"))
                                println("Endstand: ")
                                println(g.board)
                            }
                            else -> Unit
                        }
                        R.Move -> when {
                            it.isDice() -> {
                                fsm.fire(FSMEvent.DICE, mapOf("dice" to it.parseDice()))
                                when (g.state) {
                                    GameState.READY -> {
                                        g.setOwnStart(generateStartPosition(false))
                                       netOut.send(MoveC(g.me!!))
                                        g.state = GameState.RUNNING
                                    }
                                    GameState.RUNNING -> {
                                        val m = g.getBestMove(it.parseDice())
                                        println(m)
                                        g.move(m)
                                        Thread.sleep(3000)
                                        netOut.send(MoveC(listOf(m)))
                                        println(g.board)
                                    }
                                    else -> Unit
                                }
                            }
                            it.isMove() -> {
                                fsm.fire(FSMEvent.DICE, mapOf("key" to "peter"))
                                if (g.state != GameState.RUNNING) {
                                    g.setOppStart(it.parseMoves())
                                } else {
                                    g.moveOpp(it.parseMoves().first())
                                }
                                println(g.board)
                            }

                            it.setStart() -> {
                                val start = generateStartPosition(true)
                                g.setOwnStart(start)
                                netOut.send(MoveC(start))
                            }
                            else -> Unit
                        }
                        else -> Unit
                    }
                }
                println(msg)
                if (g.state == GameState.RUNNING && g.isFinished()) {
                    println("Won Game")
                    println(g.board)
                    g.state = GameState.FINISHED
                    netOut.send(Quit)
                }
            }
        }
        cl.join()


    }
}


fun connect(hostname: String): Pair<SendChannel<C>, ReceiveChannel<String>> {
    return Socket(hostname, 1078).let { s ->
        val writer = PrintWriter(s.getOutputStream(), true)
        val reader = BufferedReader(InputStreamReader(s.getInputStream()))

        val netOut = actor<C> {
            for (msg in channel) {
                writer.println(msg)
            }
        }
        val netIn = actor<String> {
            while (true) {
                val msg = reader.readLine()
                channel.send(msg)
                if (msg.contains("disconnect")) break
            }
            s.close()
            System.exit(0)
        }
        netOut to netIn as ReceiveChannel<String>
    }
}