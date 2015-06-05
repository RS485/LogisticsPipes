package logisticspipes.asm.wrapper;

public abstract class AbstractSubWrapper {

	private final AbstractWrapper originalWrapper;

	public AbstractSubWrapper(AbstractWrapper wrapper) {
		originalWrapper = wrapper;
	}

	protected final boolean isEnabled() {
		return originalWrapper.isEnabled();
	}

	protected final boolean canTryAnyway() {
		return originalWrapper.canTryAnyway();
	}

	public void handleException(Throwable e) {
		originalWrapper.handleException(e);
	}
}
