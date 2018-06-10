package io.mc.game.k

import io.mc.game.k.Board.Directon.DOWN
import io.mc.game.k.Board.Directon.UP
import io.mc.game.k.util.*

class Board(val my: List<Move> = generateStartPosition(true),
            val opp: List<Move> = generateStartPosition(false)) {

    val area: List<List<Char>> = (0 until 5).map { (0 until 5).map {'.'} }

    enum class Directon { DOWN, UP }


    init {
        val ownDir = my.toMoveList().getDirection()
            pos.toMoveList()
                    .forEach { p ->
                        p.getNumbers()
                                .let { (k, x, y) ->
                                    area[y - 1][x - 1] =
                                            Token(k, dir == ownDir).toByte()
                                }
                    }
    }

    fun isFinished(dir: Directon): Boolean {
        return (if (dir == DOWN) area[0][0] else area[4][4]).toToken().own
                || opp.none { it != Byte.MAX_VALUE }
    }

    fun move(move: Move, dir: Directon): Int {
        val result = MoveResult(own = my.toMoveList(), opp = opp.toMoveList())
        contextDirection(dir, result) {
            val (_, x, y) = pos.getOldPositionByKey(move)
            area[y - 1][x - 1] = Byte.MAX_VALUE
            val (nk, nx, ny) = move.getNumbers()
            val targetLoc = area[ny - 1][nx - 1]
            area[ny - 1][nx - 1] = Token(nk, dir == DOWN).toByte()
            pos.apply { set(nk - 1, move.code) }
            if (!targetLoc.isFree()) {
                targetLoc.toToken().let { t ->
                    Move("${t.key}$nx$ny").let {
                        if (t.own) result.captureOwn = it
                        else result.captureOpp = it
                    }
                    contextDirection(if (t.own) DOWN else UP, result) {
                        pos.apply { set(t.key - 1, Byte.MAX_VALUE) }
                    }
                }
            }
        }
        return calcMoveScore(result)
    }

    fun getAllowedMoves(w: Int, dir: Directon): Set<Move> {
        val result = hashSetOf<Move>()
        contextDirection(dir) {
            val i = w - 1
            val l = (i downTo 0).firstOrNull { !pos[it].isFree() }?.let { Move(pos[it]) }
            val r = (i..5).firstOrNull { !pos[it].isFree() }?.let { Move(pos[it]) }
            result.addAll(setOf(l, r).filterNotNull()
                    .map { it.getNumbers() }
                    .map { n ->
                        val op = if (dir == UP)
                            { a: Int -> if (a < 5) a + 1 else a }
                        else
                            { a: Int -> if (a > 1) a - 1 else a }
                        setOf(Move(n[0], op(n[1]), n[2]),
                                Move(n[0], n[1], op(n[2])),
                                Move(n[0], op(n[1]), op(n[2])))
                                .let { it - Move(n[0], n[1], n[2]) }
                    }.flatten())
        }
        return result
    }

    private fun calcMoveScore(result: MoveResult): Int {
        return 1
    }

    override fun toString(): String {
        return print()
    }
}