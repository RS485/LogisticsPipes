package logisticspipes.interfaces;


public interface IHeadUpDisplayRendererProvider {
	public IHeadUpDisplayRenderer getRenderer();
	public int getX();
	public int getY();
	public int getZ();
	public void startWaitching();
	public void stopWaitching();
}
