package com.matthiaslapierre.spaceshooter.ui.game.sprite

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.matthiaslapierre.spaceshooter.util.Utils

/**
 * Draws the game background.
 */
class BackgroundSprite(
    private val context: Context
): ISprite {

    private var background: Bitmap? = null
    private var screenWidth: Float = 0f
    private var screenHeight: Float = 0f

    override fun onDraw(canvas: Canvas, globalPaint: Paint, status: Int) {
        screenWidth = canvas.width.toFloat()
        screenHeight = canvas.height.toFloat()
        if(background == null) {
            background = Utils.generateGameBackground(
                context,
                screenWidth.toInt(),
                screenHeight.toInt()
            )
        }
        canvas.drawBitmap(background!!, 0f, 0f, null)
    }

    override fun isAlive(): Boolean = true

    override fun isHit(sprite: ISprite): Boolean = false

    override fun getScore(): Int = 0

    override fun getRectF(): RectF = RectF(
        0f,
        0f,
        screenWidth,
        screenHeight
    )

    override fun onCleared() {
        // Clear the reference to the pixel data.
        background?.recycle()
        background = null
    }

}