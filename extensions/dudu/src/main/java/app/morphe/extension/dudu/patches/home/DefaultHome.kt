package app.morphe.extension.dudu.patches.home

import app.morphe.extension.dudu.patches.DuduUtils

object DefaultHome {
    @JvmStatic
    fun setDefaultHome() {
        Thread {
            DuduUtils.executeShellCommand("cmd package set-home-activity com.dudu.autoui/.ui.activity.launcher.LauncherActivity")
        }.start()
    }
}