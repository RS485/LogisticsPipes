package thermalexpansion.part.conduit;

import codechicken.multipart.TileMultipart;
import net.minecraft.tileentity.TileEntity;

public abstract class ConduitBase implements IConduit {
	public static enum CacheTypes {
		NOT_SET, IMPORTANT, IMPORTANT2;
		
		private CacheTypes() {}
	}
	public byte			sideType[]	= { 0, 0, 0, 0, 0, 0 };
	public byte			sideMode[]	= { 1, 1, 1, 1, 1, 1 };
	public boolean		isNode;
	public Grid			gridBase;
    public CacheTypes cacheType[];
	public boolean		isInput;
	public boolean		isOutput;
	public boolean		hasServo;
	public ConduitBase	conduitCache[];
	public boolean		hasBeenTransversed;
	public boolean		isConduitDead;
	public byte			conduitType;
	public byte			internal_mode;
	public byte			sideTracker;
	public TileEntity getTile() {return null;}
	public ConduitBase getConduit() {return null;}
	public boolean passOcclusionTest(int side) {return false;}
	public void onNeighborChanged() {}
	public boolean theyPassOcclusionTest(IConduit them, int side) {return false;}
	public void cacheConduit(TileEntity theTile, int side) {}
	public TileMultipart tile() {return null;}
	public void onChunkUnload() {}
	public void onRemoved() {}
	public void tileUnloading() {}
}
