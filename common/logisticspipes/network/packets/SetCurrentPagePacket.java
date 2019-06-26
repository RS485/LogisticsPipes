package logisticspipes.network.packets;

import java.util.ArrayList;

import net.minecraft.client.Minecraft;
import java.util.Objects;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumHand;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.LPItems;
import logisticspipes.gui.guidebook.GuiGuideBook;
import logisticspipes.gui.guidebook.book.SavedTab;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.utils.StaticResolve;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

@StaticResolve
public class SetCurrentPagePacket extends ModernPacket {

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
	private ArrayList<SavedTab> savedTabs = new ArrayList<>();

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
		NBTTagList tagList = new NBTTagList();
		for (SavedTab tab : savedTabs) tagList.appendTag(tab.toTag());
		nbt.setTag("bookmarks", tagList);
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
		int size = input.readInt();
		for (int i = 0; i < size; i++) {
			savedTabs.add(new SavedTab().fromBytes(input));
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
		for (SavedTab tab : savedTabs) tab.toBytes(output);
	}

	@Override
	public ModernPacket template() {
		return new SetCurrentPagePacket(getId());
	}
}
