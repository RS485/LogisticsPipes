package logisticspipes.utils;

public final class SinkReply {
	
	public enum FixedPriority {
		DefaultRoute,
		ModBasedItemSink,
		OreDictItemSink,
		Terminus,
		APIARIST_BeeSink,
		APIARIST_Analyser,
		ItemSink,
		PassiveSupplier,
		ElectricBuffer,
		ElectricManager,
	}
	
	public final FixedPriority fixedPriority;
	public final int customPriority;
	public final boolean isPassive;
	public final boolean isDefault;
	public final int energyUse;
	public final int maxNumberOfItems;
	
	public SinkReply(FixedPriority fixedPriority, int customPriority, boolean isPassive, boolean isDefault, int energyUse, int maxNumberOfItems) {
		this.fixedPriority = fixedPriority;
		this.customPriority = customPriority;
		this.isPassive = isPassive;
		this.isDefault = isDefault;
		this.energyUse = energyUse;
		this.maxNumberOfItems = maxNumberOfItems;
	}
	
	public SinkReply(SinkReply base, int maxNumberOfItems)
	{
		this.fixedPriority = base.fixedPriority;
		this.customPriority = base.customPriority;
		this.isPassive = base.isPassive;
		this.isDefault = base.isDefault;
		this.energyUse = base.energyUse;
		this.maxNumberOfItems = maxNumberOfItems;
	}
}
