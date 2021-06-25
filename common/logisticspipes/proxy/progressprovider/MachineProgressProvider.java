package logisticspipes.proxy.progressprovider;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.tileentity.TileEntity;

import logisticspipes.api.IProgressProvider;
import logisticspipes.proxy.interfaces.IGenericProgressProvider;

public class MachineProgressProvider {

	private List<IGenericProgressProvider> providers = new ArrayList<>();

	public void registerProgressProvider(IGenericProgressProvider provider) {
		providers.add(provider);
	}

	public byte getProgressForTile(TileEntity tile) {
		if (tile instanceof IProgressProvider) {
			return ((IProgressProvider) tile).getProgress();
		}
		for (IGenericProgressProvider provider : providers) {
			if (provider.isType(tile)) {
				return provider.getProgress(tile);
			}
		}
		return 0;
	}
}
