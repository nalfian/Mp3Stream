package com.pawangsmart.androidmp3stream

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.media.MediaPlayer
import android.os.AsyncTask
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView

import java.util.concurrent.TimeUnit

import dyanamitechetan.vusikview.VusikView

class MainActivity : AppCompatActivity(), MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnCompletionListener {

    private var btn_play_pause: ImageButton? = null
    private var seekBar: SeekBar? = null
    private var textView: TextView? = null

    private var musicView: VusikView? = null

    private var mediaPlayer: MediaPlayer? = null
    private var mediaFileLength: Int = 0
    private var realtimeLength: Int = 0
    internal val handler = Handler()

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        musicView = findViewById<View>(R.id.musicView) as VusikView



        seekBar = findViewById<View>(R.id.seekbar) as SeekBar
        seekBar!!.max = 99 // 100% (0~99)
        seekBar!!.setOnTouchListener { v, event ->
            if (mediaPlayer!!.isPlaying) {
                val seekBar = v as SeekBar
                val playPosition = mediaFileLength / 100 * seekBar.progress
                mediaPlayer!!.seekTo(playPosition)
            }
            false
        }

        textView = findViewById<View>(R.id.textTimer) as TextView

        btn_play_pause = findViewById<View>(R.id.btn_play_pause) as ImageButton
        btn_play_pause!!.setOnClickListener {
            val mDialog = ProgressDialog(this@MainActivity)


            val mp3Play = @SuppressLint("StaticFieldLeak")
            object : AsyncTask<String, String, String>() {

                override fun onPreExecute() {
                    mDialog.setMessage("Please wait")
                    mDialog.show()
                }

                override fun doInBackground(vararg params: String): String {
                    try {
                        mediaPlayer!!.setDataSource(params[0])
                        mediaPlayer!!.prepare()
                    } catch (ex: Exception) {

                    }

                    return ""
                }

                override fun onPostExecute(s: String) {
                    mediaFileLength = mediaPlayer!!.duration
                    realtimeLength = mediaFileLength
                    if (!mediaPlayer!!.isPlaying) {
                        mediaPlayer!!.start()
                        btn_play_pause!!.setImageResource(R.drawable.ic_pause)
                    } else {
                        mediaPlayer!!.pause()
                        btn_play_pause!!.setImageResource(R.drawable.ic_play)
                    }

                    updateSeekBar()
                    mDialog.dismiss()
                }
            }

            mp3Play.execute("https://firebasestorage.googleapis.com/v0/b/karangasem-9e3a5.appspot.com/o/Senar%20Senja%20Savana%20feat.%20Asteriska%20(Video%20Lirik).mp3?alt=media&token=da70acce-7f7f-44be-8e65-9bd396a8e34a") // direct link mp3 file

            musicView!!.start()
        }

        mediaPlayer = MediaPlayer()
        mediaPlayer!!.setOnBufferingUpdateListener(this)
        mediaPlayer!!.setOnCompletionListener(this)


    }

    private fun updateSeekBar() {
        seekBar!!.progress = (mediaPlayer!!.currentPosition.toFloat() / mediaFileLength * 100).toInt()
        if (mediaPlayer!!.isPlaying) {
            val updater = Runnable {
                updateSeekBar()
                realtimeLength -= 1000 // declare 1 second
                textView!!.text = String.format("%d:%d", TimeUnit.MILLISECONDS.toMinutes(realtimeLength.toLong()),
                        TimeUnit.MILLISECONDS.toSeconds(realtimeLength.toLong()) - TimeUnit.MILLISECONDS.toSeconds(TimeUnit.MILLISECONDS.toMinutes(realtimeLength.toLong())))
            }
            handler.postDelayed(updater, 1000) // 1 second
        }
    }

    override fun onBufferingUpdate(mp: MediaPlayer, percent: Int) {
        seekBar!!.secondaryProgress = percent
    }

    override fun onCompletion(mp: MediaPlayer) {
        btn_play_pause!!.setImageResource(R.drawable.ic_play)
        musicView!!.stopNotesFall()

    }
}
