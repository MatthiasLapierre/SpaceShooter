package com.matthiaslapierre.spaceshooter.ui.game.sprite

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.matthiaslapierre.spaceshooter.Constants
import com.matthiaslapierre.spaceshooter.resources.Drawables
import com.matthiaslapierre.spaceshooter.resources.TypefaceHelper
import com.matthiaslapierre.spaceshooter.R
import com.matthiaslapierre.spaceshooter.util.Utils

/**
 * Draws the current level.
 */
class LevelSprite(
    private val context: Context,
    private val drawables: Drawables,
    private val typefaceHelper: TypefaceHelper
): ISprite {

    var currentLevel: Int = 0

    private var width: Float = Constants.UNDEFINED
    private var height: Float = Constants.UNDEFINED
    private val marginLeft: Float = Utils.getDimenInPx(context, R.dimen.levelIndicatorMarginLeft)
    private val marginTop: Float = Utils.getDimenInPx(context, R.dimen.levelIndicatorMarginTop)
    private var x: Float = marginLeft
    private var y: Float = marginTop
    private var isAlive: Boolean = true

    override fun onDraw(canvas: Canvas, globalPaint: Paint, status: Int) {
        if (status == ISprite.STATUS_NOT_STARTED) {
            isAlive = false
            return
        }
        val scoreBmp = Utils.generateIndicator(
            context,
            drawables,
            typefaceHelper,
            context.resources.getString(R.string.level),
            currentLevel
        )
        width = scoreBmp.width.toFloat()
        height = scoreBmp.height.toFloat()
        canvas.drawBitmap(scoreBmp, x, y, null)
        scoreBmp.recycle()
    }

    override fun isAlive(): Boolean = isAlive

    override fun isHit(sprite: ISprite): Boolean = false

    override fun getScore(): Int = 0

    override fun getRectF(): RectF = RectF(x, y, x + width, y + height)

    override fun onCleared() {

    }

}