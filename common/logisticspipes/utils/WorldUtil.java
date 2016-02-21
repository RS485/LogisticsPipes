/**
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.utils;

import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.tuples.LPPosition;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;


import java.util.LinkedList;

public class WorldUtil {

	private int _x;
	private int _y;
	private int _z;
	private BlockPos _pos;

	private World _worldObj;

	public WorldUtil(World worldObj, BlockPos pos) {
		_worldObj = worldObj;
		_x = pos.getX();
		_y = pos.getY();
		_z = pos.getZ();
	}

	public WorldUtil(TileEntity tile) {
		_worldObj = tile.getWorld();
		TileEntity getpos =_worldObj.getTileEntity(tile.getPos());
	}



	public LinkedList<AdjacentTile> getAdjacentTileEntities() {
		return getAdjacentTileEntities(false);
	}

	public LinkedList<AdjacentTile> getAdjacentTileEntities(boolean flag) {
		LinkedList<AdjacentTile> foundTiles = new LinkedList<AdjacentTile>();
		TileEntity tilePipe = null;
		if (flag) {
			tilePipe = _worldObj.getTileEntity(BlockPos.ORIGIN);
		}
		for (EnumFacing o : EnumFacing.values()) {

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

	public TileEntity getAdjacentTileEntitie(EnumFacing direction) {
		LPPosition p = new LPPosition(_x, _y, _z);
		p.moveForward(direction);
		return p.getTileEntity(_worldObj);
	}
}
