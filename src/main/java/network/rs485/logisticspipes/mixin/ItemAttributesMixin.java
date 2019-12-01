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

package network.rs485.logisticspipes.mixin;

import java.util.function.Function;

import net.minecraft.block.Block;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.InventoryProvider;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.util.math.Direction;

import alexiil.mc.lib.attributes.CustomAttributeAdder;
import alexiil.mc.lib.attributes.DefaultedAttribute;
import alexiil.mc.lib.attributes.item.FixedItemInv;
import alexiil.mc.lib.attributes.item.ItemAttributes;
import alexiil.mc.lib.attributes.item.compat.FixedInventoryVanillaWrapper;
import alexiil.mc.lib.attributes.item.compat.FixedSidedInventoryVanillaWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(ItemAttributes.class)
public class ItemAttributesMixin {

	// FIXME remove when updating LBA

	@Overwrite
	private static <T> CustomAttributeAdder<T> createBlockAdder(Function<FixedItemInv, T> convertor) {
		return (world, pos, state, list) -> {
			Block block = state.getBlock();
			Direction direction = list.getSearchDirection();
			Direction blockSide = direction == null ? null : direction.getOpposite();
			SidedInventory sidedInv;
			FixedItemInv wrapper;
			if (block instanceof InventoryProvider) {
				InventoryProvider provider = (InventoryProvider) block;
				sidedInv = provider.getInventory(state, world, pos);
				if (sidedInv != null) {
					if (sidedInv.getInvSize() > 0) {
						if (direction != null) {
							wrapper = FixedSidedInventoryVanillaWrapper.create(sidedInv, blockSide);
						} else {
							wrapper = new FixedInventoryVanillaWrapper(sidedInv);
						}

						list.add(convertor.apply(wrapper));
					} else {
						list.add((T) ((DefaultedAttribute) list.attribute).defaultValue);
					}
				}
			} else if (block.hasBlockEntity()) {
				BlockEntity be = world.getBlockEntity(pos);
				if (be instanceof ChestBlockEntity) {
					boolean checkForBlockingCats = false;
					Inventory chestInv = ChestBlock.getInventory((ChestBlock) state.getBlock(), state, world, pos, checkForBlockingCats);
					if (chestInv != null) {
						list.add(convertor.apply(new FixedInventoryVanillaWrapper(chestInv)));
					}
				} else if (be instanceof SidedInventory) {
					sidedInv = (SidedInventory) be;
					if (direction != null) {
						wrapper = FixedSidedInventoryVanillaWrapper.create(sidedInv, blockSide);
					} else {
						wrapper = new FixedInventoryVanillaWrapper(sidedInv);
					}

					list.add(convertor.apply(wrapper));
				} else if (be instanceof Inventory) {
					list.add(convertor.apply(new FixedInventoryVanillaWrapper((Inventory) be)));
				}
			}

		};
	}

}
