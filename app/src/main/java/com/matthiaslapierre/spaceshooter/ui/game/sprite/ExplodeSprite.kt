package com.matthiaslapierre.spaceshooter.ui.game.sprite

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import androidx.core.graphics.toRect
import com.matthiaslapierre.spaceshooter.Constants.EXPLODE_MAX_FRAMES
import com.matthiaslapierre.spaceshooter.resources.Drawables

class ExplodeSprite(
    private val drawables: Drawables,
    private val rectF: RectF
): ISprite {

    private var currentFrame = 1

    override fun onDraw(canvas: Canvas, globalPaint: Paint, status: Int) {
        if (currentFrame <= EXPLODE_MAX_FRAMES) {
            val drawable = drawables.getExplosionFrame(currentFrame)
            drawable.bounds = getRectF().toRect()
            drawable.draw(canvas)
        }
        currentFrame++
    }

    override fun isAlive(): Boolean {
        return currentFrame <= EXPLODE_MAX_FRAMES
    }

    override fun isHit(sprite: ISprite): Boolean = false

    override fun getScore(): Int = 0

    override fun getRectF(): RectF = rectF

    override fun onCleared() {

    }

}