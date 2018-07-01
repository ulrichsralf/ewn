package io.mc.game.k

enum class GameState { RUNNING, READY , FINISHED}

class Game(var board: Board? = Board(), var state: GameState = GameState.READY) {
    var me: List<Move>? = null
    var you: List<Move>? = null


    fun isFinished():Boolean {
        return false
    }
    fun move(move: Move) {
        board?.move(move, Board.Player.ME)
    }

    fun moveOpp(move: Move) {
        board?.move(move, Board.Player.YOU)
    }

    fun reset() {
        me = null
        you = null
        board = null
    }

    fun getAllowedMoves(dice: Int) = board?.getAllowedMoves(Board.Token(Board.Player.ME,dice)).orEmpty()

    fun getBestMove(dice: Int) = getAllowedMoves(dice).map { it to  board?.calcScore(it,Board.Player.ME) }
            .sortedBy { it.second }.lastOrNull()?.first

    fun setOppStart(start: List<Move>) {
        you = start
        if (me != null) board = Board(me!!, you!!)
    }

    fun setOwnStart(start: List<Move>) {
        me = start
        if (you != null) board = Board(me!!, you!!)
    }


}


fun main(args: Array<String>) {
    val b = Board()
    println(b)
    println(b.getAllowedMoves(Board.Token(Board.Player.ME, 3)))
    println(b.getAllowedMoves(Board.Token(Board.Player.ME, 3)).map { b.calcScore(it, Board.Player.ME) })
    println(b.getAllowedMoves(Board.Token(Board.Player.YOU, 3)))
    println(b)

}

