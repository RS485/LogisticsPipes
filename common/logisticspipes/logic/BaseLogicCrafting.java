package logisticspipes.logic;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.routing.IRequireReliableTransport;
import logisticspipes.logistics.LogisticsManagerV2;
import logisticspipes.network.GuiIDs;
import logisticspipes.network.NetworkConstants;
import logisticspipes.network.packets.GuiArgumentPacket;
import logisticspipes.network.packets.PacketCoordinates;
import logisticspipes.network.packets.PacketInventoryChange;
import logisticspipes.network.packets.PacketModuleInteger;
import logisticspipes.network.packets.PacketPipeInteger;
import logisticspipes.pipes.PipeItemsCraftingLogistics;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.interfaces.ICraftingRecipeProvider;
import logisticspipes.request.RequestTree;
import logisticspipes.routing.ExitRoute;
import logisticspipes.routing.IRouter;
import logisticspipes.utils.AdjacentTile;
import logisticspipes.utils.DelayedGeneric;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.SimpleInventory;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.WorldUtil;
import net.minecraft.block.Block;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import buildcraft.core.network.TileNetworkData;
import buildcraft.transport.TileGenericPipe;
import cpw.mods.fml.common.network.Player;

public class BaseLogicCrafting extends BaseRoutingLogic implements IRequireReliableTransport {

	protected SimpleInventory _dummyInventory = new SimpleInventory(10, "Requested items", 127);
	
	@TileNetworkData
	public int signEntityX = 0;
	@TileNetworkData
	public int signEntityY = 0;
	@TileNetworkData
	public int signEntityZ = 0;
	//public LogisticsTileEntiy signEntity;

	protected final DelayQueue< DelayedGeneric<ItemIdentifierStack>> _lostItems = new DelayQueue< DelayedGeneric<ItemIdentifierStack>>();
	
	@TileNetworkData
	public int satelliteId = 0;

	@TileNetworkData(staticSize=9)
	public int advancedSatelliteIdArray[] = new int[9];

	@TileNetworkData
	public int priority = 0;

	private PipeItemsCraftingLogistics _pipe=null;
	public BaseLogicCrafting() {
		throttleTime = 40;
	}

	/* ** SATELLITE CODE ** */

	protected int getNextConnectSatelliteId(boolean prev, int x, int y) {
		final List<ExitRoute> routes = getRoutedPipe().getRouter().getIRoutersByCost();
		int closestIdFound = prev ? 0 : Integer.MAX_VALUE;
		for (final BaseLogicSatellite satellite : BaseLogicSatellite.AllSatellites) {
			CoreRoutedPipe satPipe = satellite.getRoutedPipe();
			if(satPipe == null || satPipe.stillNeedReplace() || satPipe.getRouter() == null)
				continue;
			IRouter satRouter = satPipe.getRouter();
			for (ExitRoute route:routes){
				if (route.destination == satRouter) {
					if(x == -1 && y == -1) {
						if (!prev && satellite.satelliteId > satelliteId && satellite.satelliteId < closestIdFound) {
							closestIdFound = satellite.satelliteId;
						} else if (prev && satellite.satelliteId < satelliteId && satellite.satelliteId > closestIdFound) {
							closestIdFound = satellite.satelliteId;
						}
					} else if(y == -1) {
						if (!prev && satellite.satelliteId > advancedSatelliteIdArray[x] && satellite.satelliteId < closestIdFound) {
							closestIdFound = satellite.satelliteId;
						} else if (prev && satellite.satelliteId < advancedSatelliteIdArray[x] && satellite.satelliteId > closestIdFound) {
							closestIdFound = satellite.satelliteId;
						}
					} else {
						//TODO
					}
				}
			}
		}
		if (closestIdFound == Integer.MAX_VALUE) {
			if(x == -1 && y == -1) {
				return satelliteId;
			} else if(y == -1) {
				return advancedSatelliteIdArray[x];
			} else {
				//TODO
			}
		}

		return closestIdFound;

	}

	public void setNextSatellite(EntityPlayer player) {
		if (MainProxy.isClient(player.worldObj)) {
			final PacketCoordinates packet = new PacketCoordinates(NetworkConstants.CRAFTING_PIPE_NEXT_SATELLITE, xCoord, yCoord, zCoord);
			MainProxy.sendPacketToServer(packet.getPacket());
		} else {
			satelliteId = getNextConnectSatelliteId(false, -1, -1);
			final PacketPipeInteger packet = new PacketPipeInteger(NetworkConstants.CRAFTING_PIPE_SATELLITE_ID, xCoord, yCoord, zCoord, satelliteId);
			MainProxy.sendPacketToPlayer(packet.getPacket(), (Player)player);
		}

	}
	
	// This is called by the packet PacketCraftingPipeSatelliteId
	public void setSatelliteId(int satelliteId, int x, int y) {
		if(x == -1 && y == -1) {
			this.satelliteId = satelliteId;
		} else if(y == -1) {
			advancedSatelliteIdArray[x] = satelliteId;
		} else {
			//TODO
		}
	}

	public void setPrevSatellite(EntityPlayer player) {
		if (MainProxy.isClient(player.worldObj)) {
			final PacketCoordinates packet = new PacketCoordinates(NetworkConstants.CRAFTING_PIPE_PREV_SATELLITE, xCoord, yCoord, zCoord);
			MainProxy.sendPacketToServer(packet.getPacket());
		} else {
			satelliteId = getNextConnectSatelliteId(true, -1, -1);
			final PacketPipeInteger packet = new PacketPipeInteger(NetworkConstants.CRAFTING_PIPE_SATELLITE_ID, xCoord, yCoord, zCoord, satelliteId);
			MainProxy.sendPacketToPlayer(packet.getPacket(), (Player)player);
		}
	}

	public boolean isSatelliteConnected() {
		final List<ExitRoute> routes = getRoutedPipe().getRouter().getIRoutersByCost();
		if(!((CoreRoutedPipe)this.container.pipe).getUpgradeManager().isAdvancedSatelliteCrafter()) {
			if(satelliteId == 0) return true;
			for (final BaseLogicSatellite satellite : BaseLogicSatellite.AllSatellites) {
				if (satellite.satelliteId == satelliteId) {
					CoreRoutedPipe satPipe = satellite.getRoutedPipe();
					if(satPipe == null || satPipe.stillNeedReplace() || satPipe.getRouter() == null)
						continue;
					IRouter satRouter = satPipe.getRouter();
					for (ExitRoute route:routes) {
						if (route.destination == satRouter) {
							return true;
						}
					}
				}
			}
		} else {
			boolean foundAll = true;
			for(int i=0;i<9;i++) {
				boolean foundOne = false;
				if(advancedSatelliteIdArray[i] == 0) {
					continue;
				}
				for (final BaseLogicSatellite satellite : BaseLogicSatellite.AllSatellites) {
					if (satellite.satelliteId == advancedSatelliteIdArray[i]) {
						CoreRoutedPipe satPipe = satellite.getRoutedPipe();
						if(satPipe == null || satPipe.stillNeedReplace() || satPipe.getRouter() == null)
							continue;
						IRouter satRouter = satPipe.getRouter();
						for (ExitRoute route:routes) {
							if (route.destination == satRouter) {
								foundOne = true;
								break;
							}
						}
					}
				}
				foundAll &= foundOne;
			}
			return foundAll;
		}
		return false;
	}

	public IRouter getSatelliteRouter(int x, int y) {
		if(x == -1 && y == -1) {
			for (final BaseLogicSatellite satellite : BaseLogicSatellite.AllSatellites) {
				if (satellite.satelliteId == satelliteId) {
					CoreRoutedPipe satPipe = satellite.getRoutedPipe();
					if(satPipe == null || satPipe.stillNeedReplace() || satPipe.getRouter() == null)
						continue;
					return satPipe.getRouter();
				}
			}
		} else if(y == -1) {
			for (final BaseLogicSatellite satellite : BaseLogicSatellite.AllSatellites) {
				if (satellite.satelliteId == advancedSatelliteIdArray[x]) {
					CoreRoutedPipe satPipe = satellite.getRoutedPipe();
					if(satPipe == null || satPipe.stillNeedReplace() || satPipe.getRouter() == null)
						continue;
					return satPipe.getRouter();
				}
			}
		} else {
			//TODO
		}
		return null;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);
		_dummyInventory.readFromNBT(nbttagcompound, "");
		satelliteId = nbttagcompound.getInteger("satelliteid");
		signEntityX = nbttagcompound.getInteger("CraftingSignEntityX");
		signEntityY = nbttagcompound.getInteger("CraftingSignEntityY");
		signEntityZ = nbttagcompound.getInteger("CraftingSignEntityZ");
		
		priority = nbttagcompound.getInteger("priority");
		for(int i=0;i<9;i++) {
			advancedSatelliteIdArray[i] = nbttagcompound.getInteger("advancedSatelliteId" + i);
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);
		_dummyInventory.writeToNBT(nbttagcompound, "");
		nbttagcompound.setInteger("satelliteid", satelliteId);
		
		nbttagcompound.setInteger("CraftingSignEntityX", signEntityX);
		nbttagcompound.setInteger("CraftingSignEntityY", signEntityY);
		nbttagcompound.setInteger("CraftingSignEntityZ", signEntityZ);
		
		nbttagcompound.setInteger("priority", priority);
		for(int i=0;i<9;i++) {
			nbttagcompound.setInteger("advancedSatelliteId" + i, advancedSatelliteIdArray[i]);
		}
	}

	@Override
	public void destroy() {
		if(signEntityX != 0 && signEntityY != 0 && signEntityZ != 0) {
			worldObj.setBlock(signEntityX, signEntityY, signEntityZ, 0);
			signEntityX = 0;
			signEntityY = 0;
			signEntityZ = 0;
		}
	}

	@Override
	public void onWrenchClicked(EntityPlayer entityplayer) {
		if (MainProxy.isServer(entityplayer.worldObj)) {
			MainProxy.sendPacketToPlayer(new GuiArgumentPacket(GuiIDs.GUI_CRAFTINGPIPE_ID, new Object[]{((CoreRoutedPipe)this.container.pipe).getUpgradeManager().isAdvancedSatelliteCrafter()}).getPacket(),  (Player) entityplayer);
			entityplayer.openGui(LogisticsPipes.instance, GuiIDs.GUI_CRAFTINGPIPE_ID, worldObj, xCoord, yCoord, zCoord);
		}
	}

	@Override
	public void throttledUpdateEntity() {
		super.throttledUpdateEntity();
		if (_lostItems.isEmpty()) {
			return;
		}
		DelayedGeneric<ItemIdentifierStack> lostItem = _lostItems.poll();
		while (lostItem != null) {
			
			ItemIdentifierStack stack = lostItem.get();
			if(_pipe != null && ! _pipe.hasOrder()) { 
				SinkReply reply = LogisticsManagerV2.canSink(_pipe.getRouter(), null, true, stack.getItem(), null, true);
				if(reply == null || reply.maxNumberOfItems <1) {
					lostItem = _lostItems.poll();
					//iterator.remove(); // if we have no space for this and nothing to do, don't bother re-requesting the item.
					continue;
				}
			}
			int received = RequestTree.requestPartial(stack, (CoreRoutedPipe) container.pipe);
			if(received < stack.stackSize) {
				stack.stackSize -= received;
				_lostItems.add(new DelayedGeneric(stack,5000));
			}
			lostItem = _lostItems.poll();
		}
	}

	@Override
	public void itemArrived(ItemIdentifierStack item) {
	}

	@Override
	public void itemLost(ItemIdentifierStack item) {
		_lostItems.add(new DelayedGeneric(item,5000));
	}

	public void openAttachedGui(EntityPlayer player) {
		if (MainProxy.isClient(player.worldObj)) {
			if(player instanceof EntityPlayerMP) {
				((EntityPlayerMP)player).closeScreen();
			} else if(player instanceof EntityPlayerSP) {
				((EntityPlayerSP)player).closeScreen();
			}
			final PacketCoordinates packet = new PacketCoordinates(NetworkConstants.CRAFTING_PIPE_OPEN_CONNECTED_GUI, xCoord, yCoord, zCoord);
			MainProxy.sendPacketToServer(packet.getPacket());
			return;
		}

		//hack to avoid wrenching blocks
		int savedEquipped = player.inventory.currentItem;
		boolean foundSlot = false;
		//try to find a empty slot
		for(int i = 0; i < 9; i++) {
			if(player.inventory.getStackInSlot(i) == null) {
				foundSlot = true;
				player.inventory.currentItem = i;
				break;
			}
		}
		//okay, anything that's a block?
		if(!foundSlot) {
			for(int i = 0; i < 9; i++) {
				ItemStack is = player.inventory.getStackInSlot(i);
				if(is.getItem() instanceof ItemBlock) {
					foundSlot = true;
					player.inventory.currentItem = i;
					break;
				}
			}
		}
		//give up and select whatever is right of the current slot
		if(!foundSlot) {
			player.inventory.currentItem = (player.inventory.currentItem + 1) % 9;
		}

		final WorldUtil worldUtil = new WorldUtil(worldObj, xCoord, yCoord, zCoord);
		boolean found = false;
		for (final AdjacentTile tile : worldUtil.getAdjacentTileEntities(true)) {
			for (ICraftingRecipeProvider provider : SimpleServiceLocator.craftingRecipeProviders) {
				if (provider.canOpenGui(tile.tile)) {
					found = true;
					break;
				}
			}

			if (!found)
				found = (tile.tile instanceof IInventory && !(tile.tile instanceof TileGenericPipe));

			if (found) {
				Block block = worldObj.getBlockId(tile.tile.xCoord, tile.tile.yCoord, tile.tile.zCoord) < Block.blocksList.length ? Block.blocksList[worldObj.getBlockId(tile.tile.xCoord, tile.tile.yCoord, tile.tile.zCoord)] : null;
				if(block != null) {
					if(block.onBlockActivated(worldObj, tile.tile.xCoord, tile.tile.yCoord, tile.tile.zCoord, player, 0, 0, 0, 0)){
						break;
					}
				}
			}
		}

		player.inventory.currentItem = savedEquipped;
	}

	public void importFromCraftingTable(EntityPlayer player) {
		final WorldUtil worldUtil = new WorldUtil(worldObj, xCoord, yCoord, zCoord);
		for (final AdjacentTile tile : worldUtil.getAdjacentTileEntities(true)) {
			for (ICraftingRecipeProvider provider : SimpleServiceLocator.craftingRecipeProviders) {
				if (provider.importRecipe(tile.tile, _dummyInventory))
					break;
			}
		}
		
		if(player == null) return;
		
		if (MainProxy.isClient(player.worldObj)) {
			// Send packet asking for import
			final PacketCoordinates packet = new PacketCoordinates(NetworkConstants.CRAFTING_PIPE_IMPORT, xCoord, yCoord, zCoord);
			MainProxy.sendPacketToServer(packet.getPacket());
		} else{
			// Send inventory as packet
			final PacketInventoryChange packet = new PacketInventoryChange(NetworkConstants.CRAFTING_PIPE_IMPORT_BACK, xCoord, yCoord, zCoord, _dummyInventory);
			MainProxy.sendPacketToPlayer(packet.getPacket(), (Player)player);

		}
	}

	public void handleStackMove(int number) {
		if(MainProxy.isClient(this.worldObj)) {
			MainProxy.sendPacketToServer(new PacketPipeInteger(NetworkConstants.CRAFTING_PIPE_STACK_MOVE,xCoord,yCoord,zCoord,number).getPacket());
		}
		ItemStack stack = _dummyInventory.getStackInSlot(number);
		if(stack == null ) return;
		for(int i = 6;i < 9;i++) {
			ItemStack stackb = _dummyInventory.getStackInSlot(i);
			if(stackb == null) {
				_dummyInventory.setInventorySlotContents(i, stack);
				_dummyInventory.setInventorySlotContents(number, null);
				break;
			}
		}
	}
	
	public void priorityUp(EntityPlayer player) {
		priority++;
		if(MainProxy.isClient(player.worldObj)) {
			MainProxy.sendPacketToServer(new PacketCoordinates(NetworkConstants.CRAFTING_PIPE_PRIORITY_UP, xCoord, yCoord, zCoord).getPacket());
		} else if(player != null && MainProxy.isServer(player.worldObj)) {
			MainProxy.sendPacketToPlayer(new PacketPipeInteger(NetworkConstants.CRAFTING_PIPE_PRIORITY, xCoord, yCoord, zCoord, priority).getPacket(), (Player)player);
		}
	}
	
	public void priorityDown(EntityPlayer player) {
		priority--;
		if(MainProxy.isClient(player.worldObj)) {
			MainProxy.sendPacketToServer(new PacketCoordinates(NetworkConstants.CRAFTING_PIPE_PRIORITY_DOWN, xCoord, yCoord, zCoord).getPacket());
		} else if(player != null && MainProxy.isServer(player.worldObj)) {
			MainProxy.sendPacketToPlayer(new PacketPipeInteger(NetworkConstants.CRAFTING_PIPE_PRIORITY, xCoord, yCoord, zCoord, priority).getPacket(), (Player)player);
		}
	}
	
	public void setPriority(int amount) {
		priority = amount;
	}
	
	/* ** INTERFACE TO PIPE ** */
	public List<ItemStack> getCraftedItems() {
		//TODO: AECrafting check.
		List<ItemStack> list = new ArrayList<ItemStack>(1);
		if(_dummyInventory.getStackInSlot(9)!=null)
			list.add(_dummyInventory.getStackInSlot(9));
		return list;
	}

	public ItemStack getMaterials(int slotnr) {
		return _dummyInventory.getStackInSlot(slotnr);
	}

	/**
	 * Simply get the dummy inventory
	 * 
	 * @return the dummy inventory
	 */
	public SimpleInventory getDummyInventory() {
		return _dummyInventory;
	}
	
	public void setDummyInventorySlot(int slot, ItemStack itemstack) {
		_dummyInventory.setInventorySlotContents(slot, itemstack);
	}

	public void setNextSatellite(EntityPlayer player, int i) {
		if (MainProxy.isClient(player.worldObj)) {
			final PacketCoordinates packet = new PacketPipeInteger(NetworkConstants.CRAFTING_PIPE_NEXT_SATELLITE_ADVANCED, xCoord, yCoord, zCoord, i);
			MainProxy.sendPacketToServer(packet.getPacket());
		} else {
			advancedSatelliteIdArray[i] = getNextConnectSatelliteId(false, i, -1);
			final PacketModuleInteger packet = new PacketModuleInteger(NetworkConstants.CRAFTING_PIPE_SATELLITE_ID_ADVANCED, xCoord, yCoord, zCoord, i, advancedSatelliteIdArray[i]);
			MainProxy.sendPacketToPlayer(packet.getPacket(), (Player)player);
		}
	}

	public void setPrevSatellite(EntityPlayer player, int i) {
		if (MainProxy.isClient(player.worldObj)) {
			final PacketCoordinates packet = new PacketPipeInteger(NetworkConstants.CRAFTING_PIPE_PREV_SATELLITE_ADVANCED, xCoord, yCoord, zCoord, i);
			MainProxy.sendPacketToServer(packet.getPacket());
		} else {
			advancedSatelliteIdArray[i] = getNextConnectSatelliteId(true, i, -1);
			final PacketModuleInteger packet = new PacketModuleInteger(NetworkConstants.CRAFTING_PIPE_SATELLITE_ID_ADVANCED, xCoord, yCoord, zCoord, i, advancedSatelliteIdArray[i]);
			MainProxy.sendPacketToPlayer(packet.getPacket(), (Player)player);
		}
	}

	public void setParentPipe(
			PipeItemsCraftingLogistics pipeItemsCraftingLogistics) {
		_pipe=pipeItemsCraftingLogistics;
		
	}
}
