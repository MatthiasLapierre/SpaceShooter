package com.matthiaslapierre.spaceshooter.ui.game.sprite

import android.view.MotionEvent

/**
 * A Touchable object.
 */
interface ITouchable {
    /**
     * Handles touch event.
     */
    fun onTouch(event: MotionEvent)
}