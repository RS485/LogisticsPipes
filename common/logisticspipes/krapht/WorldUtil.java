/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.krapht;

import java.util.Iterator;
import java.util.LinkedList;

import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import buildcraft.api.core.Orientations;
import buildcraft.api.core.Position;

public class WorldUtil {
	private int _x;
	private int _y;
	private int _z;
	
	private World _worldObj;
	
	public WorldUtil(World worldObj, int x, int y, int z) {
		this._worldObj = worldObj;
		this._x = x;
		this._y = y;
		this._z = z;
	}

	public LinkedList<AdjacentTile> getAdjacentTileEntities() {
		// TODO Auto-generated method stub
		LinkedList<AdjacentTile> foundTiles = new LinkedList<AdjacentTile>();
		for (Orientations o : Orientations.values()){
			if (o == Orientations.Unknown) continue;
			Position p = new Position(_x, _y, _z, o);
			p.moveForwards(1);
			TileEntity tile = _worldObj.getBlockTileEntity((int)p.x, (int)p.y, (int)p.z);
			
			if (tile == null) continue;
			foundTiles.add(new AdjacentTile(tile, o));
		}
		return foundTiles;
	}
	
	public TileEntity getAdjecentTile(Orientations direction){
		Position pos = new Position(_x, _y, _z, direction);
		pos.moveForwards(1.0);
		return _worldObj.getBlockTileEntity((int)pos.x, (int)pos.y, (int)pos.z);
	}
}
