package com.matthiaslapierre.spaceshooter.resources

import android.content.res.AssetFileDescriptor
import android.content.res.AssetManager
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Build
import com.matthiaslapierre.spaceshooter.Constants
import java.io.IOException

/**
 * Handles sounds present in the game.
 */
class SoundEngine(
    private val assets: AssetManager
) {

    companion object{
        private const val SOUND_BTN_PRESS = 0
        private const val SOUND_CRASH = 1
        private const val SOUND_METEOR_EXPLODE = 2
        private const val SOUND_SHIP_EXPLODE = 3
        private const val SOUND_GET_POWER_UP = 4
        private const val SOUND_GAME_OVER = 5
        private const val SOUND_SHOT_HIT = 6

        private const val MUSIC_MENU = "musics/menu.ogg"
        private const val MUSIC_PLAY = "musics/play.ogg"
        private const val MUSIC_GAME_OVER = "musics/game_over.ogg"
    }

    private val soundPool: SoundPool
    private val sounds: Array<Int?> = arrayOfNulls(7)
    private var player: MediaPlayer? = null

    init {
        soundPool = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val audioAttributes = AudioAttributes
                .Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .build()
            SoundPool
                .Builder()
                .setMaxStreams(Constants.SOUND_MAX_STREAMS)
                .setAudioAttributes(audioAttributes)
                .build()
        } else {
            SoundPool(
                Constants.SOUND_MAX_STREAMS,
                AudioManager.STREAM_MUSIC,
                0
            )
        }
    }

    /**
     * Loads sound effects.
     */
    fun load() {
        Thread(Runnable {
            sounds[SOUND_BTN_PRESS] = soundPool.load(assets.openFd("sounds/sfx_btn_press1.ogg"), 1)
            sounds[SOUND_CRASH] = soundPool.load(assets.openFd("sounds/sfx_crash.ogg"), 1)
            sounds[SOUND_METEOR_EXPLODE] = soundPool.load(assets.openFd("sounds/sfx_explode_meteor.ogg"), 1)
            sounds[SOUND_SHIP_EXPLODE] = soundPool.load(assets.openFd("sounds/sfx_explode_ship.ogg"), 1)
            sounds[SOUND_GET_POWER_UP] = soundPool.load(assets.openFd("sounds/sfx_handle_coins1.ogg"), 1)
            sounds[SOUND_GAME_OVER] = soundPool.load(assets.openFd("sounds/sfx_lose.ogg"), 1)
            sounds[SOUND_SHOT_HIT] = soundPool.load(assets.openFd("sounds/sfx_shot_hit.ogg"), 1)
        }).start()
    }

    /**
     * Unloads sound effects.
     */
    fun release() {
        sounds.filterNotNull().forEach { soundID ->
            soundPool.unload(soundID)
        }
        stopMusic()
    }

    /**
     * Resumes the music.
     */
    fun resume() {
        player?.start()
    }

    /**
     * Pauses the music.
     */
    fun pause() {
        player?.pause()
    }

    fun playBtnPress() = playSound(SOUND_BTN_PRESS)
    fun playCrash() = playSound(SOUND_CRASH)
    fun playMeteorExplode() = playSound(SOUND_METEOR_EXPLODE)
    fun playShipExplode() = playSound(SOUND_SHIP_EXPLODE)
    fun playGetPowerUp() = playSound(SOUND_GET_POWER_UP)
    fun playGameOver() = playSound(SOUND_GAME_OVER)
    fun playShotHit() = playSound(SOUND_SHOT_HIT)

    fun playMenuMusic() = playMusic(MUSIC_MENU)

    fun playPlayMusic() = playMusic(MUSIC_PLAY)

    fun playGameOverMusic() = playMusic(MUSIC_GAME_OVER)

    fun stopMusic() {
        if(player != null) {
            player?.release()
            player = null
        }
    }

    private fun playSound(index: Int, volume: Float = 0.5f, infiniteLoop: Boolean = false) {
        sounds[index]?.let { soundId ->
            val loop = if(infiniteLoop) -1 else 0
            soundPool.play(soundId, volume, volume, 1, loop, 1f)
        }
    }

    private fun playMusic(filename: String) {
        stopMusic()
        try {
            val afd: AssetFileDescriptor = assets.openFd(filename)
            player = MediaPlayer().apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .build()
                    )
                }
                isLooping = true
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                setVolume(0.3f, 0.3f)
                prepare()
                start()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

}