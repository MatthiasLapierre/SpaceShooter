package com.matthiaslapierre.spaceshooter.ui.game.sprite

import android.content.Context
import android.graphics.*
import com.matthiaslapierre.spaceshooter.R
import com.matthiaslapierre.spaceshooter.resources.TypefaceHelper
import com.matthiaslapierre.spaceshooter.util.Utils

/**
 * Draws the Spash UI.
 */
class SplashSprite(
    private val context: Context,
    private val typefaceHelper: TypefaceHelper
): ISprite {

    private val titleTextPaint: Paint by lazy {
        val paint = Paint()
        paint.textSize = Utils.getDimenInPx(context, R.dimen.titleTextSize)
        paint.typeface = typefaceHelper.getFutureTypeface();
        paint.color = Color.WHITE
        paint
    }
    private val startTextPaint: Paint by lazy {
        val paint = Paint()
        paint.textSize = Utils.getDimenInPx(context, R.dimen.startTextSize)
        paint.typeface = typefaceHelper.getFutureTypeface();
        paint.color = Color.WHITE
        paint
    }
    private var screenWidth: Float = 0f
    private var screenHeight: Float = 0f
    private var isAlive: Boolean = true

    override fun onDraw(canvas: Canvas, globalPaint: Paint, status: Int) {
        screenWidth = canvas.width.toFloat()
        screenHeight = canvas.height.toFloat()
        isAlive = status == ISprite.STATUS_NOT_STARTED
        drawTitle(canvas)
        drawStartLabel(canvas)
    }

    override fun isAlive(): Boolean = isAlive

    override fun isHit(sprite: ISprite): Boolean = false

    override fun getScore(): Int = 0

    override fun getRectF(): RectF = RectF(
        0f,
        0f,
        screenWidth,
        screenHeight
    )

    override fun onCleared() {

    }

    /**
     * Draws the title of the game.
     */
    private fun drawTitle(canvas: Canvas) {
        val strTitle = context.resources.getString(R.string.app_name)
        val bounds = Rect()
        titleTextPaint.getTextBounds(strTitle, 0, strTitle.length, bounds)
        val screenWidth = canvas.width
        val screenHeight = canvas.height
        canvas.drawText(
            strTitle,
            screenWidth / 2f - bounds.width() / 2f,
            screenHeight / 5f,
            titleTextPaint
        )
    }

    /**
     * Draws the "Press to start" label.
     */
    private fun drawStartLabel(canvas: Canvas) {
        val strTitle = context.resources.getString(R.string.press_to_start)
        val bounds = Rect()
        startTextPaint.getTextBounds(strTitle, 0, strTitle.length, bounds)
        val screenWidth = canvas.width
        val screenHeight = canvas.height
        val marginBottom = Utils.getDimenInPx(context, R.dimen.startMarginBottom)
        canvas.drawText(
            strTitle,
            screenWidth / 2f - bounds.width() / 2f,
            screenHeight - marginBottom,
            startTextPaint
        )
    }

}