/**
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.utils;

import java.util.LinkedList;

import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.tuples.LPPosition;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import net.minecraftforge.common.util.ForgeDirection;

public class WorldUtil {

	private int _x;
	private int _y;
	private int _z;

	private World _worldObj;

	public WorldUtil(World worldObj, int x, int y, int z) {
		_worldObj = worldObj;
		_x = x;
		_y = y;
		_z = z;
	}

	public WorldUtil(TileEntity tile) {
		_worldObj = tile.getWorldObj();
		_x = tile.xCoord;
		_y = tile.yCoord;
		_z = tile.zCoord;
	}

	public LinkedList<AdjacentTile> getAdjacentTileEntities() {
		return getAdjacentTileEntities(false);
	}

	public LinkedList<AdjacentTile> getAdjacentTileEntities(boolean flag) {
		LinkedList<AdjacentTile> foundTiles = new LinkedList<AdjacentTile>();
		TileEntity tilePipe = null;
		if (flag) {
			tilePipe = _worldObj.getTileEntity(_x, _y, _z);
		}
		for (ForgeDirection o : ForgeDirection.values()) {
			if (o == ForgeDirection.UNKNOWN) {
				continue;
			}

			TileEntity tile = getAdjacentTileEntitie(o);

			if (tile == null) {
				continue;
			}

			if (flag) {
				if (SimpleServiceLocator.pipeInformationManager.isItemPipe(tilePipe)) {
					if (!MainProxy.checkPipesConnections(tilePipe, tile, o)) {
						continue;
					}
				}
			}
			foundTiles.add(new AdjacentTile(tile, o));
		}
		return foundTiles;
	}

	public TileEntity getAdjacentTileEntitie(ForgeDirection direction) {
		LPPosition p = new LPPosition(_x, _y, _z);
		p.moveForward(direction);
		return p.getTileEntity(_worldObj);
	}
}
