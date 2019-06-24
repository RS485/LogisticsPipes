package logisticspipes.utils

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import logisticspipes.LPConstants
import logisticspipes.LogisticsPipes
import net.minecraft.client.Minecraft
import net.minecraft.util.ResourceLocation
import java.io.IOException
import java.io.InputStreamReader

class GuideBookContents private constructor(val lang: String, val divisions: List<Division> = mutableListOf(), val title: String) {

    companion object {

        private val rm = Minecraft.getMinecraft().resourceManager


        @JvmStatic
        @JvmOverloads
        fun load(lang: String = Minecraft.getMinecraft().languageManager.currentLanguage.languageCode): GuideBookContents? {
            try {
                val res = rm.getResource(ResourceLocation(LPConstants.LP_MOD_ID, "book/$lang/book.json"))
                res.use {
                    val json = JsonParser().parse(InputStreamReader(res.inputStream)).asJsonObject
                    val title = json.get("title").asString
                    val divisions = json.get("divisions").asJsonArray.withIndex().map { (sIndex, divs) -> Division(lang, divs.asJsonObject, sIndex) }
                    return GuideBookContents(lang, divisions, title)
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

    fun getDivision(index: Int): Division {
        return divisions[index]
    }

    class Page(val dindex: Int, val cindex: Int, val index: Int, val text: String)

    class Chapter(private val lang: String, val parentTitle: String, json: JsonObject, val dindex: Int, val cindex: Int) {
        val title = json["title"].asString
        val item = json["item"].asString
        val nPages = json["pages"].asInt

        fun getPage(index: Int): Page? {
            var par = parentTitle.replace("[^A-z]".toRegex(), "_").toLowerCase()
            var cha = title.replace("[^A-z]".toRegex(), "_").toLowerCase()
            if (index !in 0 until nPages) return null
            try {
                val res = rm.getResource(ResourceLocation(LPConstants.LP_MOD_ID, "book/$lang/$par/$cha/page$index"))
                res.use {
                    val text = res.inputStream.bufferedReader().readLines().joinToString("\n")
                    return Page(dindex, cindex, index, text)
                }
            } catch (e: IOException) {
                LogisticsPipes.log.error("Couldn't find page $par/$cha/page$index for language '$lang'!")
            }
            return null
        }
    }


    class Division(lang: String, json: JsonObject, val dindex: Int) {
        val title: String = json.get("title").asString
        val chapters = json.get("chapters").asJsonArray.withIndex().map { (sIndex, cha) -> Chapter(lang, title, cha.asJsonObject, dindex, sIndex) }

        fun getChapter(index: Int): Chapter? {
            return chapters[index]
        }
    }
}