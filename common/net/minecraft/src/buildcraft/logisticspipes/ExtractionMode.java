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
}
