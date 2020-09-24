package com.matthiaslapierre.spaceshooter

import android.content.Context
import com.matthiaslapierre.spaceshooter.resources.Drawables
import com.matthiaslapierre.spaceshooter.resources.Scores
import com.matthiaslapierre.spaceshooter.resources.SoundEngine
import com.matthiaslapierre.spaceshooter.resources.TypefaceHelper
import com.matthiaslapierre.spaceshooter.resources.impl.DrawablesImpl
import com.matthiaslapierre.spaceshooter.resources.impl.ScoresImpl
import com.matthiaslapierre.spaceshooter.resources.impl.SoundEngineImpl
import com.matthiaslapierre.spaceshooter.resources.impl.TypefaceHelperImpl

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
    val drawables: Drawables = DrawablesImpl(context)
    val typefaceHelper: TypefaceHelper = TypefaceHelperImpl(context.assets)
    val soundEngine: SoundEngine = SoundEngineImpl(context.assets)
    val scores: Scores = ScoresImpl()
}