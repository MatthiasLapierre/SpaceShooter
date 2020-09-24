package com.matthiaslapierre.spaceshooter.resources.impl

import android.content.Context
import android.content.SharedPreferences
import com.matthiaslapierre.spaceshooter.resources.Scores


/**
 * Stores scores locally by using the Preferences API.
 */
class ScoresImpl: Scores {

    companion object {
        private const val PREF_DEFAULT = "com.matthiaslapierre.spaceshooter.PREF_DEFAULT"
        private const val HIGH_SCORE = "high_score"
    }

    override fun highScore(context: Context): Int {
        val p: SharedPreferences = context.getSharedPreferences(
            PREF_DEFAULT,
            Context.MODE_PRIVATE
        )
        return p.getInt(HIGH_SCORE, 0)
    }

    override fun isNewBestScore(context: Context, score: Int): Boolean = score > highScore(context)

    override fun storeHighScore(context: Context, score: Int) {
        val p: SharedPreferences = context.getSharedPreferences(
            PREF_DEFAULT,
            Context.MODE_PRIVATE
        )
        p.edit().putInt(HIGH_SCORE, score).apply()
    }

}