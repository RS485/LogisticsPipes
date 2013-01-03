package logisticspipes.pipes;

import java.util.HashMap;
import java.util.Map;

import logisticspipes.interfaces.routing.ILiquidProvider;
import logisticspipes.interfaces.routing.IRequestLiquid;
import logisticspipes.pipes.basic.liquid.LiquidRoutedPipe;
import logisticspipes.request.LiquidRequest;
import logisticspipes.request.RequestTreeNode;
import logisticspipes.routing.LiquidLogisticsPromise;
import logisticspipes.routing.LogisticsPromise;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import logisticspipes.utils.AdjacentTile;
import logisticspipes.utils.LiquidIdentifier;
import logisticspipes.utils.WorldUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.liquids.ILiquidTank;
import net.minecraftforge.liquids.ITankContainer;
import buildcraft.transport.PipeTransportLiquids;
import buildcraft.transport.TileGenericPipe;

public class PipeLiquidProvider extends LiquidRoutedPipe implements ILiquidProvider {

	public PipeLiquidProvider(int itemID) {
		super(itemID);
	}

	@Override
	public Map<LiquidIdentifier, Integer> getAvailableLiquids() {
		Map<LiquidIdentifier, Integer> map = new HashMap<LiquidIdentifier, Integer>();
		WorldUtil wUtil = new WorldUtil(worldObj, xCoord, yCoord, zCoord);
		for(AdjacentTile aTile:wUtil.getAdjacentTileEntities(true)) {
			if(aTile.tile instanceof TileGenericPipe) continue;
			if(!(aTile.tile instanceof ITankContainer)) continue;
			ILiquidTank[] tanks = ((ITankContainer)aTile.tile).getTanks(aTile.orientation.getOpposite());
			for(ILiquidTank tank:tanks) {
				if(tank.getLiquid() != null) {
					map.put(LiquidIdentifier.get(tank.getLiquid()), tank.getLiquid().amount);
				}
			}
		}
		return map;
	}
	
	@Override
	public boolean disconnectPipe(TileEntity tile) {
		return tile instanceof TileGenericPipe && ((TileGenericPipe)tile).pipe != null && ((TileGenericPipe)tile).pipe.transport instanceof PipeTransportLiquids;
	}
	
	@Override
	public TextureType getCenterTexture() {
		return Textures.LOGISTICSPIPE_LIQUID_PROVIDER;
	}

	@Override
	public void canProvide(LiquidRequest request) {
		
	}

	@Override
	public void fullFill(LiquidLogisticsPromise promise, IRequestLiquid destination) {
		
	}
}
