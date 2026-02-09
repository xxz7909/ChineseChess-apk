package com.xiangqi.app

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.xiangqi.app.databinding.ActivityMainBinding
import com.xiangqi.app.engine.EngineHelper
import com.xiangqi.app.engine.UciEngine
import com.xiangqi.app.game.GameResult
import com.xiangqi.app.game.GameState
import com.xiangqi.app.game.Move

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var gameState: GameState = GameState()
    private var engine: UciEngine? = null
    private var engineReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.board.gameState = gameState
        binding.board.onMoveSelected = { move -> onHumanMove(move) }

        if (!EngineHelper.hasEngineInAssets(this)) {
            binding.statusText.text = getString(R.string.engine_error)
            Toast.makeText(this, getString(R.string.engine_error), Toast.LENGTH_LONG).show()
        } else {
            initEngine()
        }

        binding.btnNewGame.setOnClickListener { newGame() }
    }

    private fun initEngine() {
        val engineDir = EngineHelper.prepareEngine(this)
        // 最强难度：每步思考 4 秒
        engine = UciEngine(engineDir, movetimeMs = 4000)
        engineReady = engine?.start() == true
        if (!engineReady) {
            binding.statusText.text = getString(R.string.engine_error)
        } else {
            binding.statusText.text = getString(R.string.your_turn)
        }
    }

    private fun onHumanMove(move: Move) {
        val state = gameState
        if (state.sideToMove != com.xiangqi.app.game.Side.RED) return
        if (!state.applyMove(move)) return
        binding.board.gameState = state
        binding.board.invalidate()

        if (state.gameOver != null) {
            showGameOver(state.gameOver!!)
            return
        }

        binding.statusText.text = getString(R.string.thinking)
        binding.board.enabledInput = false
        engine?.getBestMoveAsync(state.toFen()) { bestMove ->
            runOnUiThread {
                binding.board.enabledInput = true
                if (bestMove == null) {
                    binding.statusText.text = getString(R.string.your_turn)
                    Toast.makeText(this, "引擎未返回着法", Toast.LENGTH_SHORT).show()
                    return@runOnUiThread
                }
                val engineMove = Move.fromIccs(bestMove)
                if (engineMove != null && gameState.applyMove(engineMove)) {
                    binding.board.gameState = gameState
                    binding.board.invalidate()
                    if (gameState.gameOver != null) {
                        showGameOver(gameState.gameOver!!)
                    } else {
                        binding.statusText.text = getString(R.string.your_turn)
                    }
                } else {
                    binding.statusText.text = getString(R.string.your_turn)
                }
            }
        }
    }

    private fun showGameOver(result: GameResult) {
        val msg = when (result) {
            GameResult.RED_WINS -> getString(R.string.red_win)
            GameResult.BLACK_WINS -> getString(R.string.black_win)
            GameResult.DRAW -> getString(R.string.draw)
        }
        binding.statusText.text = msg
        AlertDialog.Builder(this)
            .setMessage(msg)
            .setPositiveButton(getString(R.string.new_game)) { _, _ -> newGame() }
            .setCancelable(false)
            .show()
    }

    private fun newGame() {
        gameState = GameState()
        binding.board.gameState = gameState
        binding.board.selectedSquare = null
        binding.board.enabledInput = true
        binding.board.invalidate()
        binding.statusText.text = getString(R.string.your_turn)
    }

    override fun onDestroy() {
        engine?.stop()
        super.onDestroy()
    }
}
