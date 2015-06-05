package logisticspipes.network.packets.pipe;

import java.io.IOException;
import java.util.List;

import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.modules.ModuleActiveSupplier;
import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.abstractpackets.ModuleCoordinatesPacket;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.SidedInventoryMinecraftAdapter;
import logisticspipes.utils.item.ItemIdentifier;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentTranslation;

import net.minecraftforge.common.util.ForgeDirection;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
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
	@SuppressWarnings("unchecked")
	public void processPacket(EntityPlayer player) {
		IInventory inv = this.getTile(player.worldObj, IInventory.class);
		if (inv instanceof ISidedInventory) {
			inv = new SidedInventoryMinecraftAdapter((ISidedInventory) inv, ForgeDirection.UNKNOWN, false);
		}
		IInventoryUtil util = SimpleServiceLocator.inventoryUtilFactory.getInventoryUtil(inv);
		Slot result = null;
		if (((List<Slot>) player.openContainer.inventorySlots).get(inventorySlot).slotNumber == inventorySlot) {
			result = ((List<Slot>) player.openContainer.inventorySlots).get(inventorySlot);
		}
		if (result == null) {
			for (Slot slotObject : (List<Slot>) player.openContainer.inventorySlots) {
				if (slotObject.slotNumber == inventorySlot) {
					result = slotObject;
					break;
				}
			}
		}
		if (result == null) {
			player.addChatComponentMessage(new ChatComponentTranslation("lp.chat.slotnotfound"));
		}
		int resultIndex = -1;
		if (resultIndex == -1) {
			ItemStack content = result.getStack();
			if (content != null) {
				for (int i = 0; i < util.getSizeInventory(); i++) {
					if (content == util.getStackInSlot(i)) {
						resultIndex = i;
						break;
					}
				}
			} else {
				ItemStack dummyStack = new ItemStack(Blocks.stone, 0, 0);
				NBTTagCompound nbt = new NBTTagCompound();
				nbt.setBoolean("LPStackFinderBoolean", true); //Make it unique
				dummyStack.setTagCompound(nbt);
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
						if (stack == null) {
							continue;
						}
						if (ItemIdentifier.get(stack).equals(ItemIdentifier.get(dummyStack)) && stack.stackSize == dummyStack.stackSize) {
							resultIndex = i;
							break;
						}
					}
				}
				result.putStack(null);
			}
		}
		if (resultIndex == -1) {
			player.addChatComponentMessage(new ChatComponentTranslation("lp.chat.slotnotfound"));
		} else {
			//Copy pipe to coordinates to use the getPipe method
			setPosX(getPipePosX());
			setPosY(getPipePosY());
			setPosZ(getPipePosZ());
			ModuleActiveSupplier module = this.getLogisticsModule(player, ModuleActiveSupplier.class);
			if (module != null) {
				module.slotArray[slot] = resultIndex;
			}
		}
	}

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeInt(inventorySlot);
		data.writeInt(slot);
		data.writeInt(pipePosX);
		data.writeInt(pipePosY);
		data.writeInt(pipePosZ);
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		super.readData(data);
		inventorySlot = data.readInt();
		slot = data.readInt();
		pipePosX = data.readInt();
		pipePosY = data.readInt();
		pipePosZ = data.readInt();
	}
}
