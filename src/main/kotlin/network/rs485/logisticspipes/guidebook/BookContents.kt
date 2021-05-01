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
import network.rs485.logisticspipes.gui.guidebook.DrawablePage
import network.rs485.logisticspipes.gui.guidebook.DrawablePageFactory
import network.rs485.logisticspipes.util.TextUtil
import network.rs485.markdown.HeaderParagraph
import network.rs485.markdown.ImageParagraph
import network.rs485.markdown.MarkdownParser
import network.rs485.markdown.Paragraph
import org.apache.commons.io.FilenameUtils
import java.io.IOException
import java.nio.file.Path
import java.nio.file.Paths
import java.text.MessageFormat


val MISSING_META = YamlPageMetadata("[404] the metadata was not found :P", icon = "logisticspipes:unrouted_pipe")

object BookContents {

    const val MAIN_MENU_FILE = "/main_menu.md"

    internal val specialImages = mapOf(
        "guide_book_404" to "textures/gui/guide_book_404.png"
    )

    private val specialPages = mapOf<String, PageInfoProvider>(
        DebugPage.FILE to DebugPage
    )

    private val cachedLoadedPages = hashMapOf<String, PageInfoProvider>()
    private val cachedDrawablePages = hashMapOf<String, DrawablePage>()

    fun get(markdownFile: String): PageInfoProvider {
        assert(markdownFile.isNotEmpty()) { "Cannot read an empty file" }
        return specialPages.getOrDefault(markdownFile, cachedLoadedPages.getOrPut(markdownFile) {
            loadPage(markdownFile, Minecraft.getMinecraft().languageManager.currentLanguage.languageCode)
        })
    }

    fun getDrawablePage(page: String): DrawablePage = cachedDrawablePages.getOrPut(page) {
        DrawablePageFactory.createDrawablePage(get(page))
    }

    fun clear() {
        cachedLoadedPages.clear()
        cachedDrawablePages.clear()
    }
}

private val metadataRegex = "^\\s*<!---\\s*\\n(.*?)\\n\\s*--->\\s*(.*)$".toRegex(RegexOption.DOT_MATCHES_ALL)

fun loadPage(path: String, lang: String): PageInfoProvider {
    val resolvedLocation = resolveAbsoluteLocation(resolvedLocation = Paths.get(path), language = lang).toLocation(false)
    return try {
        val bookFile = Minecraft.getMinecraft().resourceManager.getResource(ResourceLocation(LPConstants.LP_MOD_ID, resolvedLocation))
        LoadedPage(
            fileLocation = path,
            language = lang,
            unformattedText = bookFile.inputStream.bufferedReader().readLines().joinToString("\n"),
        )
    } catch (error: IOException) {
        if (lang != "en_us") {
            // Didn't find current file, checking for english version.
            if (LogisticsPipes.isDEBUG()) LogisticsPipes.log.error("Language $lang for the current file ($resolvedLocation) was not found. Defaulting to en_us.")
            loadPage(path, "en_us")
        } else {
            // English not found, this may be normal. Maybe the previous language file pointed to a non-existent file.
            val translatedError = MessageFormat.format(TextUtil.translate("misc.guide_book.missing_page"), resolvedLocation)
            object : PageInfoProvider {
                override val bookmarkable: Boolean = false
                override val language: String = lang
                override val fileLocation: String = ""
                override val metadata: YamlPageMetadata = YamlPageMetadata(
                    title = TextUtil.translate("misc.guide_book.missing_page_title"),
                    icon = "logisticspipes:itemcard",
                    menu = emptyMap()
                )
                override val paragraphs: List<Paragraph> = listOf(
                    ImageParagraph(
                        alternative = "Not found image not found?",
                        imagePath = "guide_book_404"
                    ),
                    HeaderParagraph(
                        elements = MarkdownParser.splitSpacesAndWords(translatedError),
                        headerLevel = 1
                    ),
                )
            }
        }
    }
}

private fun parseMetadata(metadataString: String, markdownFile: String): YamlPageMetadata {
    return if (metadataString.isNotEmpty()) {
        // Takes the metadata string and parses the YAML information
        try {
            Yaml.default.decodeFromString(YamlPageMetadata.serializer(), metadataString)
        } catch (e: YamlException) {
            LogisticsPipes.log.error("The following Yaml in $markdownFile is malformed! \n$metadataString", e)
            MISSING_META
        }
    } else MISSING_META
}

/**
 * @param title title of the page
 * @param icon the icon
 * @param menu menus of the page: Map of id to (category to entries)
 */
@Serializable
data class YamlPageMetadata(
    val title: String,
    val icon: String = "logisticspipes:itemcard",
    val menu: Map<String, Map<String, List<String>>> = emptyMap()
)

class LoadedPage(override val fileLocation: String, override val language: String, unformattedText: String) : PageInfoProvider {
    private val metadataString: String
    private val markdownString: String

    init {
        // Splits the input string into the metadata and the markdown parts of the page.
        val result = metadataRegex.matchEntire(unformattedText) ?: throw RuntimeException("Could not load page, regex failed in $fileLocation:\n$unformattedText")
        metadataString = result.groups[1]?.value?.trim() ?: ""
        markdownString = result.groups[2]?.value?.trim() ?: ""
    }

    override val metadata: YamlPageMetadata by lazy {
        parseMetadata(metadataString, fileLocation)
    }

    override val paragraphs: List<Paragraph> by lazy {
        MarkdownParser.parseParagraphs(markdownString)
    }

    override val bookmarkable: Boolean = fileLocation != BookContents.MAIN_MENU_FILE
}

fun Path.toLocation(absolute: Boolean = isAbsolute) = (if (absolute) "/" else "").plus(
    toString()
        .let { it.substring(FilenameUtils.getPrefixLength(it)) }
        .let { FilenameUtils.separatorsToUnix(it) }
)

fun resolveAbsoluteLocation(resolvedLocation: Path, language: String): Path =
    Paths.get("book/$language").let { base ->
        resolvedLocation.normalize()
            .filter { path -> !path.startsWith("..") }
            .fold(base, Path::resolve)
    }

interface PageInfoProvider {
    fun resolveLocation(location: String): Path = Paths.get(fileLocation).resolveSibling(location).normalize()
    fun resolveResource(location: String): ResourceLocation =
        location.lastIndexOf(':').let { idx ->
            val resourceDomain = when (idx) {
                -1 -> LPConstants.LP_MOD_ID
                else -> location.substring(0 until idx)
            }
            var resourcePath: String = ((idx + 1)..location.lastIndex).let { if (it.isEmpty()) "" else location.substring(it) }
            resourcePath = BookContents.specialImages.getOrDefault(
                key = resourcePath,
                defaultValue = resolveAbsoluteLocation(resolveLocation(resourcePath), language).toLocation(false)
            )
            ResourceLocation(resourceDomain, resourcePath)
        }

    val language: String
    val fileLocation: String
    val metadata: YamlPageMetadata
    val paragraphs: List<Paragraph>
    val bookmarkable: Boolean
}
