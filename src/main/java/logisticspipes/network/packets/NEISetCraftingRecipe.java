package logisticspipes.network.packets;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;

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

	private NonNullList<ItemStack> stackList = NonNullList.withSize(9, ItemStack.EMPTY);

	public NEISetCraftingRecipe(int id) {
		super(id);
	}

	public NonNullList<ItemStack> getStackList() {
		return this.stackList;
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
		output.writeCollection(stackList, (out, stack) -> out.writeNBTTagCompound(stack.isEmpty() ? null : stack.writeToNBT(new NBTTagCompound())));
	}

	@Override
	public void readData(LPDataInput input) {
		super.readData(input);
		NonNullList<ItemStack> readList = input.readNonNullList(inp -> {
			NBTTagCompound tag = inp.readNBTTagCompound();
			return tag == null ? null : new ItemStack(tag);
		}, ItemStack.EMPTY);
		if (readList != null) stackList = readList;
	}
}
