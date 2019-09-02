package logisticspipes.asm.addinfo;

public interface IAddInfoProvider {

	<T extends IAddInfo> T getLogisticsPipesAddInfo(Class<T> clazz);

	void setLogisticsPipesAddInfo(IAddInfo info);
}
