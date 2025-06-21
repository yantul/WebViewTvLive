package com.vasthread.webviewtv.playlist

import android.util.Log
import android.widget.Toast
import com.vasthread.webviewtv.activity.MainActivity
import com.vasthread.webviewtv.misc.application
import com.vasthread.webviewtv.misc.preference
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

data class TvChannel(val id: String, val name: String): Serializable
data class TvProgram(
    val channelId: String,
    val title: String,
    val startTime: Date,
    val endTime: Date
): Serializable

class EPGManager {
    companion object {
        private const val TAG = "EPGManager"
        private const val KEY_LAST_EPG_UPDATE = "last_epg_update"
        private const val CACHE_EPG_EXPIRATION_MS = 6 * 60 * 60 * 1000L
        private const val UPDATE_EPG_RETRY_DELAY = 20 * 1000L
        private const val epgUrl = "http://epg.51zmt.top:8000/cc.xml"
    }

    private var updatePlaylistJob: Job? = null
    private var isUpdating = false
    private var channelsInfoPair: Pair<List<TvChannel>, List<TvProgram>>? = null
    private val epgDataFile = File(application.filesDir, "tv_data.dat")

    private val dateFormat = SimpleDateFormat("yyyyMMddHHmmss Z", Locale.getDefault())
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun downloadAndParseEpg(url: String): Pair<List<TvChannel>, List<TvProgram>> {
        // 下载 EPG XML 数据
        val request = Request.Builder()
            .url(url)
            .build()

        val response = okHttpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            throw Exception("Failed to download EPG: ${response.code}")
        }

        val xmlData = response.body?.string() ?: throw Exception("Empty EPG response")

        Log.i(TAG, "Download EPG info successfully")

        return parseEpg(xmlData)
    }

    private fun parseEpg(xmlData: String): Pair<List<TvChannel>, List<TvProgram>> {
        val channels = mutableListOf<TvChannel>()
        val programs = mutableListOf<TvProgram>()
        Log.i(TAG, "Parse EPG information")

        try {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = true
            val parser = factory.newPullParser()
            parser.setInput(StringReader(xmlData))

            var currentEvent: String? = null
            var currentChannelId: String? = null
            var currentProgram: TvProgram? = null

            while (parser.next() != XmlPullParser.END_DOCUMENT) {
                try {
                    when (parser.eventType) {
                        XmlPullParser.START_TAG -> {
                           // Log.i(TAG, parser.name)
                            when (parser.name) {
                                "channel" -> {
                                    currentChannelId = parser.getAttributeValue(null, "id")
                                    currentEvent = "channel"
                                }
                                "display-name" -> {
                                    if (currentEvent == "channel" && currentChannelId != null) {
                                        val name = parser.nextText()
                                        if (name.isNotBlank()) {
                                            channels.add(TvChannel(currentChannelId, name))
                                        }
                                    }
                                }
                                "programme" -> {
                                    val channelId = parser.getAttributeValue(null, "channel") ?: continue
                                    val start = parser.getAttributeValue(null, "start") ?: continue
                                    val stop = parser.getAttributeValue(null, "stop") ?: continue
                                    currentEvent = "programme"

                                    currentProgram = TvProgram(
                                        channelId = channelId,
                                        title = "", // 将在title标签中填充
                                        startTime = parseEpgTime(start),
                                        endTime = parseEpgTime(stop)
                                    )
                                    programs.add(currentProgram!!)
                                }
                                "title" -> {
                                    if (currentEvent == "programme" && currentProgram != null) {
                                        val title = parser.nextText()
                                        val index = programs.indexOf(currentProgram)
                                        if (index != -1) {
                                            programs[index] = currentProgram!!.copy(title = title)
                                        }
                                    }
                                }
                            }
                        }
                        XmlPullParser.END_TAG -> {
                            if (parser.name == "channel" || parser.name == "programme") {
                                currentEvent = null
                                if (parser.name == "programme") {
                                    currentProgram = null
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    // 记录解析单个元素时的错误，但继续处理其他元素
                    Log.e(TAG, "Error parsing XML element: ${e.message}", e)
                    continue
                }
            }
        } catch (e: Exception) {
            // 记录整体解析错误
            Log.e(TAG, "Failed to parse EPG data: ${e.message}", e)
            throw IOException("Failed to parse EPG data: ${e.message ?: "Unknown error"}")
        }
        Log.i(TAG, "Parse EPG success")

        return Pair(channels, programs)
    }

    private fun parseEpgTime(epgTime: String): Date {
        return dateFormat.parse(epgTime) ?: throw IllegalArgumentException("Invalid EPG time format: $epgTime")
    }

    fun getCurrentProgram(programs: List<TvProgram>, channelId: String): TvProgram? {
        val now = Date()
        return programs.firstOrNull { program ->
            program.channelId == channelId &&
                    program.startTime <= now &&
                    program.endTime > now
        }
    }

    fun getCurrentProgramWithProgress(channelName: String): Pair<TvProgram, Float>? {
        Log.i(TAG, "-------> $channelName")
        if (channelsInfoPair == null) {
            return null
        }

        val channel = channelsInfoPair!!.first.firstOrNull {
            it.name == channelName
        } ?: return null

        val now = Date()
        val program = channelsInfoPair!!.second.firstOrNull {
            it.channelId == channel.id  && it.startTime <= now && it.endTime > now
        } ?: return null
        Log.i(TAG, "$program")
        Log.i(TAG, "${now.time}  ${program.startTime.time}  ${program.endTime.time}")

        val totalDuration = program.endTime.time - program.startTime.time
        val elapsed = now.time - program.startTime.time
        val progress = elapsed.toFloat() / totalDuration.toFloat()

        return Pair(program, progress.coerceIn(0f, 1f))
    }

    fun getNextProgram(channelName: String) {

    }

    private fun setEPGLastUpdate(time: Long, requestUpdate: Boolean = false) {
        preference.edit().putLong(KEY_LAST_EPG_UPDATE, time).apply()
        if (requestUpdate) requestUpdateEPG()
    }

    private fun requestUpdateEPG() {
        val lastJobCompleted = updatePlaylistJob?.isCompleted
        if (lastJobCompleted != null && !lastJobCompleted) {
            Log.i(TAG, "A job is executing, ignore!")
            return
        }
        updatePlaylistJob = CoroutineScope(Dispatchers.IO).launch {
            var times = 0
            val needUpdate = { System.currentTimeMillis() - preference.getLong(KEY_LAST_EPG_UPDATE, 0L) > CACHE_EPG_EXPIRATION_MS }
            isUpdating = true
            while (needUpdate()) {
                ++times
                Log.i(TAG, "Updating epg... times=${times}")
                try {
                    channelsInfoPair = downloadAndParseEpg(epgUrl)
                    ObjectOutputStream(FileOutputStream(epgDataFile)).use { oos ->
                        oos.writeObject(channelsInfoPair!!.first)
                        oos.writeObject(channelsInfoPair!!.second)
                    }

                    setEPGLastUpdate(System.currentTimeMillis())
                    Log.i(TAG, "Update epg successfully.")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(MainActivity.appContext, "Update epg successfully", Toast.LENGTH_SHORT).show()
                    }
                    break
                } catch (e: Exception) {
                    Log.w(TAG, "Cannot update epg, reason: ${e.message}")
                }
                if (needUpdate()) {
                    delay(UPDATE_EPG_RETRY_DELAY)
                }
            }
            isUpdating = false
        }
    }

    fun loadEPGInfo() {
        try {
            ObjectInputStream(FileInputStream(epgDataFile)).use { ois ->
                val channels = ois.readObject() as List<TvChannel>
                val programs = ois.readObject() as List<TvProgram>
                channelsInfoPair = Pair(channels, programs)
            }
        } catch (e: Exception) {
            Log.w("LOGS", "Cannot load epginfo, reason: ${e.message}")
            setEPGLastUpdate(0L)
        } finally { }
        requestUpdateEPG()
    }
}