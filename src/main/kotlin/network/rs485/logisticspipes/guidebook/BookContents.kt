/*
 * Copyright (c) 2020  RS485
 *
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0.1, or MMPL. Please check the contents of the license located in
 * https://github.com/RS485/LogisticsPipes/blob/dev/LICENSE.md
 *
 * This file can instead be distributed under the license terms of the
 * MIT license:
 *
 * Copyright (c) 2020  RS485
 *
 * This MIT license was reworded to only match this file. If you use the regular
 * MIT license in your project, replace this copyright notice (this line and any
 * lines below and NOT the copyright line above) with the lines from the original
 * MIT license located here: http://opensource.org/licenses/MIT
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this file and associated documentation files (the "Source Code"), to deal in
 * the Source Code without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Source Code, and to permit persons to whom the Source Code is furnished
 * to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Source Code, which also can be
 * distributed under the MIT.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package network.rs485.logisticspipes.guidebook

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlException
import kotlinx.serialization.Serializable
import logisticspipes.LPConstants
import logisticspipes.LogisticsPipes
import net.minecraft.client.Minecraft
import net.minecraft.util.ResourceLocation
import java.io.*
import java.nio.file.Paths


val MISSING_META = YamlPageMetadata("[404] the metadata was not found :P", icon = "logisticspipes:unrouted_pipe")

object BookContents {
    val pageMetadataMap = hashMapOf<String, YamlPageMetadata>()

    fun get(markdownFile: String): LoadedPage {
        // TODO add hashmap cache
        val pageString = getFileAsString(markdownFile, Minecraft.getMinecraft().languageManager.currentLanguage.languageCode)
        return LoadedPage(pageString, markdownFile)
    }
}

private val metadataRegex = "^\\s*<!---\\s*\\n(.*?)\\n\\s*--->\\s*(.*)$".toRegex(kotlin.text.RegexOption.DOT_MATCHES_ALL)

fun getFileAsString(path: String, lang: String): String {
    return try {
        val bookFile = Minecraft.getMinecraft().resourceManager.getResource(ResourceLocation(LPConstants.LP_MOD_ID, "book/$lang/$path"))
        bookFile.inputStream.bufferedReader().readLines().joinToString("\n")
    } catch (e: IOException) {
        if (lang != "en_us") {
            // Didn't find current file, checking for english version.
            if (LogisticsPipes.isDEBUG()) LogisticsPipes.log.error("Language $lang for the current file (book_2/$lang$path) was not found. Defaulting to en_us.")
            getFileAsString(path, "en_us")
        } else {
            // English not found, this may be normal. Maybe the previous language file pointed to a non-existent file.
            val errorMsg = "The requested file (book_2/$lang$path) was not found"
            if (LogisticsPipes.isDEBUG()) LogisticsPipes.log.error("$errorMsg. Make sure it exists or if the path is correct.")
            throw FileNotFoundException(errorMsg)
        }
    }
}

fun String.parseMetadata(markdownFile: String): YamlPageMetadata = if (this.isNotEmpty()) {
    // Takes the metadata string and parses the YAML information
    try {
        Yaml.default.parse(YamlPageMetadata.serializer(), this).normalizeMetadata(markdownFile)
    } catch (e: YamlException) {
        LogisticsPipes.log.error("Exception: $e")
        LogisticsPipes.log.error("The following Yaml is malformed! \n$this")
        MISSING_META
    }
} else MISSING_META

fun YamlPageMetadata.normalizeMetadata(markdownFile: String): YamlPageMetadata {
    // Normalize the given paths, replacing any ./.. with the appropriate absolute (resource location) paths
    val menu = this.menu.mapValues { entry ->
        entry.value.flatMap { _ ->
            entry.value.map {
                Paths.get(File(markdownFile).parent ?: "", it).normalize().parent.toString()
            }
        }
    }
    return YamlPageMetadata(this.title, this.icon, menu, this.menuAsList)
}

@Serializable
data class YamlPageMetadata(val title: String,
                            val icon: String = "logisticspipes:itemcard",
                            val menu: Map<String, List<String>> = mapOf(),
                            val menuAsList: Boolean = false)

class LoadedPage(unformattedText: String, val fileLocation: String) {
    val metadataString: String
    val markdownString: String
    val metadata: YamlPageMetadata

    init {
        // Splits the input string into the metadata and the markdown parts of the page.
        val result = metadataRegex.matchEntire(unformattedText) ?: throw RuntimeException("Could not load page, regex failed")
        metadataString = result.groups[1]?.value?.trim() ?: ""
        markdownString = result.groups[2]?.value?.trim() ?: ""
        metadata = metadataString.parseMetadata(fileLocation)
    }
}