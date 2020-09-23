package com.matthiaslapierre.spaceshooter.ui

import android.content.Context
import android.graphics.Paint
import android.graphics.PixelFormat
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.View
import com.matthiaslapierre.spaceshooter.App
import com.matthiaslapierre.spaceshooter.Constants.VIBRATION_DAMAGE_DURATION_MS
import com.matthiaslapierre.spaceshooter.R
import com.matthiaslapierre.spaceshooter.resources.Drawables
import com.matthiaslapierre.spaceshooter.resources.Scores
import com.matthiaslapierre.spaceshooter.resources.SoundEngine
import com.matthiaslapierre.spaceshooter.resources.TypefaceHelper
import com.matthiaslapierre.spaceshooter.ui.game.GameProcessor
import kotlinx.android.synthetic.main.activity_game.*

/**
 * Space Shooter Game.
 */
class GameActivity : AppCompatActivity(), SurfaceHolder.Callback, View.OnTouchListener,
    GameProcessor.GameInterface {

    companion object {
        private const val TAG = "GameActivity"
    }

    private lateinit var vibrator: Vibrator
    private lateinit var drawables: Drawables
    private lateinit var typefaceHelper: TypefaceHelper
    private lateinit var soundEngine: SoundEngine
    private lateinit var scores: Scores
    private lateinit var gameProcessor: GameProcessor
    private lateinit var holder: SurfaceHolder
    private val globalPaint: Paint by lazy {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint
    }
    private var surfaceCreated: Boolean = false
    private var drawingThread: Thread? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        // Inject dependencies.
        val appContainer = (application as App).appContainer
        drawables = appContainer.drawables
        typefaceHelper = appContainer.typefaceHelper
        soundEngine = appContainer.soundEngine
        scores = appContainer.scores

        // Start the menu music.
        soundEngine.playMenuMusic()

        // Initialize the SurfaceView.
        surfaceView.keepScreenOn = true
        holder = surfaceView.holder
        surfaceView.setZOrderOnTop(true)
        surfaceView.setOnTouchListener(this)
        holder.addCallback(this)
        holder.setFormat(PixelFormat.TRANSLUCENT)

        // Initialize the GameProcessor.
        gameProcessor = GameProcessor(
            applicationContext,
            holder,
            globalPaint,
            drawables,
            typefaceHelper,
            scores,
            this@GameActivity
        )
    }

    override fun onResume() {
        super.onResume()
        // Resume the game.
        gameProcessor.resume()
        // Start the music.
        soundEngine.resume()
    }

    override fun onPause() {
        // Pause the music.
        soundEngine.pause()
        // Pause the game.
        gameProcessor.pause()
        super.onPause()
    }

    override fun onDestroy() {
        // Unload resources.
        soundEngine.release()
        gameProcessor.release()
        super.onDestroy()
    }

    override fun onBackPressed() {
        // Do nothing
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        surfaceCreated = true
        // After the surface is created, we start the game loop.
        startDrawingThread()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {

    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        // Stop the game loop after destroying the surface.
        surfaceCreated = false
        stopDrawingThread()
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        // Send touch event to the game processor.
        event?.let {
            gameProcessor.onTouch(event)
        }
        return true
    }

    override fun onGameStart() {
        // On the game start, a sound effect is played. The main music start.
        soundEngine.playBtnPress()
        soundEngine.playPlayMusic()
    }

    override fun onGameOver() {
        // On Game Over, play a special music.
        soundEngine.playCrash()
        soundEngine.playGameOver()
        soundEngine.playGameOverMusic()
    }

    override fun onHit(playerShip: Boolean) {
        // Play hit sound.
        soundEngine.playShotHit()
        if(playerShip) {
            // Vibrate the device for a short after hitting a meteor or a laser shot
            if (Build.VERSION.SDK_INT >= 26) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        VIBRATION_DAMAGE_DURATION_MS,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                vibrator.vibrate(VIBRATION_DAMAGE_DURATION_MS)
            }
        }
    }

    override fun onPowerUpWin() {
        // The user has won an extra => Play a special sound.
        soundEngine.playGetPowerUp()
    }

    override fun onMeteorExplode() {
        // A meteor has exploded. Play the explode sound effect.
        soundEngine.playMeteorExplode()
    }

    override fun onShipExplode() {
        // A space ship has exploded. Play the explode sound effect.
        soundEngine.playShipExplode()
    }

    /**
     * Once the SurfaceHolder object is created, we start our dedicated Thread that we call
     * DrawingThread.
     */
    private fun startDrawingThread() {
        stopDrawingThread()
        drawingThread = Thread(Runnable {
            gameProcessor.execute()
        })
        drawingThread!!.start()
    }

    /**
     * Thread is stopped when the callback surfaceDestroyed is called telling us that the
     * SurfaceHolder object instance is destroyed. This happens when the application is
     * paused or stopped, for example.
     */
    private fun stopDrawingThread() {
        drawingThread?.interrupt()
        try {
            drawingThread?.join()
        } catch (e: InterruptedException) {
            Log.e(TAG, "Failed to interrupt the drawing thread")
        }
        drawingThread = null
    }

}