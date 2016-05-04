package logisticspipes.network.packets;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.blocks.crafting.LogisticsCraftingTableTileEntity;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.PipeBlockRequestTable;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

public class NEISetCraftingRecipe extends CoordinatesPacket {

	@Getter
	@Setter
	private ItemStack[] content = new ItemStack[9];

	public NEISetCraftingRecipe(int id) {
		super(id);
	}

	@Override
	public void processPacket(EntityPlayer player) {
		TileEntity tile = getTile(player.worldObj, TileEntity.class);
		if (tile instanceof LogisticsCraftingTableTileEntity) {
			((LogisticsCraftingTableTileEntity) tile).handleNEIRecipePacket(getContent());
		} else if (tile instanceof LogisticsTileGenericPipe && ((LogisticsTileGenericPipe) tile).pipe instanceof PipeBlockRequestTable) {
			((PipeBlockRequestTable) ((LogisticsTileGenericPipe) tile).pipe).handleNEIRecipePacket(getContent());
		}
	}

	@Override
	public ModernPacket template() {
		return new NEISetCraftingRecipe(getId());
	}

	@Override
	public void writeData(LPDataOutput output) throws IOException {
		super.writeData(output);

		output.writeInt(content.length);

		for (int i = 0; i < content.length; i++) {
			final ItemStack itemstack = content[i];

			if (itemstack != null) {
				output.writeByte(i);
				output.writeInt(Item.getIdFromItem(itemstack.getItem()));
				output.writeInt(itemstack.stackSize);
				output.writeInt(itemstack.getItemDamage());
				output.writeNBTTagCompound(itemstack.getTagCompound());
			}
		}
		output.writeByte(-1); // mark packet end
	}

	@Override
	public void readData(LPDataInput input) throws IOException {
		super.readData(input);

		content = new ItemStack[input.readInt()];

		byte index = input.readByte();

		while (index != -1) { // read until the end
			final int itemID = input.readInt();
			int stackSize = input.readInt();
			int damage = input.readInt();
			ItemStack stack = new ItemStack(Item.getItemById(itemID), stackSize, damage);
			stack.setTagCompound(input.readNBTTagCompound());
			content[index] = stack;
			index = input.readByte(); // read the next slot
		}
	}
}
