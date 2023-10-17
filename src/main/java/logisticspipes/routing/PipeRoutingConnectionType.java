package logisticspipes.routing;

public enum PipeRoutingConnectionType {
	canRouteTo,
	canRequestFrom,
	canPowerFrom,
	canPowerSubSystemFrom;

	public static PipeRoutingConnectionType[] values = new PipeRoutingConnectionType[] { canRouteTo, canRequestFrom, canPowerFrom, canPowerSubSystemFrom };
}
