package logisticspipes.proxy.progressprovider;

import java.util.ArrayList;
import java.util.List;

import logisticspipes.api.IProgressProvider;
import logisticspipes.proxy.interfaces.IGenericProgressProvider;

import net.minecraft.tileentity.TileEntity;

public class MachineProgressProvider {

	private List<IGenericProgressProvider> providers = new ArrayList<IGenericProgressProvider>();

	public void registerProgressProvider(IGenericProgressProvider provider) {
		providers.add(provider);
	}

	public byte getProgressForTile(TileEntity tile) {
		if (tile instanceof IProgressProvider) {
			return ((IProgressProvider) tile).getMachineProgressForLP();
		}
		for (IGenericProgressProvider provider : providers) {
			if (provider.isType(tile)) {
				return provider.getProgress(tile);
			}
		}
		return 0;
	}
}
