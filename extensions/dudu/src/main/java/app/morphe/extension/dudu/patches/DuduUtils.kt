package app.morphe.extension.dudu.patches

import android.util.Log
import java.lang.reflect.Modifier

object DuduUtils {
    fun executeShellCommand(command: String) {
        try {
            val shellManageClass = Class.forName("com.dudu.autoui.manage.shellManage.k")
            val method = shellManageClass.declaredMethods.firstOrNull { m ->
                Modifier.isPublic(m.modifiers) &&
                        Modifier.isStatic(m.modifiers) &&
                        m.returnType == Boolean::class.javaPrimitiveType &&
                        m.parameterTypes.size == 1 &&
                        m.parameterTypes[0] == String::class.java
            }
            if (method != null) {
                method.invoke(null, command)
            } else {
                Log.e("DuduUtils", "executeShellCommand: method not found")
            }
        }catch (e: Throwable) {
            e.printStackTrace()
        }
    }
}