package com.vasthread.webviewtv.widget

import android.content.Context
import android.graphics.BitmapFactory
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.bumptech.glide.Glide
import com.vasthread.webviewtv.R
import com.vasthread.webviewtv.playlist.Channel
import com.vasthread.webviewtv.playlist.EPGManager
import java.util.Date
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis

class ChannelBarView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    companion object {
        private const val DISMISS_DELAY = 4500L// 3000L
        private const val UPDATE_PERIOD = 1000L
    }

    private val tvChannelName: TextView
    private val tvChannelUrl: TextView
    private val tvProgress: TextView
    private val tvChannelNumber: TextView
    private val tvProgram: TextView
    private val tvChannelIcon: ImageView
    private val playProgress: ProgressBar
    private lateinit var updateChannelBarAction: Runnable
    private var currentChannel: Channel? = null

    private var epgInfo: EPGManager? = null

    private val dismissAction = Runnable { visibility = GONE }

    init {
        isClickable = true
        isFocusable = false
        setBackgroundResource(R.drawable.bg)
        LayoutInflater.from(context).inflate(R.layout.widget_channel_bar, this)
        tvChannelName = findViewById(R.id.tvChannelName)
        tvChannelUrl = findViewById(R.id.tvChannelUrl)
        tvProgress = findViewById(R.id.tvProgress)
        tvChannelNumber = findViewById(R.id.tvChannelNumber)
        tvChannelIcon = findViewById(R.id.channelIcon)
        tvProgram = findViewById(R.id.tvProgram)
        playProgress = findViewById(R.id.play_progress)

        visibility = GONE

        epgInfo = EPGManager()
        epgInfo!!.loadEPGInfo()

        updateChannelBarAction = Runnable {
            val time = measureTimeMillis {
                updateTvProgram()
            }
            postDelayed(updateChannelBarAction, UPDATE_PERIOD - time)
        }
    }

    fun setCurrentChannelAndShow(channel: Channel) {
        currentChannel = channel
        removeCallbacks(dismissAction)
        tvChannelName.text = channel.name
        tvChannelUrl.text = channel.url
        setIcon(channel.icon)
        setChannelNumber(channel.index)
        var tag = channel.tag
        if (tag.isEmpty()){
            tag = channel.name
        }
        setProgram(tag)
        post(updateChannelBarAction)
        setProgress(0)

        visibility = VISIBLE
    }

    fun dismiss() {
        removeCallbacks(dismissAction)
        removeCallbacks(updateChannelBarAction)
        visibility = GONE
    }

    private fun updateTvProgram(){
        var tag = currentChannel?.tag
        if (tag != null) {
            if (tag.isEmpty()){
                tag = currentChannel?.name
            }
        }
        setProgram(tag!!)
    }

    fun setProgress(progress: Int) {
        removeCallbacks(dismissAction)
        tvProgress.text = "$progress%"
        if (progress == 100) {
            postDelayed(dismissAction, DISMISS_DELAY)
        }
    }

    private fun setIcon(addr: String) {
        removeCallbacks(dismissAction)
        if (addr.isNotEmpty()) {
            Glide.with(this)
                .load(addr)
                .into(tvChannelIcon)
        } else {
            tvChannelIcon.setImageDrawable(null)
            tvChannelIcon.setBackgroundResource(0)
            // setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.no_pic))
        }
    }

    private fun setChannelNumber(channelNumber: Int) {
        removeCallbacks(dismissAction)
        tvChannelNumber.text = channelNumber.toString()
    }

    private fun calculateTimeDifference(startDate: Date, endDate: Date): String {
        // 计算时间差（毫秒）
        val durationInMillis = endDate.time - startDate.time

        // 转换为分钟和秒
        val minutes = TimeUnit.MILLISECONDS.toMinutes(durationInMillis)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(durationInMillis) -
                TimeUnit.MINUTES.toSeconds(minutes)

        // 格式化为MM:SS
        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun setProgram(channel: String) {
        removeCallbacks(dismissAction)
        val info = epgInfo!!.getCurrentProgramWithProgress(channel)

        Log.i("LOGS", "$info")
        val start = info?.first?.startTime
        val end = info?.first?.endTime
        val program = info?.first?.title
        var programStr = "";
        if (program != null) {
            if (program.isEmpty()) {
                programStr = "无信息（ 00:00 / 00:00 ）"
            } else {
                val now = Date()
                programStr = program + "（ " + calculateTimeDifference(start!!, now) + " / " + calculateTimeDifference(start, end!!) + " ）"
            }
        }
        tvProgram.text = programStr
        if (info != null) {
            playProgress.progress = (info.second * 100).toInt()
        }
    }
}