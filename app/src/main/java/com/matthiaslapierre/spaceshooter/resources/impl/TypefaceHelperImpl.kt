package com.matthiaslapierre.spaceshooter.resources.impl

import android.content.res.AssetManager
import android.graphics.Typeface
import com.matthiaslapierre.spaceshooter.resources.TypefaceHelper
import java.util.*

class TypefaceHelperImpl(
    private val assets: AssetManager
): TypefaceHelper {

    companion object {
        private const val FONT_FUTURE = "kenvector_future"
        private const val FONT_FUTURE_THIN = "kenvector_future_thin"
    }

    private val cache: Hashtable<String, Typeface> = Hashtable()

    override fun load() {
        Thread(Runnable {
            get(FONT_FUTURE)
            get(FONT_FUTURE_THIN)
        }).start()
    }

    override fun getFutureTypeface(): Typeface = get(FONT_FUTURE)!!

    override fun getFutureThinTypeface(): Typeface = get(FONT_FUTURE_THIN)!!

    private fun get(name: String): Typeface? {
        synchronized(cache) {
            if (!cache.containsKey(name)) {
                val t = Typeface.createFromAsset(
                    assets,
                    String.format("fonts/%s.ttf", name)
                )
                cache[name] = t
            }
            return cache[name]
        }
    }

}