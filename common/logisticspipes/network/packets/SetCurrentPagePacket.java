package logisticspipes.network.packets;

import java.util.ArrayList;

import net.minecraft.client.Minecraft;
import java.util.Objects;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.LPItems;
import logisticspipes.gui.guidebook.GuiGuideBook;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.utils.StaticResolve;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

@StaticResolve
public class SetCurrentPagePacket extends ModernPacket {

	@Setter
	private GuiGuideBook gui;

	@Getter
	@Setter
	private float sliderProgress;

	@Getter
	@Setter
	private int page, chapter, division;

	@Getter
	@Setter
	private EnumHand hand;

	@Getter
	@Setter
	private ArrayList<GuiGuideBook.SavedTab> savedTabs;

	public SetCurrentPagePacket(int id) {
		super(id);
	}

	@Override
	public void processPacket(EntityPlayer player) {
		ItemStack book;
		book = player.getHeldItem(hand);
		if (book.isEmpty() || book.getItem() != LPItems.itemGuideBook) return;
		NBTTagCompound nbt = book.hasTagCompound() ? Objects.requireNonNull(book.getTagCompound()) : new NBTTagCompound();
		nbt.setFloat("sliderProgress", sliderProgress);
		nbt.setInteger("page", page);
		nbt.setInteger("chapter", chapter);
		nbt.setInteger("division", division);
		nbt.setInteger("bookmarks", savedTabs.size());
		//for(GuiGuideBook.SavedTab tab: savedTabs) ;
		book.setTagCompound(nbt);
	}

	@Override
	public void readData(LPDataInput input) {
		super.readData(input);
		hand = input.readEnum(EnumHand.class);
		sliderProgress = input.readFloat();
		page = input.readInt();
		chapter = input.readInt();
		division = input.readInt();
		savedTabs.clear();
		for(int i = 0; i < input.readInt(); i++){
			GuiGuideBook.SavedTab tab = gui.new SavedTab();
			tab.fromBytes(input);
			savedTabs.add(tab);
		}
	}

	@Override
	public void writeData(LPDataOutput output) {
		super.writeData(output);
		output.writeEnum(hand);
		output.writeFloat(sliderProgress);
		output.writeInt(page);
		output.writeInt(chapter);
		output.writeInt(division);
		output.writeInt(savedTabs.size());
		for(GuiGuideBook.SavedTab tab: savedTabs) tab.toBytes(output);
	}

	@Override
	public ModernPacket template() {
		return new SetCurrentPagePacket(getId());
	}
}
