package logisticspipes.proxy.cc.interfaces;

public interface ISpecialCCPipe {
	public String getType();
	public String[] getMethodNames();
	public Object[] callMethod(int method, Object[] arguments) throws Exception;
}
