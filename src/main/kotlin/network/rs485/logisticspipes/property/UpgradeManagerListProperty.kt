/*
 * Copyright (c) 2023  RS485
 *
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0.1, or MMPL. Please check the contents of the license located in
 * https://github.com/RS485/LogisticsPipes/blob/dev/LICENSE.md
 *
 * This file can instead be distributed under the license terms of the
 * MIT license:
 *
 * Copyright (c) 2023  RS485
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

package network.rs485.logisticspipes.property

import logisticspipes.pipes.PipeLogisticsChassis
import logisticspipes.pipes.upgrades.ModuleUpgradeManager
import net.minecraft.nbt.NBTTagCompound

class UpgradeManagerListProperty : ListProperty<ModuleUpgradeManager> {
    override val tagKey: String
    private val parentChassis: PipeLogisticsChassis
    private val subProperties = arrayListOf<Property<*>>()

    private constructor(
        parentChassis: PipeLogisticsChassis,
        tagKey: String,
        list: MutableList<ModuleUpgradeManager>,
    ) : super(list) {
        this.parentChassis = parentChassis
        this.tagKey = tagKey
        this.subProperties.ensureCapacity(list.size)
        ensureObservingContents()
    }

    constructor(
        slots: Int,
        parentChassis: PipeLogisticsChassis,
        tagKey: String,
    ) : this(
        parentChassis = parentChassis,
        tagKey = tagKey,
        list = MutableList(slots) {
            ModuleUpgradeManager(
                parentChassis,
                parentChassis.originalUpgradeManager,
            )
        },
    )

    private fun ensureObservingContents() {
        // TODO: check possibility of a generalized ListProperty of Property
        val propertiesToUnobserve: MutableSet<Property<*>> = subProperties.toMutableSet()
        list.forEach {
            if (!propertiesToUnobserve.remove(it.inv)) {
                it.inv.addObserver(this::contentObserver)
                subProperties.add(it.inv)
            }
        }
        propertiesToUnobserve.forEach {
            it.propertyObservers.remove(this::contentObserver)
        }
        subProperties.removeAll(propertiesToUnobserve)
    }

    private fun contentObserver(property: Property<*>) {
        super.iChanged()
    }

    override fun iChanged() {
        super.iChanged()
        ensureObservingContents()
    }

    override fun copyValue(obj: ModuleUpgradeManager): ModuleUpgradeManager = ModuleUpgradeManager(obj)

    override fun defaultValue(idx: Int): ModuleUpgradeManager = ModuleUpgradeManager(
        parentChassis,
        parentChassis.originalUpgradeManager,
    )

    override fun readSingleFromNBT(tag: NBTTagCompound, key: String): ModuleUpgradeManager = ModuleUpgradeManager(
        parentChassis,
        parentChassis.originalUpgradeManager,
    ).apply {
        if (tag.hasKey(key)) {
            readFromNBT(tag.getCompoundTag(key), "")
        }
    }

    override fun writeSingleToNBT(tag: NBTTagCompound, key: String, value: ModuleUpgradeManager) {
        tag.setTag(key, NBTTagCompound().also {
            value.writeToNBT(it, "")
        })
    }

    override fun copyProperty(): Property<out MutableList<ModuleUpgradeManager>> =
        UpgradeManagerListProperty(
            parentChassis = parentChassis,
            tagKey = tagKey,
            list = MutableList(size) { idx -> ModuleUpgradeManager(get(idx)) },
        )

}
