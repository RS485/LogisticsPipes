package logisticspipes.proxy;


public class ClientProxy implements IProxy {
	@Override
	public String getSide() {
		return "Client";
	}
}
