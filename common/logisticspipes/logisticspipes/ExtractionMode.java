package logisticspipes.logisticspipes;

public enum ExtractionMode {
	Normal,
	LeaveFirst,
	LeaveLast,
	LeaveFirstAndLast,
	Leave1PerStack,
	Leave1PerType;

	public ExtractionMode next() {
		int next = this.ordinal() + 1;
		
		if (next >= ExtractionMode.values().length){
			next = 0;
		}
		return ExtractionMode.values()[next];
	}
	
	public static ExtractionMode getMode(int id) {
		if (id >= 0 && id < values().length) {
			return values()[id];
		}
		return Normal;
	}

	public String getExtractionModeString() {
		switch(this){
			case Normal:
				return "Normal";
			case LeaveFirst:
				return "Leave 1st stack";
			case LeaveLast: 
				return "Leave last stack";
			case LeaveFirstAndLast:
				return "Leave first & last stack";
			case Leave1PerStack:
				return "Leave 1 item per stack";
			case Leave1PerType:
				return "Leave 1 item per type";
			default:
				return "Unknown!";
		}
	}
	
}
