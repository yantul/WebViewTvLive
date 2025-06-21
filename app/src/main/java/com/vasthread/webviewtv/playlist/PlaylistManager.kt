package com.vasthread.webviewtv.playlist

import android.util.Log
import android.widget.Toast
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.vasthread.webviewtv.activity.MainActivity
import com.vasthread.webviewtv.misc.application
import com.vasthread.webviewtv.misc.preference
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit

object PlaylistManager {

    private const val TAG = "PlaylistManager"
    private const val CACHE_EXPIRATION_MS = 24 * 60 * 60 * 1000L
    private const val KEY_PLAYLIST_URL = "playlist_url"
    private const val KEY_LAST_UPDATE = "last_update"
    private const val UPDATE_RETRY_DELAY = 10 * 1000L

    private val client = OkHttpClient.Builder().connectTimeout(5, TimeUnit.SECONDS).readTimeout(5, TimeUnit.SECONDS).build()
    private val gson = GsonBuilder().setPrettyPrinting().create()!!
    private val jsonTypeToken = object : TypeToken<List<Channel>>() {}
    private val playlistFile = File(application.filesDir, "playlist.json")
    private val builtInPlaylists = mutableListOf(
        "XXX PlayList" to "https://files.cnblogs.com/files/yourname/tv.json",
    )

    private const val channelJson = """
[
  {
    "group": "央视频道",
    "name": "CCTV-1 综合",
    "tag": "CCTV1",
    "icon": "https://livecdn.zbds.top/logo/CCTV1.png",
    "urls": [
      "https://tv.cctv.com/live/cctv1/"
    ]
  },
  {
    "group": "央视频道",
    "name": "CCTV-2 财经",
    "tag": "CCTV2",
    "icon": "https://livecdn.zbds.top/logo/CCTV2.png",
    "urls": [
      "https://tv.cctv.com/live/cctv2/"
    ]
  },
   {
    "group": "央视频道",
    "name": "CCTV-3 综艺",
    "tag": "CCTV3",
    "icon": "https://livecdn.zbds.top/logo/CCTV3.png",
    "urls": [
      "https://tv.cctv.com/live/cctv3/"
    ]
  },
   {
    "group": "央视频道",
    "name": "CCTV-4 中文国际（亚）",
    "tag": "CCTV4",
    "icon": "https://livecdn.zbds.top/logo/CCTV4.png",
    "urls": [
      "https://tv.cctv.com/live/cctv4/"
    ]
  },
     {
    "group": "央视频道",
    "name": "CCTV-5 体育",
    "tag": "CCTV5",
    "icon": "https://livecdn.zbds.top/logo/CCTV5.png",
    "urls": [
      "https://tv.cctv.com/live/cctv5/"
    ]
  },
     {
    "group": "央视频道",
    "name": "CCTV-5+ 体育赛事",
    "tag": "CCTV5+",
    "icon": "https://livecdn.zbds.top/logo/CCTV5+.png",
    "urls": [
      "https://tv.cctv.com/live/cctv5plus/"
    ]
  },
     {
    "group": "央视频道",
    "name": "CCTV-6 电影",
    "tag": "CCTV6",
    "icon": "https://livecdn.zbds.top/logo/CCTV6.png",
    "urls": [
      "https://tv.cctv.com/live/cctv6/"
    ]
  },
     {
    "group": "央视频道",
    "name": "CCTV-7 国防军事",
    "tag": "CCTV7",
    "icon": "https://livecdn.zbds.top/logo/CCTV7.png",
    "urls": [
      "https://tv.cctv.com/live/cctv7/"
    ]
  },
     {
    "group": "央视频道",
    "name": "CCTV-8 电视剧",
    "tag": "CCTV8",
    "icon": "https://livecdn.zbds.top/logo/CCTV8.png",
    "urls": [
      "https://tv.cctv.com/live/cctv8/"
    ]
  },
     {
    "group": "央视频道",
    "name": "CCTV-9 记录",
    "tag": "CCTV9",
    "icon": "https://livecdn.zbds.top/logo/CCTV9.png",
    "urls": [
      "https://tv.cctv.com/live/cctvjilu/"
    ]
  },
     {
    "group": "央视频道",
    "name": "CCTV-10 科教",
    "tag": "CCTV10",
    "icon": "https://livecdn.zbds.top/logo/CCTV10.png",
    "urls": [
      "https://tv.cctv.com/live/cctv10/"
    ]
  },
     {
    "group": "央视频道",
    "name": "CCTV-11 戏曲",
    "tag": "CCTV11",
    "icon": "https://livecdn.zbds.top/logo/CCTV11.png",
    "urls": [
      "https://tv.cctv.com/live/cctv11/"
    ]
  },
     {
    "group": "央视频道",
    "name": "CCTV-12 社会与法",
    "tag": "CCTV12",
    "icon": "https://livecdn.zbds.top/logo/CCTV12.png",
    "urls": [
      "https://tv.cctv.com/live/cctv12/"
    ]
  },
  {
    "group": "央视频道",
    "name": "CCTV-13 新闻",
    "tag": "CCTV13",
    "icon": "https://livecdn.zbds.top/logo/CCTV13.png",
    "urls": [
      "https://tv.cctv.com/live/cctv13/"
    ]
  },
  {
    "group": "央视频道",
    "name": "CCTV-14 少儿",
    "tag": "CCTV14",
    "icon": "https://livecdn.zbds.top/logo/CCTV14.png",
    "urls": [
      "https://tv.cctv.com/live/cctvchild/"
    ]
  },
  {
    "group": "央视频道",
    "name": "CCTV-15 音乐",
    "tag": "CCTV15",
    "icon": "https://livecdn.zbds.top/logo/CCTV15.png",
    "urls": [
      "https://tv.cctv.com/live/cctv15/"
    ]
  },
  {
    "group": "央视频道",
    "name": "CCTV-16 奥林匹克",
    "tag": "CCTV16",
    "icon": "https://livecdn.zbds.top/logo/CCTV16.png",
    "urls": [
      "https://tv.cctv.com/live/cctv16/"
    ]
  },
  {
    "group": "央视频道",
    "name": "CCTV-17 农业农村",
    "tag": "CCTV17",
    "icon": "https://livecdn.zbds.top/logo/CCTV17.png",
    "urls": [
      "https://tv.cctv.com/live/cctv17/"
    ]
  },
  {
    "group": "央视频道",
    "name": "CCTV-4 中文国际",
    "urls": [
      "https://tv.cctv.com/live/cctveurope/index.shtml"
    ]
  },
  {
    "group": "M3U8",
    "name": "河南卫视",
    "icon": " https://livecdn.zbds.top/logo/河南卫视.png",
    "urls": [
      "http://39.164.160.249:9901/tsfile/live/0139_1.m3u8",
      "http://110.53.52.63:8888/newlive/live/hls/32/live.m3u8"
    ]
  }
]
"""

    var onPlaylistChange: ((Playlist) -> Unit)? = null
    var onUpdatePlaylistJobStateChange: ((Boolean) -> Unit)? = null
    private var updatePlaylistJob: Job? = null
    private var isUpdating = false
        set(value) {
            onUpdatePlaylistJobStateChange?.invoke(value)
        }

    fun getBuiltInPlaylists() = builtInPlaylists

    fun setPlaylistUrl(url: String) {
        preference.edit()
            .putString(KEY_PLAYLIST_URL, url)
            .putLong(KEY_LAST_UPDATE, 0)
            .apply()
        requestUpdatePlaylist()
    }

    fun getPlaylistUrl() = preference.getString(KEY_PLAYLIST_URL, builtInPlaylists[0].second)!!
//    fun getPlaylistUrl():String {
//        Log.i("+++++++", "builtInPlaylists: " + builtInPlaylists.size)
//        var url = preference.getString(KEY_PLAYLIST_URL, "")!!
//        if (url.isEmpty() && builtInPlaylists.isEmpty()) {
//            return ""
//        }
//
//        return url
//    }

    fun setLastUpdate(time: Long, requestUpdate: Boolean = false) {
        preference.edit().putLong(KEY_LAST_UPDATE, time).apply()
        if (requestUpdate) requestUpdatePlaylist()
    }

    private fun requestUpdatePlaylist() {
        val lastJobCompleted = updatePlaylistJob?.isCompleted
        if (lastJobCompleted != null && !lastJobCompleted) {
            Log.i(TAG, "A job is executing, ignore!")
            return
        }
        updatePlaylistJob = CoroutineScope(Dispatchers.IO).launch {
            var times = 0
            val needUpdate = { System.currentTimeMillis() - preference.getLong(KEY_LAST_UPDATE, 0L) > CACHE_EXPIRATION_MS }
            isUpdating = true
            while (needUpdate()) {
                ++times
                Log.i(TAG, "Updating playlist... times=${times}")
                Log.i(TAG, "builtInPlaylists size: " + builtInPlaylists.size)
                Log.i(TAG, "Updating through URL: " + getPlaylistUrl())
                try {
                    val request = Request.Builder().url(getPlaylistUrl()).get().build()
                    val response = client.newCall(request).execute()
                    if (!response.isSuccessful) throw Exception("Response code ${response.code}")

                    val remote = response.body!!.string()
                    val local = runCatching { playlistFile.readText() }.getOrNull()
                    if (remote != local) {
                        Log.i(TAG,"Remote not equl local")
                        playlistFile.writeText(remote)
                        onPlaylistChange?.invoke(createPlaylistFromJson(remote))
                    }

                    setLastUpdate(System.currentTimeMillis())
                    Log.i(TAG, "Update playlist successfully.")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(MainActivity.appContext, "Update playlist successfully", Toast.LENGTH_SHORT).show()
                    }
                    break
                } catch (e: Exception) {
                    Log.w(TAG, "Cannot update playlist, reason: ${e.message}")
                }
                if (needUpdate()) {
                    delay(UPDATE_RETRY_DELAY)
                }
            }
            isUpdating = false
        }
    }

    private fun createPlaylistFromJson(json: String): Playlist {
        val channels = gson.fromJson(json, jsonTypeToken)
        Log.i("PlaylistManager", "Create playlist from json")
        return Playlist.createFromAllChannels("default", channels)
    }

    // private fun loadBuiltInPlaylist() = createPlaylistFromJson("[]")
    private fun loadBuiltInPlaylist() = createPlaylistFromJson(channelJson)

    fun loadPlaylist(): Playlist {
        return try {
            val json = playlistFile.readText()
            // Log.w(TAG, "Response: ${json}")
            createPlaylistFromJson(json)
        } catch (e: Exception) {
            Log.w(TAG, "Cannot load playlist, reason: ${e.message}")
            setLastUpdate(0L)
            loadBuiltInPlaylist()
        } finally {
            requestUpdatePlaylist()
        }
    }

}