package com.matthiaslapierre.spaceshooter.ui.game

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.RectF
import android.view.MotionEvent
import android.view.SurfaceHolder
import androidx.annotation.WorkerThread
import com.matthiaslapierre.spaceshooter.Constants
import com.matthiaslapierre.spaceshooter.Constants.BONUS_POINTS
import com.matthiaslapierre.spaceshooter.Constants.DELTA_METEOR_MULTIPLIER
import com.matthiaslapierre.spaceshooter.Constants.DRAW_CHANCE_BOLT
import com.matthiaslapierre.spaceshooter.Constants.DRAW_CHANCE_SHIELD
import com.matthiaslapierre.spaceshooter.Constants.INITIAL_DELAY_BEFORE_ADDING_ENEMY_SHIP_IN_SECONDS
import com.matthiaslapierre.spaceshooter.Constants.MAX_METEOR_MULTIPLIER
import com.matthiaslapierre.spaceshooter.Constants.MIN_METEOR_MULTIPLIER
import com.matthiaslapierre.spaceshooter.Constants.STARS_MULTIPLIER
import com.matthiaslapierre.spaceshooter.R
import com.matthiaslapierre.spaceshooter.resources.Drawables
import com.matthiaslapierre.spaceshooter.resources.Scores
import com.matthiaslapierre.spaceshooter.resources.TypefaceHelper
import com.matthiaslapierre.spaceshooter.ui.game.sprite.*
import com.matthiaslapierre.spaceshooter.util.Utils

/**
 * Handles the game loop.
 */
class GameProcessor(
    private val context: Context,
    private val holder: SurfaceHolder,
    private val globalPaint: Paint,
    private val drawables: Drawables,
    private val typefaceHelper: TypefaceHelper,
    private val scores: Scores,
    private var gameInterface: GameInterface?
): GameOverSprite.GameOverInterface {

    private var currentStatus: Int = ISprite.STATUS_NOT_STARTED
    private var points: Int = 0
    private var workSprites: MutableList<ISprite> = mutableListOf()
    private var duration: Long = 0L
    private var isPaused: Boolean = false

    private var splashSprite: SplashSprite? = null
    private var playerSprite: PlayerSprite? = null
    private var scoreSprite: ScoreSprite? = null
    private var levelSprite: LevelSprite? = null
    private var lifeLevelSprite: LifeLevelSprite? = null
    private var backgroundSprite: BackgroundSprite? = null
    private var gameOverSprite: GameOverSprite? = null
    private var lastStarSprite: StarSprite? = null

    private var countStars: Int = 0
    private var countMeteors: Int = 0
    private var countEnemyShips: Int = 0
    private var countPowerUpToGenerate = 0
    private var lastEnemyAdditionTimestamp: Long = 0

    private var screenWidth: Float = 0f
    private var screenHeight: Float = 0f

    private var oldTouchX: Float? = null
    private var oldTouchY: Float? = null

    init {
        backgroundSprite = BackgroundSprite(context)
    }

    @WorkerThread
    fun execute() {
        /*
        In our DrawingThread, we loop as long as the Thread is active.
        First, we take care of the rendering of our game. To do this, we obtain a reference to the
        Canvas of our SurfaceHolder object by calling its lockCanvas method.
        We then empty the content of this Canvas before iterating on all the elements of our
        game that we want to return to the screen. These elements being our Sprites.
         */
        while (!Thread.interrupted()) {
            if(isPaused) continue

            val startTime = System.currentTimeMillis()
            val canvas = holder.lockCanvas()

            screenWidth = canvas.width.toFloat()
            screenHeight = canvas.height.toFloat()

            try {
                cleanCanvas(canvas)

                /*
                This iteration is performed via an Iterator object and we use it to delete Sprites
                considered as no longer alive. This work is encapsulated within a try / finally block.
                In the finally part, we ask that the updates we have made on the Canvas be posted on
                the SurfaceHolder via a call to the unlockCanvasAndPost method with the current
                instance of Canvas passed as a parameter.
                 */
                updateCanvas(canvas)
            } finally {
                holder.unlockCanvasAndPost(canvas)
            }

            /*
            The rendering time is measured before comparing this time to a constant called GAP.
            This constant allows us to add, if necessary, a delay to avoid that the rendering
            phase of the Game Loop be too fast.
             */
            val frameDuration = System.currentTimeMillis() - startTime
            val gap = Constants.MS_PER_FRAME - frameDuration
            if (gap > 0) {
                try {
                    Thread.sleep(gap)
                } catch (e: Exception) {
                    break
                }
            }

            // Update the list of sprites to draw.
            when (currentStatus) {
                ISprite.STATUS_NOT_STARTED -> {
                    // Show the home screen
                    setSplash()
                    // Add stars.
                    setStars()
                    // Add the player space ship.
                    setPlayerShip()
                }
                ISprite.STATUS_GAME_OVER -> {
                    // Show the Game Over screen.
                    setGameOver()
                }
                ISprite.STATUS_PLAY -> {
                    val level = getLevel()
                    // Add stars.
                    setStars()
                    // Add meteors.
                    setMeteors()
                    // Add enemy space ships
                    setEnemyShips()
                    // Start the guns.
                    shot()
                    // Check collisions with meteors, lasers and space ships.
                    checkCollisions()
                    // Check if the player has won a bonus.
                    checkPowerUpConsumption()
                    // Add bonus to win.
                    setPowerUp()
                    // Update the score.
                    scoreSprite?.currentScore = points
                    // Update the current level.
                    levelSprite?.currentLevel = level
                    // Update the life level.
                    lifeLevelSprite?.currentLife = playerSprite?.life ?: 0
                    if (playerSprite == null || !playerSprite!!.isAlive()) {
                        // Save the new best score.
                        if (scores.isNewBestScore(context, points)) {
                            scores.storeHighScore(context, points)
                        }
                        // Show the "Game Over" message.
                        currentStatus = ISprite.STATUS_GAME_OVER
                        gameInterface?.onGameOver()
                    }
                }
            }

            duration += (System.currentTimeMillis() - startTime)
        }
    }

    override fun onReplayBtnPressed() {
        // After pressing the replay button, we replay the game.
        startGame()
    }

    /**
     * Pauses the game (pauses the game loop).
     */
    fun pause() {
        isPaused = true
    }

    /**
     * Resumes the game (resumes the game loop).
     */
    fun resume() {
        isPaused = false
    }

    /**
     * Handles touch event.
     */
    fun onTouch(event: MotionEvent) {
        when (currentStatus) {
            ISprite.STATUS_NOT_STARTED -> startGame()
            ISprite.STATUS_PLAY -> movePlayerShip(event)
            ISprite.STATUS_GAME_OVER -> gameOverSprite?.onTouch(event)
        }
    }

    /**
     * Prevents memory leakage (some objects were not releasing memory).
     */
    fun release() {
        gameInterface = null
    }

    /**
     * Checks collisions.
     */
    private fun checkCollisions() {
        val laserSprites = workSprites.filterIsInstance<LaserSprite>()
        val meteorSprites = workSprites.filterIsInstance<MeteorSprite>()
        val enemyShipSprites = workSprites.filterIsInstance<EnemyShipSprite>()
        val playerLaserSprites = laserSprites.filter { laser -> !laser.adverse }
        val enemyLaserSprites = laserSprites.filter { laser -> laser.adverse }
        playerSprite?.let { player ->
            // Collision between the player and meteors.
            checkCollisions(player, meteorSprites)
            // Collision between the player and the enemy laser shot.
            checkCollisions(player, enemyLaserSprites)
            // Collision between the player and the enemy ships.
            checkCollisions(player, enemyShipSprites)
        }
        // Collision between the enemy ships and the player laser shots.
        checkCollisions(enemyShipSprites, playerLaserSprites)
        // Collision between meteors and laser shots.
        checkCollisions(meteorSprites, laserSprites)
    }

    /**
     * Checks collisions between living sprites and damaging sprites.
     */
    private fun checkCollisions(livingSprites: List<ISprite>, damagingSprites: List<ISprite>) {
        val iterator = livingSprites.listIterator()
        while (iterator.hasNext()) {
            val livingSprite = iterator.next()
            checkCollisions(livingSprite, damagingSprites)
        }
    }

    /**
     * Checks collisions between a living sprite and damaging sprites.
     */
    private fun checkCollisions(livingSprite: ISprite, damagingSprites: List<ISprite>) {
        for(damagingSprite in damagingSprites) {
            if(livingSprite is ILiving
                && damagingSprite is IDamaging
                && damagingSprite.isHit(livingSprite)) {
                // Update the life property.
                livingSprite.life -= damagingSprite.damage
                if(damagingSprite is IConsumable) {
                    // Indicate that the object is consumed (destroyed).
                    damagingSprite.isConsumed = true
                } else if(damagingSprite is ILiving) {
                    // If the object is living, set its health level to 0 (destroy it).
                    damagingSprite.life = 0
                    workSprites.add(ExplodeSprite(drawables, damagingSprite.getRectF()))
                    // Play sound effects.
                    if(damagingSprite is EnemyShipSprite || damagingSprite is PlayerSprite) {
                        gameInterface?.onShipExplode()
                    } else {
                        gameInterface?.onMeteorExplode()
                    }
                }
                // If the sprite is died or damaged, we play sound effects.
                if(livingSprite.life == 0) {
                    workSprites.add(ExplodeSprite(drawables, livingSprite.getRectF()))
                    if(livingSprite is EnemyShipSprite || livingSprite is PlayerSprite) {
                        gameInterface?.onShipExplode()
                    } else {
                        gameInterface?.onMeteorExplode()
                    }
                } else {
                    gameInterface?.onHit(livingSprite is PlayerSprite)
                }
            }
        }
    }

    /**
     * Checks if the player has won a bonus.
     */
    private fun checkPowerUpConsumption() {
        playerSprite?.let { player ->
            workSprites.filterIsInstance<PowerUpSprite>().forEach { powerUp ->
                if (playerSprite?.getRectF()?.intersect(powerUp.getRectF()) == true
                    && !powerUp.isConsumed) {
                    powerUp.isConsumed = true
                    when (powerUp.type) {
                        PowerUpSprite.TYPE_BOLT -> player.upgrade()
                        PowerUpSprite.TYPE_SHIELD -> player.life += (Constants.PLAYER_MAX_LIFE * 0.2f).toInt()
                        PowerUpSprite.TYPE_STAR -> points += BONUS_POINTS
                        else -> points += BONUS_POINTS
                    }
                    gameInterface?.onPowerUpWin()
                }
            }
        }
    }

    private fun getTouchOffset(
        oldVal: Float,
        newVal: Float
    ): Float {
        return newVal - oldVal
    }

    /**
     * Clears all sprites. Resets properties.
     */
    private fun resetGame() {
        workSprites = workSprites.filterIsInstance<StarSprite>().toMutableList()
        playerSprite = PlayerSprite(context, drawables)
        workSprites.add(playerSprite!!)
        splashSprite = null
        gameOverSprite = null
        scoreSprite = null
        levelSprite = null
        lifeLevelSprite = null
        duration = 0L
        points = 0
        countMeteors = 0
        countEnemyShips = 0
        lastEnemyAdditionTimestamp = 0
        countPowerUpToGenerate = 0
        oldTouchX = null
        oldTouchY = null
    }

    /**
     * Starts the game.
     */
    private fun startGame() {
        resetGame()
        currentStatus = ISprite.STATUS_PLAY
        scoreSprite = ScoreSprite(context, drawables, typefaceHelper)
        levelSprite = LevelSprite(context, drawables, typefaceHelper)
        lifeLevelSprite = LifeLevelSprite(context)
        gameInterface?.onGameStart()
    }

    /**
     * Moves the player's space ship with the fingers.
     */
    private fun movePlayerShip(event: MotionEvent) {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                oldTouchX = event.x
                oldTouchY = event.y
            }
            MotionEvent.ACTION_MOVE -> {
                val newX = event.x
                val newY = event.y
                if(oldTouchX == null) oldTouchX = newX
                if(oldTouchY == null) oldTouchY = newY
                val offsetX: Float = getTouchOffset(oldTouchX!!, newX)
                val offsetY: Float = getTouchOffset(oldTouchY!!, newY)
                playerSprite?.apply {
                    x += offsetX
                    y += offsetY
                }
                oldTouchX = newX
                oldTouchY = newY
            }
        }
    }

    /**
     * Draws sprites and removes old sprites.
     */
    private fun updateCanvas(canvas: Canvas) {
        // Background
        backgroundSprite?.onDraw(canvas, globalPaint, currentStatus)

        val iterator: MutableListIterator<ISprite> = workSprites.listIterator()
        while (iterator.hasNext()) {
            val sprite = iterator.next()
            if (sprite.isAlive()) {
                sprite.onDraw(canvas, globalPaint, currentStatus)
            } else {
                iterator.remove()
                sprite.onCleared()
                when(sprite) {
                    is StarSprite -> countStars--
                    is MeteorSprite -> {
                        /*points += if(sprite.life == 0) {
                            sprite.getScore()
                        } else {
                            1
                        }*/
                        countMeteors--
                    }
                    is EnemyShipSprite -> {
                        points += sprite.getScore()
                        countPowerUpToGenerate++
                        countEnemyShips--
                    }
                }
            }
        }

        // Foreground
        // Show the score.
        scoreSprite?.onDraw(canvas, globalPaint, currentStatus)
        // Show the current level.
        levelSprite?.onDraw(canvas, globalPaint, currentStatus)
        // Show the life level.
        lifeLevelSprite?.onDraw(canvas, globalPaint, currentStatus)
    }

    /**
     * Cleans the canvas. Pixels are cleared to 0.
     */
    private fun cleanCanvas(canvas: Canvas) {
        canvas.drawColor(0x00000000, PorterDuff.Mode.CLEAR)
    }

    /**
     * Adds the player space ship if needed.
     */
    private fun setPlayerShip() {
        if(playerSprite == null || !playerSprite!!.isAlive()) {
            playerSprite = PlayerSprite(context, drawables)
            workSprites.add(playerSprite!!)
        }
    }

    /**
     * Adds stars in the intergalactic space.
     */
    private fun setStars() {
        val init = countStars == 0
        while (countStars < getMinimumNumberOfStarsToDisplay()) {
            // Get a random y-coordinate.
            val y = if(init) Utils.getRandomFloat(0f, screenHeight) else
                -Utils.getRandomFloat(screenHeight * .05f, screenHeight)
            lastStarSprite = StarSprite(context, drawables, y)
            workSprites.add(0, lastStarSprite!!)
            countStars++
        }
    }

    /**
     * Adds the spash UI if needed.
     */
    private fun setSplash() {
        if(splashSprite == null || !splashSprite!!.isAlive()) {
            splashSprite = SplashSprite(context, typefaceHelper)
            workSprites.add(splashSprite!!)
        }
    }

    /**
     * Adds the Game Over UI if needed.
     */
    private fun setGameOver() {
        if(gameOverSprite == null || !gameOverSprite!!.isAlive()) {
            gameOverSprite = GameOverSprite(
                context,
                drawables,
                typefaceHelper,
                points,
                scores.highScore(context),
                this@GameProcessor
            )
            workSprites.add(gameOverSprite!!)
        }
    }

    /**
     * Adds meteors.
     */
    private fun setMeteors() {
        while (countMeteors < getMinimumNumberOfMeteorsToDisplay()) {
            val randomY = -Utils.getRandomFloat(screenHeight * 0.2f, screenHeight)
            val meteorSprite = MeteorSprite(
                context,
                drawables,
                randomY
            )
            workSprites.add(meteorSprite)
            countMeteors++
        }
    }

    /**
     * Adds enemy ships if needed. Positions them.
     */
    private fun setEnemyShips() {
        addEnemyShipIfNeeded()
        positionEnemyShip()
    }

    /**
     * Adds enemy ships if needed.
     */
    private fun addEnemyShipIfNeeded() {
        if(lastEnemyAdditionTimestamp == 0L) {
            lastEnemyAdditionTimestamp = System.currentTimeMillis()
        }
        // Add an enemy ship each n seconds if the number of enemy ship is insufficient.
        if (System.currentTimeMillis() - delayBeforeAddingEnemyShip(getLevel()) > lastEnemyAdditionTimestamp
            && countEnemyShips < getMinimumNumberOfEnemyShipsToDisplay()) {
            addEnemyShip()
        }
    }

    /**
     * Add an enemy ship.
     */
    private fun addEnemyShip() {
        // Get random x and y-coordinates.
        val n = Utils.getRandomInt(-1, 2) // 3 display modes
        val x: Float
        val y: Float
        when(n) {
            -1 -> { // Enter from the left
                x = -screenWidth * 0.5f
                y = Utils.getRandomFloat(0f, screenHeight * 0.45f)
            }
            1 -> { // Enter from the the right.
                x = screenWidth * 1.5f
                y = Utils.getRandomFloat(0f, screenHeight * 0.45f)
            }
            else -> { // Enter from the top.
                x = Utils.getRandomFloat(0f, screenWidth)
                y = -screenHeight * 0.5f
            }
        }
        workSprites.add(EnemyShipSprite(context, drawables, x, y))
        lastEnemyAdditionTimestamp = System.currentTimeMillis()
        countEnemyShips++
    }

    /**
     * Positions enemy ships. Sets the min x-coordinate and the max x-coordinate.
     */
    private fun positionEnemyShip() {
        val enemyShipWidth = Utils.getDimenInPx(context, R.dimen.enemyShipWidth)
        val gap = enemyShipWidth / 2f
        val enemyShips = workSprites.filterIsInstance<EnemyShipSprite>()
        val countEnemyShips = enemyShips.size
        val movementWidth = screenWidth / countEnemyShips
        var minX = gap
        var maxX = movementWidth - gap
        enemyShips.forEach { ship ->
            ship.minX = minX
            ship.maxX = maxX
            minX += movementWidth
            maxX += movementWidth
        }
    }

    /**
     * Shot with laser guns.
     */
    private fun shot() {
        // Player.
        playerSprite?.let { shot(it) }

        // Enemy space ships.
        val enemyShips = workSprites.filterIsInstance<EnemyShipSprite>()
        enemyShips.forEach { ship ->
            shot(ship)
        }
    }

    /**
     * Player laser shot.
     */
    private fun shot(playerSprite: PlayerSprite)  = playerSprite.apply {
        if(lastShotTimestamp < System.currentTimeMillis() - (1000 / Constants.PLAYER_RATE_OF_FIRE)) {
            // 3 firing modes.
            when (playerSprite.type) {
                1 ->  workSprites.add(LaserSprite(context, drawables, getRectF().centerX(), playerSprite.y))
                2 -> {
                    workSprites.add(LaserSprite(
                        context,
                        drawables,
                        getRectF().centerX() - getRectF().width() * 0.3f,
                        playerSprite.y)
                    )
                    workSprites.add(LaserSprite(
                        context,
                        drawables,
                        getRectF().centerX() + getRectF().width() * 0.3f,
                        playerSprite.y)
                    )
                }
                3 -> {
                    workSprites.add(LaserSprite(
                        context,
                        drawables,
                        getRectF().centerX() - getRectF().width() * 0.3f,
                        playerSprite.y)
                    )
                    workSprites.add(LaserSprite(
                        context,
                        drawables,
                        getRectF().centerX(),
                        playerSprite.y)
                    )
                    workSprites.add(LaserSprite(
                        context,
                        drawables,
                        getRectF().centerX() + getRectF().width() * 0.3f,
                        playerSprite.y)
                    )
                }
            }
            lastShotTimestamp = System.currentTimeMillis()
        }
    }

    /**
     * Enemy laser shots.
     */
    private fun shot(enemyShipSprite: EnemyShipSprite) = enemyShipSprite.apply {
        if(RectF(0f, 0f, screenWidth, screenHeight).contains(enemyShipSprite.getRectF())) {
            if (lastShotTimestamp < System.currentTimeMillis() - (1000 / Constants.ENEMY_RATE_OF_FIRE)) {
                workSprites.add(
                    LaserSprite(
                        context,
                        drawables,
                        getRectF().centerX(),
                        getRectF().bottom,
                        true
                    )
                )
                lastShotTimestamp = System.currentTimeMillis()
            }
        }
    }

    /**
     * Adds bonus if needed.
     */
    private fun setPowerUp() {
        while(countPowerUpToGenerate >= 1) {
            addPowerUp()
            countPowerUpToGenerate--
        }
    }

    /**
     * Adds bonus.
     */
    private fun addPowerUp() {
        playerSprite?.let { player ->
            // Get a random bonus.
            val randomInt = Utils.getRandomInt(1, 100)
            val powerUpType = when  {
                // Be careful not to win the bonus too early.
                randomInt < DRAW_CHANCE_BOLT -> // TYPE_BOLT
                    if (duration >= 30000L && player.type < 3) {
                        PowerUpSprite.TYPE_BOLT
                    } else {
                        PowerUpSprite.TYPE_STAR
                    }
                randomInt < (DRAW_CHANCE_BOLT + DRAW_CHANCE_SHIELD) -> // TYPE_SHIELD
                    // No shield extra if the shield is not damaged.
                    if (player.life < Constants.PLAYER_MAX_LIFE) {
                        PowerUpSprite.TYPE_SHIELD
                    } else {
                        PowerUpSprite.TYPE_STAR
                    }
                else -> PowerUpSprite.TYPE_STAR // Default: TYPE_STAR
            }
            val powerUpWidth = Utils.getDimenInPx(context, R.dimen.powerUpSize)
            // Get a random x.
            val randomX = Utils.getRandomFloat(0f, screenWidth - powerUpWidth)
            // Get a random y-coordinate (enter from the top).
            val y = -(screenHeight * 0.2f)
            workSprites.add(PowerUpSprite(context, drawables, powerUpType, randomX, y))
        }
    }

    /**
     * Returns the current level from the game duration.
     */
    private fun getLevel(): Int = (duration / (Constants.LEVEL_DURATION_IN_SECONDS * 1000L)).toInt() + 1

    /**
     * Minimum number of enemy space ships to display.
     */
    private fun getMinimumNumberOfEnemyShipsToDisplay(): Int {
        val enemyShipWidth = Utils.getDimenInPx(context, R.dimen.enemyShipWidth)
        return ((screenWidth / enemyShipWidth).toInt()) - 1
    }

    /**
     * Minimum number of stars to display.
     */
    private fun getMinimumNumberOfStarsToDisplay(): Int =
        (screenHeight.toInt() * STARS_MULTIPLIER).toInt()

    /**
     * Minimum number of meteors to display.
     */
    private fun getMinimumNumberOfMeteorsToDisplay(): Int {
        val minMeteors = (screenWidth * MIN_METEOR_MULTIPLIER).toInt()
        val maxMeteors = (screenWidth * MAX_METEOR_MULTIPLIER).toInt()
        val delta = (screenWidth * DELTA_METEOR_MULTIPLIER * getLevel()).toInt()
        return (minMeteors + delta).coerceAtLeast(maxMeteors)
    }

    /**
     * Returns the delay before adding the next enemy ship.
     */
    private fun delayBeforeAddingEnemyShip(level: Int): Long =
        if(countEnemyShips == 0) {
            INITIAL_DELAY_BEFORE_ADDING_ENEMY_SHIP_IN_SECONDS * 1000L
        } else {
            (Constants.LEVEL_DURATION_IN_SECONDS - ((level - 1) * 2)) * 1000L
        }

    interface GameInterface {
        fun onGameStart()
        fun onGameOver()
        fun onHit(playerShip: Boolean)
        fun onPowerUpWin()
        fun onMeteorExplode()
        fun onShipExplode()
    }

}