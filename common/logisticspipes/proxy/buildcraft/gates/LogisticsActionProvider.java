package logisticspipes.proxy.buildcraft.gates;

import java.util.Collection;

import net.minecraft.tileentity.TileEntity;

import net.minecraft.util.EnumFacing;

import buildcraft.api.statements.IActionExternal;
import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.IActionProvider;
import buildcraft.api.statements.IStatementContainer;

public class LogisticsActionProvider implements IActionProvider {

	@Override
	public Collection<IActionInternal> getInternalActions(IStatementContainer container) {
		return null;
		//Allow routing without active gate for now.
		/*
		LinkedList<IActionInternal> result = new LinkedList<IActionInternal>();
		TileEntity tile = container.getTile();

		if (!(tile instanceof LPBCTileGenericPipe)) {
			return result;
		}

		LogisticsTileGenericPipe lpPipe = ((LPBCTileGenericPipe)tile).getLpPipe();

		if(lpPipe.pipe == null) {
			return result;
		}

		ArrayList<DockingStation> stations = new ArrayList<DockingStation>();

		for (EnumFacing dir : EnumFacing.VALUES) {
			if (RobotUtils.getStation((IPipeTile) tile, dir) != null) {
				DockingStation station = RobotUtils.getStation((IPipeTile) tile, dir);
				if(!station.isTaken()) continue;
				if(!(station.robotTaking().getBoard() instanceof LogisticsRoutingBoardRobot)) continue;
				stations.add(station);
			}
		}

		if (stations.size() != 0 && lpPipe.pipe.isRoutedPipe()) {
			result.add(BuildCraftProxy.LogisticsRobotRoutingAction);
		}

		return result;
		 */
	}

	@Override
	public Collection<IActionExternal> getExternalActions(EnumFacing side, TileEntity tile) {
		return null;
	}

}
