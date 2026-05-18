package com.example.mototrack2java.data.sound

import android.content.Context
import android.media.MediaPlayer
import com.example.mototrack2java.R
import com.example.mototrack2java.domain.service.SoundPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AndroidSoundPlayer @Inject constructor(
    @ApplicationContext private val context: Context
) : SoundPlayer {

    override fun playMotoDetected() {
        MediaPlayer.create(context, R.raw.start)?.apply {
            setOnCompletionListener { player -> player.release() }
            start()
        }
    }
}
