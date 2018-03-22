package com.coskun.exoplayermediasession.player

import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSourceFactory
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.DynamicConcatenatingMediaSource
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.FileDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSink
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.exoplayer2.upstream.crypto.AesCipherDataSink
import com.google.android.exoplayer2.upstream.crypto.AesCipherDataSource
import com.google.android.exoplayer2.util.Util
import okhttp3.OkHttpClient
import java.nio.charset.Charset

/**
 * Created by Coskun Yalcinkaya.
 */
class Player(private val context: Context) {


    /**
     * Creates new [SimpleExoPlayer] instance with some configuration by lazy.
     */
    val simpleExoPlayer : SimpleExoPlayer by lazy {
        val bandwidthMeter = DefaultBandwidthMeter()
        val trackSelectionFactory = AdaptiveTrackSelection.Factory(bandwidthMeter)
        val trackSelector = DefaultTrackSelector(trackSelectionFactory)
         ExoPlayerFactory.newSimpleInstance(context, trackSelector)!!
    }


    /**
     * Cache Evictor with 100 MB caching capacity.
     **/
    private val cacheEvictor = LeastRecentlyUsedCacheEvictor(100 * 1024 * 1024)


    /**
     * Simple cache for caching media content.
     */
    private val simpleCache = SimpleCache(cacheDir(), cacheEvictor)


    /**
     * A media source for dynamically add new media sources.
     */
    private val dynamicConcatenatingMediaSource =  DynamicConcatenatingMediaSource()


    /**
     * Add new media source to dynamicConcatenatingMediaSource.
     * @param [urls] Media files urls.
     * @param [prepare] Only true when first initialization. Default false.
     * @return [simpleExoPlayer] instance.
     */
    fun addMediaSource(vararg urls: String, prepare: Boolean = false) : SimpleExoPlayer {
        for (url in urls){
            dynamicConcatenatingMediaSource.addMediaSource(createMediaSource(url))
        }
        if (prepare)
            simpleExoPlayer.prepare(dynamicConcatenatingMediaSource)
        return simpleExoPlayer
    }


    /**
     * Creates media source with given url which has ability to writing cache and reading from cache.
     */
    private fun createMediaSource(url: String) =
            ExtractorMediaSource(Uri.parse(url), cacheDataSourceFactory(), DefaultExtractorsFactory(), null, null)


    /**
     * Data source factory for caching the media while listening.
     **/
    private fun cacheDataSourceFactory() = DataSource.Factory {
        val secret = getUtf8Bytes("test_test_test_1")
        val aesCipherDataSource = AesCipherDataSource(secret, FileDataSource())
        val scratch = ByteArray(3897)
        val aesCipherDataSink = AesCipherDataSink(secret, CacheDataSink(simpleCache, Long.MAX_VALUE), scratch)
        CacheDataSource(simpleCache, okHttpDataSourceFactory().createDataSource(), aesCipherDataSource, aesCipherDataSink, CacheDataSource.FLAG_BLOCK_ON_CACHE, null)
    }


    /**
     * Cache direction where downloaded media will be cached.
     **/
    private fun cacheDir() = context.externalCacheDir


    /**
     *  OkHttp data source factory for playing online media simply.
     **/
    private fun okHttpDataSourceFactory() = OkHttpDataSourceFactory(OkHttpClient(), Util.getUserAgent(context.applicationContext, context.packageName), null)


    private fun getUtf8Bytes(value: String): ByteArray {
        return value.toByteArray(Charset.forName(C.UTF8_NAME))
    }
}

