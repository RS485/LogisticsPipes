package logisticspipes.logisticspipes;

import logisticspipes.utils.string.StringUtil;

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

	private static final String PREFIX = "misc.extractionmode.";

	public String getExtractionModeString() {
		return StringUtil.translate(PREFIX + this.name());
	}
}
