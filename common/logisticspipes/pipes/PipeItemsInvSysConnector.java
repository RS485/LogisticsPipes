package logisticspipes.pipes;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;

import logisticspipes.LogisticsPipes;
import logisticspipes.gui.hud.HUDInvSysConnector;
import logisticspipes.interfaces.IHeadUpDisplayRenderer;
import logisticspipes.interfaces.IHeadUpDisplayRendererProvider;
import logisticspipes.interfaces.IOrderManagerContentReceiver;
import logisticspipes.interfaces.routing.IDirectRoutingConnection;
import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.logisticspipes.IRoutedItem.TransportMode;
import logisticspipes.modules.LogisticsModule;
import logisticspipes.network.GuiIDs;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.hud.HUDStartWatchingPacket;
import logisticspipes.network.packets.hud.HUDStopWatchingPacket;
import logisticspipes.network.packets.orderer.OrdererManagerContent;
import logisticspipes.pipefxhandlers.Particles;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.security.SecuritySettings;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import logisticspipes.transport.TransportInvConnection;
import logisticspipes.utils.AdjacentTile;
import logisticspipes.utils.InventoryHelper;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.Pair4;
import logisticspipes.utils.PlayerCollectionList;

import logisticspipes.utils.SidedInventoryMinecraftAdapter;
import logisticspipes.utils.SimpleInventory;
import logisticspipes.utils.WorldUtil;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatMessageComponent;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.core.Position;
import buildcraft.transport.TravelingItem;
import cpw.mods.fml.common.network.Player;

public class PipeItemsInvSysConnector extends CoreRoutedPipe implements IDirectRoutingConnection, IHeadUpDisplayRendererProvider, IOrderManagerContentReceiver{
	
	private boolean init = false;
	//list of Itemdentifier, amount, destinationsimpleid, transportmode
	private LinkedList<Pair4<ItemIdentifier,Integer,Integer,TransportMode>> destination = new LinkedList<Pair4<ItemIdentifier,Integer,Integer,TransportMode>>();
	public SimpleInventory inv = new SimpleInventory(1, "Freq. card", 1);
	public int resistance;
	public Set<ItemIdentifierStack> oldList = new TreeSet<ItemIdentifierStack>();
	public final LinkedList<ItemIdentifierStack> displayList = new LinkedList<ItemIdentifierStack>();
	public final PlayerCollectionList localModeWatchers = new PlayerCollectionList();
	private HUDInvSysConnector HUD = new HUDInvSysConnector(this);
	private UUID idbuffer = UUID.randomUUID();
	
	public PipeItemsInvSysConnector(int itemID) {
		super(new TransportInvConnection(), itemID);
	}
	
	@Override
	public void enabledUpdateEntity() {
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
		WorldUtil wUtil = new WorldUtil(getWorld(), getX(), getY(), getZ());
		for (AdjacentTile tile : wUtil.getAdjacentTileEntities(true)){
			if(tile.tile instanceof IInventory) {
				IInventory inv = InventoryHelper.getInventory((IInventory) tile.tile);
				if(inv instanceof net.minecraft.inventory.ISidedInventory) {
					inv = new SidedInventoryMinecraftAdapter((net.minecraft.inventory.ISidedInventory)inv, tile.orientation.getOpposite(),false);
				}
				if(checkOneConnectedInv(inv,tile.orientation)) {
					updateContentListener();
					break;
				}
			}
		}
	}
	
	private boolean checkOneConnectedInv(IInventory inv, ForgeDirection dir) {
		boolean contentchanged = false;
		for(int i=0; i<inv.getSizeInventory();i++) {
			ItemStack stack = inv.getStackInSlot(i);
			if(stack != null) {
				ItemIdentifier ident = ItemIdentifier.get(stack);
				for(Pair4<ItemIdentifier,Integer,Integer,TransportMode> pair:destination) {
					if(pair.getValue1() == ident) {
						int tosend = Math.min(pair.getValue2(), stack.stackSize);
						if(!useEnergy(6)) break;
						sendStack(inv.decrStackSize(i, tosend),pair.getValue3(),dir, pair.getValue4());
						if(tosend < pair.getValue2()) {
							pair.setValue2(pair.getValue2() - tosend);
						} else {
							destination.remove(pair);
						}
						contentchanged = true;
						break;
					}
				}
			}
		}
		return contentchanged;
	}

	public void sendStack(ItemStack stack, int destination, ForgeDirection dir, TransportMode mode) {
		IRoutedItem itemToSend = SimpleServiceLocator.buildCraftProxy.CreateRoutedItem(this.container, stack);
		itemToSend.setDestination(destination);
		itemToSend.setTransportMode(mode);
		super.queueRoutedItem(itemToSend, dir);
		MainProxy.sendSpawnParticlePacket(Particles.OrangeParticle, getX(), getY(), getZ(), this.getWorld(), 4);
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
		EntityItem item = new EntityItem(getWorld(),this.getX(), this.getY(), this.getZ(), inv.getStackInSlot(0));
		getWorld().spawnEntityInWorld(item);
		inv.clearInventorySlotContents(0);
	}

	public Set<ItemIdentifierStack> getExpectedItems() {
		// got to be a TreeMap, because a TreeSet doesn't have the ability to retrieve the key.
		TreeMap<ItemIdentifierStack,?> list = new TreeMap<ItemIdentifierStack,Integer>();
		for(Pair4<ItemIdentifier,Integer,Integer,TransportMode> pair:destination) {
			ItemIdentifierStack currentStack = new ItemIdentifierStack(pair.getValue1(), pair.getValue2());
			Entry<ItemIdentifierStack,?> entry = list.ceilingEntry(currentStack);
			if(entry!=null && entry.getKey().getItem().uniqueID == currentStack.getItem().uniqueID){
				entry.getKey().stackSize += currentStack.stackSize;
			} else 
				list.put(currentStack,null);
		}
		return list.keySet();
	}
	
	@Override
	public boolean wrenchClicked(EntityPlayer entityplayer, SecuritySettings settings) {
		if(MainProxy.isServer(getWorld())) {
			if (settings == null || settings.openGui) {
				entityplayer.openGui(LogisticsPipes.instance, GuiIDs.GUI_Inv_Sys_Connector_ID, getWorld(), getX(), getY(), getZ());
			} else {
				entityplayer.sendChatToPlayer(ChatMessageComponent.func_111066_d("Permission denied"));
			}
		}
		return true;
	}

	@Override
	public void onAllowedRemoval() {
		if(!stillNeedReplace) {
			CoreRoutedPipe CRP = SimpleServiceLocator.connectionManager.getConnectedPipe(getRouter());
			SimpleServiceLocator.connectionManager.removeDirectConnection(getRouter());
			if(CRP != null) {
				CRP.refreshRender(true);
			}
		}
		dropFreqCard();
	}
	

	@Override
	public void invalidate() {
		if(!stillNeedReplace) {
			CoreRoutedPipe CRP = SimpleServiceLocator.connectionManager.getConnectedPipe(getRouter());
			SimpleServiceLocator.connectionManager.removeDirectConnection(getRouter());
			if(CRP != null) {
				CRP.refreshRender(true);
			}
		}
		init = false;
		super.invalidate();
	}
	
	
	@Override
	public void onChunkUnload() {
		if(!stillNeedReplace) {
			CoreRoutedPipe CRP = SimpleServiceLocator.connectionManager.getConnectedPipe(getRouter());
			SimpleServiceLocator.connectionManager.removeDirectConnection(getRouter());
			if(CRP != null) {
				CRP.refreshRender(true);
			}
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
		return hasConnectionUUID() && this.getWorld() != null && SimpleServiceLocator.connectionManager.hasDirectConnection(getRouter());
	}
	
	private boolean inventoryConnected() {
		for (int i = 0; i < 6; i++)	{
			Position p = new Position(getX(), getY(), getZ(), ForgeDirection.values()[i]);
			p.moveForwards(1);
			TileEntity tile = getWorld().getBlockTileEntity((int) p.x, (int) p.y, (int) p.z);
			if(tile instanceof IInventory) {
				return true;
			}
		}
		return false;
	}

	@Override
	public TextureType getCenterTexture() {
		if(!stillNeedReplace && hasRemoteConnection()) {
			if(inventoryConnected()) {
				return Textures.LOGISTICSPIPE_INVSYSCON_CON_TEXTURE;
			} else {
				return Textures.LOGISTICSPIPE_INVSYSCON_MIS_TEXTURE;
			}
		}
		return Textures.LOGISTICSPIPE_INVSYSCON_DIS_TEXTURE;
	}

	@Override
	public LogisticsModule getLogisticsModule() {
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
	public void addItem(ItemIdentifier item, int amount, int destinationId, TransportMode mode) {
		if(item != null && destinationId >= 0) {
			destination.addLast(new Pair4<ItemIdentifier,Integer,Integer,TransportMode>(item, amount, destinationId, mode));
			updateContentListener();
		}
	}
	
	public boolean isConnectedInv(TileEntity tile) {
		for (int i = 0; i < 6; i++)	{
			Position p = new Position(getX(), getY(), getZ(), ForgeDirection.values()[i]);
			p.moveForwards(1);
			TileEntity lTile = getWorld().getBlockTileEntity((int) p.x, (int) p.y, (int) p.z);
			if(lTile instanceof IInventory) {
				if(lTile == tile) {
					return true;
				}
				return false;
			}
		}
		return false;
	}
	
	public void handleItemEnterInv(TravelingItem arrivingItem, TileEntity tile) {
		if(isConnectedInv(tile)) {
			if(arrivingItem instanceof IRoutedItem) {
				IRoutedItem routed = (IRoutedItem)arrivingItem;
				if(hasRemoteConnection()) {
					CoreRoutedPipe CRP = SimpleServiceLocator.connectionManager.getConnectedPipe(getRouter());
					if(CRP instanceof IDirectRoutingConnection) {
						IDirectRoutingConnection pipe = (IDirectRoutingConnection) CRP;
						pipe.addItem(ItemIdentifier.get(routed.getItemStack()), routed.getItemStack().stackSize, routed.getDestination(), routed.getTransportMode());
						MainProxy.sendSpawnParticlePacket(Particles.OrangeParticle, getX(), getY(), getZ(), this.getWorld(), 4);
					}
				}
			}
		}
	}

	@Override
	public void startWatching() {
//TODO 	MainProxy.sendPacketToServer(new PacketPipeInteger(NetworkConstants.HUD_START_WATCHING, getX(), getY(), getZ(), 1).getPacket());
		MainProxy.sendPacketToServer(PacketHandler.getPacket(HUDStartWatchingPacket.class).setInteger(1).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
	}

	@Override
	public void stopWatching() {
//TODO 	MainProxy.sendPacketToServer(new PacketPipeInteger(NetworkConstants.HUD_STOP_WATCHING, getX(), getY(), getZ(), 1).getPacket());
		MainProxy.sendPacketToServer(PacketHandler.getPacket(HUDStopWatchingPacket.class).setInteger(1).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
	}

	@Override
	public IHeadUpDisplayRenderer getRenderer() {
		return HUD;
	}
	
	private void updateContentListener() {
		Set<ItemIdentifierStack> newList = getExpectedItems();
		if(!newList.equals(oldList)) {
			oldList=newList;
//TODO 		MainProxy.sendToPlayerList(new PacketPipeInvContent(NetworkConstants.ORDER_MANAGER_CONTENT, getX(), getY(), getZ(), newList).getPacket(), localModeWatchers);
			MainProxy.sendToPlayerList(PacketHandler.getPacket(OrdererManagerContent.class).setIdentSet(newList).setPosX(getX()).setPosY(getY()).setPosZ(getZ()), localModeWatchers);
		}
	}

	@Override
	public void playerStartWatching(EntityPlayer player, int mode) {
		if(mode == 1) {
			localModeWatchers.add(player);
//TODO 		MainProxy.sendPacketToPlayer(new PacketPipeInvContent(NetworkConstants.ORDER_MANAGER_CONTENT, getX(), getY(), getZ(), getExpectedItems()).getPacket(), (Player)player);
			MainProxy.sendPacketToPlayer(PacketHandler.getPacket(OrdererManagerContent.class).setIdentSet(getExpectedItems()).setPosX(getX()).setPosY(getY()).setPosZ(getZ()), (Player)player);
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
	public void setOrderManagerContent(Collection<ItemIdentifierStack> list) {
		displayList.clear();
		displayList.addAll(list);
	}

}
