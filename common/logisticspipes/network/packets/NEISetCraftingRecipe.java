package logisticspipes.network.packets;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.blocks.crafting.LogisticsCraftingTableTileEntity;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.PipeBlockRequestTable;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.utils.StaticResolve;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

@StaticResolve
public class NEISetCraftingRecipe extends CoordinatesPacket {

	@Getter
	@Setter
	private NonNullList<ItemStack> stackList = NonNullList.withSize(9, ItemStack.EMPTY);

	public NEISetCraftingRecipe(int id) {
		super(id);
	}

	@Override
	public void processPacket(EntityPlayer player) {
		TileEntity tile = getTileAs(player.world, TileEntity.class);
		if (tile instanceof LogisticsCraftingTableTileEntity) {
			((LogisticsCraftingTableTileEntity) tile).handleNEIRecipePacket(getStackList());
		} else if (tile instanceof LogisticsTileGenericPipe && ((LogisticsTileGenericPipe) tile).pipe instanceof PipeBlockRequestTable) {
			((PipeBlockRequestTable) ((LogisticsTileGenericPipe) tile).pipe).handleNEIRecipePacket(getStackList());
		}
	}

	@Override
	public ModernPacket template() {
		return new NEISetCraftingRecipe(getId());
	}

	@Override
	public void writeData(LPDataOutput output) {
		super.writeData(output);

		output.writeInt(stackList.size());

		for (int i = 0; i < stackList.size(); i++) {
			final ItemStack stack = stackList.get(i);

			if (!stack.isEmpty()) {
				output.writeByte(i);
				output.writeInt(Item.getIdFromItem(stack.getItem()));
				output.writeInt(stack.getCount());
				output.writeInt(stack.getItemDamage());
				output.writeNBTTagCompound(stack.getTagCompound());
			}
		}
		output.writeByte(-1); // mark packet end
	}

	@Override
	public void readData(LPDataInput input) {
		super.readData(input);

		byte index = input.readByte();

		while (index != -1) { // read until the end
			final int itemID = input.readInt();
			int stackSize = input.readInt();
			int damage = input.readInt();
			ItemStack stack = new ItemStack(Item.getItemById(itemID), stackSize, damage);
			stack.setTagCompound(input.readNBTTagCompound());
			stackList.set(index, stack);
			index = input.readByte(); // read the next slot
		}
	}
}
