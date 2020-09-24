package com.matthiaslapierre.spaceshooter.ui.game.sprite

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.Drawable
import androidx.core.graphics.toRect
import com.matthiaslapierre.spaceshooter.R
import com.matthiaslapierre.spaceshooter.resources.Drawables
import com.matthiaslapierre.spaceshooter.util.Utils

/**
 * Bonus to win.
 */
class PowerUpSprite(
    context: Context,
    drawables: Drawables,
    val type: Int,
    private val x: Float,
    private var y: Float
) : ISprite, IConsumable {

    companion object {
        /**
         * Space ship upgrade.
         */
        const val TYPE_BOLT = 0

        /**
         * Shield repair.
         */
        const val TYPE_SHIELD = 1

        /**
         * +n points
         */
        const val TYPE_STAR = 2
    }

    override var isConsumed: Boolean = false

    /**
     * Drawable resource.
     */
    private val drawable: Drawable by lazy {
        when(type) {
            TYPE_BOLT -> drawables.getPowerUpBolt()
            TYPE_SHIELD -> drawables.getPowerUpShield()
            TYPE_STAR -> drawables.getPowerUpStar()
            else -> drawables.getPowerUpStar()
        }
    }

    /**
     * Sprite width.
     */
    private val width = Utils.getDimenInPx(context, R.dimen.powerUpSize)
    /**
     * Sprite height.
     */
    private val height = Utils.getDimenInPx(context, R.dimen.powerUpSize)
    /**
     * Sprite speed.
     */
    private val speed = Utils.getDimenInPx(context, R.dimen.powerUpSpeed)
    private var isAlive = true

    override fun onDraw(canvas: Canvas, globalPaint: Paint, status: Int) {
        val screenHeight = canvas.height
        isAlive = status == ISprite.STATUS_PLAY && y < screenHeight && !isConsumed

        if(status == ISprite.STATUS_PLAY) {
            y += speed
        }
        drawable.bounds = getRectF().toRect()
        drawable.draw(canvas)
    }

    override fun isAlive(): Boolean = isAlive

    override fun isHit(sprite: ISprite): Boolean = isAlive
            && y >= 0
            && sprite is PlayerSprite
            && sprite.getRectF().intersect(getRectF())

    override fun getScore(): Int = 0

    override fun getRectF(): RectF = RectF(
    x - width / 2f,
    y - height /2f,
    x + width / 2f,
    y + height / 2f
    )

    override fun onCleared() {

    }

}