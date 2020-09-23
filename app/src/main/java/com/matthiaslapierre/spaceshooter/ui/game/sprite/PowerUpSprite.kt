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

class PowerUpSprite(
    context: Context,
    drawables: Drawables,
    val type: Int,
    private val x: Float,
    private var y: Float
) : ISprite, IConsumable {

    companion object {
        const val TYPE_BOLT = 0
        const val TYPE_SHIELD = 1
        const val TYPE_STAR = 2
    }
    private val drawable: Drawable by lazy {
        when(type) {
            TYPE_BOLT -> drawables.getPowerUpBolt()
            TYPE_SHIELD -> drawables.getPowerUpShield()
            TYPE_STAR -> drawables.getPowerUpStar()
            else -> drawables.getPowerUpStar()
        }
    }
    private val width = Utils.getDimenInPx(context, R.dimen.powerUpSize)
    private val height = Utils.getDimenInPx(context, R.dimen.powerUpSize)
    private val speed = Utils.getDimenInPx(context, R.dimen.powerUpSpeed)
    private var isAlive = true

    override var isConsumed: Boolean = false

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