package io.mc.game.k

import io.mc.game.k.util.generateStartPosition
import io.mc.game.k.util.getDirection
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.runBlocking
import org.squirrelframework.foundation.fsm.StateMachineBuilderFactory
import org.squirrelframework.foundation.fsm.StateMachineConfiguration
import org.squirrelframework.foundation.fsm.annotation.StateMachineParameters
import org.squirrelframework.foundation.fsm.impl.AbstractUntypedStateMachine
import java.util.*


/**
 * ralf on 6/24/18.
 */
enum class FSMEvent {
    CONNECT,
    LOGIN,
    LISTE,
    MOVE,
    WRONG_MOVE,
    NAME_TAKEN,
    REJECTED,
    REQUESTED,
    REQUEST,
    ACCEPT,
    DICE,
    SET_ME_TOKEN,
    SET_YOU_TOKEN,
    FINISH,
    RESTART
}

@StateMachineParameters(
        stateType = String::class,
        eventType = FSMEvent::class,
        contextType = Map::class)
class EwnStateMachine : AbstractUntypedStateMachine() {

    val netOut = io.mc.game.k.connection.first
    val game = Game()

    fun connected(from: String, to: String, event: FSMEvent, context: Map<String, String>) = runBlocking {
        val name = context["name"] as String
        netOut.send(Login(name))
    }


    fun loggedIn(from: String, to: String, event: FSMEvent, context: Map<String, String>?) = runBlocking {
        netOut.send(PlayerList)
    }

    fun liste(from: String, to: String, event: FSMEvent, context: Map<String, String>) = runBlocking {
        val liste = context["liste"] as List<String>
        if (liste.isEmpty()) {
            delay(15000)
            netOut.send(PlayerList)
        } else {
            netOut.send(RequestGame(liste.random()))
        }
    }

    fun nameTaken(from: String, to: String, event: FSMEvent, context: Map<String, String>?) = runBlocking {
        netOut.send(Login(names.random()))
    }

    fun requestedGame(from: String, to: String, event: FSMEvent, context: Map<String, String>?) = runBlocking {
        netOut.send(Yes)
    }

    fun gameRejected(from: String, to: String, event: FSMEvent, context: Map<String, String>?) = runBlocking {
        delay(15000)
        netOut.send(PlayerList)
    }

    fun loggedInToAccepted(from: String, to: String, event: FSMEvent, context: Map<String, String>?) {
    }

    fun requestedToAccepted(from: String, to: String, event: FSMEvent, context: Map<String, String>?) {
    }

    fun acceptedToMeTokenSet(from: String, to: String, event: FSMEvent, context: Map<String, String>) {
    }

    fun acceptedToSetYouToken(from: String, to: String, event: FSMEvent, context: Map<String, String>) {
        val startToken = context["move"] as List<Move>
        game.setOppStart(startToken)

    }

    fun setStartPosition(from: String, to: String, event: FSMEvent, context: Map<String, String>) = runBlocking {
        val startPosition = generateStartPosition(game.you?.getDirection() == Board.Directon.UP)
        game.setOwnStart(startPosition)
        netOut.send(MoveC(startPosition))
    }

    fun meTokenSetToPlaying(from: String, to: String, event: FSMEvent, context: Map<String, String>) {
    }

    fun move(from: String, to: String, event: FSMEvent, context: Map<String, String>) = runBlocking {
        val move = context["move"] as List<Move>
        game.moveOpp(move.first())
    }


    fun dice(from: String, to: String, event: FSMEvent, context: Map<String, String>) = runBlocking {
        val dice = context["dice"] as Int
        game.getBestMove(dice)?.let {
            game.move(it)
            netOut.send(MoveC(listOf(it)))
        }

    }

    fun wrongMove(from: String, to: String, event: FSMEvent, context: Map<String, String>?) = runBlocking {
        netOut.send(PlayerList)
    }

    fun won(from: String, to: String, event: FSMEvent, context: Map<String, String>?) = runBlocking {
        println("won")
        netOut.send(PlayerList)
    }

    fun lost(from: String, to: String, event: FSMEvent, context: Map<String, String>?) {
        println("lost")
    }
}

val builder = StateMachineBuilderFactory.create(EwnStateMachine::class.java).apply {
    StateMachineConfiguration.getInstance().enableDebugMode(true)
    externalTransition()
            .from("STARTED")
            .to("CONNECTED")
            .on(FSMEvent.CONNECT)
            .callMethod("connected")
    externalTransition()
            .from("CONNECTED")
            .to("CONNECTED")
            .on(FSMEvent.NAME_TAKEN)
            .callMethod("nameTaken")
    externalTransition()
            .from("CONNECTED")
            .to("LOGGED_IN")
            .on(FSMEvent.LOGIN)
            .callMethod("loggedIn")
    externalTransition()
            .from("LOGGED_IN")
            .to("LOGGED_IN")
            .on(FSMEvent.LISTE)
            .callMethod("liste")
    externalTransition()
            .from("GAME_REJECTED")
            .to("LOGGED_IN")
            .on(FSMEvent.LISTE)
            .callMethod("liste")
    externalTransition()
            .from("LOGGED_IN")
            .to("GAME_REQUESTED")
            .on(FSMEvent.REQUESTED)
            .callMethod("requestedGame")
    externalTransition()
            .from("GAME_REQUESTED")
            .to("GAME_REJECTED")
            .on(FSMEvent.REJECTED)
            .callMethod("gameRejected")
    externalTransition()
            .from("GAME_REQUESTED")
            .to("GAME_ACCEPTED")
            .on(FSMEvent.ACCEPT)
            .callMethod("requestedToAccepted")
    externalTransition()
            .from("GAME_ACCEPTED")
            .to("YOU_TOKEN_SET")
            .on(FSMEvent.MOVE)
            .callMethod("acceptedToSetYouToken")
    externalTransition()
            .from("YOU_TOKEN_SET")
            .to("PLAYING")
            .on(FSMEvent.DICE)
            .callMethod("setStartPosition")
    externalTransition()
            .from("PLAYING")
            .to("PLAYING")
            .on(FSMEvent.MOVE)
            .callMethod("move")
    externalTransition()
            .from("PLAYING")
            .to("PLAYING")
            .on(FSMEvent.DICE)
            .callMethod("dice")
    externalTransition()
            .from("PLAYING")
            .to("LOGGED_IN")
            .on(FSMEvent.WRONG_MOVE)
            .callMethod("wrongMove")
    externalTransition()
            .from("LOGGED_IN")
            .to("GAME_ACCEPTED")
            .on(FSMEvent.ACCEPT)
            .callMethod("loggedInToAccepted")
    externalTransition()
            .from("GAME_ACCEPTED")
            .to("ME_TOKEN_SET")
            .on(FSMEvent.SET_ME_TOKEN)
            .callMethod("acceptedToMeTokenSet")
    externalTransition()
            .from("ME_TOKEN_SET")
            .to("PLAYING")
            .on(FSMEvent.SET_YOU_TOKEN)
            .callMethod("meTokenSetToPlaying")
    externalTransition()
            .from("PLAYING")
            .to("LOGGED_IN")
            .on(FSMEvent.FINISH)
            .callMethod("won")
}
val fsm = builder.newStateMachine("STARTED")


val names = (1..100).map { "Ralf-Bot-$it" }
fun <T> List<T>.random() = this[Random().nextInt(size)]