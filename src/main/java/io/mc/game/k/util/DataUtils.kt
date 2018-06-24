package io.mc.game.k.util

import io.mc.game.k.Board
import io.mc.game.k.Board.Directon.DOWN
import io.mc.game.k.Board.Directon.UP
import io.mc.game.k.Move

fun String.moveToByte() = (toInt() - 111).let {
    Integer.valueOf(it.toString(), 6).toByte()
}

fun Byte.byteToMove() = (toInt() and 0xff).toString(6)
        .let { it.toInt() + 111 }.toString()




fun Move.isAtTop() = x + y < 6
fun Byte.isFree() = this == Byte.MAX_VALUE
fun List<Move>.getDirection() = if (first().isAtTop()) DOWN else UP
fun Board.Player.other() = if (this == Board.Player.YOU) Board.Player.ME else Board.Player.YOU



fun Board.getString(): String {
    return area.takeLast(5).joinToString(separator = "\n") {
        it.takeLast(5).joinToString(separator = " ") { it.getString() }
    }

}

fun Board.Token.getString(): String {
    return player.let {
        when (it) {
            Board.Player.ME -> w.toString().color(ConsoleColor.ANSI_GREEN)
            Board.Player.YOU -> w.toString().color(ConsoleColor.ANSI_RED)
            else -> ".".color(ConsoleColor.ANSI_WHITE)
        }
    }
}
