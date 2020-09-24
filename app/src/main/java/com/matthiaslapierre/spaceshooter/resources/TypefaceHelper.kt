package com.matthiaslapierre.spaceshooter.resources

import android.graphics.Typeface

/**
 * Caches typefaces.
 */
interface TypefaceHelper {

    /**
     * Loads typefaces.
     */
    fun load()

    /**
     * Gets the main typeface.
     */
    fun getFutureTypeface(): Typeface

    /**
     * Gets the thin variant.
     */
    fun getFutureThinTypeface(): Typeface

}