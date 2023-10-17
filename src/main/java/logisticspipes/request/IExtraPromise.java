package logisticspipes.request;

import logisticspipes.request.resources.IResource;

public interface IExtraPromise extends IPromise {

	void registerExtras(IResource requestType);

	@Override
	IExtraPromise copy();

	boolean isProvided();

	void lowerAmount(int usedcount);

	void setAmount(int amount);
}
