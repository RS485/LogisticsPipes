package logisticspipes.pipes;

public enum SatelliteNamingResult {
	SUCCESS, DUPLICATE_NAME, BLANK_NAME;

	@Override
	public String toString() {
		return super.toString().toLowerCase();
	}
}
