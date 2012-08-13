package net.minecraft.src.buildcraft.krapht.logic;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import net.minecraft.src.Block;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.buildcraft.krapht.IRequireReliableTransport;
import net.minecraft.src.buildcraft.krapht.LogisticsManager;
import net.minecraft.src.buildcraft.krapht.LogisticsRequest;
import net.minecraft.src.buildcraft.krapht.RoutedPipe;
import net.minecraft.src.buildcraft.krapht.network.NetworkConstants;
import net.minecraft.src.buildcraft.krapht.network.PacketCoordinates;
import net.minecraft.src.buildcraft.krapht.network.TileNetworkData;
import net.minecraft.src.buildcraft.krapht.routing.IRouter;
import net.minecraft.src.buildcraft.krapht.routing.Router;
import net.minecraft.src.krapht.AdjacentTile;
import net.minecraft.src.krapht.InventoryUtil;
import net.minecraft.src.krapht.InventoryUtilFactory;
import net.minecraft.src.krapht.ItemIdentifier;
import net.minecraft.src.krapht.SimpleInventory;
import net.minecraft.src.krapht.WorldUtil;
import net.minecraftforge.common.ISidedInventory;
import buildcraft.api.core.Orientations;
import buildcraft.core.CoreProxy;
import buildcraft.factory.TileAutoWorkbench;
import buildcraft.transport.TileGenericPipe;

public abstract class BaseLogicCrafting extends BaseRoutingLogic implements IRequireReliableTransport {

	protected SimpleInventory _dummyInventory = new SimpleInventory(10, "Requested items", 127);
	protected final InventoryUtilFactory _invUtilFactory;
	protected final InventoryUtil _dummyInvUtil;

	@TileNetworkData
	public int signEntityX = 0;
	@TileNetworkData
	public int signEntityY = 0;
	@TileNetworkData
	public int signEntityZ = 0;
	//public LogisticsTileEntiy signEntity;

	protected final LinkedList<ItemIdentifier> _lostItems = new LinkedList<ItemIdentifier>();

	@TileNetworkData
	public int satelliteId = 0;

	public BaseLogicCrafting() {
		this(new InventoryUtilFactory());
	}

	public BaseLogicCrafting(InventoryUtilFactory invUtilFactory) {
		_invUtilFactory = invUtilFactory;
		_dummyInvUtil = _invUtilFactory.getInventoryUtil(_dummyInventory);
		throttleTime = 40;
	}

	/* ** SATELLITE CODE ** */

	protected int getNextConnectSatelliteId(boolean prev) {
		final HashMap<Router, Orientations> routes = getRouter().getRouteTable();
		int closestIdFound = prev ? 0 : Integer.MAX_VALUE;
		for (final BaseLogicSatellite satellite : BaseLogicSatellite.AllSatellites) {
			if (routes.containsKey(satellite.getRouter())) {
				if (!prev && satellite.satelliteId > satelliteId && satellite.satelliteId < closestIdFound) {
					closestIdFound = satellite.satelliteId;
				} else if (prev && satellite.satelliteId < satelliteId && satellite.satelliteId > closestIdFound) {
					closestIdFound = satellite.satelliteId;
				}
			}
		}
		if (closestIdFound == Integer.MAX_VALUE) {
			return satelliteId;
		}

		return closestIdFound;

	}

	public void setNextSatellite() {
		satelliteId = getNextConnectSatelliteId(false);
	}

	public void setPrevSatellite() {
		satelliteId = getNextConnectSatelliteId(true);
	}

	public boolean isSatelliteConnected() {
		for (final BaseLogicSatellite satellite : BaseLogicSatellite.AllSatellites) {
			if (satellite.satelliteId == satelliteId) {
				if (getRouter().getRouteTable().containsKey(satellite.getRouter())) {
					return true;
				}
			}
		}
		return false;
	}

	public IRouter getSatelliteRouter() {
		for (final BaseLogicSatellite satellite : BaseLogicSatellite.AllSatellites) {
			if (satellite.satelliteId == satelliteId) {
				return satellite.getRouter();
			}
		}
		return null;
	}

	/* ** OTHER CODE ** */

	public int RequestsItem(ItemIdentifier item) {
		if (item == null) {
			return 0;
		}
		return _dummyInvUtil.getItemCount(item);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);
		_dummyInventory.readFromNBT(nbttagcompound, "");
		satelliteId = nbttagcompound.getInteger("satelliteid");
		signEntityX = nbttagcompound.getInteger("CraftingSignEntityX");
		signEntityY = nbttagcompound.getInteger("CraftingSignEntityY");
		signEntityZ = nbttagcompound.getInteger("CraftingSignEntityZ");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);
		_dummyInventory.writeToNBT(nbttagcompound, "");
		nbttagcompound.setInteger("satelliteid", satelliteId);
		
		nbttagcompound.setInteger("CraftingSignEntityX", signEntityX);
		nbttagcompound.setInteger("CraftingSignEntityY", signEntityY);
		nbttagcompound.setInteger("CraftingSignEntityZ", signEntityZ);
	}

	@Override
	public void destroy() {
		if(signEntityX != 0 && signEntityY != 0 && signEntityZ != 0) {
			worldObj.setBlockWithNotify(signEntityX, signEntityY, signEntityZ, 0);
			signEntityX = 0;
			signEntityY = 0;
			signEntityZ = 0;
		}
	}

	@Override
	public void onWrenchClicked(EntityPlayer entityplayer) {
	}

	@Override
	public void throttledUpdateEntity() {
		super.throttledUpdateEntity();
		if (_lostItems.isEmpty()) {
			return;
		}
		System.out.println("Item lost");
		final Iterator<ItemIdentifier> iterator = _lostItems.iterator();
		while (iterator.hasNext()) {
			final LogisticsRequest request = new LogisticsRequest(iterator.next(), 1, getRoutedPipe());
			if (LogisticsManager.Request(request, ((RoutedPipe) container.pipe).getRouter().getRoutersByCost(), null)) {
				iterator.remove();
			}
		}
	}

	@Override
	public void itemArrived(ItemIdentifier item) {
	}

	@Override
	public void itemLost(ItemIdentifier item) {
		_lostItems.add(item);
	}

	public void openAttachedGui(EntityPlayer player) {
		if (CoreProxy.isRemote()) {
			final PacketCoordinates packet = new PacketCoordinates(NetworkConstants.CRAFTING_PIPE_OPEN_CONNECTED_GUI, xCoord, yCoord, zCoord);
			CoreProxy.sendToServer(packet.getPacket());
		}
		final WorldUtil worldUtil = new WorldUtil(worldObj, xCoord, yCoord, zCoord);
		for (final AdjacentTile tile : worldUtil.getAdjacentTileEntities()) {
			if (tile.tile instanceof ISidedInventory) {
				Block block = worldObj.getBlockId(tile.tile.xCoord, tile.tile.yCoord, tile.tile.zCoord) < Block.blocksList.length ? Block.blocksList[worldObj.getBlockId(tile.tile.xCoord, tile.tile.yCoord, tile.tile.zCoord)] : null;
				if(block != null) {
					if(block.blockActivated(worldObj, tile.tile.xCoord, tile.tile.yCoord, tile.tile.zCoord, player)){
						break;
					}
				}
			}
			if (tile.tile instanceof IInventory && !(tile.tile instanceof TileGenericPipe)) {
				Block block = worldObj.getBlockId(tile.tile.xCoord, tile.tile.yCoord, tile.tile.zCoord) < Block.blocksList.length ? Block.blocksList[worldObj.getBlockId(tile.tile.xCoord, tile.tile.yCoord, tile.tile.zCoord)] : null;
				if(block != null) {
					if(block.blockActivated(worldObj, tile.tile.xCoord, tile.tile.yCoord, tile.tile.zCoord, player)){
						break;
					}
				}
			}
		}
	}

	public void importFromCraftingTable() {
		final WorldUtil worldUtil = new WorldUtil(worldObj, xCoord, yCoord, zCoord);
		new LinkedList<AdjacentTile>();
		TileAutoWorkbench bench = null;
		for (final AdjacentTile tile : worldUtil.getAdjacentTileEntities()) {
			if (!(tile.tile instanceof TileAutoWorkbench)) {
				continue;
			}
			bench = (TileAutoWorkbench) tile.tile;
			break;
		}
		if (bench == null) {
			return;
		}

		// Import
		for (int i = 0; i < bench.getSizeInventory(); i++) {
			if (i >= _dummyInventory.getSizeInventory() - 1) {
				break;
			}
			final ItemStack newStack = bench.getStackInSlot(i) == null ? null : bench.getStackInSlot(i).copy();
			_dummyInventory.setInventorySlotContents(i, newStack);
		}

		// Compact
		for (int i = 0; i < _dummyInventory.getSizeInventory() - 1; i++) {
			final ItemStack stackInSlot = _dummyInventory.getStackInSlot(i);
			if (stackInSlot == null) {
				continue;
			}
			final ItemIdentifier itemInSlot = ItemIdentifier.get(stackInSlot);
			for (int j = i + 1; j < _dummyInventory.getSizeInventory() - 1; j++) {
				final ItemStack stackInOtherSlot = _dummyInventory.getStackInSlot(j);
				if (stackInOtherSlot == null) {
					continue;
				}
				if (itemInSlot == ItemIdentifier.get(stackInOtherSlot)) {
					stackInSlot.stackSize += stackInOtherSlot.stackSize;
					_dummyInventory.setInventorySlotContents(j, null);
				}
			}
		}
		for (int i = 0; i < _dummyInventory.getSizeInventory() - 1; i++) {
			if (_dummyInventory.getStackInSlot(i) != null) {
				continue;
			}
			for (int j = i + 1; j < _dummyInventory.getSizeInventory() - 1; j++) {
				if (_dummyInventory.getStackInSlot(j) == null) {
					continue;
				}
				_dummyInventory.setInventorySlotContents(i, _dummyInventory.getStackInSlot(j));
				_dummyInventory.setInventorySlotContents(j, null);
				break;
			}
		}

		_dummyInventory.setInventorySlotContents(9, bench.findRecipe());
	}

	/* ** INTERFACE TO PIPE ** */
	public ItemStack getCraftedItem() {
		return _dummyInventory.getStackInSlot(9);
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
}
