package logisticspipes.asm.wrapper;

import net.minecraft.block.entity.BlockEntity;

import logisticspipes.proxy.interfaces.IGenericProgressProvider;

public class GenericProgressProviderWrapper extends AbstractWrapper implements IGenericProgressProvider {

	private IGenericProgressProvider provider;
	private final String name;

	GenericProgressProviderWrapper(IGenericProgressProvider provider, String name) {
		this.provider = provider;
		this.name = name;
	}

	@Override
	public boolean isType(BlockEntity tile) {
		if (isEnabled()) {
			try {
				return provider.isType(tile);
			} catch (Exception | NoClassDefFoundError e) {
				handleException(e);
			}
		}
		return false;
	}

	@Override
	public byte getProgress(BlockEntity tile) {
		if (isEnabled()) {
			try {
				return provider.getProgress(tile);
			} catch (Exception | NoClassDefFoundError e) {
				handleException(e);
			}
		}
		return 0;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getTypeName() {
		return "ProgressProvider";
	}
}
