package logisticspipes.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.DelayQueue;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.routing.IRequireReliableTransport;
import logisticspipes.items.ItemUpgrade;
import logisticspipes.logistics.LogisticsManagerV2;
import logisticspipes.network.GuiIDs;
import logisticspipes.network.NetworkConstants;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.oldpackets.PacketCoordinates;
import logisticspipes.network.oldpackets.PacketGuiArgument;
import logisticspipes.network.oldpackets.PacketModuleInteger;
import logisticspipes.network.oldpackets.PacketPipeInteger;
import logisticspipes.network.packets.cpipe.CPipeNextSatellite;
import logisticspipes.network.packets.cpipe.CPipePrevSatellite;
import logisticspipes.network.packets.cpipe.CPipeSatelliteId;
import logisticspipes.network.packets.cpipe.CPipeSatelliteImport;
import logisticspipes.network.packets.cpipe.CPipeSatelliteImportBack;
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
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.LiquidIdentifier;
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

	protected SimpleInventory _dummyInventory = new SimpleInventory(11, "Requested items", 127);
	protected SimpleInventory _liquidInventory = new SimpleInventory(ItemUpgrade.MAX_LIQUID_CRAFTER, "Liquid items", 1);
	
	@TileNetworkData(staticSize=ItemUpgrade.MAX_LIQUID_CRAFTER)
	protected int[] amount = new int[ItemUpgrade.MAX_LIQUID_CRAFTER];
	@TileNetworkData(staticSize=ItemUpgrade.MAX_LIQUID_CRAFTER)
	public int liquidSatelliteIdArray[] = new int[ItemUpgrade.MAX_LIQUID_CRAFTER];
	@TileNetworkData
	public int liquidSatelliteId = 0;

	
	@TileNetworkData
	public int signEntityX = 0;
	@TileNetworkData
	public int signEntityY = 0;
	@TileNetworkData
	public int signEntityZ = 0;
	//public LogisticsTileEntiy signEntity;

	@TileNetworkData(staticSize=6)
	public boolean[] craftingSigns = new boolean[6];
	
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
	protected int getNextConnectSatelliteId(boolean prev, int x) {
		int closestIdFound = prev ? 0 : Integer.MAX_VALUE;
		for (final BaseLogicSatellite satellite : BaseLogicSatellite.AllSatellites) {
			CoreRoutedPipe satPipe = satellite.getRoutedPipe();
			if(satPipe == null || satPipe.stillNeedReplace() || satPipe.getRouter() == null || satPipe.isLiquidPipe()) continue;
			IRouter satRouter = satPipe.getRouter();
			ExitRoute route = getRoutedPipe().getRouter().getDistanceTo(satRouter);
			if(route != null) {
				if(x == -1) {
					if (!prev && satellite.satelliteId > satelliteId && satellite.satelliteId < closestIdFound) {
						closestIdFound = satellite.satelliteId;
					} else if (prev && satellite.satelliteId < satelliteId && satellite.satelliteId > closestIdFound) {
						closestIdFound = satellite.satelliteId;
					}
				} else {
					if (!prev && satellite.satelliteId > advancedSatelliteIdArray[x] && satellite.satelliteId < closestIdFound) {
						closestIdFound = satellite.satelliteId;
					} else if (prev && satellite.satelliteId < advancedSatelliteIdArray[x] && satellite.satelliteId > closestIdFound) {
						closestIdFound = satellite.satelliteId;
					}
				}
			}
		}
		if (closestIdFound == Integer.MAX_VALUE) {
			if(x == -1) {
				return satelliteId;
			} else {
				return advancedSatelliteIdArray[x];
			}
		}
		return closestIdFound;
	}
	
	protected int getNextConnectLiquidSatelliteId(boolean prev, int x) {
		int closestIdFound = prev ? 0 : Integer.MAX_VALUE;
		for (final BaseLogicLiquidSatellite satellite : BaseLogicLiquidSatellite.AllSatellites) {
			CoreRoutedPipe satPipe = satellite.getRoutedPipe();
			if(satPipe == null || satPipe.stillNeedReplace() || satPipe.getRouter() == null || !satPipe.isLiquidPipe()) continue;
			IRouter satRouter = satPipe.getRouter();
			ExitRoute route = getRoutedPipe().getRouter().getDistanceTo(satRouter);
			if(route != null) {
				if(x == -1) {
					if (!prev && satellite.satelliteId > liquidSatelliteId && satellite.satelliteId < closestIdFound) {
						closestIdFound = satellite.satelliteId;
					} else if (prev && satellite.satelliteId < liquidSatelliteId && satellite.satelliteId > closestIdFound) {
						closestIdFound = satellite.satelliteId;
					}
				} else {
					if (!prev && satellite.satelliteId > liquidSatelliteIdArray[x] && satellite.satelliteId < closestIdFound) {
						closestIdFound = satellite.satelliteId;
					} else if (prev && satellite.satelliteId < liquidSatelliteIdArray[x] && satellite.satelliteId > closestIdFound) {
						closestIdFound = satellite.satelliteId;
					}
				}
			}
		}
		if (closestIdFound == Integer.MAX_VALUE) {
			if(x == -1) {
				return liquidSatelliteId;
			} else {
				return liquidSatelliteIdArray[x];
			}
		}
		return closestIdFound;
	}

	public void setNextSatellite(EntityPlayer player) {
		if (MainProxy.isClient(player.worldObj)) {
			final CoordinatesPacket packet = PacketHandler.getPacket(CPipeNextSatellite.class).setPosX(xCoord).setPosY(yCoord).setPosZ(zCoord);
			MainProxy.sendPacketToServer(packet.getPacket());
		} else {
			satelliteId = getNextConnectSatelliteId(false, -1);
			final CoordinatesPacket packet = PacketHandler.getPacket(CPipeSatelliteId.class).setPipeId(satelliteId).setPosX(xCoord).setPosY(yCoord).setPosZ(zCoord);
			MainProxy.sendPacketToPlayer(packet.getPacket(), (Player)player);
		}

	}
	
	// This is called by the packet PacketCraftingPipeSatelliteId
	public void setSatelliteId(int satelliteId, int x) {
		if(x == -1) {
			this.satelliteId = satelliteId;
		} else {
			advancedSatelliteIdArray[x] = satelliteId;
		}
	}

	public void setPrevSatellite(EntityPlayer player) {
		if (MainProxy.isClient(player.worldObj)) {
			final CoordinatesPacket packet = PacketHandler.getPacket(CPipePrevSatellite.class).setPosX(xCoord).setPosY(yCoord).setPosZ(zCoord);
			MainProxy.sendPacketToServer(packet.getPacket());
		} else {
			satelliteId = getNextConnectSatelliteId(true, -1);
			final CoordinatesPacket packet = PacketHandler.getPacket(CPipeSatelliteId.class).setPipeId(satelliteId).setPosX(xCoord).setPosY(yCoord).setPosZ(zCoord);
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
		//TODO check for LiquidCrafter
		return false;
	}

	public IRouter getSatelliteRouter(int x) {
		if(x == -1) {
			for (final BaseLogicSatellite satellite : BaseLogicSatellite.AllSatellites) {
				if (satellite.satelliteId == satelliteId) {
					CoreRoutedPipe satPipe = satellite.getRoutedPipe();
					if(satPipe == null || satPipe.stillNeedReplace() || satPipe.getRouter() == null)
						continue;
					return satPipe.getRouter();
				}
			}
		} else {
			for (final BaseLogicSatellite satellite : BaseLogicSatellite.AllSatellites) {
				if (satellite.satelliteId == advancedSatelliteIdArray[x]) {
					CoreRoutedPipe satPipe = satellite.getRoutedPipe();
					if(satPipe == null || satPipe.stillNeedReplace() || satPipe.getRouter() == null)
						continue;
					return satPipe.getRouter();
				}
			}
		}
		return null;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);
		_dummyInventory.readFromNBT(nbttagcompound, "");
		_liquidInventory.readFromNBT(nbttagcompound, "LiquidInv");
		satelliteId = nbttagcompound.getInteger("satelliteid");
		signEntityX = nbttagcompound.getInteger("CraftingSignEntityX");
		signEntityY = nbttagcompound.getInteger("CraftingSignEntityY");
		signEntityZ = nbttagcompound.getInteger("CraftingSignEntityZ");
		
		priority = nbttagcompound.getInteger("priority");
		for(int i=0;i<9;i++) {
			advancedSatelliteIdArray[i] = nbttagcompound.getInteger("advancedSatelliteId" + i);
		}
		for(int i=0;i<6;i++) {
			craftingSigns[i] = nbttagcompound.getBoolean("craftingSigns" + i);
		}
		if(nbttagcompound.hasKey("LiquidAmount")) {
			amount = nbttagcompound.getIntArray("LiquidAmount");
		}
		if(amount.length < ItemUpgrade.MAX_LIQUID_CRAFTER) {
			amount = new int[ItemUpgrade.MAX_LIQUID_CRAFTER];
		}
		for(int i=0;i<ItemUpgrade.MAX_LIQUID_CRAFTER;i++) {
			liquidSatelliteIdArray[i] = nbttagcompound.getInteger("liquidSatelliteIdArray" + i);
		}
		for(int i=0;i<ItemUpgrade.MAX_LIQUID_CRAFTER;i++) {
			liquidSatelliteIdArray[i] = nbttagcompound.getInteger("liquidSatelliteIdArray" + i);
		}
		liquidSatelliteId = nbttagcompound.getInteger("liquidSatelliteId");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);
		_dummyInventory.writeToNBT(nbttagcompound, "");
		_liquidInventory.writeToNBT(nbttagcompound, "LiquidInv");
		nbttagcompound.setInteger("satelliteid", satelliteId);
		
		nbttagcompound.setInteger("CraftingSignEntityX", signEntityX);
		nbttagcompound.setInteger("CraftingSignEntityY", signEntityY);
		nbttagcompound.setInteger("CraftingSignEntityZ", signEntityZ);
		
		nbttagcompound.setInteger("priority", priority);
		for(int i=0;i<9;i++) {
			nbttagcompound.setInteger("advancedSatelliteId" + i, advancedSatelliteIdArray[i]);
		}
		for(int i=0;i<6;i++) {
			nbttagcompound.setBoolean("craftingSigns" + i, craftingSigns[i]);
		}
		for(int i=0;i<ItemUpgrade.MAX_LIQUID_CRAFTER;i++) {
			nbttagcompound.setInteger("liquidSatelliteIdArray" + i, liquidSatelliteIdArray[i]);
		}
		nbttagcompound.setIntArray("LiquidAmount", amount);
		nbttagcompound.setInteger("liquidSatelliteId", liquidSatelliteId);
	}

	@Override
	public void destroy() {
		if(signEntityX != 0 && signEntityY != 0 && signEntityZ != 0) {
			//TODO not sure setBlockMetadataWithNotify(signEntityX, signEntityY, signEntityZ, 0, 0, 1);
			worldObj.setBlockMetadataWithNotify(signEntityX, signEntityY, signEntityZ, 0, 1);
			signEntityX = 0;
			signEntityY = 0;
			signEntityZ = 0;
		}
	}

	@Override
	public void onWrenchClicked(EntityPlayer entityplayer) {
		if (MainProxy.isServer(entityplayer.worldObj)) {
			MainProxy.sendPacketToPlayer(new PacketGuiArgument(GuiIDs.GUI_CRAFTINGPIPE_ID, new Object[]{((CoreRoutedPipe)this.container.pipe).getUpgradeManager().isAdvancedSatelliteCrafter(), ((CoreRoutedPipe)this.container.pipe).getUpgradeManager().getLiquidCrafter(), amount, ((CoreRoutedPipe)this.container.pipe).getUpgradeManager().hasByproductExtractor()}).getPacket(),  (Player) entityplayer);
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
				SinkReply reply = LogisticsManagerV2.canSink(_pipe.getRouter(), null, true, stack.getItem(), null, true,true);
				if(reply == null || reply.maxNumberOfItems <1) {
					lostItem = _lostItems.poll();
					//iterator.remove(); // if we have no space for this and nothing to do, don't bother re-requesting the item.
					continue;
				}
			}
			int received = RequestTree.requestPartial(stack, (CoreRoutedPipe) container.pipe);
			if(received < stack.stackSize) {
				stack.stackSize -= received;
				_lostItems.add(new DelayedGeneric<ItemIdentifierStack>(stack,5000));
			}
			lostItem = _lostItems.poll();
		}
	}

	@Override
	public void itemArrived(ItemIdentifierStack item) {
	}

	@Override
	public void itemLost(ItemIdentifierStack item) {
		_lostItems.add(new DelayedGeneric<ItemIdentifierStack>(item,5000));
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
			final CoordinatesPacket packet = PacketHandler.getPacket(CPipeSatelliteImport.class).setPosX(xCoord).setPosY(yCoord).setPosZ(zCoord);
			MainProxy.sendPacketToServer(packet.getPacket());
		} else{
			// Send inventory as packet
			final CoordinatesPacket packet = PacketHandler.getPacket(CPipeSatelliteImportBack.class).setInventory(_dummyInventory).setPosX(xCoord).setPosY(yCoord).setPosZ(zCoord);
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

	public ItemStack getByproductItem() {
		return _dummyInventory.getStackInSlot(10);
	}
	
	public ItemStack getMaterials(int slotnr) {
		return _dummyInventory.getStackInSlot(slotnr);
	}

	public LiquidIdentifier getLiquidMaterial(int slotnr) {
		ItemStack stack = _liquidInventory.getStackInSlot(slotnr);
		if(stack == null) return null;
		return ItemIdentifier.get(stack).getLiquidIdentifier();
	}

	/**
	 * Simply get the dummy inventory
	 * 
	 * @return the dummy inventory
	 */
	public SimpleInventory getDummyInventory() {
		return _dummyInventory;
	}

	public SimpleInventory getLiquidInventory() {
		return _liquidInventory;
	}
	
	public void setDummyInventorySlot(int slot, ItemStack itemstack) {
		_dummyInventory.setInventorySlotContents(slot, itemstack);
	}

	public void setNextSatellite(EntityPlayer player, int i) {
		if (MainProxy.isClient(player.worldObj)) {
			final PacketCoordinates packet = new PacketPipeInteger(NetworkConstants.CRAFTING_PIPE_NEXT_SATELLITE_ADVANCED, xCoord, yCoord, zCoord, i);
			MainProxy.sendPacketToServer(packet.getPacket());
		} else {
			advancedSatelliteIdArray[i] = getNextConnectSatelliteId(false, i);
			final PacketModuleInteger packet = new PacketModuleInteger(NetworkConstants.CRAFTING_PIPE_SATELLITE_ID_ADVANCED, xCoord, yCoord, zCoord, i, advancedSatelliteIdArray[i]);
			MainProxy.sendPacketToPlayer(packet.getPacket(), (Player)player);
		}
	}

	public void setPrevSatellite(EntityPlayer player, int i) {
		if (MainProxy.isClient(player.worldObj)) {
			final PacketCoordinates packet = new PacketPipeInteger(NetworkConstants.CRAFTING_PIPE_PREV_SATELLITE_ADVANCED, xCoord, yCoord, zCoord, i);
			MainProxy.sendPacketToServer(packet.getPacket());
		} else {
			advancedSatelliteIdArray[i] = getNextConnectSatelliteId(true, i);
			final PacketModuleInteger packet = new PacketModuleInteger(NetworkConstants.CRAFTING_PIPE_SATELLITE_ID_ADVANCED, xCoord, yCoord, zCoord, i, advancedSatelliteIdArray[i]);
			MainProxy.sendPacketToPlayer(packet.getPacket(), (Player)player);
		}
	}

	public void setParentPipe(PipeItemsCraftingLogistics pipeItemsCraftingLogistics) {
		_pipe=pipeItemsCraftingLogistics;
	}

	public void changeLiquidAmount(int change, int slot, EntityPlayer player) {
		if (MainProxy.isClient(player.worldObj)) {
			final PacketModuleInteger packet = new PacketModuleInteger(NetworkConstants.LIQUID_CRAFTING_PIPE_AMOUNT, xCoord, yCoord, zCoord, slot, change);
			MainProxy.sendPacketToServer(packet.getPacket());
		} else {
			amount[slot] += change;
			if(amount[slot] <= 0) {
				amount[slot] = 0;
			}
			final PacketModuleInteger packet = new PacketModuleInteger(NetworkConstants.LIQUID_CRAFTING_PIPE_AMOUNT, xCoord, yCoord, zCoord, slot, amount[slot]);
			MainProxy.sendPacketToPlayer(packet.getPacket(), (Player)player);
		}
	}

	public void setPrevLiquidSatellite(EntityPlayer player, int i) {
		if (MainProxy.isClient(player.worldObj)) {
			final PacketCoordinates packet = new PacketPipeInteger(NetworkConstants.LIQUID_CRAFTING_PIPE_PREV_SATELLITE_ADVANCED, xCoord, yCoord, zCoord, i);
			MainProxy.sendPacketToServer(packet.getPacket());
		} else {
			if(i == -1) {
				liquidSatelliteId = getNextConnectLiquidSatelliteId(true, i);
				final PacketModuleInteger packet = new PacketModuleInteger(NetworkConstants.LIQUID_CRAFTING_PIPE_SATELLITE_ID_ADVANCED, xCoord, yCoord, zCoord, i, liquidSatelliteId);
				MainProxy.sendPacketToPlayer(packet.getPacket(), (Player)player);
			} else {
				liquidSatelliteIdArray[i] = getNextConnectLiquidSatelliteId(true, i);
				final PacketModuleInteger packet = new PacketModuleInteger(NetworkConstants.LIQUID_CRAFTING_PIPE_SATELLITE_ID_ADVANCED, xCoord, yCoord, zCoord, i, liquidSatelliteIdArray[i]);
				MainProxy.sendPacketToPlayer(packet.getPacket(), (Player)player);
			}
		}
	}

	public void setNextLiquidSatellite(EntityPlayer player, int i) {
		if (MainProxy.isClient(player.worldObj)) {
			final PacketCoordinates packet = new PacketPipeInteger(NetworkConstants.LIQUID_CRAFTING_PIPE_NEXT_SATELLITE_ADVANCED, xCoord, yCoord, zCoord, i);
			MainProxy.sendPacketToServer(packet.getPacket());
		} else {
			if(i == -1) {
				liquidSatelliteId = getNextConnectLiquidSatelliteId(false, i);
				final PacketModuleInteger packet = new PacketModuleInteger(NetworkConstants.LIQUID_CRAFTING_PIPE_SATELLITE_ID_ADVANCED, xCoord, yCoord, zCoord, i, liquidSatelliteId);
				MainProxy.sendPacketToPlayer(packet.getPacket(), (Player)player);		
			} else {
				liquidSatelliteIdArray[i] = getNextConnectLiquidSatelliteId(false, i);
				final PacketModuleInteger packet = new PacketModuleInteger(NetworkConstants.LIQUID_CRAFTING_PIPE_SATELLITE_ID_ADVANCED, xCoord, yCoord, zCoord, i, liquidSatelliteIdArray[i]);
				MainProxy.sendPacketToPlayer(packet.getPacket(), (Player)player);
			}
		}
	}

	public void setLiquidAmount(int[] amount) {
		if(MainProxy.isClient(worldObj)) {
			this.amount = amount;
		}
	}

	public void defineLiquidAmount(int integer, int slot) {
		if(MainProxy.isClient(worldObj)) {
			amount[slot] = integer;
		}
	}
	
	public int[] getLiquidAmount() {
		return amount;
	}

	public void setLiquidSatelliteId(int integer, int slot) {
		if(slot == -1) {
			liquidSatelliteId = integer;
		} else {
			liquidSatelliteIdArray[slot] = integer;
		}	
	}

	public IRouter getLiquidSatelliteRouter(int x) {
		if(x == -1) {
			for (final BaseLogicLiquidSatellite satellite : BaseLogicLiquidSatellite.AllSatellites) {
				if (satellite.satelliteId == liquidSatelliteId) {
					CoreRoutedPipe satPipe = satellite.getRoutedPipe();
					if(satPipe == null || satPipe.stillNeedReplace() || satPipe.getRouter() == null)
						continue;
					return satPipe.getRouter();
				}
			}
		} else {
			for (final BaseLogicLiquidSatellite satellite : BaseLogicLiquidSatellite.AllSatellites) {
				if (satellite.satelliteId == liquidSatelliteIdArray[x]) {
					CoreRoutedPipe satPipe = satellite.getRoutedPipe();
					if(satPipe == null || satPipe.stillNeedReplace() || satPipe.getRouter() == null)
						continue;
					return satPipe.getRouter();
				}
			}
		}
		return null;
	}
}
