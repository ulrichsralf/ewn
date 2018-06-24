package io.mc.game.k

import org.squirrelframework.foundation.fsm.StateMachineBuilderFactory
import org.squirrelframework.foundation.fsm.annotation.StateMachineParameters
import org.squirrelframework.foundation.fsm.impl.AbstractUntypedStateMachine


/**
 * ralf on 6/24/18.
 */
enum class FSMEvent {
    LOGIN,
    GET_REQUESTED,
    SET_ME_TOKEN,
    SET_YOUR_TOKEN,
    START_GAME,
    FINISH
}

@StateMachineParameters(
        stateType = String::class,
        eventType = FSMEvent::class,
        contextType = Map::class)
class EwnStateMachine : AbstractUntypedStateMachine() {


    fun fromInitToLogin(from: String, to: String, event: FSMEvent, context: Map<String,String>) {
        println("from $from to $to event $event context $context")
    }

}

fun main(args: Array<String>) {
    val builder = StateMachineBuilderFactory.create(EwnStateMachine::class.java)
    builder.externalTransition()
            .from("UNINITIALIZED")
            .to("LOGGED_IN")
            .on(FSMEvent.LOGIN)
            .callMethod("fromInitToLogin")
    val fsm = builder.newStateMachine("UNINITIALIZED")
    println(fsm.currentState)
    fsm.fire(FSMEvent.LOGIN, mapOf("key" to "peter"))
    println(fsm.currentState)


}