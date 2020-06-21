package logisticspipes.utils

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import logisticspipes.LPConstants
import logisticspipes.LogisticsPipes
import net.minecraft.client.Minecraft
import net.minecraft.util.ResourceLocation
import java.io.IOException
import java.io.InputStreamReader

class GuideBookContents private constructor(val lang: String, val divisions: ArrayList<Division> = ArrayList(), val title: String) {

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
                    val divs = json.get("divisions").asJsonArray
                    val divisions: ArrayList<Division> = ArrayList()
                    for((index, div) in divs.withIndex()) divisions.add(Division(lang, div.asJsonObject, index))
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

    class Page(val index: Int, val text: String)

    class Chapter(val lang: String, val parentTitle: String, json: JsonObject, pindex: Int, index: Int){
        val title = json.get("title").asString
        val item = json.get("item").asString
        val pages = json.get("pages").asInt
        val parentindex = pindex
        val index = index

        fun getPage(index: Int): Page? {
            var par = parentTitle.replace("[^A-z]".toRegex(), "_").toLowerCase()
            var cha = title.replace("[^A-z]".toRegex(), "_").toLowerCase()
            if (index !in 0 until pages) return null
            try {
                val res = rm.getResource(ResourceLocation(LPConstants.LP_MOD_ID, "book/$lang/$par/$cha/page$index"))
                res.use {
                    val text = res.inputStream.bufferedReader().readLines().joinToString("\n")
                    return Page(index, text)
                }
            } catch (e: IOException) {
                LogisticsPipes.log.error("Couldn't find page $par/$cha/page$index for language '$lang'!")
            }
            return null
        }
    }


    class Division constructor(val lang: String, json: JsonObject, index: Int){
        val title: String = json.get("title").asString
        val chas = json.get("chapters").asJsonArray
        val chapters: ArrayList<Chapter> = ArrayList()
        val index = index

        init {
            for ((sIndex, cha) in chas.withIndex()) chapters.add(Chapter(lang, title, cha.asJsonObject, index, sIndex))
        }

        fun getChapter(index: Int): Chapter?{
            return chapters[index]
        }
    }
}