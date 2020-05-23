/*
 * Copyright (c) 2020  RS485
 *
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0.1, or MMPL. Please check the contents of the license located in
 * https://github.com/RS485/LogisticsPipes/blob/dev/LICENSE.md
 *
 * This file can instead be distributed under the license terms of the
 * MIT license:
 *
 * Copyright (c) 2020  RS485
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

package network.rs485.logisticspipes.network.packets;

import java.util.ArrayList;
import java.util.Objects;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumHand;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.LPItems;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.utils.StaticResolve;
import network.rs485.logisticspipes.gui.guidebook.SavedPage;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

@StaticResolve
public class SetCurrentPagePacket extends ModernPacket {

	@Getter
	@Setter
	private SavedPage page;

	@Getter
	@Setter
	private EnumHand hand;

	@Getter
	@Setter
	private ArrayList<SavedPage> savedPages = new ArrayList<>();

	public SetCurrentPagePacket(int id) {
		super(id);
	}

	@Override
	public void processPacket(EntityPlayer player) {
		ItemStack book;
		book = player.getHeldItem(hand);
		if (book.isEmpty() || book.getItem() != LPItems.itemGuideBook) return;
		NBTTagCompound nbt = book.hasTagCompound() ? Objects.requireNonNull(book.getTagCompound()) : new NBTTagCompound();
		nbt.setTag("page", page.toTag());
		NBTTagList tagList = new NBTTagList();
		for (SavedPage tab : savedPages) tagList.appendTag(tab.toTag());
		nbt.setTag("bookmarks", tagList);
		book.setTagCompound(nbt);
	}

	@Override
	public void readData(LPDataInput input) {
		super.readData(input);
		hand = input.readEnum(EnumHand.class);
		page = new SavedPage().fromBytes(input);
		int size = input.readInt();
		for (int i = 0; i < size; i++) {
			savedPages.add(new SavedPage().fromBytes(input));
		}
	}

	@Override
	public void writeData(LPDataOutput output) {
		super.writeData(output);
		output.writeEnum(hand);
		page.toBytes(output);
		output.writeInt(savedPages.size());
		for (SavedPage tab : savedPages) tab.toBytes(output);
	}

	@Override
	public ModernPacket template() {
		return new SetCurrentPagePacket(getId());
	}
}
