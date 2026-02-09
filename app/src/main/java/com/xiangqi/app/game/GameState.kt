package com.xiangqi.app.game

/**
 * 中国象棋标准起始 FEN（Pikafish / UCI 通用）
 * 红方在下（小写为黑，大写为红）
 */
const val START_FEN = "rnbakabnr/9/1c5c1/p1p1p1p1p/9/9/P1P1P1P1P/1C5C1/9/RNBAKABNR r - - 0 1"

class GameState(fen: String = START_FEN) {
    val board: Array<Array<Piece?>> = Array(10) { Array(9) { null } }
    var sideToMove: Side = Side.RED
        private set
    var gameOver: GameResult? = null
        private set
    var lastMove: Move? = null
        private set

    init {
        parseFen(fen)
    }

    private fun parseFen(fen: String) {
        val parts = fen.trim().split("\\s+".toRegex())
        val boardFen = parts.getOrNull(0) ?: return
        sideToMove = if (parts.getOrNull(1)?.lowercase() == "b") Side.BLACK else Side.RED
        var row = 0
        var col = 0
        for (c in boardFen) {
            when {
                c == '/' -> { row++; col = 0 }
                c.isDigit() -> col += c.digitToInt()
                else -> {
                    val p = PieceType.fromFen(c) ?: continue
                    if (row in 0..9 && col in 0..8) {
                        board[row][col] = Piece(p.first, p.second)
                    }
                    col++
                }
            }
        }
    }

    fun toFen(): String {
        val sb = StringBuilder()
        for (r in 0..9) {
            if (r > 0) sb.append('/')
            var empty = 0
            for (c in 0..8) {
                val piece = board[r][c]
                if (piece == null) {
                    empty++
                } else {
                    if (empty > 0) { sb.append(empty); empty = 0 }
                    val ch = piece.type.fen
                    sb.append(if (piece.side == Side.RED) ch.uppercaseChar() else ch)
                }
            }
            if (empty > 0) sb.append(empty)
        }
        sb.append(" ${if (sideToMove == Side.RED) "r" else "b"} - - 0 1")
        return sb.toString()
    }

    fun pieceAt(sq: Square): Piece? = board.getOrNull(sq.row)?.getOrNull(sq.col)

    fun applyMove(move: Move): Boolean {
        val p = pieceAt(move.from) ?: return false
        if (p.side != sideToMove) return false
        val captured = board[move.to.row][move.to.col]
        board[move.to.row][move.to.col] = p
        board[move.from.row][move.from.col] = null
        sideToMove = if (sideToMove == Side.RED) Side.BLACK else Side.RED
        lastMove = move
        if (captured?.type == PieceType.KING) gameOver = if (captured.side == Side.RED) GameResult.BLACK_WINS else GameResult.RED_WINS
        return true
    }

    fun setGameOver(result: GameResult) { gameOver = result }

    fun copy(): GameState {
        val g = GameState(toFen())
        g.lastMove = lastMove
        g.gameOver = gameOver
        return g
    }
}

enum class GameResult { RED_WINS, BLACK_WINS, DRAW }
