package com.matthiaslapierre.spaceshooter.resources

import android.graphics.drawable.Drawable

/**
 * Caches drawable resources.
 */
interface Drawables{

    /**
     * Loads drawables.
     */
    fun load()

    /**
     * Gets the button background.
     */
    fun getButton(): Drawable

    /**
     * Gets a meteor.
     * @param type
     * @param size
     */
    fun getMeteor(type: Int, size: Int): Drawable

    /**
     * Gets a digit.
     * @param digit 0-9
     */
    fun getDigit(digit: Int): Drawable

    /**
     * Gets a random star.
     */
    fun getRandomStar(): Drawable

    /**
     * Gets a laser.
     * @param adverse shot by an enemy ship
     */
    fun getLaser(adverse: Boolean): Drawable

    /**
     * Gets the player ship.
     */
    fun getPlayerShip(type: Int): Drawable

    /**
     * Gets the enemy ship.
     */
    fun getEnemyShip(): Drawable

    /**
     * Gets an explosion frame.
     * @param frame index of the frame
     */
    fun getExplosionFrame(frame: Int): Drawable

    /**
     * Power-up. Space ship upgrade.
     */
    fun getPowerUpBolt(): Drawable

    /**
     * Shield repair.
     */
    fun getPowerUpShield(): Drawable

    /**
     * +n points
     */
    fun getPowerUpStar(): Drawable

    /**
     * Gets a drawable resource and cache it.
     */
    fun get(resId: Int): Drawable

}