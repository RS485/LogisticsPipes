package logisticspipes.request;

import network.rs485.logisticspipes.routing.request.Resource;

public interface ExtraPromise extends Promise {

	void registerExtras(Resource requestType);

	@Override
	ExtraPromise copy();

	boolean isProvided();

	void lowerAmount(int usedcount);

	void setAmount(int amount);
}
