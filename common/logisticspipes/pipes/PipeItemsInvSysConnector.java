package logisticspipes.pipes;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import logisticspipes.LogisticsPipes;
import logisticspipes.gui.hud.HUDInvSysConnector;
import logisticspipes.interfaces.IHeadUpDisplayRenderer;
import logisticspipes.interfaces.IHeadUpDisplayRendererProvider;
import logisticspipes.interfaces.ILogisticsModule;
import logisticspipes.interfaces.IOrderManagerContentReceiver;
import logisticspipes.interfaces.routing.IDirectRoutingConnection;
import logisticspipes.logic.LogicInvSysConnection;
import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.logisticspipes.IRoutedItem.TransportMode;
import logisticspipes.logisticspipes.SidedInventoryAdapter;
import logisticspipes.network.GuiIDs;
import logisticspipes.network.NetworkConstants;
import logisticspipes.network.packets.PacketPipeInteger;
import logisticspipes.network.packets.PacketPipeInvContent;
import logisticspipes.pipefxhandlers.Particles;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.RoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import logisticspipes.transport.TransportInvConnection;
import logisticspipes.utils.AdjacentTile;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.Pair4;
import logisticspipes.utils.SimpleInventory;
import logisticspipes.utils.WorldUtil;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.ISidedInventory;
import buildcraft.api.core.Position;
import buildcraft.core.utils.Utils;
import buildcraft.transport.EntityData;
import cpw.mods.fml.common.network.Player;

public class PipeItemsInvSysConnector extends RoutedPipe implements IDirectRoutingConnection, IHeadUpDisplayRendererProvider, IOrderManagerContentReceiver{
	
	private boolean init = false;
	private LinkedList<Pair4<ItemIdentifier,UUID,UUID,TransportMode>> destination = new LinkedList<Pair4<ItemIdentifier,UUID,UUID,TransportMode>>();
	public SimpleInventory inv = new SimpleInventory(1, "Freq. card", 1);
	public int resistance;
	public final LinkedList<ItemIdentifierStack> oldList = new LinkedList<ItemIdentifierStack>();
	public final LinkedList<ItemIdentifierStack> displayList = new LinkedList<ItemIdentifierStack>();
	public final List<EntityPlayer> localModeWatchers = new ArrayList<EntityPlayer>();
	private HUDInvSysConnector HUD = new HUDInvSysConnector(this);
	private UUID idbuffer = UUID.randomUUID();
	
	public PipeItemsInvSysConnector(int itemID) {
		super(new TransportInvConnection(), new LogicInvSysConnection(), itemID);
	}
	
	@Override
	public void updateEntity() {
		super.updateEntity();
		if(MainProxy.isClient()) return;
		if(!init) {
			if(hasConnectionUUID()) {
				if(!SimpleServiceLocator.connectionManager.addDirectConnection(getConnectionUUID(), getRouter())) {
					dropFreqCard();
				}
				CoreRoutedPipe CRP = SimpleServiceLocator.connectionManager.getConnectedPipe(getRouter());
				if(CRP != null) {
					CRP.refreshRender(true);
				}
				getRouter().update(true);
				this.refreshRender(true);
				init = true;
				idbuffer = getConnectionUUID();
			}
		}
		if(init && !hasConnectionUUID()) {
			init = false;
			CoreRoutedPipe CRP = SimpleServiceLocator.connectionManager.getConnectedPipe(getRouter());
			SimpleServiceLocator.connectionManager.removeDirectConnection(getRouter());
			if(CRP != null) {
				CRP.refreshRender(true);
			}
		}
		if(init && idbuffer != null && !idbuffer.equals(getConnectionUUID())) {
			init = false;
			CoreRoutedPipe CRP = SimpleServiceLocator.connectionManager.getConnectedPipe(getRouter());
			SimpleServiceLocator.connectionManager.removeDirectConnection(getRouter());
			if(CRP != null) {
				CRP.refreshRender(true);
			}
		}
		if(destination.size() > 0) {
			checkConnectedInvs();
		}
	}

	private void checkConnectedInvs() {
		WorldUtil wUtil = new WorldUtil(worldObj, xCoord, yCoord, zCoord);
		for (AdjacentTile tile : wUtil.getAdjacentTileEntities(true)){
			if(tile.tile instanceof IInventory) {
				IInventory inv = Utils.getInventory((IInventory) tile.tile);
				if(inv instanceof ISidedInventory) {
					inv = new SidedInventoryAdapter((ISidedInventory)inv, tile.orientation.getOpposite());
				}
				checkOneConnectedInv(inv,tile.orientation);
				break;
			}
		}
	}
	
	private void checkOneConnectedInv(IInventory inv, ForgeDirection dir) {
		for(int i=0; i<inv.getSizeInventory();i++) {
			ItemStack stack = inv.getStackInSlot(i);
			if(stack != null) {
				ItemIdentifier ident = ItemIdentifier.get(stack);
				for(Pair4<ItemIdentifier,UUID,UUID,TransportMode> pair:destination) {
					if(pair.getValue1() == ident) {
						if(!useEnergy(6)) break;
						sendStack(stack.splitStack(1),pair.getValue2(),pair.getValue3(),dir, pair.getValue4());
						destination.remove(pair);
						if(stack.stackSize <=0 ) {
							inv.setInventorySlotContents(i, null);	
						} else {
							inv.setInventorySlotContents(i, stack);
						}
						updateContentListener();
						break;
					}
				}
			}
		}
	}

	public void sendStack(ItemStack stack, UUID source, UUID destination, ForgeDirection dir, TransportMode mode) {
		IRoutedItem itemToSend = SimpleServiceLocator.buildCraftProxy.CreateRoutedItem(stack, this.worldObj);
		itemToSend.setSource(source);
		itemToSend.setDestination(destination);
		itemToSend.setTransportMode(mode);
		super.queueRoutedItem(itemToSend, dir);
		MainProxy.sendSpawnParticlePacket(Particles.VioletParticle, xCoord, yCoord, this.zCoord, this.worldObj, 4);
	}
	
	private UUID getConnectionUUID() {
		if(inv != null) {
			if(inv.getStackInSlot(0) != null) {
				if(inv.getStackInSlot(0).hasTagCompound()) {
					if(inv.getStackInSlot(0).getTagCompound().hasKey("UUID")) {
						return UUID.fromString(inv.getStackInSlot(0).getTagCompound().getString("UUID"));
					}
				}
			}
		}
		return null;
	}
	
	private boolean hasConnectionUUID() {
		if(inv != null) {
			if(inv.getStackInSlot(0) != null) {
				if(inv.getStackInSlot(0).hasTagCompound()) {
					if(inv.getStackInSlot(0).getTagCompound().hasKey("UUID")) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private void dropFreqCard() {
		if(inv.getStackInSlot(0) == null) return;
		EntityItem item = new EntityItem(worldObj,this.xCoord, this.yCoord, this.zCoord, inv.getStackInSlot(0));
		worldObj.spawnEntityInWorld(item);
		inv.setInventorySlotContents(0, null);
	}

	public LinkedList<ItemIdentifierStack> getExpectedItems() {
		LinkedList<ItemIdentifierStack> list = new LinkedList<ItemIdentifierStack>();
		for(Pair4<ItemIdentifier,UUID,UUID,TransportMode> pair:destination) {
			boolean found = false;
			for(ItemIdentifierStack stack:list) {
				if(stack.getItem() == pair.getValue1()) {
					found = true;
					stack.stackSize += 1;
				}
			}
			if(!found) {
				list.add(new ItemIdentifierStack(pair.getValue1(), 1));
			}
		}
		return list;
	}
	
	@Override
	public boolean blockActivated(World world, int i, int j, int k,	EntityPlayer entityplayer) {
		if (SimpleServiceLocator.buildCraftProxy.isWrenchEquipped(entityplayer) && !(entityplayer.isSneaking())) {
			entityplayer.openGui(LogisticsPipes.instance, GuiIDs.GUI_Inv_Sys_Connector_ID, world, i, j, k);
			return true;
		}
		return false;
	}

	@Override
	public void onBlockRemoval() {
		super.onBlockRemoval();
		CoreRoutedPipe CRP = SimpleServiceLocator.connectionManager.getConnectedPipe(getRouter());
		SimpleServiceLocator.connectionManager.removeDirectConnection(getRouter());
		if(CRP != null) {
			CRP.refreshRender(true);
		}
		dropFreqCard();
	}
	

	@Override
	public void invalidate() {
		CoreRoutedPipe CRP = SimpleServiceLocator.connectionManager.getConnectedPipe(getRouter());
		SimpleServiceLocator.connectionManager.removeDirectConnection(getRouter());
		if(CRP != null) {
			CRP.refreshRender(true);
		}
		init = false;
		super.invalidate();
	}
	
	
	@Override
	public void onChunkUnload() {
		CoreRoutedPipe CRP = SimpleServiceLocator.connectionManager.getConnectedPipe(getRouter());
		SimpleServiceLocator.connectionManager.removeDirectConnection(getRouter());
		if(CRP != null) {
			CRP.refreshRender(true);
		}
		init = false;
		super.onChunkUnload();
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);
		inv.writeToNBT(nbttagcompound, "");
		nbttagcompound.setInteger("resistance", resistance);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);
		inv.readFromNBT(nbttagcompound, "");
		resistance = nbttagcompound.getInteger("resistance");
	}
	
	private boolean hasRemoteConnection() {
		return hasConnectionUUID() && this.worldObj != null && SimpleServiceLocator.connectionManager.hasDirectConnection(getRouter());
	}
	
	private boolean inventoryConnected() {
		for (int i = 0; i < 6; i++)	{
			Position p = new Position(xCoord, yCoord, zCoord, ForgeDirection.values()[i]);
			p.moveForwards(1);
			TileEntity tile = worldObj.getBlockTileEntity((int) p.x, (int) p.y, (int) p.z);
			if(tile instanceof IInventory) {
				return true;
			}
		}
		return false;
	}

	@Override
	public TextureType getCenterTexture() {
		return hasRemoteConnection() ? inventoryConnected() ? Textures.LOGISTICSPIPE_INVSYSCON_CON_TEXTURE : Textures.LOGISTICSPIPE_INVSYSCON_MIS_TEXTURE : Textures.LOGISTICSPIPE_INVSYSCON_DIS_TEXTURE;
	}

	@Override
	public ILogisticsModule getLogisticsModule() {
		return null;
	}

	@Override
	public ItemSendMode getItemSendMode() {
		return ItemSendMode.Fast;
	}

	@Override
	public int getConnectionResistance() {
		return resistance;
	}

	@Override
	public void addItem(ItemIdentifier item, UUID sourceId, UUID destinationId, TransportMode mode) {
		if(item != null && destinationId != null) {
			destination.addLast(new Pair4<ItemIdentifier,UUID,UUID,TransportMode>(item,sourceId,destinationId, mode));
			updateContentListener();
		}
	}
	
	public boolean isConnectedInv(TileEntity tile) {
		for (int i = 0; i < 6; i++)	{
			Position p = new Position(xCoord, yCoord, zCoord, ForgeDirection.values()[i]);
			p.moveForwards(1);
			TileEntity lTile = worldObj.getBlockTileEntity((int) p.x, (int) p.y, (int) p.z);
			if(lTile instanceof IInventory) {
				if(lTile == tile) {
					return true;
				}
				return false;
			}
		}
		return false;
	}
	
	public void handleItemEnterInv(EntityData data, TileEntity tile) {
		if(isConnectedInv(tile)) {
			if(data.item instanceof IRoutedItem) {
				IRoutedItem routed = (IRoutedItem)data.item;
				if(hasRemoteConnection()) {
					CoreRoutedPipe CRP = SimpleServiceLocator.connectionManager.getConnectedPipe(getRouter());
					if(CRP instanceof IDirectRoutingConnection) {
						IDirectRoutingConnection pipe = (IDirectRoutingConnection) CRP;
						for(int i=0; i < data.item.getItemStack().stackSize;i++) {
							pipe.addItem(ItemIdentifier.get(routed.getItemStack()), routed.getSource(), routed.getDestination(), routed.getTransportMode());
							MainProxy.sendSpawnParticlePacket(Particles.VioletParticle, xCoord, yCoord, this.zCoord, this.worldObj, 4);
						}
					}
				}
			}
		}
	}

	@Override
	public int getX() {
		return this.xCoord;
	}

	@Override
	public int getY() {
		return this.yCoord;
	}

	@Override
	public int getZ() {
		return this.zCoord;
	}

	@Override
	public void startWaitching() {
		MainProxy.sendPacketToServer(new PacketPipeInteger(NetworkConstants.HUD_START_WATCHING, xCoord, yCoord, zCoord, 1).getPacket());
	}

	@Override
	public void stopWaitching() {
		MainProxy.sendPacketToServer(new PacketPipeInteger(NetworkConstants.HUD_STOP_WATCHING, xCoord, yCoord, zCoord, 1).getPacket());
	}

	@Override
	public IHeadUpDisplayRenderer getRenderer() {
		return HUD;
	}
	
	private void updateContentListener() {
		if(!getExpectedItems().equals(oldList)) {
			oldList.clear();
			oldList.addAll(getExpectedItems());
			MainProxy.sendToPlayerList(new PacketPipeInvContent(NetworkConstants.ORDER_MANAGER_CONTENT, xCoord, yCoord, zCoord, getExpectedItems()).getPacket(), localModeWatchers);
		}
	}

	@Override
	public void playerStartWatching(EntityPlayer player, int mode) {
		if(mode == 1) {
			localModeWatchers.add(player);
			MainProxy.sendPacketToPlayer(new PacketPipeInvContent(NetworkConstants.ORDER_MANAGER_CONTENT, xCoord, yCoord, zCoord, getExpectedItems()).getPacket(), (Player)player);
		} else {
			super.playerStartWatching(player, mode);
		}
	}

	@Override
	public void playerStopWatching(EntityPlayer player, int mode) {
		super.playerStopWatching(player, mode);
		localModeWatchers.remove(player);
	}
	
	@Override
	public void setOrderManagerContent(LinkedList<ItemIdentifierStack> list) {
		displayList.clear();
		displayList.addAll(list);
	}
}
