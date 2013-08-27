package logisticspipes.ticks;

import java.lang.reflect.Field;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

import logisticspipes.LogisticsPipes;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.FluidIdentifier;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.BuildCraftTransport;
import buildcraft.api.core.Position;
import buildcraft.core.ITileBufferHolder;
import buildcraft.core.TileBuffer;
import buildcraft.transport.TravelingItem;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TileGenericPipe;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class WorldTickHandler implements ITickHandler {
	
	public static LinkedList<TileGenericPipe> clientPipesToReplace = new LinkedList<TileGenericPipe>();
	public static LinkedList<TileGenericPipe> serverPipesToReplace = new LinkedList<TileGenericPipe>();
	private Field entitiesToLoad = null;
	private Field delayedEntitiesToLoad = null;
	
	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) {}

	@SuppressWarnings("unchecked")
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
/*		if(entitiesToLoad == null || delayedEntitiesToLoad == null) {
			try {
				entitiesToLoad = PipeTransportItems.class.getDeclaredField("entitiesToLoad");
				entitiesToLoad.setAccessible(true);
				delayedEntitiesToLoad = PipeTransportItems.class.getDeclaredField("delayedEntitiesToLoad");
				delayedEntitiesToLoad.setAccessible(true);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			}
		}*/
		while(localList.size() > 0) {
			//try {
				TileGenericPipe tile = localList.get(0);
				int x = tile.xCoord;
				int y = tile.yCoord;
				int z = tile.zCoord;
				World world = tile.worldObj;

				//TE or its chunk might've gone away while we weren't looking
				TileEntity tilecheck = world.getBlockTileEntity(x, y, z);
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
/*						for(TravelingItem entity:((List<TravelingItem>)entitiesToLoad.get(newTile.pipe.transport))) {
							entity.setContainer(newTile);
						}
						for(TravelingItem entity:((List<TravelingItem>)delayedEntitiesToLoad.get(newTile.pipe.transport))) {
							entity.setContainer(newTile);
						}*/
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
