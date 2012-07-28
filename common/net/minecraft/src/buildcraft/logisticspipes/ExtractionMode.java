package net.minecraft.src.buildcraft.logisticspipes;

public enum ExtractionMode {
	Normal,
	LeaveFirst,
	LeaveLast,
	LeaveFirstAndLast,
	Leave1PerStack;

	public ExtractionMode next() {
		int next = this.ordinal() + 1;
		
		if (next >= this.values().length){
			next = 0;
		}
		return ExtractionMode.values()[next];
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
			default:
				return "Unknown!";
		}
	}
	
}
