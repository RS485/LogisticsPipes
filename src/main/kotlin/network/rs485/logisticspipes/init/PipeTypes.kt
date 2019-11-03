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

import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import network.rs485.logisticspipes.ModID
import network.rs485.logisticspipes.pipe.DummyPipe
import network.rs485.logisticspipes.pipe.PipeType
import network.rs485.logisticspipes.pipe.shape.PipeShapes

object PipeTypes {

    // Note this isn't all registered pipes, but all pipes added by LogisticsPipes.
    // If you want all registered pipes, get them from Registries.PipeType
    var All: Set<PipeType<*, *>> = emptySet()
        private set

    val Unrouted = create("unrouted", PipeType.Builder(PipeShapes.Default, ::DummyPipe).build())
    val Basic = create("basic", PipeType.Builder(PipeShapes.Default, ::DummyPipe).build())

    val Supplier = create("supplier", PipeType.Builder(PipeShapes.Default, ::DummyPipe).build())
    val Provider = create("provider", PipeType.Builder(PipeShapes.Default, ::DummyPipe).build())
    val Crafting = create("crafting", PipeType.Builder(PipeShapes.Default, ::DummyPipe).build())
    val Satellite = create("satellite", PipeType.Builder(PipeShapes.Default, ::DummyPipe).build())
    val Request = create("request", PipeType.Builder(PipeShapes.Default, ::DummyPipe).build())
    val ChassisMk1 = create("chassis_mk1", PipeType.Builder(PipeShapes.Default, ::DummyPipe).build())
    val ChassisMk2 = create("chassis_mk2", PipeType.Builder(PipeShapes.Default, ::DummyPipe).build())
    val ChassisMk3 = create("chassis_mk3", PipeType.Builder(PipeShapes.Default, ::DummyPipe).build())
    val ChassisMk4 = create("chassis_mk4", PipeType.Builder(PipeShapes.Default, ::DummyPipe).build())
    val ChassisMk5 = create("chassis_mk5", PipeType.Builder(PipeShapes.Default, ::DummyPipe).build())
    val RemoteOrderer = create("remote_orderer", PipeType.Builder(PipeShapes.Default, ::DummyPipe).build())
    val Firewall = create("firewall", PipeType.Builder(PipeShapes.Default, ::DummyPipe).build())
    val SystemEntrance = create("system_entrance", PipeType.Builder(PipeShapes.Default, ::DummyPipe).build())
    val SystemDestination = create("system_destination", PipeType.Builder(PipeShapes.Default, ::DummyPipe).build())
    val InventorySystemConnector = create("inventory_system_connector", PipeType.Builder(PipeShapes.Default, ::DummyPipe).build())

    val FluidBasic = create("fluid_basic", PipeType.Builder(PipeShapes.Default, ::DummyPipe).build())
    val FluidSupplier = create("fluid_supplier", PipeType.Builder(PipeShapes.Default, ::DummyPipe).build())

    val HighSpeedGain = create("high_speed_gain", PipeType.Builder(PipeShapes.Default, ::DummyPipe).build())
    val HighSpeedLine = create("high_speed_line", PipeType.Builder(PipeShapes.Default, ::DummyPipe).build())
    val HighSpeedSpeedup = create("high_speed_speedup", PipeType.Builder(PipeShapes.Default, ::DummyPipe).build())
    val HighSpeedCurve = create("high_speed_curve", PipeType.Builder(PipeShapes.Default, ::DummyPipe).build())
    val HighSpeedSCurve = create("high_speed_s_curve", PipeType.Builder(PipeShapes.Default, ::DummyPipe).build())

    private fun <T : PipeType<*, *>> create(name: String, type: T): T {
        return Registry.register(Registries.PipeType, Identifier(ModID, name), type)
                .also { All += it }
    }

}