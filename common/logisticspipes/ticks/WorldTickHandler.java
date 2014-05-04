package logisticspipes.ticks;

import java.lang.reflect.Field;
import java.util.EnumSet;
import java.util.LinkedList;

import logisticspipes.LogisticsPipes;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.FluidIdentifier;
import logisticspipes.utils.item.ItemIdentifier;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import thermalexpansion.part.conduit.item.TravelingItem;
import buildcraft.BuildCraftTransport;
import buildcraft.core.ITileBufferHolder;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TileGenericPipe;

public class WorldTickHandler implements ITickHandler {
	
	public static LinkedList<TileGenericPipe> clientPipesToReplace = new LinkedList<TileGenericPipe>();
	public static LinkedList<TileGenericPipe> serverPipesToReplace = new LinkedList<TileGenericPipe>();
	
	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) {}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) {
		LinkedList<TileGenericPipe> localList;
		if(type.contains(TickType.CLIENT)) {
			localList = clientPipesToReplace;
		} else if(type.contains(TickType.SERVER)) {
			localList = serverPipesToReplace;
		} else {
			System.out.println("not client, not server ... what is " + type);
			return;
		}
		while(localList.size() > 0) {
			//try {
				TileGenericPipe tile = localList.get(0);
				int x = tile.xCoord;
				int y = tile.yCoord;
				int z = tile.zCoord;
				World world = tile.worldObj;

				//TE or its chunk might've gone away while we weren't looking
				TileEntity tilecheck = world.getTileEntity(x, y, z);
				if(tilecheck != tile) {
					localList.remove(0);
					continue;
				}

				TileGenericPipe newTile = new LogisticsTileGenericPipe();
				for(Field field:tile.getClass().getDeclaredFields()) {
					try {
						field.setAccessible(true);
						field.set(newTile, field.get(tile));
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
				}
				tile.pipe = null;
				world.setBlockTileEntity(x, y, z, newTile);
				if(newTile.pipe != null) {
					newTile.pipe.setTile(newTile);
					if(newTile.pipe.transport instanceof PipeTransportItems) {
						for(TravelingItem entity:((PipeTransportItems)newTile.pipe.transport).items) {
							entity.setContainer(newTile);
						}
					}
				}

				//getTile creates the TileCache as needed.
				for (ForgeDirection o : ForgeDirection.VALID_DIRECTIONS) {
					TileEntity tileSide = newTile.getTile(o);

					if (tileSide instanceof ITileBufferHolder) {
						((ITileBufferHolder) tileSide).blockCreated(o, BuildCraftTransport.genericPipeBlock.blockID, newTile);
					}
				}
				//newTile.scheduleNeighborChange();
			/*} catch (IllegalAccessException e) {
				e.printStackTrace();
			}*/
			localList.remove(0);
		}
		ItemIdentifier.tick();
		FluidIdentifier.initFromForge(true);
		if(type.contains(TickType.SERVER)) {
			HudUpdateTick.tick();
			SimpleServiceLocator.craftingPermissionManager.tick();
			if(LogisticsPipes.WATCHDOG) {
				Watchdog.tickServer();
			}
		} else {
			if(LogisticsPipes.WATCHDOG) {
				Watchdog.tickClient();
			}
		}
	}

	@Override
	public EnumSet<TickType> ticks() {
		return EnumSet.of(TickType.CLIENT, TickType.SERVER);
	}

	@Override
	public String getLabel() {
		return "LogisticsPipes WorldTick";
	}
}
