package com.example.ui

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.math.sin

object SoundEffects {
    private const val SAMPLE_RATE = 22050

    fun playTick() {
        try {
            // Generate a very short, crisp organic click tap sound: 20ms duration
            val durationMs = 20
            val numSamples = (SAMPLE_RATE * durationMs / 1000)
            val buffer = ShortArray(numSamples)
            
            for (i in 0 until numSamples) {
                val t = i.toDouble() / SAMPLE_RATE
                // Rapidly decaying snappy click envelope
                val envelope = Math.exp(-t * 220.0) 
                // Crisp 1100 Hz pitch 
                val angle = 2.0 * Math.PI * 1100.0 * t
                val sample = (sin(angle) * Short.MAX_VALUE * 0.25 * envelope).toInt()
                buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            playAudio(buffer)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playStart() {
        try {
            // A quick, ascending, springy synth pop
            val durationMs = 120
            val numSamples = (SAMPLE_RATE * durationMs / 1000)
            val buffer = ShortArray(numSamples)
            
            for (i in 0 until numSamples) {
                val t = i.toDouble() / SAMPLE_RATE
                // Decay envelope
                val envelope = Math.exp(-t * 15.0)
                // Rise pitch gracefully from 550Hz to 1100Hz
                val freq = 550.0 + (550.0 * (t / (durationMs / 1000.0)))
                val angle = 2.0 * Math.PI * freq * t
                val sample = (sin(angle) * Short.MAX_VALUE * 0.25 * envelope).toInt()
                buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            playAudio(buffer)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playSuccess() {
        try {
            // A sparkling chime chord (overlaid frequencies with lush decay)
            val durationMs = 400
            val numSamples = (SAMPLE_RATE * durationMs / 1000)
            val buffer = ShortArray(numSamples)
            
            for (i in 0 until numSamples) {
                val t = i.toDouble() / SAMPLE_RATE
                // Smooth attack and beautiful ringing tail
                val envelope = if (t < 0.015) {
                    t / 0.015
                } else {
                    Math.exp(-(t - 0.015) * 6.5)
                }
                
                // C Major 7/9 lush synth bell triad frequencies
                val s1 = sin(2.0 * Math.PI * 1046.50 * t) // C6
                val s2 = sin(2.0 * Math.PI * 1318.51 * t) // E6
                val s3 = sin(2.0 * Math.PI * 1567.98 * t) // G6
                val s4 = sin(2.0 * Math.PI * 1975.53 * t) // B6
                val s5 = sin(2.0 * Math.PI * 2349.32 * t) // D7
                
                val combined = (s1 * 0.22 + s2 * 0.18 + s3 * 0.15 + s4 * 0.12 + s5 * 0.10) * envelope
                val sample = (combined * Short.MAX_VALUE * 0.5).toInt()
                buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            playAudio(buffer)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playPop() {
        try {
            // Generate a bouncy organic bubble pop sound: 50ms duration
            val durationMs = 50
            val numSamples = (SAMPLE_RATE * durationMs / 1000)
            val buffer = ShortArray(numSamples)
            
            for (i in 0 until numSamples) {
                val t = i.toDouble() / SAMPLE_RATE
                // Crisp fast bubble decay envelope
                val envelope = Math.exp(-t * 90.0)
                // Pitch sweeps up from 300Hz to 800Hz for that bounce element
                val freq = 300.0 + (500.0 * (t / (durationMs / 1000.0)))
                val angle = 2.0 * Math.PI * freq * t
                val sample = (sin(angle) * Short.MAX_VALUE * 0.3 * envelope).toInt()
                buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            playAudio(buffer)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playRemove() {
        try {
            // A quick descending negative-action clean sound: 120ms duration
            val durationMs = 120
            val numSamples = (SAMPLE_RATE * durationMs / 1000)
            val buffer = ShortArray(numSamples)
            
            for (i in 0 until numSamples) {
                val t = i.toDouble() / SAMPLE_RATE
                val envelope = Math.exp(-t * 25.0)
                // Descending frequency sweep
                val freq = 800.0 - (500.0 * (t / (durationMs / 1000.0)))
                val angle = 2.0 * Math.PI * freq * t
                val sample = (sin(angle) * Short.MAX_VALUE * 0.25 * envelope).toInt()
                buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            playAudio(buffer)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playClick() {
        try {
            // A short, tight woodblock click or key-switch tap: 15ms duration
            val durationMs = 15
            val numSamples = (SAMPLE_RATE * durationMs / 1000)
            val buffer = ShortArray(numSamples)
            
            for (i in 0 until numSamples) {
                val t = i.toDouble() / SAMPLE_RATE
                // Extremely rapid snappy decay
                val envelope = Math.exp(-t * 300.0)
                val freq = 650.0
                val angle = 2.0 * Math.PI * freq * t
                val sample = (sin(angle) * Short.MAX_VALUE * 0.35 * envelope).toInt()
                buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            playAudio(buffer)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playSave() {
        try {
            // A bright and premium double-tone ascending bell sound: 250ms duration
            val durationMs = 250
            val numSamples = (SAMPLE_RATE * durationMs / 1000)
            val buffer = ShortArray(numSamples)
            
            for (i in 0 until numSamples) {
                val t = i.toDouble() / SAMPLE_RATE
                val sampleValue = if (t < 0.1) {
                    val envelope = Math.exp(-t * 30.0)
                    sin(2.0 * Math.PI * 659.25 * t) * envelope * 0.25
                } else {
                    val t2 = t - 0.1
                    val envelope = Math.exp(-t2 * 15.0)
                    sin(2.0 * Math.PI * 880.0 * t2) * envelope * 0.25
                }
                val sample = (sampleValue * Short.MAX_VALUE).toInt()
                buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            playAudio(buffer)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playClear() {
        try {
            // A low sweep representing erasing or swiping clean: 150ms duration
            val durationMs = 150
            val numSamples = (SAMPLE_RATE * durationMs / 1000)
            val buffer = ShortArray(numSamples)
            
            for (i in 0 until numSamples) {
                val t = i.toDouble() / SAMPLE_RATE
                val envelope = Math.exp(-t * 18.0)
                val freq = 450.0 - (300.0 * (t / (durationMs / 1000.0)))
                val angle = 2.0 * Math.PI * freq * t
                val sample = (sin(angle) * Short.MAX_VALUE * 0.25 * envelope).toInt()
                buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            playAudio(buffer)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun playAudio(buffer: ShortArray) {
        Thread {
            try {
                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(buffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(buffer, 0, buffer.size)
                audioTrack.play()
                
                // Hold-release static track after play duration
                val playDurationMs = (buffer.size * 1000L / SAMPLE_RATE) + 120L
                Thread.sleep(playDurationMs)
                
                audioTrack.stop()
                audioTrack.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }
}
