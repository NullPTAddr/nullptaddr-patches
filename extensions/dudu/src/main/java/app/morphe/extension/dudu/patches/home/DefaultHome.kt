package app.morphe.extension.dudu.patches.home

import android.content.Intent
import android.os.Handler
import android.os.Looper
import app.morphe.extension.dudu.patches.DuduUtils
import app.morphe.extension.shared.Utils

object DefaultHome {
    @JvmStatic
    fun setDefaultHome() {
        val handler = Handler(Looper.getMainLooper())
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
        }
        val componentString = "com.dudu.autoui/com.dudu.autoui.ui.activity.launcher.LauncherActivity"

        handler.post(object : Runnable{
            override fun run() {
                val pm = Utils.getContext()?.packageManager ?: run {
                    handler.postDelayed(this, 10_000L)
                    return
                }
                val resolve = intent.resolveActivity(pm)
                if (resolve.flattenToString() != componentString){
                    Thread {
                        DuduUtils.executeShellCommand("cmd package set-home-activity $componentString")
                    }.start()
                }
                handler.postDelayed(this, 10_000L)
            }
        })
    }
}