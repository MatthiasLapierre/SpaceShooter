package com.matthiaslapierre.spaceshooter.ui.game.sprite

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import com.matthiaslapierre.spaceshooter.R

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
            background = generateBackground(screenWidth.toInt(), screenHeight.toInt())
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
        background?.recycle()
        background = null
    }

    private fun generateBackground(screenWidth: Int, screenHeight: Int): Bitmap {
        val bgBmp = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.RGB_565)
        val canvas = Canvas(bgBmp)
        val tile = BitmapFactory.decodeResource(context.resources, R.drawable.bg)
        var left = 0f
        var top = 0f
        while (left < screenWidth) {
            while (top < screenHeight) {
                canvas.drawBitmap(
                    tile,
                    left,
                    top,
                    null)
                top += tile.height
            }
            top = 0f
            left += tile.width
        }
        tile.recycle()
        return bgBmp
    }

}