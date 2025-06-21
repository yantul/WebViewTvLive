package com.vasthread.webviewtv.misc

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.TrafficStats
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat.startActivity
import java.util.*


private val mainLooper = Looper.getMainLooper()

fun adjustValue(value: Int, size: Int, next: Boolean): Int {
    return if (next) {
        if (value + 1 >= size) 0 else value + 1
    } else {
        if (value - 1 < 0) size - 1 else value - 1
    }
}

fun isMainThread() = Looper.myLooper() == mainLooper

private val myUid = application.applicationInfo.uid

fun getTrafficBytes(): Long {
    return TrafficStats.getUidRxBytes(myUid) + TrafficStats.getUidTxBytes(myUid)
}

fun checkDrawOverlayPermission(context: Context): Boolean {
    if (!(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context))) {
        return true
    }
    return false
}

@SuppressLint("ObsoleteSdkInt")
fun requestDrawOverlays(context: Context) {
    if (!checkDrawOverlayPermission(context)) {
        var intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))
        startActivity(context, intent, null)
    }
}

fun checkAndRequestSettingPermission(activity: Activity, context: Context): Boolean {
    return false
}

/**
 * 获取自启动管理页面的Intent
 * @param context context
 * @return 返回自启动管理页面的Intent
 */
fun getAutostartSettingIntent(context: Context) {
    var componentName: ComponentName? = null
    val brand = Build.MANUFACTURER
    val intent = Intent()
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    when (brand.lowercase(Locale.getDefault())) {
        "samsung" -> componentName = ComponentName(
            "com.samsung.android.sm",
            "com.samsung.android.sm.app.dashboard.SmartManagerDashBoardActivity"
        )

        "huawei" ->             //荣耀V8，EMUI 8.0.0，Android 8.0上，以下两者效果一样
            componentName = ComponentName(
                "com.huawei.systemmanager",
                "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity"
            )

        "xiaomi" -> componentName =
            ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")

        "vivo" -> //            componentName = new ComponentName("com.iqoo.secure", "com.iqoo.secure.safaguard.PurviewTabActivity");
            componentName = ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity")

        "oppo" -> //            componentName = new ComponentName("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity");
            componentName =
                ComponentName("com.coloros.oppoguardelf", "com.coloros.powermanager.fuelgaue.PowerUsageModelActivity")

        "yulong", "360" -> componentName = ComponentName(
            "com.yulong.android.coolsafe",
            "com.yulong.android.coolsafe.ui.activity.autorun.AutoRunListActivity"
        )

        "meizu" -> componentName = ComponentName("com.meizu.safe", "com.meizu.safe.permission.SmartBGActivity")
        "oneplus" -> componentName =
            ComponentName("com.oneplus.security", "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity")

        "letv" -> {
            intent.setAction("com.letv.android.permissionautoboot")
            intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS")
            intent.setData(Uri.fromParts("package", context.packageName, null))
        }

        else -> {
            intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS")
            intent.setData(Uri.fromParts("package", context.packageName, null))
        }
    }
    intent.setComponent(componentName)

    startActivity(context, intent, null)
}

@Suppress("DEPRECATION")
fun checkAutoStartPermission(context: Context): Boolean {

    return true
}