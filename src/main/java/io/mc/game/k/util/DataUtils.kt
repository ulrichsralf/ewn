package io.mc.game.k.util

import io.mc.game.k.*
import io.mc.game.k.Board.Directon.*

fun String.moveToByte() = (toInt() - 111).let {
    Integer.valueOf(it.toString(), 6).toByte()
}

fun Byte.byteToMove() = (toInt() and 0xff).toString(6)
        .let { it.toInt() + 111 }.toString()

fun Token.toByte() = (key + (if (own) 10 else 20)).toByte()
fun Byte.toToken() = toInt().let { Token(it % 10, it < 20) }
fun Token.isFree() = key == 7

fun ByteArray.toMoveList(): List<Move> {
    return filterNot { it.isFree() }
            .map { Move(it) }
}

fun ByteArray.getOldPositionByKey(move: Move) =
        Move(this[move.getKey() - 1]).getNumbers()

fun List<Move>.toByteArray(): ByteArray {
    val array = ByteArray(6, { Byte.MAX_VALUE })
    forEach { array[it.getKey() - 1] = it.code }
    return array
}
fun List<String>.toMoveArray() = map { Move(it) }.toByteArray()

fun Move.isAtTop() = getNumbers().takeLast(2).fold(0, { a, b -> a + b }) < 6
fun Byte.isFree() = this == Byte.MAX_VALUE

fun Move.isAt(pos: Byte): Pair<Boolean, Boolean> {
    val a = code.byteToMove()
    val b = pos.byteToMove()
    return (a.first() == b.first()) to (a.drop(1) == b.drop(1))
}

fun Move.getNumbers() = toString().map { it.toString().toInt() }
fun Move.getKey() = getNumbers().first()

fun Board.Directon.other() = if (this == OPP) OWN else OPP

fun <T> robust(body: () -> T): T? {
    try {
        return body()
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}
fun Board.print(): String {
    return area.map {
        it.map {
            val t = it.toToken()
            when {
                t.key == 7 -> " . "
                t.own -> " ${t.key} ".color(ConsoleColor.ANSI_GREEN)
                else -> " ${t.key} ".color(ConsoleColor.ANSI_RED)
            }
        }.joinToString(separator = "")
    }.joinToString(separator = "\n")
            .plus("\nmy:${my.toMoveList()}\nop:${opp.toMoveList()}")
}


fun Board.printMoves(key: Int, directon: Board.Directon) {
    val moves = getAllowedMoves(key, directon).mapIndexed { i, move ->
        move.getNumbers()
                .let { (_, x, y) -> Move(i + 1, x, y) }
    }.toByteArray()
    val b = if (directon == Board.Directon.OWN)
        Board(moves, ByteArray(0))
    else
        Board(ByteArray(0), moves)
    println(b)
}


fun Move.toBitString() = Integer.toBinaryString(this.code.toInt() and 0xff)
