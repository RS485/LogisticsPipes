package logisticspipes.proxy.td;

import logisticspipes.logisticspipes.IRoutedItem.TransportMode;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.routing.ItemRoutingInformation;
import logisticspipes.transport.LPTravelingItem.LPTravelingItemServer;
import logisticspipes.utils.item.ItemIdentifierStack;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import cofh.thermaldynamics.ducts.Duct;
import cofh.thermaldynamics.ducts.TDDucts;
import cofh.thermaldynamics.ducts.item.TileItemDuct;
import cofh.thermaldynamics.ducts.item.TravelingItem;
import cofh.thermaldynamics.multiblock.IMultiBlock;

public class LPItemDuct extends TileItemDuct {
	
	public final LogisticsTileGenericPipe pipe;
	public final ForgeDirection dir;
	
	public LPItemDuct(LogisticsTileGenericPipe pipe, ForgeDirection orientation) {
		this.pipe = pipe;
		this.dir = orientation;
	}

	@Override
	public RouteInfo canRouteItem(ItemStack arg0) {
		return noRoute;
	}

	@Override
	public void transferItem(TravelingItem item) {
		ItemRoutingInformation info = (ItemRoutingInformation) item.lpRoutingInformation;
		info.setItem(ItemIdentifierStack.getFromStack(item.stack));
		LPTravelingItemServer lpItem = new LPTravelingItemServer(info);
		lpItem.setSpeed(info._transportMode == TransportMode.Active ? 0.3F : 0.2F);
		pipe.pipe.transport.injectItem(lpItem, ForgeDirection.getOrientation(item.direction));
	}

	@Override
	public boolean isBlockedSide(int paramInt) {
		return isLPBlockedSide(paramInt, false);
	}

	public boolean isLPBlockedSide(int paramInt, boolean ignoreSystemDisconnect) {
		ForgeDirection dir = ForgeDirection.getOrientation(paramInt);
		if(pipe.tilePart.hasBlockingPluggable(dir)) return true;
		if(pipe.pipe != null && pipe.pipe.isSideBlocked(dir, ignoreSystemDisconnect)) {
			return false;
		}
		return super.isBlockedSide(paramInt);
	}
	
	public Duct getDuctType() {
		if(this.duct == null) {
			this.duct = TDDucts.itemBasic;
		}
		return this.duct;
	}
	
	@Override
	public void handleSideUpdate(int paramInt) {
		super.handleSideUpdate(paramInt);
		this.isOutput = true;
	}

	@Override
	public IMultiBlock getConnectedSide(byte paramByte) {
		if(ForgeDirection.getOrientation(paramByte) != dir) {
			return null;
		}
		return super.getConnectedSide(paramByte);
	}

	@Override
	public NeighborTypes getCachedSideType(byte paramByte) {
		if(ForgeDirection.getOrientation(paramByte) != dir) {
			return null;
		}
		return super.getCachedSideType(paramByte);
	}

	@Override
	public ConnectionTypes getConnectionType(byte paramByte) {
		if(ForgeDirection.getOrientation(paramByte) != dir) {
			return null;
		}
		return super.getConnectionType(paramByte);
	}

	@Override
	public IMultiBlock getCachedTile(byte paramByte) {
		if(ForgeDirection.getOrientation(paramByte) != dir) {
			return null;
		}
		return super.getCachedTile(paramByte);
	}

	@Override
	public TileEntity getAdjTileEntitySafe(int ordinal) {
		if(ForgeDirection.getOrientation(ordinal) != dir) {
			return null;
		}
		return super.getAdjTileEntitySafe(ordinal);
	}
}
