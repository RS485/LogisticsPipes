package logisticspipes.proxy.object3d.operation;

import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.object3d.interfaces.I3DOperation;

public class LPScale implements I3DOperation {

	private final Object ori;

	public LPScale(double d) {
		ori = SimpleServiceLocator.cclProxy.getScale(d);
	}

	public LPScale(double d, double e, double f) {
		ori = SimpleServiceLocator.cclProxy.getScale(d, e, f);
	}

	@Override
	public Object getOriginal() {
		return ori;
	}

}
