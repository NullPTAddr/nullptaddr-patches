package app.morphe.patches.dudu.stings

import app.morphe.patcher.patch.resourcePatch
import app.morphe.patches.all.misc.resources.resourceMappingPatch
import app.morphe.patches.dudu.Constans
import app.morphe.util.findElementByAttributeValue
import java.io.File


@Suppress("unused")
val stringsPatch = resourcePatch("Strings Patch") {
    dependsOn(resourceMappingPatch)
    compatibleWith(Constans.compatibility)

    val stringsToReplace = mutableMapOf(
        "recent_power_consumption" to "Consumption",

        )

    execute {
        val stringResourceFiles = mutableListOf<File>()

        get("res").walk().forEach { file ->
            if (file.isFile && file.name.equals("strings.xml", ignoreCase = true)) {
                stringResourceFiles.add(file)
            }
        }

        var foundString = false
        stringResourceFiles.forEach { filePath ->
            document(filePath.absolutePath).use { document ->
                stringsToReplace.forEach { (key, value) ->
                    var node = document.documentElement.childNodes.findElementByAttributeValue(
                        "name",
                        key
                    )

                    // String is not localized in all languages.
                    if (node != null) {
                        node.textContent = value
                        foundString = true
                    }
                }
            }
        }
    }
}