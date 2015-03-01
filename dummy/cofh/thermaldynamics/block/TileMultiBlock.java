package cofh.thermaldynamics.block;

import net.minecraft.tileentity.TileEntity;
import cofh.thermaldynamics.ducts.Duct;
import cofh.thermaldynamics.multiblock.IMultiBlock;
import cofh.thermaldynamics.multiblock.MultiBlockGrid;

public class TileMultiBlock extends TileEntity implements IMultiBlock {
	
	public static enum NeighborTypes {}
	
	public static enum ConnectionTypes {}
	
	public Duct				duct;
	public boolean			isOutput;
	public boolean			isInput;
	public boolean			isNode;
	public MultiBlockGrid	myGrid;
	
	public boolean isBlockedSide(int paramInt) {
		throw new UnsupportedOperationException();
	}
	
	public boolean isSideConnected(byte ordinal) {
		throw new UnsupportedOperationException();
	}
	
	public TileEntity getAdjTileEntitySafe(int ordinal) {
		throw new UnsupportedOperationException();
	}
	
	public Duct getDuctType() {
		throw new UnsupportedOperationException();
	}
	
	public void blockPlaced() {
		throw new UnsupportedOperationException();
	}
	
	public void tickMultiBlock() {
		throw new UnsupportedOperationException();
	}
	
	public void handleSideUpdate(int paramInt) {
		throw new UnsupportedOperationException();
	}
	
	public IMultiBlock getConnectedSide(byte paramByte) {
		throw new UnsupportedOperationException();
	}
	
	public void onNeighborTileChange(int paramInt1, int paramInt2, int paramInt3) {
		throw new UnsupportedOperationException();
	}
	
	public void onNeighborBlockChange() {
		throw new UnsupportedOperationException();
	}
}
