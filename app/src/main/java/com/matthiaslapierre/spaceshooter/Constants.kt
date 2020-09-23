package com.matthiaslapierre.spaceshooter

object Constants {

    /**
     * This constant allows us to add, if necessary, a delay to avoid that the rendering phase
     * of the Game Loop be too fast.
     */
    const val MS_PER_FRAME = 20

    /**
     * Maximum of number of simultaneous streams that can be played simultaneously
     * by [android.media.SoundPool].
     */
    const val SOUND_MAX_STREAMS = 5

    /**
     * Number of life.
     */
    const val PLAYER_MAX_LIFE = 20

    /**
     * Rate of fire.
     */
    const val PLAYER_RATE_OF_FIRE = 2

    /**
     * Count frames in the explode animation.
     */
    const val EXPLODE_MAX_FRAMES = 39

    /**
     * Count frames per line in the image containing all frames used by the explode
     * animation.
     */
    const val EXPLODE_FRAMES_PER_LINE = 8

    /**
     * Undefined value.
     */
    const val UNDEFINED = -999f

    /**
     * Level duration.
     */
    const val LEVEL_DURATION_IN_SECONDS = 15

    /**
     *
     */
    const val METEORS_MULTIPLIER = 2

    /**
     * Rate of fire.
     */
    const val ENEMY_RATE_OF_FIRE = 1

    /**
     * Number of life for enemy.
     */
    const val ENEMY_MAX_LIFE = 10

    const val ENEMY_DAMAGE = 5

}