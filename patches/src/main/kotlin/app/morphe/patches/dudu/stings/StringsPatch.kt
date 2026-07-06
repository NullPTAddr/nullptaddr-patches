package app.morphe.patches.dudu.stings

import app.morphe.patcher.patch.resourcePatch
import app.morphe.patches.all.misc.resources.resourceMappingPatch
import app.morphe.patches.dudu.shared.Constants
import app.morphe.patches.dudu.signature.spoofSignaturePatch
import app.morphe.util.asSequence


@Suppress("unused")
val stringsPatch = resourcePatch(
    "Strings Patch",
    description = "Replace strings in strings.xml"
) {
    compatibleWith(Constants.compatibility)
    dependsOn(spoofSignaturePatch, resourceMappingPatch)

    val stringsToReplace = mutableMapOf(
        "Total Power Consumption" to "Total Cons.",
        "Avg Power Consumption" to "Avg Cons.",
        "Recent Power Consumption" to "Recent Cons.",
        "Current Power Usage" to "Current Power",
        "Battery Temperature" to "Battery Temp.",
    )

    execute {
        get("res")
            .walk()
            .filter { it.isFile && it.name.equals("strings.xml", ignoreCase = true) }
            .forEach { file ->
                document(file.absolutePath).use { document ->
                    document.documentElement.childNodes.asSequence().forEach { node ->
                        stringsToReplace[node.textContent]?.let { replacement ->
                            node.textContent = replacement
                        }
                    }
                }
            }
    }
}