package com.matthiaslapierre.spaceshooter.ui.game.sprite

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import androidx.core.graphics.toRect
import com.matthiaslapierre.spaceshooter.Constants
import com.matthiaslapierre.spaceshooter.R
import com.matthiaslapierre.spaceshooter.util.Utils

/**
 * Draws the health bar.
 */
class LifeLevelSprite(
    private val context: Context
): ISprite {

    var currentLife: Int = 0

    private var width: Float = Utils.getDimenInPx(context, R.dimen.lifeProgressWidth)
    private var height: Float = Utils.getDimenInPx(context, R.dimen.lifeProgressHeight)
    private var x: Float = Constants.UNDEFINED
    private var y: Float = Constants.UNDEFINED
    private var isAlive: Boolean = true

    override fun onDraw(canvas: Canvas, globalPaint: Paint, status: Int) {
        if (status == ISprite.STATUS_NOT_STARTED) {
            isAlive = false
            return
        }

        val bmp = Utils.generateLifeLevel(context, currentLife)

        val screenWidth = canvas.width.toFloat()
        val screenHeight = canvas.height.toFloat()
        x = screenWidth - bmp.width
        y = screenHeight * 0.3f

        canvas.drawBitmap(
            bmp,
            Rect(0, 0, bmp.width, bmp.height),
            getRectF().toRect(),
            globalPaint
        )
    }

    override fun isAlive(): Boolean = isAlive

    override fun isHit(sprite: ISprite): Boolean = false

    override fun getScore(): Int = 0

    override fun getRectF(): RectF = RectF(
        x,
        y,
        x + width,
        y + height
    )

    override fun onCleared() {

    }

}