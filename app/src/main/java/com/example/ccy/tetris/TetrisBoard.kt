package com.example.ccy.tetris

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import com.example.ccy.tetris.R


class TetrisBoard : View {

    private val colorRectDarwable = intArrayOf(R.drawable.cell_rectangle_0, R.drawable.cell_rectangle_1,
            R.drawable.cell_rectangle_2, R.drawable.cell_rectangle_2,
            R.drawable.cell_rectangle_3, R.drawable.cell_rectangle_4,
            R.drawable.cell_rectangle_5, R.drawable.cell_rectangle_6)
    private val colorTable = intArrayOf(
            Color.parseColor("#000000"), Color.parseColor("#CC6666"), Color.parseColor("#66CC66"), Color.parseColor("#6666CC"), Color.parseColor("#CCCC66"), Color.parseColor("#CC66CC"), Color.parseColor("#66CCCC"), Color.parseColor("#DAAA00"))
    private var dropLines = 0
    private var lines: Int = 0
    private val paint = Paint()
    var board = Array(TetrisUtil.BoardHeightSize) { Array<BoardType>(TetrisUtil.BoardWidthSize){ BoardType(0,false) }}
    private var curPiece: TetrisPiece? = null
    private var nextPiece: TetrisPiece? = null
    var curX: Int = 0
    var curY: Int = 0
    var isPaused: Boolean = false
    var isStarted: Boolean = false
    private var isGameOver: Boolean = false
    var thread = GameThread()
    private var isWaitPiece = true
    private var gameOver: Drawable? = null
    var mView: GameView? = null

    var transfer = 0
    var isCreate: Boolean = true
    var shape: Int
        get() = curPiece!!.pieceShape
        set(value) {
            curPiece!!.pieceShape = value
        }

    var gameWidth = 0
    var gameHeight = 0

    constructor(context: Context) : super(context, null)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        gameOver = context.resources
                .getDrawable(R.drawable.fade_rectangle)
        curPiece = TetrisPiece()
        nextPiece = TetrisPiece()
    }

    private fun clearBoard() {

        for (i in 0 until TetrisUtil.BoardHeightSize) {
            for (j in 0 until TetrisUtil.BoardWidthSize) {
                board[i][j].color = 0
                board[i][j].finished = false
            }

        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        gameHeight = h
        gameWidth = w
    }

    private fun newPiece() {

        dropLines = 0
        curPiece = nextPiece
        nextPiece = TetrisPiece()
        nextPiece!!.setRandomShape()
        showNextPiece()
        curX = TetrisUtil.BoardWidthSize / 2
        curY = TetrisUtil.BoardHeightSize - 1 + curPiece!!.minY()!!
        isCreate = true

        if (!tryMove(curPiece, curX, curY - 1)) {
            curPiece!!.setShape(0)
            nextPiece!!.setShape(0)
            isGameOver = true
            isStarted = false
            isPaused = false
            isWaitPiece = true
            return
        }
        startAnimator()
        isWaitPiece = false
    }

    private fun clearCurrentPiece() {
        for (i in 0..3) {
            val x = curX + curPiece!!.x(i)
            val y = curY - curPiece!!.y(i)
            board[y][x].color = 0

        }
    }

    private fun recoveryCurPiece() {
        for (i in 0..3) {
            val x = curX + curPiece!!.x(i)
            val y = curY - curPiece!!.y(i)
            board[y][x].color = curPiece!!.pieceShape
        }

    }
    private fun setFinished(finished: Boolean) {
        for (i in 0..3) {
            val x = curX + curPiece!!.x(i)
            val y = curY - curPiece!!.y(i)
            board[y][x].finished = finished
        }

    }

    private fun tryMove(newPiece: TetrisPiece?, newX: Int, newY: Int):Boolean{
        clearCurrentPiece()
        for (i in 0..3) {
            val x = newX + newPiece!!.x(i)
            val y = newY - newPiece.y(i)
            if (x < 0 || x >= TetrisUtil.BoardWidthSize || y >= TetrisUtil.BoardHeightSize) {
                recoveryCurPiece()
                return false
            }
            if(y < 0){
                recoveryCurPiece()
                setFinished(true)
                return false
            }
            if (board[y][x].color != 0) {
                recoveryCurPiece()
                setFinished(true)//在这里有bug
                return false
            }
        }

        curPiece = newPiece
        curX = newX
        curY = newY
        recoveryCurPiece()
        return true

    }



    fun move(direction: Direction) {
        if (!isStarted) {
            return
        }
        when (direction) {
            Direction.LEFT -> {

                if (tryMove(curPiece, curX - 1, curY))
                    invalidate()
            }
            Direction.RIGHT -> {
                if (tryMove(curPiece, curX + 1, curY))
                    invalidate()
            }
            Direction.ROTATE -> {
                if (curPiece!!.pieceShape == 5)
                    return
                if (tryMove(curPiece!!.rotatedLeft(), curX, curY))
                    invalidate()
            }

        }

    }


    private fun dropOneLine() {
        if (!isStarted) {
            return
        }
        if (tryMove(curPiece, curX, curY - 1)) {
            startAnimator()
            dropLines++
            if (dropLines % 5 == 0)
                mView!!.setScore(dropLines)
        } else {
            isWaitPiece = true
        }

    }

    fun drop() {
        if (!isStarted) {
            return
        }
        var l = 0
        while(tryMove(curPiece, curX, curY - 1)){
            l++
            dropLines++
            if (dropLines % 5 == 0)
                mView!!.setScore(dropLines)
        }
        curY += l
        startAnimator(l*TetrisUtil.squareWidth)
        curY -=l
        isWaitPiece = true

    }


    private fun removeFullLInes() {

        var numFullines = 0
        var i = 0
        while (i < TetrisUtil.BoardHeightSize) {
            val lineFull = (0 until TetrisUtil.BoardWidthSize).none { board[i][it].color == 0 }
            if (lineFull) {
                numFullines++
                for (k in i until TetrisUtil.BoardHeightSize - 1) {

                    for (j in 0 until TetrisUtil.BoardWidthSize) {
                        board[k][j].color = board[k + 1][j].color
                        board[k][j].finished = board[k + 1][j].finished
                    }
                }
                for (j in 0 until TetrisUtil.BoardWidthSize) {
                    board[TetrisUtil.BoardHeightSize - 1][j].color = 0
                    board[TetrisUtil.BoardHeightSize - 1][j].finished = false
                }
                i--
            }
            i++
        }
        if (numFullines > 0) {
            mView!!.setScore(numFullines * 10)
            lines += numFullines
            mView!!.setLines(lines)
        }
        invalidate()
    }

    fun start() {

        isStarted = true
        isPaused = false
        isCreate = true
        recoveryCurPiece()
        handler.removeCallbacks(thread)
        handler.postDelayed(thread, 500)
        isWaitPiece = false
        invalidate()
    }

    fun restart() {

        isCreate = true
        isStarted = true
        isPaused = false
        isGameOver = false
        lines = 0
        dropLines = 0
        mView!!.clearScore()
        clearBoard()
        curPiece!!.setNoShape()
        nextPiece!!.setRandomShape()
        newPiece()
        handler.removeCallbacks(thread)
        handler.postDelayed(thread, 500)
        isWaitPiece = false

    }

    fun pause() {

        isStarted = false
        isPaused = true
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        paint.isAntiAlias = true
        paint.color = Color.GRAY
        paint.strokeWidth = 3f

        drawDrawable(canvas,resources.getDrawable(R.drawable.background_rectangle),0,0,gameWidth,gameHeight)
        for (i in 0 until TetrisUtil.BoardHeightSize) {
            for (j in 0 until TetrisUtil.BoardWidthSize) {
                val shape = board[i][j].color
                val finished = board[i][j].finished
                if (shape != 0 ) {
                    var color = colorRectDarwable[shape - 1]
                    if(!finished)
                    drawSquare(j, i, canvas) { x, y, canvas ->
                        drawDrawable(canvas, resources.getDrawable(color), (x * TetrisUtil.squareWidth + 1),
                                ((TetrisUtil.BoardHeightSize - y - 2) * TetrisUtil.squareHeight + transfer + 1), (x * TetrisUtil.squareWidth + TetrisUtil.squareWidth - 1),
                                ((TetrisUtil.BoardHeightSize - y - 2) * TetrisUtil.squareHeight + transfer + TetrisUtil.squareHeight - 1)) }
                    else
                        drawSquare(j, i, canvas) { x, y, canvas ->
                            drawDrawable(canvas, resources.getDrawable(color), (x * TetrisUtil.squareWidth + 1),
                                    ((TetrisUtil.BoardHeightSize - y - 1) * TetrisUtil.squareHeight  + 1), (x * TetrisUtil.squareWidth + TetrisUtil.squareWidth - 1),
                                    ((TetrisUtil.BoardHeightSize - y - 1) * TetrisUtil.squareHeight  + TetrisUtil.squareHeight - 1)) }
                }
            }
        }
        createEndGameStates(canvas)
    }

    private fun drawSquare( x: Int, y: Int, canvas: Canvas,draw:(Int,Int,Canvas) -> Unit ){
        draw(x,y,canvas)
    }


    inner class GameThread : Runnable {
        override fun run() {
            if (isGameOver) {
                mView!!.setBtnText()
                isStarted = false
                mView!!.clearScore()
                return
            }
            if (isStarted) {
                dropOneLine()
                if (isWaitPiece) {
                    removeFullLInes()
                    newPiece()
                }
            }
            handler.postDelayed(this, 500)

        }
    }

    private fun showNextPiece() {

        val dx = nextPiece!!.maxX()!! - nextPiece!!.minX()!! + 1
        val dy = nextPiece!!.maxY()!! - nextPiece!!.minY()!! + 1
        val bitmap = Bitmap.createBitmap(dx * TetrisUtil.squareWidth, dy * TetrisUtil.squareHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        for (i in 0..3) {
            val x = nextPiece!!.x(i) - nextPiece!!.minX()!!
            val y = nextPiece!!.y(i) - nextPiece!!.minY()!!
            drawDrawable(canvas,resources.getDrawable(colorRectDarwable[nextPiece!!.pieceShape -1]),(x * TetrisUtil.squareWidth + 1), (y * TetrisUtil.squareHeight + 1), (x * TetrisUtil.squareWidth + TetrisUtil.squareWidth - 1), (y * TetrisUtil.squareHeight + TetrisUtil.squareHeight - 1))
        }
        mView!!.setImg(bitmap)
    }

    fun setCurPiece(index: Int, x: Int, y: Int) {
        curPiece!!.setX(index, x)
        curPiece!!.setY(index, y)
    }

    fun getX(index: Int): Int {
        return curPiece!!.x(index)
    }

    fun getY(index: Int) = curPiece!!.y(index)

    fun getDropLines() = lines


    fun setDropLines(l: Int) {
        lines = l
    }

    fun setNextPiece() {
        nextPiece!!.setRandomShape()
        showNextPiece()
    }

    private fun createEndGameStates(canvas: Canvas) {

        val middleX = TetrisUtil.BoardWidth / 2
        val middleY = TetrisUtil.BoardHeight / 2
        if (isPaused) {
            gameOver!!.alpha = 127
            gameOver!!.setBounds(0, 0, TetrisUtil.BoardWidth,
                    TetrisUtil.BoardHeight)
            gameOver!!.draw(canvas)
            gameOver!!.alpha = 255
            paint.color = resources.getColor(R.color.text_black)
            paint.alpha = 255
            paint.textSize = 72f
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("Start", middleX.toFloat(), middleY.toFloat(), paint)
        } else if (isGameOver) {
            gameOver!!.alpha = 127
            gameOver!!.setBounds(0, 0, TetrisUtil.BoardWidth,
                    TetrisUtil.BoardHeight)
            gameOver!!.draw(canvas)
            gameOver!!.alpha = 255
            paint.color = resources.getColor(R.color.text_black)
            paint.alpha = 255
            paint.textSize = 72f
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("GAMEOVER", middleX.toFloat(), middleY.toFloat(), paint)
        }
    }

    private fun drawDrawable(canvas: Canvas, drawable: Drawable, startX: Int, startY: Int, endX: Int, endY: Int) {
        drawable.setBounds(startX, startY, endX, endY)
        drawable.draw(canvas)
    }

    private fun startAnimator(to:Int = TetrisUtil.squareHeight) {
            val valueAnimator = ValueAnimator.ofInt(0,to)
            valueAnimator.duration = 500
            valueAnimator.addUpdateListener { animator ->
                transfer = animator.animatedValue as Int
                invalidate()
            }
            valueAnimator.start()
    }

}
