package io.mc.game.k

import io.mc.game.k.Board.Directon.OPP
import io.mc.game.k.Board.Directon.OWN
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
        if (dir == OWN) own = pos.toMoveList()
        else opp = pos.toMoveList()
    }
}

class GameContext(val pos: ByteArray,
                  val area: List<ByteArray>,
                  val dir: Board.Directon, val result: MoveResult = MoveResult()) {
}

enum class GameState {
    NEW, INIT_OPP, INIT_OWN, STARTED, FINISHED, ERROR
}

class Game(var state: GameState = GameState.NEW) {
    var board: Board? = null
    var dir: Board.Directon? = null
    var own: List<Move>? = null

    fun setOwnStart(my: List<Move>) {

        dir = if (my.first().isAtTop()) OWN else OPP
        own = my
        state = GameState.INIT_OWN

    }

    fun isFinished() = board?.isFinished(dir!!) ?: false

    fun getOwnPos() = board?.my?.toMoveList().orEmpty()

    fun setOppStart(ms: List<Move>) {

        dir = if (ms.first().isAtTop()) OWN else OPP
        board = Board(opp = ms.toByteArray(), my = own?.toByteArray()
                ?: generateStartPosition(!ms.first().isAtTop()))
        state = GameState.INIT_OPP

    }

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
    g.setOwnStart(generateStartPosition(true).toMoveList())
    g.setOppStart(generateStartPosition(false).toMoveList())
    println(g.board)
    println(g.getAllowedMoves(3))


}

