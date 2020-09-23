package com.matthiaslapierre.spaceshooter.ui.game.sprite

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.Drawable
import androidx.core.graphics.toRect
import com.matthiaslapierre.spaceshooter.Constants.UNDEFINED
import com.matthiaslapierre.spaceshooter.R
import com.matthiaslapierre.spaceshooter.resources.Drawables
import com.matthiaslapierre.spaceshooter.util.Utils

class StarSprite(
    private val context: Context,
    private val drawables: Drawables,
    var y: Float
): ISprite {

    private val drawable: Drawable = drawables.getRandomStar()
    private val width: Float = Utils.getDimenInPx(context, R.dimen.starSize)
    private val height: Float = Utils.getDimenInPx(context, R.dimen.starSize)
    private val speed: Float = Utils.getRandomFloat(
        context.resources.getDimension(R.dimen.starSpeedMin),
        context.resources.getDimension(R.dimen.starSpeedMax)
    )
    private var x: Float = UNDEFINED
    private var isAlive: Boolean = true

    override fun onDraw(canvas: Canvas, globalPaint: Paint, status: Int) {
        if(x == UNDEFINED) {
            val screenWidth = canvas.width
            x = Utils.getRandomFloat(0f, screenWidth - width)
        }
        val screenHeight = canvas.height
        isAlive = y < screenHeight
        if(status != ISprite.STATUS_GAME_OVER) {
            y += speed
        }
        drawable.bounds = getRectF().toRect()
        drawable.draw(canvas)
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