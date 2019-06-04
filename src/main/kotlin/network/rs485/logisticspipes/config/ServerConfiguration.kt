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

import com.google.gson.*
import com.google.gson.annotations.JsonAdapter
import logisticspipes.utils.PlayerIdentifier
import java.lang.reflect.Type
import java.util.*

private class ServerConfigurationAdapter : JsonSerializer<Map<PlayerIdentifier, PlayerConfiguration>>, JsonDeserializer<Map<PlayerIdentifier, PlayerConfiguration>> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Map<PlayerIdentifier, PlayerConfiguration>? {
        if (json.isJsonNull) {
            return null
        } else if (!json.isJsonObject) {
            throw JsonParseException("Expected an object from a map")
        }

        return json.asJsonObject.entrySet().associate { (key, value) ->
            if (value !is JsonObject) throw JsonParseException("Expected values in map object to be objects")
            Pair(
                    PlayerIdentifier.get(value["name"].asString, UUID.fromString(key)),
                    context.deserialize<PlayerConfiguration>(value["config"], PlayerConfiguration::class.java)
            )
        }
    }

    override fun serialize(src: Map<PlayerIdentifier, PlayerConfiguration>?, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        if (src == null) {
            return JsonNull.INSTANCE
        }

        return JsonObject().apply {
            src.keys.forEach {
                this.add(it.id.toString(), JsonObject().apply {
                    this.add("name", JsonPrimitive(it.username))
                    this.add("config", context.serialize(src[it], PlayerConfiguration::class.java))
                })
            }
        }
    }
}

internal class ServerConfiguration {
    @JsonAdapter(ServerConfigurationAdapter::class)
    var playerConfigurations: Map<PlayerIdentifier, PlayerConfiguration> = emptyMap()
}