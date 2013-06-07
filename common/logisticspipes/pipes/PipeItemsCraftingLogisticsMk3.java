package logisticspipes.pipes;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import logisticspipes.config.Configs;
import logisticspipes.gui.hud.HUDCraftingMK3;
import logisticspipes.interfaces.IChestContentReceiver;
import logisticspipes.interfaces.IHeadUpDisplayRenderer;
import logisticspipes.network.NetworkConstants;
import logisticspipes.network.oldpackets.PacketPipeInvContent;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import logisticspipes.transport.CraftingPipeMk3Transport;
import logisticspipes.utils.AdjacentTile;
import logisticspipes.utils.ISimpleInventoryEventHandler;
import logisticspipes.utils.InventoryHelper;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.SimpleInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.core.Position;
import buildcraft.core.EntityPassiveItem;
import buildcraft.core.utils.Utils;
import buildcraft.transport.PipeTransportItems;
import cpw.mods.fml.common.network.Player;

public class PipeItemsCraftingLogisticsMk3 extends PipeItemsCraftingLogisticsMk2 implements ISimpleInventoryEventHandler, IChestContentReceiver {
	
	public SimpleInventory inv = new SimpleInventory(16, "Buffer", 127);
	
	public List<ItemIdentifierStack> bufferList = new LinkedList<ItemIdentifierStack>();
	private HUDCraftingMK3 HUD = new HUDCraftingMK3(this);
	
	public PipeItemsCraftingLogisticsMk3(int itemID) {
		super(new CraftingPipeMk3Transport(), itemID);
		((CraftingPipeMk3Transport)transport).pipe = this;
		inv.addListener(this);
	}

	@Override
	protected int neededEnergy() {
		return 20;
	}

	@Override
	protected int itemsToExtract() {
		return 128;
	}
	
	@Override
	protected int stacksToExtract() {
		if(SimpleServiceLocator.buildCraftProxy.checkMaxItems()) {
			return 8;
		}
		return 2;
	}
	
	@Override
	public void enabledUpdateEntity() {
		super.enabledUpdateEntity();
		if(inv.isEmpty()) return;
		if(worldObj.getWorldTime() % 6 != 0) return;
		//Add from internal buffer
		List<AdjacentTile> crafters = locateCrafters();
		if(crafters.size() < 1) {sendBuffer();return;}
		boolean change = false;
		for(AdjacentTile tile : crafters) {
			for(int i=0;i<inv.getSizeInventory();i++) {
				ItemStack slot = inv.getStackInSlot(i);
				if(slot == null) continue;
				ForgeDirection insertion = tile.orientation.getOpposite();
				if(getUpgradeManager().hasSneakyUpgrade()) {
					insertion = getUpgradeManager().getSneakyOrientation();
				}
				ItemStack toadd = slot.copy();
				toadd.stackSize = Math.min(toadd.stackSize, toadd.getMaxStackSize());
				toadd.stackSize = Math.min(toadd.stackSize, ((IInventory)tile.tile).getInventoryStackLimit());
				ItemStack added = InventoryHelper.getTransactorFor(tile.tile).add(toadd, insertion, true);
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
		if(!_orderManager.hasOrders()){
			sendBuffer();
		}
		if(change) {
			inv.onInventoryChanged();
		}
	}

	private void sendBuffer() {
		for(int i=0;i<inv.getSizeInventory();i++) {
			ItemStack stackToSend = inv.getStackInSlot(i);
			if(stackToSend==null) continue;
			Position p = new Position(container.xCoord, container.yCoord, container.zCoord, null);
			Position entityPos = new Position(p.x + 0.5, p.y + Utils.getPipeFloorOf(stackToSend), p.z + 0.5, ForgeDirection.UNKNOWN);
			EntityPassiveItem entityItem = new EntityPassiveItem(worldObj, entityPos.x, entityPos.y, entityPos.z, stackToSend);
			entityItem.setSpeed(Utils.pipeNormalSpeed * Configs.LOGISTICS_DEFAULTROUTED_SPEED_MULTIPLIER);
			((PipeTransportItems) transport).entityEntering(entityItem, entityPos.orientation);
			inv.setInventorySlotContents(i, null);
			break;
		}
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onBlockRemoval() {
		super.onBlockRemoval();
		inv.dropContents(worldObj, getX(), getY(), getZ());
	}

	@Override
	public TextureType getCenterTexture() {
		return Textures.LOGISTICSPIPE_CRAFTERMK3_TEXTURE;
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
		MainProxy.sendToPlayerList(new PacketPipeInvContent(NetworkConstants.PIPE_CHEST_CONTENT, getX(), getY(), getZ(), ItemIdentifierStack.getListFromInventory(inv, true)).getPacket(), localModeWatchers);
	}
	
	@Override
	public void playerStartWatching(EntityPlayer player, int mode) {
		super.playerStartWatching(player, mode);
		if(mode == 1) {
			MainProxy.sendPacketToPlayer(new PacketPipeInvContent(NetworkConstants.PIPE_CHEST_CONTENT, getX(), getY(), getZ(), ItemIdentifierStack.getListFromInventory(inv, true)).getPacket(), (Player)player);
		}
	}

	@Override
	public void setReceivedChestContent(Collection<ItemIdentifierStack> list) {
		bufferList.clear();
		bufferList.addAll(list);
	}

	@Override
	public IHeadUpDisplayRenderer getRenderer() {
		return HUD;
	}
}
