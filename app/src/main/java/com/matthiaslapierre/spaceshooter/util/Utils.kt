package com.matthiaslapierre.spaceshooter.util

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import androidx.annotation.DimenRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.toRect
import com.matthiaslapierre.spaceshooter.Constants
import com.matthiaslapierre.spaceshooter.R
import com.matthiaslapierre.spaceshooter.resources.Drawables
import com.matthiaslapierre.spaceshooter.resources.TypefaceHelper
import kotlin.random.Random

object Utils {

    /**
     * Multiplies value by the density factor.
     */
    fun applyScaleFactor(px: Int) = Resources.getSystem().displayMetrics.density * px

    /**
     * Retrieves a dimensional for a particular resource ID for use as a size in raw pixels.
     */
    fun getDimenInPx(context: Context, @DimenRes id: Int): Float = context.resources.getDimensionPixelSize(id).toFloat()

    /**
     * Gets a random int.
     */
    fun getRandomInt(minValue: Int, maxValue: Int): Int = Random.nextInt(minValue, maxValue)

    /**
     * Gets a random float.
     */
    fun getRandomFloat(minValue: Float, maxValue: Float): Float = Random.nextFloat() * (maxValue - minValue) + minValue

    /**
     * Generates a bitmap showing the score.
     */
    fun generateIndicator(
        context: Context,
        drawables: Drawables,
        typefaceHelper: TypefaceHelper,
        title: String,
        value: Int
    ): Bitmap {
        val digits = value.toDigits()
        val strTitle = "$title: "

        val textPaint = Paint().apply {
            color = ContextCompat.getColor(context, R.color.indicatorTextColor)
            textSize = getDimenInPx(context, R.dimen.indicatorTextSize)
            typeface = typefaceHelper.getFutureTypeface()
        }
        val textBounds = Rect()
        textPaint.getTextBounds(strTitle, 0, strTitle.length, textBounds)

        val digitSize = getDimenInPx(context, R.dimen.indicatorDigitSize)
        val digitMargin = getDimenInPx(context, R.dimen.scoreDigitMargin)
        val textMarginRight = getDimenInPx(context, R.dimen.indicatorTextMarginRight)
        val width = textBounds.width() + textMarginRight + (digits.size * digitSize) + (digitMargin * (digits.size - 1))

        val bitmap = Bitmap.createBitmap(width.toInt(), digitSize.toInt(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        var currentX = 0f
        canvas.drawText(
            strTitle,
            0f,
            digitSize / 2f -((textPaint.descent() + textPaint.ascent()) / 2f),
            textPaint
        )
        currentX += textBounds.width() + textMarginRight

        digits.reversedArray().forEach { digit ->
            val drawable = drawables.getDigit(digit)
            drawable.bounds = RectF(currentX, 0f, currentX + digitSize, digitSize).toRect()
            drawable.draw(canvas)
            currentX += digitSize + digitMargin
        }

        return bitmap
    }

    /**
     * Generates a bitmap showing the number of life remaining.
     */
    fun generateLifeLevel(
        context: Context,
        drawables: Drawables,
        life: Int
    ): Bitmap {
        val width = getDimenInPx(context, R.dimen.lifeProgressSize)
        val height = getDimenInPx(context, R.dimen.lifeProgressSize)
        val progressLineWidth = getDimenInPx(context, R.dimen.lifeIndicatorProgressLineWidth)
        val rect = RectF(
            progressLineWidth,
            progressLineWidth,
            width - progressLineWidth,
            height - progressLineWidth
        )

        val progressBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ContextCompat.getColor(context, R.color.lifeIndicatorBackground)
            strokeWidth = progressLineWidth
            style = Paint.Style.STROKE
        }
        val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ContextCompat.getColor(context, R.color.lifeIndicatorProgress)
            strokeWidth = progressLineWidth
            style = Paint.Style.STROKE
        }

        val bitmap = Bitmap.createBitmap(width.toInt(), height.toInt(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val maxLife = Constants.PLAYER_MAX_LIFE
        canvas.drawArc(
            rect,
            -90f,
            360f,
            false,
            progressBackgroundPaint
        )
        canvas.drawArc(
            rect,
            -90f,
            -360.0f / maxLife * life,
            false,
            progressPaint
        )

        canvas.save()
        canvas.rotate(45f, width / 2f, height / 2f)
        val shipDrawable = drawables.getPlayerShip()
        val shipWidth = width * 0.7f
        val shipHeight = shipWidth * shipDrawable.intrinsicHeight / shipDrawable.intrinsicWidth
        val shipPositionLeft = (width - shipWidth) / 2f
        val shipPositionTop = (height - shipHeight) / 2f
        shipDrawable.bounds = RectF(
            shipPositionLeft,
            shipPositionTop,
            shipPositionLeft + shipWidth,
            shipPositionTop + shipHeight
        ).toRect()
        shipDrawable.draw(canvas)
        canvas.restore()

        return bitmap
    }

    /**
     * Generates a button.
     */
    fun generateButton(
        context: Context,
        drawables: Drawables,
        typefaceHelper: TypefaceHelper,
        title: String
    ): Bitmap {
        val buttonTitleTextPaint = Paint().apply {
            color = ContextCompat.getColor(context, R.color.buttonTitleTextColor)
            textSize = getDimenInPx(context, R.dimen.btnTitleTextSize)
            typeface = typefaceHelper.getFutureTypeface()
        }
        val textBounds = Rect()
        buttonTitleTextPaint.getTextBounds(title, 0, title.length, textBounds)
        val buttonWidth = textBounds.width() * 1.5f
        val buttonHeight = getDimenInPx(context, R.dimen.btnHeight)
        val bitmap = Bitmap.createBitmap(buttonWidth.toInt(), buttonHeight.toInt(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val buttonDrawable = drawables.getButton()
        buttonDrawable.bounds = Rect(0, 0, buttonWidth.toInt(), buttonHeight.toInt())
        buttonDrawable.draw(canvas)
        canvas.drawText(
            title,
            (buttonWidth / 2f) - (textBounds.width() / 2f),
            buttonHeight / 2f -((buttonTitleTextPaint.descent() + buttonTitleTextPaint.ascent()) / 2f),
            buttonTitleTextPaint
        )
        return bitmap
    }

}

/**
 * Parses number to digits.
 */
fun Int.toDigits(): Array<Int> {
    val digits = mutableListOf<Int>()
    var i = this
    if(i == 0) {
        digits.add(0)
    } else {
        while (i > 0) {
            digits.add(i % 10)
            i /= 10
        }
    }
    return digits.toTypedArray()
}