package logisticspipes.logisticspipes;

import logisticspipes.utils.string.StringUtils;

public enum ExtractionMode {
	Normal,
	LeaveFirst,
	LeaveLast,
	LeaveFirstAndLast,
	Leave1PerStack,
	Leave1PerType;

	public ExtractionMode next() {
		int next = ordinal() + 1;

		if (next >= ExtractionMode.values().length) {
			next = 0;
		}
		return ExtractionMode.values()[next];
	}

	public static ExtractionMode getMode(int id) {
		if (id >= 0 && id < ExtractionMode.values().length) {
			return ExtractionMode.values()[id];
		}
		return Normal;
	}

	private static final String PREFIX = "misc.extractionmode.";

	public String getExtractionModeString() {
		return StringUtils.translate(ExtractionMode.PREFIX + name());
	}
}
