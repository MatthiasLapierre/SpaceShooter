package com.matthiaslapierre.spaceshooter.ui.game.sprite

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import androidx.core.graphics.toRect
import com.matthiaslapierre.spaceshooter.Constants.ENEMY_DAMAGE
import com.matthiaslapierre.spaceshooter.Constants.ENEMY_MAX_LIFE
import com.matthiaslapierre.spaceshooter.R
import com.matthiaslapierre.spaceshooter.resources.Drawables
import com.matthiaslapierre.spaceshooter.util.Utils
import kotlin.math.min

class EnemyShipSprite(
    private val context: Context,
    drawables: Drawables,
    private var x: Float,
    private var y: Float
): ISprite, ILiving, IDamaging {

    override var life: Int = ENEMY_MAX_LIFE
    override val damage: Int = ENEMY_DAMAGE

    var minX: Float = 0f
    var maxX: Float = 0f
    var speedX: Float = 0f
    var speedY: Float = 0f
    var lastShotTimestamp: Long = 0L

    private var drawable = drawables.getEnemyShip()
    private var width: Float = Utils.getDimenInPx(context, R.dimen.enemyShipWidth)
    private var height: Float = width * drawable.intrinsicHeight / drawable.intrinsicWidth
    private var isAlive: Boolean = true

    override fun onDraw(canvas: Canvas, globalPaint: Paint, status: Int) {
        isAlive = status != ISprite.STATUS_NOT_STARTED && life > 0

        val screenWidth = canvas.width
        val centerX = getRectF().centerX()
        val centerY = getRectF().centerY()
        val maxSpeed = Utils.getDimenInPx(context, R.dimen.enemyShipSpeed)
        speedX = when {
            centerX < 0 -> maxSpeed
            centerX > screenWidth -> -maxSpeed
            centerX < minX -> min(minX - centerX, maxSpeed)
            centerX > maxX -> -min(centerX - maxX, maxSpeed)
            else -> {
                val speed = (maxX - minX) * maxSpeed / screenWidth
                when (centerX) {
                    minX -> {
                        speed
                    }
                    maxX -> {
                        -speed
                    }
                    else -> {
                        if(speedX == 0f) {
                            speedX = speed
                        }
                        speedX
                    }
                }
            }
        }
        speedY = when {
            centerY < height -> maxSpeed
            else -> {
                0f
            }
        }

        if (status == ISprite.STATUS_PLAY) {
            x += speedX
            y += speedY
        }
        drawable.bounds = getRectF().toRect()
        drawable.draw(canvas)
    }

    override fun isAlive(): Boolean = life > 0

    override fun isHit(sprite: ISprite): Boolean = (isAlive() && sprite is PlayerSprite
            && getRectF().intersect(sprite.getRectF()))

    override fun getScore(): Int = 10

    override fun getRectF(): RectF = RectF(x, y,x + width,y + height)

    override fun onCleared() {

    }

}