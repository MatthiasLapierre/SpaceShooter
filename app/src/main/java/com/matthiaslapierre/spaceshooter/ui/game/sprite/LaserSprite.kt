package com.matthiaslapierre.spaceshooter.ui.game.sprite

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.Drawable
import androidx.core.graphics.toRect
import com.matthiaslapierre.spaceshooter.Constants.LASER_DAMAGE
import com.matthiaslapierre.spaceshooter.R
import com.matthiaslapierre.spaceshooter.resources.Drawables
import com.matthiaslapierre.spaceshooter.util.Utils

/**
 * Draw a laser shot.
 */
class LaserSprite(
    context: Context,
    drawables: Drawables,
    private var x: Float,
    private var y: Float,
    val adverse: Boolean = false
): ISprite, IDamaging, IConsumable {

    override val damage: Int = LASER_DAMAGE
    override var isConsumed: Boolean = false

    private val drawable: Drawable = drawables.getLaser(adverse)
    private val width: Float by lazy {
        Utils.getDimenInPx(context, R.dimen.laserStdWidth)
    }
    private val height: Float by lazy {
        width * drawable.intrinsicHeight / drawable.intrinsicWidth
    }
    private val speed: Float by lazy {
        if(adverse) {
            Utils.getDimenInPx(context, R.dimen.enemyLaserSpeed)
        } else {
            Utils.getDimenInPx(context, R.dimen.playerLaserSpeed)
        }
    }
    private var isAlive = true

    override fun onDraw(canvas: Canvas, globalPaint: Paint, status: Int) {
        val screenWidth = canvas.width
        val screenHeight = canvas.height
        isAlive = status == ISprite.STATUS_PLAY && y >= 0 && y <= screenHeight && !isConsumed

        // Move the laser shot.
        if(status == ISprite.STATUS_PLAY) {
            if(adverse) {
                y += speed
            } else {
                y -= speed
            }
        }

        if(adverse) {
            // If it's an enemy shot, we 180-rotate the canvas.
            canvas.save()
            canvas.rotate(180f, canvas.width / 2f, canvas.height / 2f)
            drawable.bounds = RectF(
                screenWidth - getRectF().right,
                screenHeight - getRectF().bottom,
                screenWidth - getRectF().left,
                screenHeight - getRectF().top
            ).toRect()
            drawable.draw(canvas)
            canvas.restore()
        } else {
            drawable.bounds = getRectF().toRect()
            drawable.draw(canvas)
        }
    }

    override fun isAlive(): Boolean = isAlive

    override fun isHit(sprite: ISprite): Boolean = isAlive
            && sprite is ILiving
            && sprite.getRectF().top >= 0
            && ((sprite is PlayerSprite && adverse)
            || (sprite is EnemyShipSprite && !adverse)
            || (sprite is MeteorSprite && !adverse))
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