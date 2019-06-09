/*
 * Copyright (c) 2019  RS485
 *
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0.1, or MMPL. Please check the contents of the license located in
 * https://github.com/RS485/LogisticsPipes/blob/dev/LICENSE.md
 *
 * This file can instead be distributed under the license terms of the
 * MIT license:
 *
 * Copyright (c) 2019  RS485
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

package network.rs485.logisticspipes.config

import com.google.gson.Gson
import com.google.gson.JsonParseException
import logisticspipes.LogisticsPipes
import logisticspipes.utils.PlayerIdentifier
import net.minecraftforge.fml.common.FMLCommonHandler
import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.*

class ServerConfigurationManager {
    private val fileName = "logisticspipes.json"

    private val configFile: File
    private val gson = Gson()
    private val internalRepresentation: ServerConfiguration

    init {
        configFile = File(FMLCommonHandler.instance().savesDirectory, fileName)
        internalRepresentation = try {
            configFile.bufferedReader(Charsets.UTF_8).use {
                gson.fromJson(gson.newJsonReader(it), ServerConfiguration::class.java)
            }
        } catch (e: JsonParseException) {
            LogisticsPipes.log.error("Cannot read LP configuration! Moving current configuration away and starting a new one!")
            Files.move(configFile.toPath(), getTimedFile(".bkp").toPath())
            ServerConfiguration()
        } catch (e: FileNotFoundException) {
            LogisticsPipes.log.info("Starting a new LP configuration")
            ServerConfiguration()
        }
    }

    fun getPlayers(): Set<PlayerIdentifier> {
        return internalRepresentation.playerConfigurations.keys
    }

    fun getPlayerConfiguration(identifier: PlayerIdentifier): PlayerConfiguration {
        return internalRepresentation.playerConfigurations[identifier] ?: PlayerConfiguration()
    }

    fun setClientConfiguration(identifier: PlayerIdentifier, configuration: ClientConfiguration) {
        val newConfigurations = internalRepresentation.playerConfigurations.toMutableMap()
        newConfigurations.computeIfAbsent(identifier) { PlayerConfiguration() }.merge(configuration)
        internalRepresentation.playerConfigurations = newConfigurations
        writeChange()
    }

    fun setPlayerConfiguration(identifier: PlayerIdentifier, configuration: PlayerConfiguration) {
        val newConfigurations = HashMap(internalRepresentation.playerConfigurations)
        newConfigurations[identifier] = configuration
        internalRepresentation.playerConfigurations = newConfigurations
        writeChange()
    }

    private fun writeChange() {
        val tmpFile = getTimedFile(".tmp")
        try {
            tmpFile.bufferedWriter(Charsets.UTF_8).use {
                val jsonElement = gson.toJsonTree(internalRepresentation, ServerConfiguration::class.java)
                gson.toJson(jsonElement, gson.newJsonWriter(it))
            }
            Files.move(tmpFile.toPath(), configFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
        } finally {
            Files.deleteIfExists(tmpFile.toPath())
        }
    }

    private fun getTimedFile(suffix: String = ""): File {
        val time = Calendar.getInstance().timeInMillis.toString()
        return File(configFile.parentFile, "${configFile.name}.$time$suffix")
    }
}
