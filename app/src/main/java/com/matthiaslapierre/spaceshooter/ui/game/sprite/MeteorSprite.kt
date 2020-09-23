package com.matthiaslapierre.spaceshooter.ui.game.sprite

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.Drawable
import androidx.core.graphics.toRect
import com.matthiaslapierre.spaceshooter.Constants.METEOR_POINTS_MULTIPLIER
import com.matthiaslapierre.spaceshooter.Constants.UNDEFINED
import com.matthiaslapierre.spaceshooter.R
import com.matthiaslapierre.spaceshooter.resources.Drawables
import com.matthiaslapierre.spaceshooter.util.Utils

/**
 * Draws a meteor.
 */
class MeteorSprite(
    context: Context,
    drawables: Drawables,
    var y: Float
): ISprite, ILiving, IDamaging {

    companion object {
        const val SIZE_TINY = 1
        const val SIZE_SMALL = 2
        const val SIZE_MEDIUM = 3
        const val SIZE_BIG = 4

        const val TYPE_BROWN = 1
        const val TYPE_GREY = 2
    }

    /**
     * Random size.
     */
    private val size: Int = Utils.getRandomInt(SIZE_TINY, SIZE_BIG + 1)

    /**
     * Random type.
     */
    private val type: Int = Utils.getRandomInt(TYPE_BROWN, TYPE_GREY + 1)
    /**
     * Random speed.
     */
    private val speed: Float = Utils.getRandomFloat(
        Utils.getDimenInPx(context, R.dimen.meteorSpeedMin),
        Utils.getDimenInPx(context, R.dimen.meteorSpeedMax)
    )
    /**
     * Points to win after destroying a meteor.
     */
    private val points: Int = size * type * METEOR_POINTS_MULTIPLIER
    /**
     * Drawable resource.
     */
    private val drawable: Drawable = drawables.getMeteor(type, size)
    /**
     * Sprite width.
     */
    private val width: Float by lazy {
        when (size) {
            SIZE_TINY -> Utils.getDimenInPx(context, R.dimen.meteorTinyWidth)
            SIZE_SMALL -> Utils.getDimenInPx(context, R.dimen.meteorSmallWidth)
            SIZE_MEDIUM -> Utils.getDimenInPx(context, R.dimen.meteorMediumWidth)
            SIZE_BIG -> Utils.getDimenInPx(context, R.dimen.meteorBigWidth)
            else -> Utils.getDimenInPx(context, R.dimen.meteorTinyWidth)
        }
    }
    /**
     * Sprite height.
     */
    private val height: Float by lazy {
        width * drawable.intrinsicHeight / drawable.intrinsicWidth
    }
    /**
     * x-coordinate.
     */
    private var x: Float = UNDEFINED
    private var isAlive: Boolean = true

    /**
     * Health level.
     */
    override var life: Int = size * type
        set(value) {
            field = if(value > 0) {
                value
            } else {
                0
            }
        }

    /**
     * Damages caused when the meteor has hiten the player.
     */
    override val damage: Int = size * type

    override fun onDraw(canvas: Canvas, globalPaint: Paint, status: Int) {
        if(x == UNDEFINED) {
            // Initialize the x-coordinate.
            val screenWidth = canvas.width
            x = Utils.getRandomFloat(0f, screenWidth - width)
        }
        val screenHeight = canvas.height
        isAlive = status != ISprite.STATUS_NOT_STARTED && y < screenHeight && life > 0
        if(status == ISprite.STATUS_PLAY) {
            y += speed
        }
        drawable.bounds = getRectF().toRect()
        drawable.draw(canvas)
    }

    override fun isAlive(): Boolean = isAlive

    override fun isHit(sprite: ISprite): Boolean = isAlive()
            && y >= 0
            && sprite is PlayerSprite
            && getRectF().intersect(sprite.getRectF())

    override fun getScore(): Int = points

    override fun getRectF(): RectF = RectF(
        x,
        y,
        x + width,
        y + height
    )

    override fun onCleared() {

    }

}