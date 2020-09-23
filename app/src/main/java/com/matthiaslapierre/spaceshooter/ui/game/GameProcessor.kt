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
import com.matthiaslapierre.spaceshooter.R
import com.matthiaslapierre.spaceshooter.resources.Drawables
import com.matthiaslapierre.spaceshooter.resources.Scores
import com.matthiaslapierre.spaceshooter.resources.TypefaceHelper
import com.matthiaslapierre.spaceshooter.ui.game.sprite.*
import com.matthiaslapierre.spaceshooter.util.Utils
import kotlin.math.max

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
    private var screenWidth: Float = 0f
    private var screenHeight: Float = 0f
    private var duration: Long = 0L
    private var lastAddingTimestamp: Long = 0
    private var countPowerUpToGenerate = 0
    private var isPaused: Boolean = false

    private var oldTouchX: Float? = null
    private var oldTouchY: Float? = null

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

                // Show the score.
                scoreSprite?.onDraw(canvas, globalPaint, currentStatus)
                // Show the current level.
                levelSprite?.onDraw(canvas, globalPaint, currentStatus)
                // Show the life level.
                lifeLevelSprite?.onDraw(canvas, globalPaint, currentStatus)
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

            // Add background.
            setBackground()

            when (currentStatus) {
                ISprite.STATUS_NOT_STARTED -> {
                    // Show the home screen
                    setSplash()
                    // Add stars.
                    setStars()
                    // Add the play ship.
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
                    // Add enemy ships
                    setEnemyShips()
                    // Start the guns.
                    shot()
                    // Update life properties.
                    checkCollisions()
                    // Check if the player has won power up.
                    checkPowerUpConsumption()
                    // Add power up.
                    setPowerUp()
                    // Show the score.
                    setScore(points)
                    // Show the current level.
                    setLevel(level)
                    // Show the life level.
                    setLifeLevel(playerSprite?.life ?: 0)
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
        startGame()
    }

    fun pause() {
        isPaused = true
    }

    fun resume() {
        isPaused = false
    }

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
    fun clean() {
        gameInterface = null
    }

    private fun checkCollisions() {
        val laserSprites = workSprites.filterIsInstance<LaserSprite>()
        val meteorSprites = workSprites.filterIsInstance<MeteorSprite>()
        val enemyShipSprites = workSprites.filterIsInstance<EnemyShipSprite>()
        val playerLaserSprites = laserSprites.filter { laser -> !laser.adverse }
        val enemyLaserSprites = laserSprites.filter { laser -> laser.adverse }
        playerSprite?.let { player ->
            checkCollisions(player, meteorSprites)
            checkCollisions(player, enemyLaserSprites)
            checkCollisions(player, enemyShipSprites)
        }
        checkCollisions(enemyShipSprites, playerLaserSprites)
        checkCollisions(meteorSprites, laserSprites)
    }

    private fun checkCollisions(livingSprites: List<ISprite>, damagingSprites: List<ISprite>) {
        val iterator = livingSprites.listIterator()
        while (iterator.hasNext()) {
            val livingSprite = iterator.next()
            checkCollisions(livingSprite, damagingSprites)
        }
    }

    private fun checkCollisions(livingSprite: ISprite, damagingSprites: List<ISprite>) {
        for(damagingSprite in damagingSprites) {
            if(livingSprite is ILiving
                && damagingSprite is IDamaging
                && damagingSprite.isHit(livingSprite)) {
                livingSprite.life -= damagingSprite.damage
                if(damagingSprite is IConsumable) {
                    damagingSprite.isConsumed = true
                } else if(damagingSprite is ILiving) {
                    damagingSprite.life = 0
                    workSprites.add(ExplodeSprite(drawables, damagingSprite.getRectF()))
                    if(damagingSprite is EnemyShipSprite || damagingSprite is PlayerSprite) {
                        gameInterface?.onShipExplode()
                    } else {
                        gameInterface?.onMeteorExplode()
                    }
                }
                if(livingSprite.life == 0) {
                    workSprites.add(ExplodeSprite(drawables, livingSprite.getRectF()))
                    if(damagingSprite is EnemyShipSprite || damagingSprite is PlayerSprite) {
                        gameInterface?.onShipExplode()
                    } else {
                        gameInterface?.onMeteorExplode()
                    }
                } else {
                    gameInterface?.onHit()
                }
            }
        }
    }

    private fun checkPowerUpConsumption() {
        playerSprite?.let { player ->
            workSprites.filterIsInstance<PowerUpSprite>().forEach { powerUp ->
                if (playerSprite?.getRectF()?.intersect(powerUp.getRectF()) == true
                    && !powerUp.isConsumed) {
                    powerUp.isConsumed = true
                    when (powerUp.type) {
                        PowerUpSprite.TYPE_BOLT -> player.upgrate()
                        PowerUpSprite.TYPE_SHIELD -> player.life += (Constants.PLAYER_MAX_LIFE * 0.2f).toInt()
                        PowerUpSprite.TYPE_STAR -> points += 10
                        else -> points += 10
                    }
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
     * Cleans all sprites. Resets properties.
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
        lastAddingTimestamp = 0
    }

    /**
     * Starts the game.
     */
    private fun startGame() {
        resetGame()
        currentStatus = ISprite.STATUS_PLAY
        gameInterface?.onGameStart()
    }

    /**
     * Moves the player's ship with the finger.
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
                        points += if(sprite.life == 0) {
                            sprite.getScore()
                        } else {
                            1
                        }
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
    }

    /**
     * Cleans the canvas. Pixels are cleared to 0.
     */
    private fun cleanCanvas(canvas: Canvas) {
        canvas.drawColor(0x00000000, PorterDuff.Mode.CLEAR)
    }

    private fun setBackground() {
        if(backgroundSprite == null || !backgroundSprite!!.isAlive()) {
            backgroundSprite = BackgroundSprite(context)
            workSprites.add(0, backgroundSprite!!)
        }
    }

    private fun setPlayerShip() {
        if(playerSprite == null || !playerSprite!!.isAlive()) {
            playerSprite = PlayerSprite(context, drawables)
            workSprites.add(playerSprite!!)
        }
    }

    private fun setScore(points: Int) {
        if(scoreSprite == null || !scoreSprite!!.isAlive()) {
            scoreSprite = ScoreSprite(context, drawables, typefaceHelper)
        }
        scoreSprite!!.currentScore = points
    }

    private fun setLevel(level: Int) {
        if(levelSprite == null || !levelSprite!!.isAlive()) {
            levelSprite = LevelSprite(context, drawables, typefaceHelper)
        }
        levelSprite!!.currentLevel = level
    }

    private fun setLifeLevel(life: Int) {
        if(lifeLevelSprite == null || !lifeLevelSprite!!.isAlive()) {
            lifeLevelSprite = LifeLevelSprite(context, drawables)
        }
        lifeLevelSprite!!.currentLife = life
    }

    private fun setStars() {
        val init = countStars == 0
        while (countStars < countMinStars()) {
            val y = if(init) Utils.getRandomFloat(0f, screenHeight) else
                -Utils.getRandomFloat(screenHeight * .05f, screenHeight)
            lastStarSprite = StarSprite(context, drawables, y)
            workSprites.add(1, lastStarSprite!!)
            countStars++
        }
    }

    private fun setSplash() {
        if(splashSprite == null || !splashSprite!!.isAlive()) {
            splashSprite = SplashSprite(context, typefaceHelper)
            workSprites.add(splashSprite!!)
        }
    }

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

    private fun setMeteors() {
        while (countMeteors < countMinMeteors()) {
            val meteorSprite = MeteorSprite(
                context,
                drawables,
                -Utils.getRandomFloat(screenHeight * 0.2f, screenHeight)
            )
            workSprites.add(meteorSprite)
            countMeteors++
        }
    }

    private fun setEnemyShips() {
        addEnemyShipIfNeeded()
        positionEnemyShip()
    }

    private fun addEnemyShipIfNeeded() {
        if(lastAddingTimestamp == 0L) {
            lastAddingTimestamp = System.currentTimeMillis()
        }
        if (System.currentTimeMillis() - delayBeforeAddingEnemyShip(getLevel()) > lastAddingTimestamp
            && countEnemyShips < countMaxEnemyShips()) {
            addEnemyShip()
        }
    }

    private fun addEnemyShip() {
        val n = Utils.getRandomInt(-1, 2)
        val x: Float
        val y: Float
        when(n) {
            -1 -> {
                x = -screenWidth * 0.5f
                y = Utils.getRandomFloat(0f, screenHeight * 0.5f)
            }
            1 -> {
                x = screenWidth * 1.5f
                y = Utils.getRandomFloat(0f, screenHeight * 0.5f)
            }
            else -> {
                x = Utils.getRandomFloat(0f, screenWidth)
                y = -screenHeight * 0.5f
            }
        }
        workSprites.add(EnemyShipSprite(context, drawables, x, y))
        lastAddingTimestamp = System.currentTimeMillis()
        countEnemyShips++
    }

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

    private fun shot() {
        playerSprite?.let { shot(it) }

        val enemyShips = workSprites.filterIsInstance<EnemyShipSprite>()
        enemyShips.forEach { ship ->
            shot(ship)
        }
    }

    private fun shot(playerSprite: PlayerSprite)  = playerSprite.apply {
        if(lastShotTimestamp < System.currentTimeMillis() - (1000 / Constants.PLAYER_RATE_OF_FIRE)) {
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

    private fun shot(enemyShipSprite: EnemyShipSprite) = enemyShipSprite.apply {
        if(RectF(0f, 0f, screenWidth, screenHeight).contains(enemyShipSprite.getRectF())) {
            if (lastShotTimestamp < System.currentTimeMillis() - (1000 / Constants.ENEMY_RATE_OF_FIRE)) {
                workSprites.add(
                    LaserSprite(
                        context,
                        drawables,
                        getRectF().centerX(),
                        getRectF().bottom,
                        LaserSprite.TYPE_STANDARD,
                        true
                    )
                )
                lastShotTimestamp = System.currentTimeMillis()
            }
        }
    }

    private fun setPowerUp() {
        while(countPowerUpToGenerate >= 1) {
            addPowerUp()
            countPowerUpToGenerate--
        }
    }

    private fun addPowerUp() {
        playerSprite?.let { player ->
            val powerUps = arrayOf(
                PowerUpSprite.TYPE_SHIELD,
                PowerUpSprite.TYPE_BOLT,
                PowerUpSprite.TYPE_STAR,
                PowerUpSprite.TYPE_SHIELD,
                PowerUpSprite.TYPE_STAR,
                PowerUpSprite.TYPE_SHIELD,
                PowerUpSprite.TYPE_STAR,
                PowerUpSprite.TYPE_BOLT,
                PowerUpSprite.TYPE_SHIELD,
                PowerUpSprite.TYPE_BOLT,
                PowerUpSprite.TYPE_STAR,
                PowerUpSprite.TYPE_SHIELD,
                PowerUpSprite.TYPE_STAR,
                PowerUpSprite.TYPE_SHIELD,
                PowerUpSprite.TYPE_STAR,
                PowerUpSprite.TYPE_BOLT,
                PowerUpSprite.TYPE_SHIELD
            )
            val powerUpType = when (powerUps[Utils.getRandomInt(0, powerUps.size)]) {
                PowerUpSprite.TYPE_BOLT -> if (duration >= 30000L && player.type < 3) {
                    PowerUpSprite.TYPE_BOLT
                } else {
                    PowerUpSprite.TYPE_STAR
                }
                PowerUpSprite.TYPE_SHIELD -> if (player.life < Constants.PLAYER_MAX_LIFE) {
                    PowerUpSprite.TYPE_SHIELD
                } else {
                    PowerUpSprite.TYPE_STAR
                }
                else -> PowerUpSprite.TYPE_STAR
            }
            val powerUpWidth = Utils.getDimenInPx(context, R.dimen.powerUpSize)
            val randomX = Utils.getRandomFloat(0f, screenWidth - powerUpWidth)
            val y = -(screenHeight * 0.2f)
            workSprites.add(PowerUpSprite(context, drawables, powerUpType, randomX, y))
        }
    }

    private fun getLevel(): Int = (duration / (Constants.LEVEL_DURATION_IN_SECONDS * 1000L)).toInt() + 1

    private fun countMaxEnemyShips(): Int {
        val enemyShipWidth = Utils.getDimenInPx(context, R.dimen.enemyShipWidth)
        return ((screenWidth / enemyShipWidth).toInt()) - 1
    }

    private fun countMinStars(): Int = (screenHeight.toInt() * 0.05).toInt()

    private fun countMinMeteors(): Int {
        val minMeteors = (screenWidth * 0.001f).toInt()
        val maxMeteors = (screenWidth * 0.004f).toInt()
        val delta = (screenWidth * 0.001f * getLevel()).toInt()
        return (minMeteors + delta).coerceAtLeast(maxMeteors)
    }

    private fun delayBeforeAddingEnemyShip(level: Int): Long =
        if(countEnemyShips == 0) {
            5
        } else {
            (Constants.LEVEL_DURATION_IN_SECONDS - ((level - 1) * 2)) * 1000L
        }

    interface GameInterface {
        fun onGameStart()
        fun onGameOver()
        fun onHit()
        fun onMeteorExplode()
        fun onShipExplode()
    }

}