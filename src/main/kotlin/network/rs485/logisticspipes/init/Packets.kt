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

package network.rs485.logisticspipes.init

import drawer.readFrom
import drawer.write
import io.netty.buffer.Unpooled
import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.EmptyModule
import kotlinx.serialization.modules.SerialModule
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.Identifier
import net.minecraft.util.PacketByteBuf
import network.rs485.logisticspipes.LogisticsPipes
import network.rs485.logisticspipes.ModID
import network.rs485.logisticspipes.packet.*
import java.util.stream.Stream
import kotlin.streams.asSequence

object Packets {

    // Server → Client
    object S2C {

        // val CompilerStatus = create("compiler_status", CompilerStatusPacket.serializer(), CompilerStatusPacket.module)
        val CraftingPipeSignStack = create("crafting_pipe_sign_stack", CraftingPipeSignStackPacket.serializer())
        val ItemAmountSignUpdate = create("item_amount_sign_update", ItemAmountSignUpdatePacket.serializer())
        val CellInsert = create("cell_insert", CellInsertPacket.serializer())
        val CellUntrack = create("cell_untrack", CellUntrackPacket.serializer())

        private fun <T : Packet> create(name: String, serializer: KSerializer<T>, context: SerialModule = EmptyModule): PacketWrapper<T> {
            val id = Identifier(ModID, name)
            ClientSidePacketRegistry.INSTANCE.register(id) { ctx, buf -> serializer.readFrom(buf, context).handle(ctx) }
            return PacketWrapperImpl(id, serializer, context)
        }

        interface PacketWrapper<T : Packet> {
            val id: Identifier
            fun send(packet: T, player: PlayerEntity)

            // For use with PlayerStream.*
            fun send(packet: T, players: Stream<out PlayerEntity>) = send(packet, players.asSequence().asIterable())

            fun send(packet: T, players: Iterable<PlayerEntity>) {
                LogisticsPipes.logger.fatal("packet sending is BROKEN! Not sending $packet")
                //players.forEach { send(packet, it) }
            }
        }

        private class PacketWrapperImpl<T : Packet>(override val id: Identifier, private val ser: KSerializer<T>, private val context: SerialModule) : PacketWrapper<T> {
            override fun send(packet: T, player: PlayerEntity) {
                val buf = PacketByteBuf(Unpooled.buffer())
                ser.write(packet, buf, context)
                ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, id, buf)
            }
        }

    }

    // Client → Server
    object C2S {

        @JvmField
        val GuideBookPage = create("guide_book_page", GuideBookPagePacket.serializer())

        private fun <T : Packet> create(name: String, serializer: KSerializer<T>, context: SerialModule = EmptyModule): PacketWrapper<T> {
            val id = Identifier(ModID, name)
            ServerSidePacketRegistry.INSTANCE.register(id) { ctx, buf -> serializer.readFrom(buf, context).handle(ctx) }
            return PacketWrapperImpl(id, serializer, context)
        }

        interface PacketWrapper<T : Packet> {
            val id: Identifier
            fun send(packet: T)
        }

        private class PacketWrapperImpl<T : Packet>(override val id: Identifier, private val ser: KSerializer<T>, private val context: SerialModule) : PacketWrapper<T> {
            override fun send(packet: T) {
                val buf = PacketByteBuf(Unpooled.buffer())
                ser.write(packet, buf, context)
                ClientSidePacketRegistry.INSTANCE.sendToServer(id, buf)
            }
        }
    }

    init {
        S2C
        C2S
    }

}