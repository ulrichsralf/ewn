package io.mc.game.k

import io.mc.game.k.util.generateStartPosition
import io.mc.game.k.util.isAtTop
import io.mc.game.k.util.print
import io.mc.game.k.util.toMoveList
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

internal class GameTest {

    val testPos1 = listOf(Move(1, 2, 1), Move(2, 1, 2), Move(3, 3, 1), Move(4, 2, 2), Move(5, 1, 3), Move(6, 1, 1))
    val testPos2 = listOf(Move(1, 5, 4), Move(2, 3, 5), Move(3, 5, 3), Move(4, 5, 5), Move(5, 4, 4), Move(6, 4, 5))

    @Test
    fun testStartPos() {
        val g = Game()
        g.setStartPos(generateStartPosition(true).toMoveList(), generateStartPosition(false).toMoveList())
        assertNotNull(g.board)
        val board = g.board!!
        assertNotNull(board.my)
        assertNotNull(board.opp)
        println(g.board!!.print())

        assertTrue { board.my.toMoveList().all { it.isAtTop() } }
        assertTrue { board.opp.toMoveList().all { !it.isAtTop() } }
    }

    @Test
    fun testMoveOwn() {
        val g = Game()
        g.setStartPos(testPos1, testPos2)
        assertEquals(Board.Directon.DOWN, g.dir )
        val m = Move(4, 3, 3)
        println(g.board)
        g.move(m)
        println(g.board)
        assertTrue { g.board!!.my.toMoveList().contains(m) }
        assertFalse { g.board!!.opp.toMoveList().contains(m) }
    }

    @Test
    fun testMoveOwnBottom() {
        val g = Game()
        g.setStartPos(testPos2, testPos1)
        assertEquals(Board.Directon.UP, g.dir )
        val m = Move(4, 3, 3)
        println(g.board)
        g.move(m)
        println(g.board)
        assertTrue { g.board!!.my.toMoveList().contains(m) }
        assertFalse { g.board!!.opp.toMoveList().contains(m) }
    }

    @Test
    fun testMoveOpp() {
        val g = Game()
        g.setStartPos(testPos1, testPos2)

        val m = Move(5, 3, 3)
        println(g.board)
        g.moveOpp(m)
        println(g.board)
        assertTrue { g.board!!.opp.toMoveList().contains(m) }
        assertFalse { g.board!!.my.toMoveList().contains(m) }
    }
}