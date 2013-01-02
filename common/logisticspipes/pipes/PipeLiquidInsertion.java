package logisticspipes.pipes;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import logisticspipes.pipes.basic.liquid.LiquidRoutedPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import logisticspipes.transport.PipeLiquidTransportLogistics;
import logisticspipes.utils.Pair;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.LiquidStack;

public class PipeLiquidInsertion extends LiquidRoutedPipe {
	
	private List<Pair<UUID, Integer>> localJamList = new ArrayList<Pair<UUID, Integer>>();
	
	public PipeLiquidInsertion(int itemID) {
		super(itemID);
	}

	@Override
	public void updateEntity() {
		super.updateEntity();
		List<UUID> tempJamList = new ArrayList<UUID>();
		if(!localJamList.isEmpty()) {
			List<Pair<UUID, Integer>> toRemove = new ArrayList<Pair<UUID, Integer>>();
			for(Pair<UUID, Integer> part: localJamList) {
				part.setValue2(part.getValue2() - 1);
				if(part.getValue2() <= 0) {
					toRemove.add(part);
				} else {
					tempJamList.add(part.getValue1());
				}
			}
			if(!toRemove.isEmpty()) {
				localJamList.removeAll(toRemove);
			}
		}
		PipeLiquidTransportLogistics transport = (PipeLiquidTransportLogistics) this.transport;
		for(ForgeDirection dir:ForgeDirection.VALID_DIRECTIONS) {
			LiquidStack stack = transport.sideTanks[dir.ordinal()].getLiquid();
			if(stack == null) continue;
			Pair<UUID, Integer> result = SimpleServiceLocator.logisticsLiquidManager.getBestReply(stack, getRouter(), tempJamList);
			if(result == null || result.getValue1() == null || result.getValue2() == 0) continue;
			System.out.println();
		}
	}

	@Override
	public TextureType getCenterTexture() {
		return Textures.LOGISTICSPIPE_LIQUID_INSERTION;
	}
}