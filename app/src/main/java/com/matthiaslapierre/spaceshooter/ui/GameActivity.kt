package com.matthiaslapierre.spaceshooter.ui

import android.graphics.Paint
import android.graphics.PixelFormat
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.View
import com.matthiaslapierre.spaceshooter.App
import com.matthiaslapierre.spaceshooter.R
import com.matthiaslapierre.spaceshooter.resources.Drawables
import com.matthiaslapierre.spaceshooter.resources.Scores
import com.matthiaslapierre.spaceshooter.resources.SoundEngine
import com.matthiaslapierre.spaceshooter.resources.TypefaceHelper
import com.matthiaslapierre.spaceshooter.ui.game.DrawingThread
import kotlinx.android.synthetic.main.activity_game.*

class GameActivity : AppCompatActivity(), SurfaceHolder.Callback, View.OnTouchListener,
    DrawingThread.GameInterface {

    companion object {
        private const val TAG = "GameActivity"
    }

    private lateinit var drawables: Drawables
    private lateinit var typefaceHelper: TypefaceHelper
    private lateinit var soundEngine: SoundEngine
    private lateinit var scores: Scores

    private lateinit var holder: SurfaceHolder
    private val globalPaint: Paint by lazy {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint
    }
    private var surfaceCreated: Boolean = false
    private var drawingThread: DrawingThread? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

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
    }

    override fun onDestroy() {
        soundEngine.release()
        super.onDestroy()
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
            drawingThread?.onTouch(event)
        }
        return true
    }

    override fun onGameStart() {
        soundEngine.playBtnPress1()
        soundEngine.playPlayMusic()
    }

    override fun onGameOver() {
        soundEngine.playGameOver()
        soundEngine.playGameOverMusic()
    }

    override fun onHit() {
        soundEngine.playShotHit()
    }

    override fun onMeteorExplode() {
        soundEngine.playMeteorExplode()
    }

    override fun onShot() {
        soundEngine.playLaser(8)
    }

    /**
     * Once the SurfaceHolder object is created, we start our dedicated Thread that we call
     * DrawingThread.
     */
    private fun startDrawingThread() {
        stopDrawingThread()
        drawingThread = DrawingThread(
            applicationContext,
            holder,
            globalPaint,
            drawables,
            typefaceHelper,
            scores,
            this@GameActivity
        )
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
        drawingThread?.clean()
        drawingThread = null
    }

}