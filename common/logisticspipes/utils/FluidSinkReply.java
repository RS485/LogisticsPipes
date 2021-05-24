package logisticspipes.utils;

public final class FluidSinkReply {

	public enum FixedFluidPriority {
		TERMINUS,
		FLUID_SINK
	}

	public final FixedFluidPriority fixedPriority;
	public final long sinkAmount;

	public int getSinkAmountInt() {
		return Math.max(0, (int) Math.min(Integer.MAX_VALUE, sinkAmount));
	}

	public FluidSinkReply(FixedFluidPriority fixedPriority, long sinkAmount) {
		this.fixedPriority = fixedPriority;
		this.sinkAmount = sinkAmount;
	}
}
