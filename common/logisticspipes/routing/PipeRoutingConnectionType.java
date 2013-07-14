package logisticspipes.routing;


public enum PipeRoutingConnectionType{
	canRouteTo,
	canRequestFrom,
	canPowerFrom;
	
	public static PipeRoutingConnectionType[] values = new PipeRoutingConnectionType[] {canRouteTo, canRequestFrom, canPowerFrom};
}
