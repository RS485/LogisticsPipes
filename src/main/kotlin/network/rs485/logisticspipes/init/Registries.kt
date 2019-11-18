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
import net.minecraft.util.registry.DefaultedRegistry
import net.minecraft.util.registry.MutableRegistry
import net.minecraft.util.registry.Registry
import net.minecraft.util.registry.SimpleRegistry
import network.rs485.logisticspipes.ModID
import network.rs485.logisticspipes.module.ModuleType
import network.rs485.logisticspipes.pipe.PipeType
import network.rs485.logisticspipes.pipe.sign.PipeSignType
import network.rs485.logisticspipes.pipe.upgrade.UpgradeType
import network.rs485.logisticspipes.transport.CellContentType
import network.rs485.logisticspipes.util.SerializableKey

object Registries {

    val SignType = create("sign_type", DefaultedRegistry<PipeSignType<*>>("$ModID:crafting"))
    val PipeType = create("pipe_type", SimpleRegistry<PipeType<*, *, *>>())
    val ModuleType = create("module_type", SimpleRegistry<ModuleType<*>>())
    val UpgradeType = create("upgrade_type", SimpleRegistry<UpgradeType<*>>())
    val CellContentType = create("cell_content_type", SimpleRegistry<CellContentType<*>>())
    val SerializableKey = create("serializable_key", SimpleRegistry<SerializableKey<*>>())

    private fun <T : MutableRegistry<*>> create(name: String, registry: T): T {
        return Registry.register(Registry.REGISTRIES, Identifier(ModID, name), registry)
    }

}