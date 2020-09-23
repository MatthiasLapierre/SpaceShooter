package com.matthiaslapierre.spaceshooter.ui.game.sprite

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import androidx.core.graphics.toRect
import com.matthiaslapierre.spaceshooter.R
import com.matthiaslapierre.spaceshooter.resources.Drawables
import com.matthiaslapierre.spaceshooter.resources.TypefaceHelper
import com.matthiaslapierre.spaceshooter.util.Utils

/**
 * Game Over UI.
 */
class GameOverSprite(
    private val context: Context,
    private val drawables: Drawables,
    private val typefaceHelper: TypefaceHelper,
    private val lastScore: Int,
    private val bestScore: Int,
    private var gameOverInterface: GameOverInterface?
) : ISprite, ITouchable {

    private var screenWidth: Float = 0f
    private var screenHeight: Float = 0f
    private var isAlive: Boolean = true
    private val titleTextPaint: Paint by lazy {
        val paint = Paint()
        paint.textSize = Utils.getDimenInPx(context, R.dimen.gameOverTitleTextSize)
        paint.typeface = typefaceHelper.getFutureTypeface();
        paint.color = Color.WHITE
        paint
    }
    private val scoreTextPaint: Paint by lazy {
        val paint = Paint()
        paint.textSize = Utils.getDimenInPx(context, R.dimen.finalScoreTextSize)
        paint.typeface = typefaceHelper.getFutureTypeface();
        paint.color = Color.WHITE
        paint
    }
    private var replayRect: RectF? = null

    override fun onDraw(canvas: Canvas, globalPaint: Paint, status: Int) {
        isAlive = status == ISprite.STATUS_GAME_OVER
        screenWidth = canvas.width.toFloat()
        screenHeight = canvas.height.toFloat()

        // Get string resources.
        val strTitle = context.resources.getString(R.string.game_over)
        val strLastScore = context.resources.getString(R.string.last_score_params, lastScore)
        val strBestScore = context.resources.getString(R.string.best_score_params, bestScore)

        // Get the replay button.
        val replayBmp = Utils.generateButton(
            context,
            drawables,
            typefaceHelper,
            context.resources.getString(R.string.replay)
        )

        // Get dimensions.
        val titleBounds = Rect()
        val lastScoreBounds = Rect()
        val bestScoreBounds = Rect()
        titleTextPaint.getTextBounds(strTitle, 0, strTitle.length, titleBounds)
        scoreTextPaint.getTextBounds(strLastScore, 0, strLastScore.length, lastScoreBounds)
        scoreTextPaint.getTextBounds(strBestScore, 0, strBestScore.length, bestScoreBounds)
        val scoreMarginTop = Utils.getDimenInPx(context, R.dimen.finalScoreMarginTop)
        val replayBtnMarginTop = Utils.getDimenInPx(context, R.dimen.replayBtnMarginTop)

        // Compute the UI height.
        val height = titleBounds.height() +
                scoreMarginTop +
                lastScoreBounds.height() +
                (lastScoreBounds.height() * 2f) +
                bestScoreBounds.height() +
                replayBtnMarginTop

        // Draw the "Game Over" title.
        var y = (screenHeight / 2f) - (height / 2f)
        canvas.drawText(
            strTitle,
            screenWidth / 2f - titleBounds.width() / 2f,
            y,
            titleTextPaint
        )


        // Draw the last score.
        y += lastScoreBounds.height() + scoreMarginTop
        canvas.drawText(
            strLastScore,
            screenWidth / 2f - lastScoreBounds.width() / 2f,
            y,
            scoreTextPaint
        )

        // Draw the best score.
        y += lastScoreBounds.height() * 2f
        canvas.drawText(
            strBestScore,
            screenWidth / 2f - bestScoreBounds.width() / 2f,
            y,
            scoreTextPaint
        )

        // Draw the replay button.
        y += bestScoreBounds.height() + replayBtnMarginTop
        val replayBtnX = screenWidth / 2f - replayBmp.width / 2f
        replayRect = RectF(
            replayBtnX,
            y,
            replayBtnX + replayBmp.width,
            y + replayBmp.height
        )
        canvas.drawBitmap(
            replayBmp,
            Rect(0, 0, replayBmp.width, replayBmp.height),
            replayRect!!.toRect(),
            null
        )
    }

    override fun isAlive(): Boolean = isAlive

    override fun isHit(sprite: ISprite): Boolean = false

    override fun getScore(): Int = 0

    override fun getRectF(): RectF = RectF(
        0f,
        0f,
        screenWidth,
        screenHeight
    )

    override fun onTouch(event: MotionEvent) {
        if(event.action == MotionEvent.ACTION_UP
            && replayRect != null
            && replayRect!!.contains(event.x, event.y)) {
            gameOverInterface?.onReplayBtnPressed()
        }
    }

    override fun onCleared() {
        gameOverInterface = null
    }

    interface GameOverInterface {
        fun onReplayBtnPressed()
    }

}