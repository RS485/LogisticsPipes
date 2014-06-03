package logisticspipes.network.packets.block;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import logisticspipes.blocks.crafting.LogisticsCraftingTableTileEntity;
import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.abstractpackets.NBTCoordinatesPacket;
import logisticspipes.proxy.MainProxy;

public class CraftingTableFuzzyFlagsInitPacket extends NBTCoordinatesPacket {

	public CraftingTableFuzzyFlagsInitPacket(int id) {
		super(id);
	}

	@Override
	public void processPacket(EntityPlayer player) {
		LogisticsCraftingTableTileEntity tile = this.getTile(player.worldObj, LogisticsCraftingTableTileEntity.class);
		if(tile == null) return;
		if(!MainProxy.isClient(tile.getWorldObj()))
			return;
		if(!tile.isFuzzy()) return;
		NBTTagCompound tag = this.getTag();
		NBTTagList lst = tag.getTagList("fuzzyFlags");
		for(int i = 0; i < 9; i++) {
			NBTTagCompound comp = (NBTTagCompound) lst.tagAt(i);
			tile.fuzzyFlags[i].ignore_dmg = comp.getBoolean("ignore_dmg");
			tile.fuzzyFlags[i].ignore_nbt = comp.getBoolean("ignore_nbt");
			tile.fuzzyFlags[i].use_od = comp.getBoolean("use_od");
			tile.fuzzyFlags[i].use_category = comp.getBoolean("use_category");
		}
	}

	@Override
	public ModernPacket template() {
		return new CraftingTableFuzzyFlagsInitPacket(getId());
	}

	public ModernPacket setCraftingTable(LogisticsCraftingTableTileEntity tile) {
		this.setTilePos(tile);
		NBTTagCompound tag = new NBTTagCompound();
		NBTTagList lst = new NBTTagList();
		for(int i = 0; i < 9; i++) {
			NBTTagCompound comp = new NBTTagCompound();
			comp.setBoolean("ignore_dmg", tile.fuzzyFlags[i].ignore_dmg);
			comp.setBoolean("ignore_nbt", tile.fuzzyFlags[i].ignore_nbt);
			comp.setBoolean("use_od", tile.fuzzyFlags[i].use_od);
			comp.setBoolean("use_category", tile.fuzzyFlags[i].use_category);
			lst.appendTag(comp);
		}
		tag.setTag("fuzzyFlags", lst);
		this.setTag(tag);
		return this;
	}
}
