package com.matthiaslapierre.spaceshooter.resources

/**
 * Handles sounds present in the game.
 */
interface SoundEngine {

    /**
     * Loads sound effects.
     */
    fun load()

    /**
     * Unloads sound effects.
     */
    fun release()

    /**
     * Resumes the music.
     */
    fun resume()

    /**
     * Pauses the music.
     */
    fun pause()

    /**
     * Plays a button press sound effect.
     */
    fun playBtnPress()

    /**
     * Plays the crash sound effect.
     */
    fun playCrash()

    /**
     * Plays the meteor explode sound effect.
     */
    fun playMeteorExplode()

    /**
     * Plays the space ship explode sound effect.
     */
    fun playShipExplode()

    /**
     * Plays a sound after getting a power-up.
     */
    fun playGetPowerUp()

    /**
     * Plays "Game Over" sound effect.
     */
    fun playGameOver()

    /**
     * Plays a sound after the laser shot hits its target.
     */
    fun playShotHit()

    /**
     * Plays the menu music.
     */
    fun playMenuMusic()

    /**
     * Plays the main music.
     */
    fun playPlayMusic()

    /**
     * Plays the Game Over music.
     */
    fun playGameOverMusic()

}