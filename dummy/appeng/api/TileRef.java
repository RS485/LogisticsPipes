package appeng.api;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import appeng.api.exceptions.AppEngTileMissingException;

public class TileRef<T> extends WorldCoord {
	
	//private int dimension;
	private World w;
	
	public TileRef( TileEntity gte ) {
		super( gte.xCoord, gte.yCoord, gte.zCoord );
		TileEntity te = gte;
		w = te.worldObj;
		if ( te.worldObj == null )
			throw new RuntimeException("Tile has no world.");
		//dimension = te.worldObj.provider.dimensionId;
	}
	
	@SuppressWarnings("unchecked")
	public T getTile() throws AppEngTileMissingException
	{
		//World w = DimensionManager.getWorld( dimension );
		if (w.getChunkFromBlockCoords( x, z ).isChunkLoaded )
		{
			TileEntity te = w.getBlockTileEntity( x, y, z );
			if ( te != null )
			{
				try
				{
					T ttt = (T) te; // I have no idea if this causes the exception or not...
					return ttt;
				}
				catch( ClassCastException err )
				{
					
				}
			}
		}
		throw new AppEngTileMissingException( w, x,y,z);
	}
			
};
