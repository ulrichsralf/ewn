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
    INIT, STARTED, FINISHED, ERROR
}

class Game(var state: GameState = GameState.INIT) {
    var board: Board? = null
    var dir: Board.Directon? = null

    fun setOwnStart(my: List<Move>, opp: List<Move>) {
        checkError {
            dir = if (my.first().isAtTop()) OWN else OPP
            board = Board(my = my.toByteArray(), opp = opp.toByteArray())
            state = GameState.STARTED
        }
    }

    fun getOwnPos() = board?.my?.toMoveList().orEmpty()
    fun setOppStart(ms: List<Move>) {
        checkError {
            dir = if (ms.first().isAtTop()) OWN else OPP
            board = Board(opp = ms.toByteArray(), my = generateStartPosition(!ms.first().isAtTop()))
            state = GameState.STARTED
        }
    }

    fun getAllowedMoves(w: Int) = board!!.getAllowedMoves(w, dir!!)
    fun move(m: Move) {
        checkError { board?.move(m, dir!!) }
    }

    fun moveOpp(m: Move) {
        checkError { board?.move(m, dir!!.other()) }
    }

    private fun checkError(b: () -> Unit) {
        try {
            b()
        } catch (e: Exception) {
            state = GameState.ERROR
        }
    }
}


fun main(args: Array<String>) {
    val test = Board(my = listOf("122", "345", "211").toMoveArray(),
            opp = listOf("655", "154").toMoveArray())


    val g = Game()
    g.setOwnStart(generateStartPosition(true).toMoveList(),
            generateStartPosition(false).toMoveList())

    println(g.board)
    val g2 = Game()
    g2.setOppStart(generateStartPosition(true).toMoveList())
    println(g2.board)
    val g3 = Game()
    g3.setOppStart(generateStartPosition(false).toMoveList())
    println(g3.board)
    g3.move(g3.getAllowedMoves(3).first())
    println(g3.board)


}

