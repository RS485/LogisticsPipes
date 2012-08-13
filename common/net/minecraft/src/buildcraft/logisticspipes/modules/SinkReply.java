package net.minecraft.src.buildcraft.logisticspipes.modules;

public class SinkReply {
	
	public enum FixedPriority {
		DefaultRoute,
		Terminus,
		ItemSink,
		APIARIST_BeeSink,
		PassiveSupplier,
		APIARIST_Analyser
	}
	
	public FixedPriority fixedPriority;
	public int customPriority;
	public boolean isPassive;
	public boolean isDefault;
	public int maxNumberOfItems;
}
