package io.mc.game.k

import io.mc.game.k.util.generateStartPosition
import io.mc.game.k.util.getNumbers
import io.mc.game.k.util.robust
import io.mc.game.k.util.toMoveList
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


fun runGame(name : String = "ralf") = runBlocking {
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
                            it.isLoggedIn() -> netOut.send(PlayerList)
                            it.isListe() -> {
                                println(it.getPlayerListe())
                            }
                            else -> Unit
                        }
                        R.Message -> when {
                            it.isStart() -> g.reset()
                            it.isEnd() -> {
                                println("Endstand: ")
                                println(g.board)
                                g.state = GameState.FINISHED
                            }
                            else -> Unit
                        }
                        R.Move -> when {
                            it.isDice() -> {
                                when (g.state) {
//                                    GameState.OPP_READY -> {
//                                        netOut.send(MoveC(g.getOwnPos()))
//                                        g.state = GameState.RUNNING
//                                    }
                                    GameState.RUNNING -> {
                                        val all = g.getAllowedMoves(it.parseDice())
                                        println("Zug ${it.parseDice()} : Züge: $all")
                                        val m = all.sortedBy { it.getNumbers().let { it[1] + it[2] } }.first()
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
                                if (g.state != GameState.RUNNING) {
                                  //  robust { g.setOppStart(it.parseMoves()) }
                                } else {
                                    robust { g.moveOpp(it.parseMoves().first()) }
                                    println(g.board)
                                }
                            }
                            it.setStart() -> {
                                val start = generateStartPosition(true).toMoveList()
                           //     g.setOwnStart(start)
                                netOut.send(MoveC(start))
                            }
                            else -> {

                            }
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