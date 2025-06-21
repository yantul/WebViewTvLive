package com.vasthread.webviewtv.playlist

import com.google.gson.annotations.SerializedName
import com.vasthread.webviewtv.settings.SettingsManager

data class Channel @JvmOverloads constructor(
    var name: String = "",
    @SerializedName("icon")
    var icon: String = "",
    @SerializedName("tag")
    var tag: String = "",
    @SerializedName("group")
    var groupName: String = "",
    var urls: List<String> = emptyList(),
) {

    val url: String
        get() {
            var index = SettingsManager.getChannelLastSourceIndex(name)
            if (index >= urls.size || index < 0) index = 0
            return urls[index]
        }

    var index: Int = 0
        set(value) {
            field = value
        }

    override fun toString(): String {
        return "name=$name, groupName=$groupName, urls=$urls, icon=$icon, tag=$tag"
    }
}