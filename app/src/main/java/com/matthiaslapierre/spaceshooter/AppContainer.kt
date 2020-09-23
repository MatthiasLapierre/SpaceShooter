package com.matthiaslapierre.spaceshooter

import android.content.Context
import com.matthiaslapierre.spaceshooter.resources.Drawables
import com.matthiaslapierre.spaceshooter.resources.TypefaceHelper
import com.matthiaslapierre.spaceshooter.resources.Scores
import com.matthiaslapierre.spaceshooter.resources.SoundEngine

/**
 * To solve the issue of reusing objects, you can create your own dependencies container class
 * that you use to get dependencies. All instances provided by this container can be public.
 * Because these dependencies are used across the whole application, they need to be placed in
 * a common place all activities can use: the application class.
 * @see https://developer.android.com/training/dependency-injection/manual
 */
class AppContainer(
    context: Context
) {
    val drawables = Drawables(context)
    val typefaceHelper = TypefaceHelper(context.assets)
    val soundEngine = SoundEngine(context.assets)
    val scores = Scores()
}