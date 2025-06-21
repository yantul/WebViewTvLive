package com.vasthread.webviewtv.activity

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.os.Build
import android.app.job.JobInfo
import android.app.job.JobScheduler
import androidx.annotation.RequiresApi
import android.content.ComponentName
import android.content.Intent
import android.util.Log
import com.vasthread.webviewtv.settings.SettingsManager

class BootReceiver : BroadcastReceiver() {
    @SuppressLint("ObsoleteSdkInt")
    override fun onReceive(context: Context, intent: Intent) {
        Log.e("BootReceiver", "BootReceiver onReceive" + intent.action.toString())
        // if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // 开机后执行的逻辑
            if (!SettingsManager.isAutoStartOnBoot())
            {
                return
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // scheduleJob(context)
                    startActivity(context)
                }
                else
                {
                    startActivity(context)
                }
            } catch (e: Exception) {
                Log.w("BootReceiver", "Cannot start activity main, reason: ${e.message}")
            }
        //}
    }

    private fun startActivity(context: Context) {
        val launchIntent = Intent(context, MainActivity::class.java)
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        launchIntent.setAction("android.intent.action.MAIN")
        launchIntent.addCategory("android.intent.category.LAUNCHER")

        context.startActivity(launchIntent)
        Log.e("BootReceiver", "Launch intent is $launchIntent")
    }

    @SuppressLint("ObsoleteSdkInt")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun scheduleJob(context: Context) {
        val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        val jobInfo = JobInfo.Builder(1, ComponentName(context, MainActivity::class.java))
            .setOverrideDeadline(0) // 立即执行
            .build()
        jobScheduler.schedule(jobInfo)
    }
}