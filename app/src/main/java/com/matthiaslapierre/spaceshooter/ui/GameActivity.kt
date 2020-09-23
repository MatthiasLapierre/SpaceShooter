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
import com.matthiaslapierre.spaceshooter.Constants
import com.matthiaslapierre.spaceshooter.Constants.VIBRATION_DAMAGE_DURATION_MS
import com.matthiaslapierre.spaceshooter.R
import com.matthiaslapierre.spaceshooter.resources.Drawables
import com.matthiaslapierre.spaceshooter.resources.Scores
import com.matthiaslapierre.spaceshooter.resources.SoundEngine
import com.matthiaslapierre.spaceshooter.resources.TypefaceHelper
import com.matthiaslapierre.spaceshooter.ui.game.GameProcessor
import kotlinx.android.synthetic.main.activity_game.*

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

        soundEngine.playMenuMusic()

        surfaceView.keepScreenOn = true
        holder = surfaceView.holder
        surfaceView.setZOrderOnTop(true)
        surfaceView.setOnTouchListener(this)
        holder.addCallback(this)
        holder.setFormat(PixelFormat.TRANSLUCENT)

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
        gameProcessor.resume()
        soundEngine.resume()
    }

    override fun onPause() {
        soundEngine.pause()
        gameProcessor.pause()
        super.onPause()
    }

    override fun onDestroy() {
        soundEngine.release()
        gameProcessor.clean()
        super.onDestroy()
    }

    override fun onBackPressed() {

    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        surfaceCreated = true
        startDrawingThread()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {

    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        surfaceCreated = false
        stopDrawingThread()
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        event?.let {
            gameProcessor.onTouch(event)
        }
        return true
    }

    override fun onGameStart() {
        soundEngine.playBtnPress()
        soundEngine.playPlayMusic()
    }

    override fun onGameOver() {
        soundEngine.playCrash()
        soundEngine.playGameOver()
        soundEngine.playGameOverMusic()
    }

    override fun onHit(playerShip: Boolean) {
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
        soundEngine.playGetPowerUp()
    }

    override fun onMeteorExplode() {
        soundEngine.playMeteorExplode()
    }

    override fun onShipExplode() {
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