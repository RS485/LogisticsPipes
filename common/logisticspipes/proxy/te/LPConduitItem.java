package logisticspipes.proxy.te;

import java.lang.reflect.Field;
import java.util.concurrent.Callable;

import logisticspipes.Configs;
import logisticspipes.logisticspipes.IRoutedItem.TransportMode;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.routing.IRouterManager;
import logisticspipes.routing.RoutedEntityItem;
import logisticspipes.routing.RoutedEntityItemSaveHandler;
import logisticspipes.ticks.QueuedTasks;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.SimpleStackInventory;
import logisticspipes.utils.tuples.LPPosition;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import thermalexpansion.part.conduit.ConduitBase;
import thermalexpansion.part.conduit.GridTickHandler;
import thermalexpansion.part.conduit.IConduit;
import thermalexpansion.part.conduit.item.ConduitItem;
import thermalexpansion.part.conduit.item.ItemRoute;
import buildcraft.api.core.Position;
import buildcraft.transport.BlockGenericPipe;
import buildcraft.transport.PipeTransportItems;

public class LPConduitItem extends ConduitItem {
	public static boolean dontCheckRoutes = false;
	
	private int tick=0;
	private LogisticsTileGenericPipe pipe;
	public SimpleStackInventory localConduitInv;
	private final int side;
	private final ForgeDirection dir;
	
	public LPConduitItem(LogisticsTileGenericPipe pipe, int side) {
		super((byte)0);
		if(!Configs.TE_PIPE_SUPPORT) {
			throw new RuntimeException("This shoudln't be used if the option is disabled");
		}
		this.pipe = pipe;
		this.side = side;
		this.dir = ForgeDirection.VALID_DIRECTIONS[side];
		setSides();
		QueuedTasks.queueTask(new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				GridTickHandler.tickConduitToAdd.add(LPConduitItem.this);
				return null;
			}
			
		});
	}

	private void setSides() {
		this.isOutput = true;
		this.isNode = true;
		try {
			Field cacheImportantF = ConduitItem.class.getDeclaredField("cacheImportant2");
			cacheImportantF.setAccessible(true);
			IInventory[] cacheImportant = (IInventory[]) cacheImportantF.get(this);
			cacheImportant[dir.getOpposite().ordinal()] = localConduitInv = new SimpleStackInventory(9 * 6, "Internal Inventory", 64);
			this.cacheType[dir.getOpposite().ordinal()] = ConduitBase.CacheTypes.IMPORTANT2;
			this.sideType[dir.getOpposite().ordinal()] = 2;
		} catch(NoSuchFieldException e) {
			throw new RuntimeException(e);
		} catch(SecurityException e) {
			throw new RuntimeException(e);
		} catch(IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch(IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void updateLPStatus() {
		if(tick++ == 5) {
			pipe.getWorld().markBlockForRenderUpdate(pipe.getX(), pipe.getY(), pipe.getZ());
		}
	}
	
	@Override
	public void onNeighborChanged() {
		if(MainProxy.isClient(pipe.worldObj)) return;
		boolean wasNode = false;
		LPPosition pos = new LPPosition((TileEntity)pipe);
		pos.moveForward(dir);
		TileEntity curTile = pos.getTileEntity(pipe.worldObj);
		int oldType = sideType[side];
		sideType[side] = 0;
		if(curTile != null) {
			if(isConduit(curTile)) {
				if(theyPassOcclusionTest((IConduit)curTile, side) && pipe.canTEConduitConnect(((IConduit)curTile).getConduit(), side)) {
					cacheConduit(curTile, side);
					sideType[side] = 1;
				}
			} else if(isImportant(curTile, side)) {
				cacheImportant(curTile, side);
				sideType[side] = 2;
				isNode = true;
			}
		}
		if(sideType[side] != oldType && (oldType == 1 || sideType[side] == 1)) {
			if(conduitCache[side].tile() != null) {
				conduitCache[side].onNeighborChanged();
			}
		}
		if(gridBase != null) {
			if(isNode && !wasNode) {
				gridBase.makeNode(this);
			} else if(!isNode && wasNode) {
				gridBase.makeConduit(this);
			}
		}
	}

	@Override
	public routeInfo canRouteItem(ItemStack stack, boolean isSelf, int maxTransferSize) {
		routeInfo route = new routeInfo();
		route.canRoute = true;
		route.stackSize = 0;
		route.side = 0;
		return route;
	}
	
	public routeInfo canRouteLPItem(ItemStack stack, NBTTagCompound data, ItemRoute aRoute) {
		if(dontCheckRoutes) {
			routeInfo route = new routeInfo();
			route.canRoute = true;
			route.stackSize = 0;
			route.side = 0;
			return route;
		} else {
			RoutedEntityItemSaveHandler handler = new RoutedEntityItemSaveHandler(null);
			handler.readFromNBT(data);
			IRouterManager rm = SimpleServiceLocator.routerManager;
			int destinationint = rm.getIDforUUID(handler.destinationUUID);
			ForgeDirection orientation = pipe.getRoutingPipe().getRouter().getExitFor(destinationint, handler.transportMode == TransportMode.Active, ItemIdentifier.get(stack));
			if((side == orientation.ordinal() || orientation == ForgeDirection.UNKNOWN) && !pipe.getRoutingPipe().getRouter().getId().equals(handler.destinationUUID)) {
				return noRoute;
			}
			routeInfo route = new routeInfo();
			route.canRoute = true;
			route.stackSize = 0;
			route.side = 0;
			return route;
		}
	}

	@Override
	public ItemStack insertItem(ForgeDirection from, ItemStack item) {
		if(MainProxy.isClient(pipe.worldObj)) return null;
		if(pipe.injectItem(item, true, from) == 0) {
			return item;
		} else {
			return null;
		}
	}

	@Override
	public void insertItem(thermalexpansion.part.conduit.item.TravelingItem travelingItem) {
		if(MainProxy.isClient(pipe.worldObj)) return;
		if(travelingItem.routedLPInfo == null) {
			insertItem(ForgeDirection.VALID_DIRECTIONS[travelingItem.direction].getOpposite(), travelingItem.stack);
		} else {
			ForgeDirection from = ForgeDirection.VALID_DIRECTIONS[travelingItem.direction].getOpposite();
			if (BlockGenericPipe.isValid(pipe.pipe) && pipe.pipe.transport instanceof PipeTransportItems) {
				Position itemPos = new Position(pipe.xCoord + 0.5, pipe.yCoord + 0.5, pipe.zCoord + 0.5, from.getOpposite());
				itemPos.moveBackwards(0.4);
				buildcraft.transport.TravelingItem pipedItem = new buildcraft.transport.TravelingItem(travelingItem.routedLPInfo.getInteger("LP_BC_TRAVELING_ID"));
				pipedItem.setPosition(itemPos.x, itemPos.y, itemPos.z);
				pipedItem.setItemStack(travelingItem.stack.copy());
				pipedItem.setContainer(pipe);
				RoutedEntityItem item = new RoutedEntityItem(pipedItem);
				item.loadFromNBT(travelingItem.routedLPInfo);
				item.refreshDestinationInformation();
				((PipeTransportItems) pipe.pipe.transport).injectItem(item, itemPos.orientation);
			}
		}
	}
}
