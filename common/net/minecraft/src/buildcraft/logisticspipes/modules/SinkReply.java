package net.minecraft.src.buildcraft.logisticspipes.modules;

public class SinkReply {
	
	public enum FixedPriority {
		DefaultRoute,
		Terminus,
		ItemSink,
		PassiveSupplier
	}
	
	public FixedPriority fixedPriority;
	public int customPriority;
	public boolean isPassive;
	public boolean isDefault;
	public int maxNumberOfItems;
}
