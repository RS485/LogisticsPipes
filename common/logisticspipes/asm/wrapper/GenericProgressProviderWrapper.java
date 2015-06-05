package logisticspipes.asm.wrapper;

import logisticspipes.proxy.interfaces.IGenericProgressProvider;

import net.minecraft.tileentity.TileEntity;

public class GenericProgressProviderWrapper extends AbstractWrapper implements IGenericProgressProvider {

	private IGenericProgressProvider provider;
	private final String name;

	GenericProgressProviderWrapper(IGenericProgressProvider provider, String name) {
		this.provider = provider;
		this.name = name;
	}

	@Override
	public boolean isType(TileEntity tile) {
		if (isEnabled()) {
			try {
				return provider.isType(tile);
			} catch (Exception e) {
				handleException(e);
			} catch (NoClassDefFoundError e) {
				handleException(e);
			}
		}
		return false;
	}

	@Override
	public byte getProgress(TileEntity tile) {
		if (isEnabled()) {
			try {
				return provider.getProgress(tile);
			} catch (Exception e) {
				handleException(e);
			} catch (NoClassDefFoundError e) {
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
