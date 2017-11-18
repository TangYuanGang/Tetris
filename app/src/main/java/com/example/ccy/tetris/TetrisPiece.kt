package com.example.ccy.tetris

import java.util.Random

enum class Direction { LEFT, RIGHT ,ROTATE}



class TetrisPiece {
    var pieceShape: Int = 0
    private val random = Random()
    private val coords = Array(4) { IntArray(2) }

    fun setX(index: Int, x: Int) {
        coords[index][0] = x
    }

    fun setY(index: Int, y: Int) {
        coords[index][1] = y
    }

    init {
        setShape(0)
    }

    fun setRandomShape() {
        setShape(random.nextInt(7) + 1)
    }

    fun setShape(shape: Int) {

        for (i in 0..3) {
            for (j in 0..1) {
                coords[i][j] = coordsTable[shape][i][j]
            }
        }
        pieceShape = shape
    }

    fun minX(): Int? = (0..3).map { coords[it][0] }.min()

    fun maxX(): Int? = (0..3).map { coords[it][0] }.max()

    fun minY(): Int? = (0..3).map { coords[it][1] }.min()

    fun maxY(): Int? = (0..3).map { coords[it][1] }.max()


    fun rotatedLeft(): TetrisPiece {//��ת����
        if (pieceShape == 0) {
            return this
        }
        val result = TetrisPiece()
        result.pieceShape = pieceShape
        for (i in 0..3) {
            result.setX(i, y(i))
            result.setY(i, -x(i))
        }
        return result
    }

    fun y(index: Int) = coords[index][1]


    fun x(index: Int) =  coords[index][0]


    fun setNoShape()= setShape(0)

    companion object {
        val coordsTable = arrayOf(
                arrayOf(intArrayOf(0, 0), intArrayOf(0, 0), intArrayOf(0, 0),
                        intArrayOf(0, 0)), arrayOf(intArrayOf(0, -1), intArrayOf(0, 0),
                intArrayOf(-1, 0), intArrayOf(-1, 1)), arrayOf(intArrayOf(0, -1),
                intArrayOf(0, 0), intArrayOf(1, 0), intArrayOf(1, 1)),
                arrayOf(intArrayOf(0, -1), intArrayOf(0, 0),
                        intArrayOf(0, 1), intArrayOf(0, 2)), arrayOf(intArrayOf(-1, 0),
                intArrayOf(0, 0), intArrayOf(1, 0), intArrayOf(0, 1)),
                arrayOf(intArrayOf(0, 0), intArrayOf(1, 0), intArrayOf(0, 1),
                        intArrayOf(1, 1)), arrayOf(intArrayOf(-1, -1),
                intArrayOf(0, -1), intArrayOf(0, 0), intArrayOf(0, 1)),
                arrayOf(intArrayOf(1, -1), intArrayOf(0, -1), intArrayOf(0, 0),
                        intArrayOf(0, 1)))
    }

}
