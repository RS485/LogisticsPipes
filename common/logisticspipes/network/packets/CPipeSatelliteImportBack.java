package logisticspipes.network.packets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.LinkedList;

import logisticspipes.logic.BaseLogicCrafting;
import logisticspipes.network.SendNBTTagCompound;
import logisticspipes.network.packets.abstracts.CoordinatesPacket;
import logisticspipes.network.packets.abstracts.ModernPacket;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import buildcraft.transport.TileGenericPipe;
import cpw.mods.fml.client.FMLClientHandler;

@Accessors(chain=true)
public class CPipeSatelliteImportBack extends CoordinatesPacket {
	
	@Setter
	public IInventory inventory;
	@Getter
	public LinkedList<ItemStack> itemStacks;

	public CPipeSatelliteImportBack(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new CPipeSatelliteImportBack(getID());
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {

		super.writeData(data);

		for (int i = 0; i < inventory.getSizeInventory(); i++) {
			data.writeByte(i);

			final ItemStack itemstack = inventory.getStackInSlot(i);

			if (itemstack != null) {
				data.writeInt(itemstack.itemID);
				data.writeInt(itemstack.stackSize);
				data.writeInt(itemstack.getItemDamage());
				SendNBTTagCompound.writeNBTTagCompound(itemstack.getTagCompound(), data);
			} else {
				data.writeInt(0);
			}
		}
		data.writeByte(-1); // mark packet end
	}

	@Override
	public void readData(DataInputStream data) throws IOException {

		super.readData(data);

		itemStacks = new LinkedList<ItemStack>(); // TODO ... => Map<slotid, ItemStack>
		
		byte index = data.readByte();

		while (index != -1) { // read until the end
			final int itemID = data.readInt();
			if (itemID == 0) {
				itemStacks.addLast(null);
			} else {
				ItemStack stack = new ItemStack(itemID, data.readInt(), data.readInt());
				stack.setTagCompound(SendNBTTagCompound.readNBTTagCompound(data));
				itemStacks.addLast(stack);
			}
			
			index = data.readByte(); // read the next slot
		}
	}

	@Override
	public void processPacket(EntityPlayerMP player) {
		final TileGenericPipe pipe = getPipe(FMLClientHandler.instance().getClient().theWorld);
		if (pipe == null) {
			return;
		}

		if (!(pipe.pipe.logic instanceof BaseLogicCrafting)) {
			return;
		}

		final BaseLogicCrafting craftingPipe = (BaseLogicCrafting) pipe.pipe.logic;
		for (int i = 0; i < itemStacks.size(); i++) {
			craftingPipe.setDummyInventorySlot(i, itemStacks.get(i));
		}
	}
}
