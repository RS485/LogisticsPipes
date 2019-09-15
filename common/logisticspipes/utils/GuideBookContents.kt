package logisticspipes.utils

import com.google.gson.JsonParser
import logisticspipes.LogisticsPipes
import net.minecraft.client.MinecraftClient
import net.minecraft.util.Identifier
import network.rs485.logisticspipes.ModID
import java.io.IOException
import java.io.InputStreamReader

class GuideBookContents private constructor(val lang: String, val pages: Int, val title: String) {

    fun getPage(index: Int): Page? {
        if (index !in 0 until pages) return null
        try {
            val res = rm.getResource(Identifier(ModID, "book/$lang/page$index"))
            res.use {
                val text = res.inputStream.bufferedReader().readLines().joinToString("\n")
                return Page(index, text)
            }
        } catch (e: IOException) {
            LogisticsPipes.log.error("Couldn't find page $index for language '$lang'!")
        }
        return null
    }

    companion object {

        private val rm = MinecraftClient.getInstance().resourceManager

        @JvmStatic
        @JvmOverloads
        fun load(lang: String = MinecraftClient.getInstance().languageManager.language.code): GuideBookContents? {
            try {
                val res = rm.getResource(Identifier(ModID, "book/$lang/book.json"))
                res.use {
                    val json = JsonParser().parse(InputStreamReader(res.inputStream)).asJsonObject
                    val title = json.get("title").asString
                    val pages = json.get("page_count").asInt
                    if (pages < 1) {
                        LogisticsPipes.log.error("Guide book for language '$lang' has $pages pages?!")
                        return null
                    }
                    return GuideBookContents(lang, pages, title)
                }
            } catch (e: IOException) {
                if (lang != "en_us") {
                    LogisticsPipes.log.error("Couldn't find book data for language '$lang'! Falling back to en_us…")
                    return load("en_us")
                } else {
                    LogisticsPipes.log.error("Couldn't find book data for en_us! Something is horribly wrong…")
                }
            } catch (e: UnsupportedOperationException) {
                LogisticsPipes.log.error("Invalid book.json for language '$lang'!", e)
            }
            return null
        }

    }

    class Page(val index: Int, val text: String)

}