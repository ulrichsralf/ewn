package io.mc.game.k

import org.squirrelframework.foundation.fsm.StateMachineBuilderFactory
import org.squirrelframework.foundation.fsm.annotation.StateMachineParameters
import org.squirrelframework.foundation.fsm.impl.AbstractUntypedStateMachine


/**
 * ralf on 6/24/18.
 */
enum class FSMEvent {
    LOGIN,
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


    fun startToLogin(from: String, to: String, event: FSMEvent, context: Map<String, String>) {
        println("from $from to $to event $event context $context")
    }

    fun loggedInToRequestedGame(from: String, to: String, event: FSMEvent, context: Map<String, String>) {
        println("from $from to $to event $event context $context")
    }

    fun loggedInToAccepted(from: String, to: String, event: FSMEvent, context: Map<String, String>) {
        println("from $from to $to event $event context $context")
    }

    fun acceptedToMeTokenSet(from: String, to: String, event: FSMEvent, context: Map<String, String>) {
        println("from $from to $to event $event context $context")
    }

    fun requestToSetYouToken(from: String, to: String, event: FSMEvent, context: Map<String, String>) {
        println("from $from to $to event $event context $context")
    }

    fun youTokenSetToPlaying(from: String, to: String, event: FSMEvent, context: Map<String, String>) {
        println("from $from to $to event $event context $context")
    }

    fun meTokenSetToPlaying(from: String, to: String, event: FSMEvent, context: Map<String, String>) {
        println("from $from to $to event $event context $context")
    }

    fun playingToEnd(from: String, to: String, event: FSMEvent, context: Map<String, String>) {
        println("from $from to $to event $event context $context")
    }

    fun restart(from: String, to: String, event: FSMEvent, context: Map<String, String>) {
        println("from $from to $to event $event context $context")
    }
}

val builder = StateMachineBuilderFactory.create(EwnStateMachine::class.java).apply {
    externalTransition()
            .from("STARTED")
            .to("LOGGED_IN")
            .on(FSMEvent.LOGIN)
            .callMethod("startToLogin")
    externalTransition()
            .from("LOGGED_IN")
            .to("GAME_REQUESTED")
            .on(FSMEvent.REQUEST)
            .callMethod("loggedInToRequestedGame")
    externalTransition()
            .from("GAME_REQUESTED")
            .to("GAME_ACCEPTED")
            .on(FSMEvent.ACCEPT)
            .callMethod("requestedToAccepted")
    externalTransition()
            .from("GAME_ACCEPTED")
            .to("YOU_TOKEN_SET")
            .on(FSMEvent.DICE)
            .callMethod("acceptToSetYouToken")
    externalTransition()
            .from("YOU_TOKEN_SET")
            .to("PLAYING")
            .on(FSMEvent.SET_ME_TOKEN)
            .callMethod("youTokenSetToPlaying")
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
            .to("FINISHED")
            .on(FSMEvent.FINISH)
            .callMethod("playingToEnd")
    externalTransition()
            .from("FINISHED")
            .to("STARTED")
            .on(FSMEvent.RESTART)
            .callMethod("restart")
}
val fsm = builder.newStateMachine("STARTED")



fun main(args: Array<String>) {

    println(fsm.currentState)
    fsm.fire(FSMEvent.LOGIN, mapOf("key" to "peter"))
    fsm.fire(FSMEvent.REQUEST, mapOf("key" to "peter"))
    fsm.fire(FSMEvent.SET_YOU_TOKEN, mapOf("key" to "peter"))
    fsm.fire(FSMEvent.SET_ME_TOKEN, mapOf("key" to "peter"))
    println(fsm.currentState)
    fsm.fire(FSMEvent.FINISH, mapOf("key" to "peter"))
    println(fsm.currentState)
    fsm.fire(FSMEvent.RESTART, mapOf("key" to "peter"))
    println(fsm.currentState)
}