package logisticspipes.pipes;

import java.util.LinkedList;

import logisticspipes.config.Textures;
import logisticspipes.gui.hud.HUDCraftingMK3;
import logisticspipes.interfaces.IChestContentReceiver;
import logisticspipes.interfaces.IHeadUpDisplayRenderer;
import logisticspipes.network.NetworkConstants;
import logisticspipes.network.packets.PacketPipeInvContent;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.transport.CraftingPipeMk3Transport;
import logisticspipes.utils.AdjacentTile;
import logisticspipes.utils.ISimpleInventoryEventHandler;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.SimpleInventory;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import buildcraft.core.inventory.Transactor;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

public class PipeItemsCraftingLogisticsMk3 extends PipeItemsCraftingLogisticsMk2 implements ISimpleInventoryEventHandler, IChestContentReceiver {
	
	public SimpleInventory inv = new SimpleInventory(16, "Buffer", 127);
	
	public LinkedList<ItemIdentifierStack> bufferList = new LinkedList<ItemIdentifierStack>();
	private HUDCraftingMK3 HUD = new HUDCraftingMK3(this);
	
	public PipeItemsCraftingLogisticsMk3(int itemID) {
		super(new CraftingPipeMk3Transport(), itemID);
		((CraftingPipeMk3Transport)transport).pipe = this;
		inv.addListener(this);
	}

	@Override
	public void updateEntity() {
		super.updateEntity();
		//Add from interal buffer
		LinkedList<AdjacentTile> crafters = locateCrafters();
		if(crafters.size() < 1) return;
		boolean change = false;
		for(AdjacentTile tile:locateCrafters()) {
			for(int i=0;i<inv.getSizeInventory();i++) {
				ItemStack slot = inv.getStackInSlot(i);
				if(slot == null) continue;
				//IC2 workAround
				for(int j=0;j < 2 && !change;j++) {
					if(j == 1 &&SimpleServiceLocator.electricItemProxy.isElectricItem(slot) && slot.hasTagCompound() && slot.getTagCompound().getName().equals("")) {
						slot.getTagCompound().setName("tag");
					}
					ItemStack added = Transactor.getTransactorFor(tile.tile).add(slot, tile.orientation.reverse(), true);
					slot.stackSize -= added.stackSize;
					if(added.stackSize != 0) {
						change = true;
					}
					if(slot.stackSize <= 0) {
						inv.setInventorySlotContents(i, null);
					} else {
						inv.setInventorySlotContents(i, slot);
					}
				}
			}
		}
		if(change) {
			inv.onInventoryChanged();
		}
	}

	@Override
	public void onBlockRemoval() {
		super.onBlockRemoval();
		inv.dropContents(worldObj, xCoord, yCoord, zCoord);
	}

	@Override
	public int getCenterTexture() {
		if(SimpleServiceLocator.buildCraftProxy.checkMaxItems()) {
			return Textures.LOGISTICSPIPE_CRAFTERMK3_TEXTURE;
		} else {
			return Textures.LOGISTICSPIPE_CRAFTERMK3_TEXTURE_DIS;
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);
		inv.writeToNBT(nbttagcompound, "buffer");
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);
		inv.readFromNBT(nbttagcompound, "buffer");
	}

	@Override
	public void InventoryChanged(SimpleInventory inventory) {
		MainProxy.sendToPlayerList(new PacketPipeInvContent(NetworkConstants.PIPE_CHEST_CONTENT, xCoord, yCoord, zCoord, ItemIdentifierStack.getListFromInventory(inv, true)).getPacket(), localModeWatchers);
	}
	
	@Override
	public void playerStartWatching(EntityPlayer player, int mode) {
		super.playerStartWatching(player, mode);
		if(mode == 1) {
			PacketDispatcher.sendPacketToPlayer(new PacketPipeInvContent(NetworkConstants.PIPE_CHEST_CONTENT, xCoord, yCoord, zCoord, ItemIdentifierStack.getListFromInventory(inv, true)).getPacket(), (Player)player);
		}
	}

	@Override
	public void setReceivedChestContent(LinkedList<ItemIdentifierStack> list) {
		bufferList.clear();
		bufferList.addAll(list);
	}

	@Override
	public IHeadUpDisplayRenderer getRenderer() {
		return HUD;
	}
}
