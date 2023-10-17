package logisticspipes.proxy.object3d.operation;

import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.object3d.interfaces.I3DOperation;

public class LPUVScale implements I3DOperation {

	private final Object obj;

	public LPUVScale(double i, double d) {
		obj = SimpleServiceLocator.cclProxy.getUVScale(i, d);
	}

	@Override
	public Object getOriginal() {
		return obj;
	}

}
