package com.example.ccy.tetris


import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.DisplayMetrics
import android.view.View
import android.view.View.OnClickListener
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.example.ccy.tetris.R

class GameActivity : AppCompatActivity(), OnClickListener,GameView {
    private var score: Int = 0
    private var lines: Int = 0
    private var up: Button? = null
    private var down: Button? = null
    private var right: Button? = null
    private var left: Button? = null
    var img: ImageView? = null
    private var startButton: Button? = null
    private var restartButton: Button? = null
    private var gameBoard: TetrisBoard? = null
    private var highScoreLabel: TextView? = null
    private var scoreLabel: TextView? = null
    private var linesLabel: TextView? = null
    private val screenWidth: Int
        get() {
            val wm = this.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val outMetrics = DisplayMetrics()
            wm.defaultDisplay.getMetrics(outMetrics)

            return outMetrics.widthPixels
        }

    private val highScore: Int
        get() = getPreferences(Context.MODE_PRIVATE).getInt("highscore", 0)

    val state: String
        get() = startButton!!.text.toString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_game)

        getLayoutData()
        initView()

    }

    private fun initView() {
        img = findViewById<View>(R.id.iv_next) as ImageView
        startButton = findViewById<View>(R.id.bt_start) as Button
        restartButton = findViewById<View>(R.id.bt_restart) as Button
        gameBoard = findViewById<View>(R.id.tb_game) as TetrisBoard
        highScoreLabel = findViewById<View>(R.id.tv_highScore) as TextView
        scoreLabel = findViewById<View>(R.id.tv_score) as TextView
        linesLabel = findViewById<View>(R.id.tv_lines) as TextView
        gameBoard!!.mView = this
        up = findViewById<View>(R.id.bt_up) as Button
        down = findViewById<View>(R.id.bt_down) as Button
        right = findViewById<View>(R.id.bt_right) as Button
        left = findViewById<View>(R.id.bt_left) as Button
        startButton!!.setOnClickListener(this)
        restartButton!!.setOnClickListener(this)
        up!!.setOnClickListener(this)
        down!!.setOnClickListener(this)
        right!!.setOnClickListener(this)
        left!!.setOnClickListener(this)
        score = 0
    }

    override fun onResume() {

        startButton!!.text = "继续"
        gameBoard!!.setNextPiece()
        gameBoard!!.setDropLines(getPreferences(Context.MODE_PRIVATE).getInt("lines", 0))
        setLines(getPreferences(Context.MODE_PRIVATE).getInt("lines", 0))
        setScore(getPreferences(Context.MODE_PRIVATE).getInt("score", 0))
        gameBoard!!.curX = getPreferences(Context.MODE_PRIVATE).getInt("curx", 0)
        gameBoard!!.curY = getPreferences(Context.MODE_PRIVATE).getInt("cury", 0)
        gameBoard!!.shape = getPreferences(Context.MODE_PRIVATE).getInt("cshape", 0)
        for (i in 0 until TetrisUtil.BoardHeightSize) {
            for (j in 0 until TetrisUtil.BoardWidthSize) {
                gameBoard!!.board[i][j].color = getPreferences(Context.MODE_PRIVATE).getInt("" + i * TetrisUtil.BoardWidthSize + j, 0)
            }
        }
        for (i in 0..3) {
            val x = getPreferences(Context.MODE_PRIVATE).getInt("cx" + i, 0)
            val y = getPreferences(Context.MODE_PRIVATE).getInt("cy" + i, 0)

            gameBoard!!.setCurPiece(i, x, y)
        }
        gameBoard!!.isPaused =true
        gameBoard!!.invalidate()
        super.onResume()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.bt_start -> {
                if (startButton!!.text.toString() == "") {
                    return
                }
                if (startButton!!.text.toString() == "继续") {
                    gameBoard!!.start()
                    startButton!!.text = "暂停"
                    return
                } else if (startButton!!.text.toString() == "暂停") {
                    startButton!!.text = "继续"
                    gameBoard!!.pause()
                }
            }
            R.id.bt_restart -> {
                startButton!!.text = "暂停"
                gameBoard!!.restart()
            }
            R.id.bt_up -> gameBoard!!.move(Direction.ROTATE)
            R.id.bt_right -> gameBoard!!.move(Direction.RIGHT)
            R.id.bt_left -> gameBoard!!.move(Direction.LEFT)
            R.id.bt_down -> gameBoard!!.drop()
            else -> {
            }
        }

    }

    private fun getLayoutData() {

        TetrisUtil.BoardWidth = screenWidth * 10 / 16
        TetrisUtil.BoardHeight = TetrisUtil.BoardWidth * 2
        TetrisUtil.squareHeight = TetrisUtil.BoardHeight / TetrisUtil.BoardHeightSize
        TetrisUtil.squareWidth = TetrisUtil.BoardWidth / TetrisUtil.BoardWidthSize//�õ��߶�

    }

    override fun onDestroy() {
        val editor = getPreferences(Context.MODE_PRIVATE).edit()
        for (i in 0..3) {
            val x = gameBoard!!.getX(i)
            val y = gameBoard!!.getY(i)
            editor.putInt("cx" + i, x)
            editor.putInt("cy" + i, y)
        }
        editor.putInt("lines", gameBoard!!.getDropLines())
        editor.putInt("score", score)
        editor.putInt("cshape", gameBoard!!.shape)
        editor.putInt("curx", gameBoard!!.curX)
        editor.putInt("cury", gameBoard!!.curY)
        for (i in 0 until TetrisUtil.BoardHeightSize) {
            for (j in 0 until TetrisUtil.BoardWidthSize) {
                editor.putInt("" + i * TetrisUtil.BoardWidthSize + j, gameBoard!!.board[i][j].color)
            }
        }
        editor.commit()
        super.onDestroy()
    }

    override fun setImg(bitmap: Bitmap) {
        img!!.setImageBitmap(bitmap)
    }

    override fun setScore(s: Int) {
        this.score += s
        val max = Math.max(score, highScore)
        saveHighScore(max)
        showScore()

    }

    private fun showScore() {
        scoreLabel!!.text = "SCORE" + "\n" + this.score
        showHighScore()
    }

    private fun saveHighScore(s: Int) {
        val editor = getPreferences(Context.MODE_PRIVATE).edit()
        editor.putInt("highscore", s)
        editor.commit()
    }

    private fun showHighScore() {
        highScoreLabel!!.text = "HIGHSCORE\n" + highScore
    }

    override fun clearScore() {
        this.score = 0
        showScore()
    }

    override fun setLines(l: Int) {
        lines = l
        showLines()
    }

    override fun showLines() {
        linesLabel!!.text = "LINES" + "\n" + this.lines
    }

    override fun setBtnText() {
        startButton!!.text = ""
    }
}
