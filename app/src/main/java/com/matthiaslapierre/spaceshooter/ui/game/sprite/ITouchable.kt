package com.matthiaslapierre.spaceshooter.ui.game.sprite

import android.view.MotionEvent

interface ITouchable {
    /**
     * Handles touch event.
     */
    fun onTouch(event: MotionEvent)
}