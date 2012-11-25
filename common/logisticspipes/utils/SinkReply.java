package logisticspipes.utils;

public class SinkReply {
	
	public enum FixedPriority {
		DefaultRoute,
		Terminus,
		APIARIST_Analyser,
		APIARIST_Refiller,
		ItemSink,
		APIARIST_BeeSink,
		PassiveSupplier,
	}
	
	public FixedPriority fixedPriority;
	public int customPriority;
	public boolean isPassive;
	public boolean isDefault;
	public int maxNumberOfItems;
}
