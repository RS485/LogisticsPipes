package logisticspipes.proxy.interfaces;

public interface IProxyController {
	public void setEnabled(boolean flag);
	public boolean isEnabled();
	public String getProxyName();
	public String getReason();
}
