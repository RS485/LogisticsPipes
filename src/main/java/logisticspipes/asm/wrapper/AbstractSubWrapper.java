package logisticspipes.asm.wrapper;

import java.util.List;

public abstract class AbstractSubWrapper extends AbstractWrapper {

	private final AbstractWrapper originalWrapper;

	public AbstractSubWrapper(AbstractWrapper wrapper) {
		originalWrapper = wrapper;
	}

	@Override
	protected final boolean isEnabled() {
		return originalWrapper.isEnabled();
	}

	@Override
	protected final boolean canTryAnyway() {
		return originalWrapper.canTryAnyway();
	}

	@Override
	public void handleException(Throwable e) {
		originalWrapper.handleException(e);
	}

	@Override
	public String getName() {
		return originalWrapper.getName();
	}

	@Override
	public String getTypeName() {
		return originalWrapper.getTypeName();
	}

	@Override
	public WrapperState getState() {
		return originalWrapper.getState();
	}

	@Override
	public Throwable getReason() {
		return originalWrapper.getReason();
	}

	@Override
	public String getModId() {
		return originalWrapper.getModId();
	}

	@Override
	public List<Class<?>> getWrapperInterfaces() {
		return originalWrapper.getWrapperInterfaces();
	}

	@Override
	public void reEnable() {
		originalWrapper.reEnable();
	}
}
