package logisticspipes.proxy.enderio;

import java.util.ArrayList;
import java.util.List;

import logisticspipes.proxy.interfaces.IEnderIOProxy;
import net.minecraft.tileentity.TileEntity;
import crazypants.enderio.machine.hypercube.HyperCubeRegister;
import crazypants.enderio.machine.hypercube.TileHyperCube;
import crazypants.enderio.machine.hypercube.TileHyperCube.IoMode;
import crazypants.enderio.machine.hypercube.TileHyperCube.SubChannel;

public class EnderIOProxy implements IEnderIOProxy {

	@Override
	public boolean isHyperCube(TileEntity tile) {
		return tile instanceof TileHyperCube;
	}

	@Override
	public List<TileEntity> getConnectedHyperCubes(TileEntity tile) {
		List<TileHyperCube> cons = HyperCubeRegister.instance.getCubesForChannel(((TileHyperCube)tile).getChannel());
		List<TileEntity> tiles = new ArrayList<TileEntity>();
		for(TileHyperCube cube: cons) {
			if(cube != tile) {
				tiles.add(cube);
			}
		}
		return tiles;
	}

	@Override
	public boolean isSendAndReceive(TileEntity tile) {
		return IoMode.BOTH == ((TileHyperCube)tile).getModeForChannel(SubChannel.ITEM);
	}
	
	@Override
	public boolean isEnderIO() {
		return true;
	}
}
