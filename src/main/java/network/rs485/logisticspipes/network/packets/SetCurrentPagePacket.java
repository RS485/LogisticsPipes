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

import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import logisticspipes.LPItems;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.utils.StaticResolve;
import network.rs485.logisticspipes.gui.guidebook.IPageData;
import network.rs485.logisticspipes.gui.guidebook.PageData;
import network.rs485.logisticspipes.guidebook.ItemGuideBook;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

@StaticResolve
public class SetCurrentPagePacket extends ModernPacket {

	@Nullable
	private IPageData currentPage;

	private EntityEquipmentSlot equipmentSlot;

	private List<? extends IPageData> bookmarks;

	public SetCurrentPagePacket(int id) {
		super(id);
	}

	@Override
	public void processPacket(EntityPlayer player) {
		if (currentPage == null) return;
		ItemStack book = player.getItemStackFromSlot(equipmentSlot);
		if (book.isEmpty() || !(book.getItem() instanceof ItemGuideBook)) return;
		NBTTagCompound compound;
		if (book.hasTagCompound()) {
			compound = Objects.requireNonNull(book.getTagCompound());
		} else {
			compound = new NBTTagCompound();
		}
		final NBTTagCompound nbt = LPItems.itemGuideBook.updateNBT(compound, currentPage, bookmarks);
		book.setTagCompound(nbt);
	}

	@Override
	public void readData(LPDataInput input) {
		super.readData(input);
		equipmentSlot = input.readEnum(EntityEquipmentSlot.class);
		currentPage = new PageData(input);
		bookmarks = input.readArrayList(PageData::new);
	}

	@Override
	public void writeData(LPDataOutput output) {
		super.writeData(output);
		if (currentPage == null) throw new NullPointerException("Current page may not be null");
		output.writeEnum(equipmentSlot);
		currentPage.write(output);
		output.writeCollection(bookmarks);
	}

	@Override
	public ModernPacket template() {
		return new SetCurrentPagePacket(getId());
	}

	public SetCurrentPagePacket setCurrentPage(IPageData currentPage) {
		this.currentPage = currentPage;
		return this;
	}

	public SetCurrentPagePacket setEquipmentSlot(EntityEquipmentSlot equipmentSlot) {
		this.equipmentSlot = equipmentSlot;
		return this;
	}

	public SetCurrentPagePacket setBookmarks(List<? extends IPageData> bookmarks) {
		this.bookmarks = bookmarks;
		return this;
	}
}
