package logisticspipes.network.packets.pipe;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextComponentTranslation;

import net.minecraftforge.items.CapabilityItemHandler;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.modules.ModuleActiveSupplier;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.abstractpackets.ModuleCoordinatesPacket;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.StaticResolve;
import logisticspipes.utils.item.ItemIdentifier;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

@StaticResolve
public class SlotFinderNumberPacket extends ModuleCoordinatesPacket {

	@Getter
	@Setter
	private int pipePosX;
	@Getter
	@Setter
	private int pipePosY;
	@Getter
	@Setter
	private int pipePosZ;
	@Setter
	private int inventorySlot;
	@Getter
	@Setter
	private int slot;

	public SlotFinderNumberPacket(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new SlotFinderNumberPacket(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		TileEntity inv = this.getTileAs(player.world, tile -> tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null));
		IInventoryUtil util = SimpleServiceLocator.inventoryUtilFactory.getInventoryUtil(inv, null);
		if (util == null) return;
		Slot result = null;
		if (player.openContainer.inventorySlots.get(inventorySlot).slotNumber == inventorySlot) {
			result = player.openContainer.inventorySlots.get(inventorySlot);
		}
		if (result == null) {
			for (Slot slotObject : player.openContainer.inventorySlots) {
				if (slotObject.slotNumber == inventorySlot) {
					result = slotObject;
					break;
				}
			}
		}
		if (result == null) {
			player.sendMessage(new TextComponentTranslation("lp.chat.slotnotfound"));
			return;
		}
		int resultIndex = -1;
		ItemStack content = result.getStack();
		if (!content.isEmpty()) {
			for (int i = 0; i < util.getSizeInventory(); i++) {
				if (content == util.getStackInSlot(i)) {
					resultIndex = i;
					break;
				}
			}
		} else {
			ItemStack dummyStack = new ItemStack(Blocks.DIRT, 1, 0);
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setBoolean("LPStackFinderBoolean", true); //Make it unique
			dummyStack.setTagCompound(nbt); // dummyStack: yay, I am unique
			result.putStack(dummyStack);
			for (int i = 0; i < util.getSizeInventory(); i++) {
				if (dummyStack == util.getStackInSlot(i)) {
					resultIndex = i;
					break;
				}
			}
			if (resultIndex == -1) {
				for (int i = 0; i < util.getSizeInventory(); i++) {
					ItemStack stack = util.getStackInSlot(i);
					if (stack.isEmpty()) {
						continue;
					}
					if (ItemIdentifier.get(stack).equals(ItemIdentifier.get(dummyStack)) && stack.getCount() == dummyStack.getCount()) {
						resultIndex = i;
						break;
					}
				}
			}
			result.putStack(ItemStack.EMPTY);
		}

		if (resultIndex == -1) {
			player.sendMessage(new TextComponentTranslation("lp.chat.slotnotfound"));
		} else {
			//Copy pipe to coordinates to use the getPipe method
			setPosX(getPipePosX());
			setPosY(getPipePosY());
			setPosZ(getPipePosZ());
			ModuleActiveSupplier module = this.getLogisticsModule(player, ModuleActiveSupplier.class);
			if (module != null) {
				module.slotAssignmentPattern.set(slot, resultIndex);
			}
		}
	}

	@Override
	public void writeData(LPDataOutput output) {
		super.writeData(output);
		output.writeInt(inventorySlot);
		output.writeInt(slot);
		output.writeInt(pipePosX);
		output.writeInt(pipePosY);
		output.writeInt(pipePosZ);
	}

	@Override
	public void readData(LPDataInput input) {
		super.readData(input);
		inventorySlot = input.readInt();
		slot = input.readInt();
		pipePosX = input.readInt();
		pipePosY = input.readInt();
		pipePosZ = input.readInt();
	}
}
