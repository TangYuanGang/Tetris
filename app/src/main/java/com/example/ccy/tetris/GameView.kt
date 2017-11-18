package com.example.ccy.tetris

import android.graphics.Bitmap

/**
 * Created by ccy on 2017/11/17.
 */
interface GameView {
    fun setImg(bitmap: Bitmap)

    fun setScore(s: Int)
    fun clearScore()

    fun setLines(l: Int)

    fun showLines()

    fun setBtnText()
}