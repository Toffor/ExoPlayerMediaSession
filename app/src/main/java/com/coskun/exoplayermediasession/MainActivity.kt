package com.coskun.exoplayermediasession

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.coskun.exoplayermediasession.player.Player

class MainActivity : AppCompatActivity() {

    private val url = "http://www.hochmuth.com/mp3/Haydn_Cello_Concerto_D-1.mp3"
    private val url2 = "http://www.hochmuth.com/mp3/Tchaikovsky_Rococo_Var_orch.mp3"
    private val url3 = "http://www.hochmuth.com/mp3/Vivaldi_Sonata_eminor_.mp3"
    private val player by lazy {
        Player(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        player.addMediaSource(url, url2, url3, prepare = true)
                .playWhenReady = true
    }

    override fun onDestroy() {
        super.onDestroy()
        player.simpleExoPlayer.release()
    }
}
