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
     * Player strength of the shield.
     */
    const val PLAYER_MAX_LIFE = 20

    /**
     * Player rate of fire.
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
     * Enemies rate of fire.
     */
    const val ENEMY_RATE_OF_FIRE = 1

    /**
     * Strength of the enemies shield
     */
    const val ENEMY_MAX_LIFE = 10

    /**
     * Damages caused by enemies.
     */
    const val ENEMY_DAMAGE = 5

    /**
     * Number of points won for the destruction of an enemy.
     */
    const val ENEMY_POINTS = 10

    /**
     * Damages caused by a laser shot.
     */
    const val LASER_DAMAGE = 1

    /**
     * Vibration duration after hitting a meteor or a laser shot.
     */
    const val VIBRATION_DAMAGE_DURATION_MS = 150L

    /**
     * Point multiplier used after destroying a meteor.
     */
    const val METEOR_POINTS_MULTIPLIER = 2

    /**
     * Points to add to the score when the user win a special bonus.
     */
    const val BONUS_POINTS = 10

    /**
     * Delay before adding the first enemy ship.
     */
    const val INITIAL_DELAY_BEFORE_ADDING_ENEMY_SHIP_IN_SECONDS = 5L

    const val STARS_MULTIPLIER = 0.05f

    const val MIN_METEOR_MULTIPLIER = 0.001f

    const val MAX_METEOR_MULTIPLIER = 0.004f

    const val DELTA_METEOR_MULTIPLIER = 0.001f

    const val DRAW_CHANCE_BOLT = 20

    const val DRAW_CHANCE_SHIELD = 35

    const val DRAW_CHANCE_STAR = 45

}