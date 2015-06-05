package logisticspipes.proxy.binnie;

import java.lang.reflect.Field;

import logisticspipes.proxy.interfaces.IBinnieProxy;

import net.minecraft.tileentity.TileEntity;

import binnie.core.machines.Machine;
import binnie.core.machines.TileEntityMachine;
import binnie.genetics.machine.Analyser;
import lombok.SneakyThrows;

public class BinnieProxy implements IBinnieProxy {

	private Field machine = null;
	private Field machinePackage = null;

	@Override
	@SneakyThrows(Exception.class)
	public boolean isTileAnalyser(TileEntity tile) {
		if (tile instanceof TileEntityMachine) {
			if (machine == null) {
				machine = TileEntityMachine.class.getDeclaredField("machine");
				machine.setAccessible(true);
				machinePackage = Machine.class.getDeclaredField("machinePackage");
				machinePackage.setAccessible(true);
			}
			Object m = machine.get(tile);
			Object mP = machinePackage.get(m);
			if (mP instanceof Analyser.PackageAnalyser) {
				return true;
			}
		}
		return false;
	}

}
