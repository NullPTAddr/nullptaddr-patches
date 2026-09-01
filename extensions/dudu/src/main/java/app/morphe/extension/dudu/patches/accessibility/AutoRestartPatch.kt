package app.morphe.extension.dudu.patches.accessibility

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings
import app.morphe.extension.dudu.patches.DuduUtils
import app.morphe.extension.shared.Utils

class AutoRestartPatch {
    companion object {
        private var retryingCount = 0
        private fun getContext(): Context = Utils.getContext()

        private fun isServiceStarted(): Boolean {
            val context = getContext()
            val activityService = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val runningServices = activityService.getRunningServices(Int.MAX_VALUE)
            for (runningService in runningServices) {
                if (runningService.service.className == "com.dudu.autoui.service.DuduAccessibilityService") return true
            }

            return false
        }

        @JvmStatic
        fun stopService(): Boolean {
            val context = getContext()
            if (context.checkSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS) != PackageManager.PERMISSION_GRANTED) return false

            val cr = context.contentResolver
            val fullService = "com.dudu.autoui/com.dudu.autoui.service.DuduAccessibilityService"
            val compactService = "com.dudu.autoui/.service.DuduAccessibilityService"

            val enabled = Settings.Secure.getString(
                cr,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: ""

            val services = enabled.split(":").toMutableSet()

            services.remove(fullService)
            services.remove(compactService)

            // 1️⃣ Disable accessibility
            Settings.Secure.putString(
                cr,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
                services.joinToString(":")
            )

            return true
        }

        private fun startService(): Boolean {
            val context = getContext()
            if (context.checkSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS) != PackageManager.PERMISSION_GRANTED) return false

            val cr = context.contentResolver
            val fullService = "com.dudu.autoui/com.dudu.autoui.service.DuduAccessibilityService"

            val enabled = Settings.Secure.getString(
                cr,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: ""

            val services = enabled.split(":").toMutableSet()

            services.add(fullService)
            Settings.Secure.putString(
                cr,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
                services.joinToString(":")
            )
            return true
        }

        @JvmStatic
        fun restartService(): Boolean {
            if (isServiceStarted()) return true
            val context = getContext()
            val isWriteSecureSettingGranted =
                context.checkSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS) == PackageManager.PERMISSION_GRANTED
            if (isWriteSecureSettingGranted && retryingCount > 1) {
                DuduUtils.executeShellCommand("am force-stop com.dudu.autoui")
                return true
            }
            if (stopService()) {
                Thread.sleep(300)
                startService()
                if (isWriteSecureSettingGranted) {
                    retryingCount++
                }
                return true
            }
            return false
        }
    }
}