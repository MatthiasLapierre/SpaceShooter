package com.matthiaslapierre.spaceshooter.ui.game.sprite

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import androidx.core.graphics.toRect
import com.matthiaslapierre.spaceshooter.Constants.PLAYER_MAX_LIFE
import com.matthiaslapierre.spaceshooter.Constants.UNDEFINED
import com.matthiaslapierre.spaceshooter.R
import com.matthiaslapierre.spaceshooter.resources.Drawables
import com.matthiaslapierre.spaceshooter.util.Utils

class PlayerSprite(
    context: Context,
    drawables: Drawables
): ISprite, ILiving {

    var x: Float = UNDEFINED
    var y: Float = UNDEFINED
    var lastShotTimestamp: Long = 0L
    override var life: Int = PLAYER_MAX_LIFE
        set(value) {
            field = if(value > 0) {
                value
            } else {
                0
            }
        }
    private var drawable = drawables.getPlayerShip()
    private var width: Float = Utils.getDimenInPx(context, R.dimen.playerShipWidth)
    private var height: Float = width * drawable.intrinsicHeight / drawable.intrinsicWidth
    private val initialBottom = Utils.getDimenInPx(context, R.dimen.playerShipInitialBottom)

    override fun onDraw(canvas: Canvas, globalPaint: Paint, status: Int) {
        val screenWidth = canvas.width
        val screenHeight = canvas.height

        if(x == UNDEFINED) {
            x = screenWidth / 2f - width / 2f
            y = screenHeight - initialBottom - height
        }

        val minX = 0f
        val maxX = screenWidth - width
        val minY = screenHeight / 4f
        val maxY = screenHeight - height

        if(y < minY) {
            y = minY
        }
        if(y > maxY) {
            y = maxY
        }
        if(x < minX) {
            x = minX
        }
        if(x > maxX) {
            x = maxX
        }

        drawable.bounds = getRectF().toRect()
        drawable.draw(canvas)
    }

    override fun isAlive(): Boolean = life > 0

    override fun isHit(sprite: ISprite): Boolean = false

    override fun getScore(): Int = 0

    override fun getRectF(): RectF = RectF(x, y,x + width,y + height)

    override fun onCleared() {

    }

}