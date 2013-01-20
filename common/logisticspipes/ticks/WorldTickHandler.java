package logisticspipes.ticks;

import java.lang.reflect.Field;
import java.util.EnumSet;
import java.util.LinkedList;

import logisticspipes.LogisticsPipes;
import logisticspipes.proxy.MainProxy;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.BuildCraftTransport;
import buildcraft.api.core.Position;
import buildcraft.core.ITileBufferHolder;
import buildcraft.core.TileBuffer;
import buildcraft.transport.EntityData;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TileGenericPipe;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class WorldTickHandler implements ITickHandler {
	
	public static LinkedList<TileGenericPipe> clientPipesToReplace = new LinkedList<TileGenericPipe>();
	public static LinkedList<TileGenericPipe> serverPipesToReplace = new LinkedList<TileGenericPipe>();
	
	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) {}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) {
		LinkedList<TileGenericPipe> localList;
		if(MainProxy.isClient()) {
			localList = clientPipesToReplace;
		} else {
			localList = serverPipesToReplace;
		}
		while(localList.size() > 0) {
			try {
				TileGenericPipe tile = localList.get(0);
				int x = tile.xCoord;
				int y = tile.yCoord;
				int z = tile.zCoord;
				World world = tile.worldObj;
				TileGenericPipe newTile = LogisticsPipes.logisticsTileGenericPipe.newInstance();
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
						for(EntityData entity:((PipeTransportItems)newTile.pipe.transport).travelingEntities.values()) {
							entity.item.setContainer(newTile);
						}
					}
				}
				
				for (ForgeDirection o : ForgeDirection.VALID_DIRECTIONS) {
					Position pos = new Position(newTile.xCoord, newTile.yCoord, newTile.zCoord, o);
					pos.moveForwards(1.0);

					newTile.tileBuffer[o.ordinal()] = new TileBuffer();
					newTile.tileBuffer[o.ordinal()].initialize(newTile.worldObj, (int) pos.x, (int) pos.y, (int) pos.z);
				}

				for (ForgeDirection o : ForgeDirection.VALID_DIRECTIONS) {
					TileEntity tileSide = newTile.getTile(o);

					if (tileSide instanceof ITileBufferHolder) {
						((ITileBufferHolder) tileSide).blockCreated(o, BuildCraftTransport.genericPipeBlock.blockID, newTile);
					}
				}
				
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			localList.remove(0);
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
