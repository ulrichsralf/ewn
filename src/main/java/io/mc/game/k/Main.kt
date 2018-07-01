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


val connection = connect("localhost")

fun main(args: Array<String>) {
    runGame()
}


fun runGame(name: String = "ralf") = runBlocking {

    connection.let { (netOut, netIn) ->
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
                    println(fsm.currentState)
                    when (msg.parseResponseCode()) {
                        R.Success -> when {
                            it.isConnected() -> fsm.fire(FSMEvent.CONNECT, mapOf("name" to names.random()))
                            it.isLoggedIn() -> fsm.fire(FSMEvent.LOGIN)
                            it.isAccepted() -> fsm.fire(FSMEvent.ACCEPT)
                            it.isListe() -> fsm.fire(FSMEvent.LISTE, mapOf("liste" to it.getPlayerListe()))
                            else -> Unit
                        }
                        R.Message -> when {
                            it.isStart() -> fsm.fire(FSMEvent.RESTART)
                            it.isEnd() -> fsm.fire(FSMEvent.FINISH)
                            it.isRequested() -> fsm.fire(FSMEvent.REQUESTED)
                            else -> Unit
                        }
                        R.GameRequestRejected -> fsm.fire(FSMEvent.REJECTED)
                        R.NameConflict -> fsm.fire(FSMEvent.NAME_TAKEN)
                        R.Move -> when {
                            it.isDice() -> fsm.fire(FSMEvent.DICE, mapOf("dice" to it.parseDice()))
                            it.isMove() -> {
                                if (it.message == "E01") {
                                    fsm.fire(FSMEvent.WRONG_MOVE)
                                } else {
                                    fsm.fire(FSMEvent.MOVE, mapOf("move" to it.parseMoves()))
                                }
                            }
                            it.setStart() -> fsm.fire(FSMEvent.SET_ME_TOKEN)
                            else -> Unit
                        }
                        else -> Unit
                    }
                }
                println(msg)
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

