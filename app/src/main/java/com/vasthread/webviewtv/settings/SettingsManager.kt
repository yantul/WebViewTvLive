package com.vasthread.webviewtv.settings

import android.content.Intent
import android.provider.Settings
import androidx.core.content.ContextCompat.startActivity
import com.vasthread.webviewtv.activity.MainActivity
import com.vasthread.webviewtv.misc.getAutostartSettingIntent
import com.vasthread.webviewtv.misc.preference
import com.vasthread.webviewtv.misc.requestDrawOverlays
import com.vasthread.webviewtv.playlist.PlaylistManager
import java.security.AccessController.getContext
import java.util.UUID

object SettingsManager {

    private const val KEY_APP_AUTO_START_ON_BOOT = "app_auto_start_on_boot"
    private const val KEY_WEB_VIEW_TOUCHABLE = "web_view_touchable"
    private const val KEY_MAX_LOADING_TIME = "max_loading_time"
    private const val KEY_UUID = "uuid"

    fun getPlaylistNames(): Array<String> {
        val builtInPlaylists = PlaylistManager.getBuiltInPlaylists()
        val names = arrayOfNulls<String>(PlaylistManager.getBuiltInPlaylists().size)
        for (i in names.indices) {
            names[i] = builtInPlaylists[i].first
        }
        return names.requireNoNulls()
    }

    fun getSelectedPlaylistPosition(): Int {
        val playlistUrl = PlaylistManager.getPlaylistUrl()
        val builtInPlaylists = PlaylistManager.getBuiltInPlaylists()
        for (i in builtInPlaylists.indices) {
            if (builtInPlaylists[i].second == playlistUrl) {
                return i
            }
        }
        return 0
    }

    fun setSelectedPlaylistPosition(position: Int) {
        val builtInPlaylists = PlaylistManager.getBuiltInPlaylists()
        PlaylistManager.setPlaylistUrl(builtInPlaylists[position].second)
    }

    fun setWebViewTouchable(touchable: Boolean) {
        preference.edit().putBoolean(KEY_WEB_VIEW_TOUCHABLE, touchable).apply()
    }

    fun setRequestAutoStartOnBoot(autoStartOnBoot: Boolean) {

        if (!autoStartOnBoot) {
            setAutoStartOnBoot(false)
            return
        }

        requestDrawOverlays(MainActivity.appContext)
        getAutostartSettingIntent(MainActivity.appContext)
        setAutoStartOnBoot(true)
    }

    fun setAutoStartOnBoot(autoStartOnBoot: Boolean) {
        preference.edit().putBoolean(KEY_APP_AUTO_START_ON_BOOT, autoStartOnBoot).apply()
    }

    fun isAutoStartOnBoot(): Boolean {
        return preference.getBoolean(KEY_APP_AUTO_START_ON_BOOT, false)
    }

    fun isWebViewTouchable(): Boolean {
        return preference.getBoolean(KEY_WEB_VIEW_TOUCHABLE, false)
    }

    fun setMaxLoadingTime(second: Int) {
        preference.edit().putInt(KEY_MAX_LOADING_TIME, second).apply()
    }

    fun getMaxLoadingTime(): Int {
        return preference.getInt(KEY_MAX_LOADING_TIME, 15)
    }

    private fun lastSourceIndexKey(channelName: String) = "source_index[$channelName]"

    fun setChannelLastSourceIndex(channelName: String, index: Int) {
        preference.edit().putInt(lastSourceIndexKey(channelName), index).apply()
    }

    fun getChannelLastSourceIndex(channelName: String): Int {
        return preference.getInt(lastSourceIndexKey(channelName), 0)
    }

    fun getUserId(): String {
        var uuid = preference.getString(KEY_UUID, null)
        if (uuid.isNullOrBlank()) {
            uuid = UUID.randomUUID().toString()
            preference.edit().putString(KEY_UUID, uuid).apply()
        }
        return uuid
    }
}