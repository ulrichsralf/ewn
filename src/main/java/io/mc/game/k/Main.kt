package io.mc.game.k

import io.mc.game.k.util.robust
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


fun main(args: Array<String>) = runBlocking {
    var g = Game()


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
                            it.isConnected() -> netOut.send(Login("ralf"))
                            it.isLoggedIn() -> netOut.send(PlayerList)
                            it.isListe() -> {
                                println(it.getPlayerListe())
                            }
                            else -> Unit
                        }
                        R.Message -> when {
                            it.isStart() -> g = Game()
                            else -> Unit
                        }
                        R.Move -> when {
                            it.isDice() -> {
                                val d = it.parseDice()
                                val am = g.getAllowedMoves(d)
                                g.move(am.first())
                                netOut.send(MoveC(am.take(1)))
                            }
                            else -> {
                                robust { it.parseMoves() }?.let {
                                    if (g.state == GameState.INIT) {
                                        g.setOppStart(it)
                                        netOut.send(MoveC(g.getOwnPos()))
                                    } else {
                                        g.moveOpp(it.first())
                                    }

                                }
                            }
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