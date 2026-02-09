package com.xiangqi.app.ui

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.xiangqi.app.game.*
import kotlin.math.min

/**
 * 9×10 棋盘视图。红方在下方（row 9），黑方在上方（row 0）。
 */
class BoardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    var gameState: GameState? = null
        set(value) {
            field = value
            invalidate()
        }

    var selectedSquare: Square? = null
        set(value) {
            field = value
            invalidate()
        }

    var onMoveSelected: ((Move) -> Unit)? = null
    var enabledInput: Boolean = true

    private val boardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#c4a35a")
        style = Paint.Style.FILL
    }
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#2c1810")
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }
    private val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#c9a227")
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }
    private val lastMovePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#4a7c59")
        style = Paint.Style.FILL
        alpha = 120
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }

    private var cellW = 0f
    private var cellH = 0f
    private var boardLeft = 0f
    private var boardTop = 0f

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val pad = 24f
        val usableW = w - 2 * pad
        val usableH = h - 2 * pad
        // 9 列 8 格宽，10 行 9 格高
        cellW = min(usableW / 8f, usableH / 9f)
        cellH = cellW
        boardLeft = pad + (usableW - 8 * cellW) / 2
        boardTop = pad + (usableH - 9 * cellH) / 2
        textPaint.textSize = cellW * 0.5f
    }

    override fun onDraw(canvas: Canvas) {
        if (cellW <= 0) return
        val state = gameState ?: return

        // 棋盘背景
        val r = RectF(boardLeft - 8, boardTop - 8, boardLeft + 8 * cellW + 8, boardTop + 9 * cellH + 8)
        canvas.drawRoundRect(r, 8f, 8f, boardPaint)

        // 楚河汉界
        linePaint.strokeWidth = 2f
        canvas.drawLine(boardLeft, boardTop + 4.5f * cellH, boardLeft + 8 * cellW, boardTop + 4.5f * cellH, linePaint)
        val riverText = Paint(linePaint).apply { color = Color.parseColor("#2c1810"); textSize = cellW * 0.4f; textAlign = Paint.Align.CENTER }
        canvas.drawText("楚 河        汉 界", boardLeft + 4 * cellW, boardTop + 4.5f * cellH + riverText.textSize / 2, riverText)

        // 网格：8 条竖线，9 条横线（中间断开）
        linePaint.strokeWidth = 2f
        for (i in 0..8) {
            val x = boardLeft + i * cellW
            canvas.drawLine(x, boardTop, x, boardTop + 4 * cellH, linePaint)
            canvas.drawLine(x, boardTop + 5 * cellH, x, boardTop + 9 * cellH, linePaint)
        }
        for (j in 0..9) {
            val y = boardTop + j * cellH
            canvas.drawLine(boardLeft, y, boardLeft + 8 * cellW, y, linePaint)
        }
        // 九宫斜线
        canvas.drawLine(boardLeft + 3 * cellW, boardTop, boardLeft + 5 * cellW, boardTop + 2 * cellH, linePaint)
        canvas.drawLine(boardLeft + 5 * cellW, boardTop, boardLeft + 3 * cellW, boardTop + 2 * cellH, linePaint)
        canvas.drawLine(boardLeft + 3 * cellW, boardTop + 7 * cellH, boardLeft + 5 * cellW, boardTop + 9 * cellH, linePaint)
        canvas.drawLine(boardLeft + 5 * cellW, boardTop + 7 * cellH, boardLeft + 3 * cellW, boardTop + 9 * cellH, linePaint)

        // 上次着法高亮
        state.lastMove?.let { move ->
            val fx = boardLeft + move.from.col * cellW + cellW / 2
            val fy = boardTop + move.from.row * cellH + cellH / 2
            val tx = boardLeft + move.to.col * cellW + cellW / 2
            val ty = boardTop + move.to.row * cellH + cellH / 2
            canvas.drawCircle(fx, fy, cellW * 0.35f, lastMovePaint)
            canvas.drawCircle(tx, ty, cellW * 0.35f, lastMovePaint)
        }

        // 选中格
        selectedSquare?.let { sq ->
            val cx = boardLeft + sq.col * cellW + cellW / 2
            val cy = boardTop + sq.row * cellH + cellH / 2
            canvas.drawCircle(cx, cy, cellW * 0.4f, highlightPaint)
        }

        // 棋子
        for (row in 0..9) {
            for (col in 0..8) {
                val piece = state.pieceAt(Square(col, row)) ?: continue
                val cx = boardLeft + col * cellW + cellW / 2
                val cy = boardTop + row * cellH + cellH / 2
                textPaint.color = if (piece.side == Side.RED) Color.parseColor("#c62828") else Color.parseColor("#1b1b1b")
                val shadow = Paint(textPaint).apply { color = Color.BLACK; alpha = 80 }
                canvas.drawText(piece.type.displayName(piece.side), cx + 1f, cy + 1f, shadow)
                canvas.drawText(piece.type.displayName(piece.side), cx, cy, textPaint)
            }
        }
    }

    private fun squareAt(x: Float, y: Float): Square? {
        val col = ((x - boardLeft) / cellW).toInt()
        val row = ((y - boardTop) / cellH).toInt()
        if (col in 0..8 && row in 0..9) return Square(col, row)
        return null
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!enabledInput) return false
        val state = gameState ?: return false
        if (state.gameOver != null) return false
        if (event.action != MotionEvent.ACTION_UP) return true
        val sq = squareAt(event.x, event.y) ?: return true
        val selected = selectedSquare
        if (selected == null) {
            val piece = state.pieceAt(sq)
            if (piece != null && piece.side == Side.RED) selectedSquare = sq
            return true
        }
        if (selected == sq) {
            selectedSquare = null
            return true
        }
        val move = Move(selected, sq)
        if (state.pieceAt(sq)?.side == Side.RED) {
            selectedSquare = sq
            return true
        }
        onMoveSelected?.invoke(move)
        selectedSquare = null
        return true
    }
}
