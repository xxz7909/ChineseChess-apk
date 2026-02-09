package com.xiangqi.app.game

enum class Side { RED, BLACK }

enum class PieceType(val fen: Char, val redName: String, val blackName: String) {
    ROOK('r', "车", "車"),
    KNIGHT('n', "马", "馬"),
    BISHOP('b', "相", "象"),
    ADVISOR('a', "仕", "士"),
    KING('k', "帅", "将"),
    CANNON('c', "炮", "砲"),
    PAWN('p', "兵", "卒");

    fun displayName(side: Side): String = if (side == Side.RED) redName else blackName

    companion object {
        private val byFenLower = values().associateBy { it.fen.lowercaseChar() }
        fun fromFen(c: Char): Pair<PieceType, Side>? {
            val lower = c.lowercaseChar()
            val type = byFenLower[lower] ?: return null
            val side = if (c.isUpperCase()) Side.RED else Side.BLACK
            return type to side
        }
    }
}

data class Piece(val type: PieceType, val side: Side)
