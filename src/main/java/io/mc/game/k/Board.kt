package io.mc.game.k

import io.mc.game.k.Board.Directon.DOWN
import io.mc.game.k.Board.Directon.UP
import io.mc.game.k.util.generateStartPosition
import io.mc.game.k.util.getDirection
import io.mc.game.k.util.getString

class Board(me: List<Move> = generateStartPosition(true),
            you: List<Move> = generateStartPosition(false),
            val dir: Directon = me.getDirection()) {


    data class Token(val player: Player = Player.NONE, val w: Int = 0)

    enum class Player { ME, YOU, NONE }
    enum class Directon { DOWN, UP }

    val area: BoardArea = Array(6, { Array(6, { Token() }) })
    private val upMoveFun = { a: Int -> if (a > 2) a - 1 else a }
    private val downMoveFun = { a: Int -> if (a < 6) a + 1 else a }
    private val moveFunMap = mutableMapOf<Player, MoveFun>()


    init {
        me.forEach { area[it.x][it.y] = Token(Player.ME, it.w) }
        you.forEach { area[it.x][it.y] = Token(Player.YOU, it.w) }
        moveFunMap[Player.ME] = if (dir == DOWN) downMoveFun else upMoveFun
        moveFunMap[Player.YOU] = if (dir == UP) downMoveFun else upMoveFun
    }


    fun move(move: Move, player: Player): Board {
        moveToken(move, Token(player, move.w))
        return this
    }

    fun calcScore(move: Move, player: Player): Int {
        val b = this.clone()
        val token = b.moveToken(move, Token(player, move.w))
        val capture = token.player != Player.NONE
        val own = token.player == player
        //val playerCount =  getMoves(player).count()
        //val otherCount = getMoves(player.other()).count()
        val captureF = if (capture) 2 else 1
        //val ownF = if (own) 1 else 2
        return 100 * captureF //* ownF
    }

    private fun getMoves(player: Player): List<Move> {
        return (1..6).mapNotNull { area.search(Token(player, it)) }
    }

    fun clone(): Board {
        return Board(getMoves(Player.ME), getMoves(Player.YOU), dir)
    }

    private fun <Token> Array<Array<Token>>.search(token: Board.Token): Move? {
        for (x in 1..5) {
            for (y in 1..5) {
                if (this[x][y] == token) return Move(token.w, x, y)
            }
        }
        return null
    }

    private fun moveToken(move: Move, token: Token): Token {
        val oldPos = area.search(token) ?: let {
            println(this)
            throw IllegalStateException("Token $token not found, $area")
        }
        area[oldPos.x][oldPos.y] = Token()
        val oldToken = area[move.x][move.y]
        area[move.x][move.y] = token
        return oldToken
    }

    fun getAllowedMoves(token: Board.Token): Set<Move> {
        val result = hashSetOf<Move>()
        val moveFun = moveFunMap[token.player]!!
        val l = (token.w downTo 1).mapNotNull { area.search(token) }.firstOrNull()
        val r = (token.w..6).mapNotNull { area.search(token) }.firstOrNull()
        result.addAll(setOf(l, r).filterNotNull()
                .map {
                    setOf(Move(it.w, moveFun(it.x), it.y),
                            Move(it.w, it.x, moveFun(it.y)),
                            Move(it.w, moveFun(it.x), moveFun(it.y))) - it
                }.flatten())
        return result
    }


    override fun toString(): String {
        return getString()
    }
}


data class Move(val w: Int, val x: Int, val y: Int) {
    constructor(move: String) : this(move.substring(0, 1).toInt(), move.substring(1, 2).toInt(), move.substring(2, 3).toInt())
}

typealias MoveFun = (Int) -> Int
typealias BoardArea = Array<Array<Board.Token>>