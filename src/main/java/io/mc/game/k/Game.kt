package io.mc.game.k

import io.mc.game.k.Board.Directon.UP
import io.mc.game.k.Board.Directon.DOWN
import io.mc.game.k.util.*


data class Token(val key: Int, val own: Boolean)

class Move(val code: Byte) {
    constructor(code: String) : this(code.moveToByte())
    constructor(key: Int, x: Int, y: Int) : this("$key$x$y")

    override fun toString() = code.byteToMove()
    override fun equals(other: Any?): Boolean {
        return other is Move && code == other.code
    }

    override fun hashCode(): Int {
        return code.hashCode()
    }
}

data class MoveResult(var captureOwn: Move? = null,
                      var captureOpp: Move? = null,
                      var own: List<Move> = listOf(),
                      var opp: List<Move> = listOf()) {
    fun setMoves(pos: ByteArray, dir: Board.Directon) {
        if (dir == DOWN) own = pos.toMoveList()
        else opp = pos.toMoveList()
    }
}

class GameContext(val pos: ByteArray,
                  val area: List<ByteArray>,
                  val dir: Board.Directon, val result: MoveResult = MoveResult()) {
}

enum class GameState {
    CLEAN, RUNNING, FINISHED, ERROR
}


class Game(var state: GameState = GameState.CLEAN) {
    var board: Board? = null
    var dir: Board.Directon? = null

    fun reset() {
        board = null
        dir = null
        state = GameState.CLEAN
    }

    fun setStartPos(my: List<Move>, opp: List<Move>) {
        dir = my.getDirection()
        board = Board(my = my.toByteArray(), opp = opp.toByteArray())
        state = GameState.RUNNING
    }

    fun isFinished() = board?.isFinished(dir!!) ?: false

    fun getOwnPos() = board?.my?.toMoveList().orEmpty()


    fun getAllowedMoves(w: Int) = board!!.getAllowedMoves(w, dir!!)
    fun move(m: Move) {
        board?.move(m, dir!!)
    }

    fun moveOpp(m: Move) {
        board?.move(m, dir!!.other())
    }

}


fun main(args: Array<String>) {
    val test = Board(my = listOf("122", "345", "211").toMoveArray(),
            opp = listOf("655", "154").toMoveArray())


    val g = Game()
    // g.setOwnStart(generateStartPosition(true).toMoveList())
    println(g.board)
    println(g.getAllowedMoves(3))
    g.move(g.getAllowedMoves(3).first())
    println(g.board)
    g.move(g.getAllowedMoves(3).first())
    println(g.board)
    g.move(g.getAllowedMoves(3).first())
    println(g.board)
    g.move(g.getAllowedMoves(3).first())
    println(g.board)

}

