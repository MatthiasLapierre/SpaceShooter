package com.matthiaslapierre.spaceshooter.resources

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.matthiaslapierre.spaceshooter.Constants.EXPLODE_FRAMES_PER_LINE
import com.matthiaslapierre.spaceshooter.Constants.EXPLODE_MAX_FRAMES
import com.matthiaslapierre.spaceshooter.R
import com.matthiaslapierre.spaceshooter.ui.game.sprite.MeteorSprite
import com.matthiaslapierre.spaceshooter.util.Utils
import java.util.*

/**
 * Caches drawable resources.
 */
class Drawables(
    private val context: Context
) {

    private val cache: Hashtable<Int, Drawable> = Hashtable()

    /**
     * Loads drawables.
     */
    fun load() {
        Thread(Runnable {
            // Load explosion frames
            cacheExplosionFrame()
        }).start()
    }

    /**
     * Gets the button background.
     */
    fun getButton(): Drawable = get(R.drawable.button_blue)

    /**
     * Gets a meteor.
     * @param type
     * @param size
     */
    fun getMeteor(type: Int, size: Int): Drawable {
        val resIds: Array<Int> = when (type) {
            MeteorSprite.TYPE_BROWN -> when (size) {
                MeteorSprite.SIZE_TINY -> arrayOf(
                    R.drawable.meteor_brown_tiny1,
                    R.drawable.meteor_brown_tiny2
                )
                MeteorSprite.SIZE_SMALL -> arrayOf(
                    R.drawable.meteor_brown_small1,
                    R.drawable.meteor_brown_small2
                )
                MeteorSprite.SIZE_MEDIUM -> arrayOf(
                    R.drawable.meteor_brown_med1,
                    R.drawable.meteor_brown_med2
                )
                MeteorSprite.SIZE_BIG -> arrayOf(
                    R.drawable.meteor_brown_big1,
                    R.drawable.meteor_brown_big2,
                    R.drawable.meteor_brown_big3,
                    R.drawable.meteor_brown_big4
                )
                else -> arrayOf(
                    R.drawable.meteor_brown_tiny1,
                    R.drawable.meteor_brown_tiny2
                )
            }
            MeteorSprite.TYPE_GREY -> when (size) {
                MeteorSprite.SIZE_TINY -> arrayOf(
                    R.drawable.meteor_grey_tiny1,
                    R.drawable.meteor_grey_tiny2
                )
                MeteorSprite.SIZE_SMALL -> arrayOf(
                    R.drawable.meteor_grey_small1,
                    R.drawable.meteor_grey_small2
                )
                MeteorSprite.SIZE_MEDIUM -> arrayOf(
                    R.drawable.meteor_grey_med1,
                    R.drawable.meteor_grey_med2
                )
                MeteorSprite.SIZE_BIG -> arrayOf(
                    R.drawable.meteor_grey_big1,
                    R.drawable.meteor_grey_big2,
                    R.drawable.meteor_grey_big3,
                    R.drawable.meteor_grey_big4
                )
                else -> arrayOf(
                    R.drawable.meteor_grey_tiny1,
                    R.drawable.meteor_grey_tiny2
                )
            }
            else -> arrayOf(
                R.drawable.meteor_brown_tiny1,
                R.drawable.meteor_brown_tiny2
            )
        }
        val index = Utils.getRandomInt(0, resIds.size)
        return get(resIds[index])
    }

    /**
     * Gets a digit.
     * @param digit 0-9
     */
    fun getDigit(digit: Int): Drawable {
        val resId = when (digit) {
            1 -> R.drawable.numeral1
            2 -> R.drawable.numeral2
            3 -> R.drawable.numeral3
            4 -> R.drawable.numeral4
            5 -> R.drawable.numeral5
            6 -> R.drawable.numeral6
            7 -> R.drawable.numeral7
            8 -> R.drawable.numeral8
            9 -> R.drawable.numeral9
            else -> R.drawable.numeral0
        }
        return get(resId)
    }

    /**
     * Gets a random star.
     */
    fun getRandomStar(): Drawable {
        val resIds = arrayOf(R.drawable.star1, R.drawable.star2, R.drawable.star3)
        val index = Utils.getRandomInt(0, resIds.size)
        return get(resIds[index])
    }

    /**
     * Gets a laser.
     * @param adverse shot by an enemy ship
     */
    fun getLaser(adverse: Boolean): Drawable =
        if(adverse) {
            get(R.drawable.laser_red1)
        } else {
            get(R.drawable.laser_blue1)
        }

    /**
     * Gets the player ship.
     */
    fun getPlayerShip(type: Int): Drawable = when (type) {
        1 -> get(R.drawable.player_ship1_blue)
        2 -> get(R.drawable.player_ship2_blue)
        else -> get(R.drawable.player_ship3_blue)
    }

    /**
     * Gets the enemy ship.
     */
    fun getEnemyShip(): Drawable = get(R.drawable.enemy_red_2)

    /**
     * Gets an explosion frame.
     * @param frame index of the frame
     */
    fun getExplosionFrame(frame: Int): Drawable = get("explode_$frame".hashCode())

    /**
     * Power-up. Space ship upgrate.
     */
    fun getPowerUpBolt() = get(R.drawable.powerup_bolt)

    /**
     * Shield repair.
     */
    fun getPowerUpShield() = get(R.drawable.powerup_shield)

    /**
     * +n points
     */
    fun getPowerUpStar() = get(R.drawable.powerup_star)

    /**
     * Gets a drawable resource and cache it.
     */
    fun get(resId: Int): Drawable {
        synchronized(cache) {
            if (!cache.containsKey(resId)) {
                val drawable = ContextCompat.getDrawable(context, resId)
                cache[resId] = drawable
            }
            return cache[resId]!!
        }
    }

    /**
     * Caches explosion frames.
     */
    private fun cacheExplosionFrame() {
        synchronized(cache) {
            splitExplosionAnimation().forEachIndexed { frame, drawable ->
                cache["explode_${frame+1}".hashCode()] = drawable
            }
        }
    }

    /**
     * Splits a bitmap and returns explosion frames.
     */
    private fun splitExplosionAnimation(): List<Drawable> {
        val drawables = mutableListOf<Drawable>()
        val fullBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.explode)
        val frameSize = fullBitmap.width / EXPLODE_FRAMES_PER_LINE
        for(frame in 1 until EXPLODE_MAX_FRAMES + 1) {
            val line = ((frame - 1) / EXPLODE_FRAMES_PER_LINE)
            val column = ((frame - 1) % EXPLODE_FRAMES_PER_LINE)
            val frameBitmap = Bitmap.createBitmap(frameSize, frameSize, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(frameBitmap)
            canvas.drawBitmap(
                fullBitmap,
                Rect(
                    column * frameSize,
                    line * frameSize,
                    column * frameSize + frameSize,
                    line * frameSize + frameSize
                ),
                Rect(
                    0,
                    0,
                    frameSize,
                    frameSize
                ),
                null
            )
            drawables.add(BitmapDrawable(context.resources, frameBitmap))
        }
        return drawables
    }

}