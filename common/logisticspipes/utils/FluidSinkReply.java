package logisticspipes.utils;

public final class FluidSinkReply {
    public enum FixedFluidPriority{
        Terminus,
        ItemSink
    }

    public final FixedFluidPriority fixedFluidPriority;
    public final int sinkAmount;

    public FluidSinkReply(FixedFluidPriority fixedFluidPriority, int sinkAmount){
        this.fixedFluidPriority = fixedFluidPriority;
        this.sinkAmount = sinkAmount;
    }
}
