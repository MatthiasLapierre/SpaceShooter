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

class LaserSprite(
    context: Context,
    drawables: Drawables,
    private var x: Float,
    private var y: Float,
    private val type: Int = TYPE_STANDARD,
    val adverse: Boolean = false
): ISprite, IDamaging, IConsumable {

    companion object {
        const val TYPE_STANDARD = 0
        const val TYPE_HEAVY = 1
    }
    private val drawable: Drawable = drawables.getLaser(adverse, type == TYPE_HEAVY)
    private val width: Float by lazy {
        if(type == TYPE_HEAVY) {
            Utils.getDimenInPx(context, R.dimen.laserHeavyWidth)
        } else {
            Utils.getDimenInPx(context, R.dimen.laserStdWidth)
        }
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
    override val damage: Int
        get() = if(type == TYPE_HEAVY) {
            2
        } else {
            1
        }
    override var isConsumed: Boolean = false

    override fun onDraw(canvas: Canvas, globalPaint: Paint, status: Int) {
        val screenWidth = canvas.width
        val screenHeight = canvas.height
        isAlive = status == ISprite.STATUS_PLAY && y >= 0 && y <= screenHeight && !isConsumed

        if(status == ISprite.STATUS_PLAY) {
            if(adverse) {
                y += speed
            } else {
                y -= speed
            }
        }

        if(adverse) {
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