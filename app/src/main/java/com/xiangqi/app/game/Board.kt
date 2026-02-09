package com.xiangqi.app.game

/**
 * 棋盘坐标：ICCS 格式
 * 列 a-i (0-8)，行 0-9。红方在下 (行 0-2)，黑方在上 (行 7-9)。
 */
data class Square(val col: Int, val row: Int) {
    fun toIccs(): String {
        require(col in 0..8 && row in 0..9)
        return "${'a' + col}$row"
    }
    companion object {
        fun fromIccs(s: String): Square? {
            if (s.length != 2) return null
            val c = s[0].lowercaseChar()
            val r = s[1]
            if (c !in 'a'..'i' || r !in '0'..'9') return null
            return Square(c - 'a', r - '0')
        }
    }
}

/**
 * 着法：从 from 到 to（ICCS 如 "e3e4"）
 */
data class Move(val from: Square, val to: Square) {
    fun toIccs(): String = "${from.toIccs()}${to.toIccs()}"
    companion object {
        fun fromIccs(s: String): Move? {
            if (s.length != 4) return null
            val from = Square.fromIccs(s.take(2)) ?: return null
            val to = Square.fromIccs(s.drop(2)) ?: return null
            return Move(from, to)
        }
    }
}
