package io.mc.game.k.util

import io.mc.game.k.Move
import java.util.*


fun generateStartPosition(top: Boolean): ByteArray {
    return (
            if (top)
                listOf(1 to 1, 1 to 2, 1 to 3, 2 to 1, 2 to 2, 3 to 1)
            else
                listOf(3 to 5, 4 to 4, 4 to 5, 5 to 3, 5 to 4, 5 to 5)
            ).apply {
        Collections.shuffle(this, Random())
    }.mapIndexed { i, p -> Move("${i + 1}${p.first}${p.second}") }.toByteArray()
}
