package com.matthiaslapierre.spaceshooter.util

import android.content.Context
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
     * Draws a health bar.
     */
    fun generateLifeLevel(
        context: Context,
        life: Int
    ): Bitmap {
        val width = getDimenInPx(context, R.dimen.lifeProgressWidth)
        val height = getDimenInPx(context, R.dimen.lifeProgressHeight)
        val borderWidth = width * 0.3f

        val maxLife = Constants.PLAYER_MAX_LIFE
        val bgRect = RectF(
            0f,
            0f,
            width,
            height
        )
        val progressRect = RectF(
            borderWidth,
            height - (height * life / maxLife) + borderWidth,
            width - borderWidth,
            height - borderWidth
        )

        val progressBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ContextCompat.getColor(context, R.color.lifeIndicator)
            strokeWidth = borderWidth
            style = Paint.Style.STROKE
        }
        val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ContextCompat.getColor(context, R.color.lifeIndicator)
            style = Paint.Style.FILL
        }

        val bitmap = Bitmap.createBitmap(width.toInt(), height.toInt(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawRect(
            bgRect,
            progressBackgroundPaint
        )
        canvas.drawRect(
            progressRect,
            progressPaint
        )

        return bitmap
    }

    /**
     * Draws a button.
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

    /**
     * Generates the game background.
     */
    fun generateGameBackground(
        context: Context,
        screenWidth: Int,
        screenHeight: Int
    ): Bitmap {
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