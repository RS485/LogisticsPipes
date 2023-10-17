package logisticspipes.proxy.object3d.operation;

import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.object3d.interfaces.I3DOperation;

public class LPColourMultiplier implements I3DOperation {

	private final Object ori;

	public LPColourMultiplier(int i) {
		ori = SimpleServiceLocator.cclProxy.getColourMultiplier(i);
	}

	@Override
	public Object getOriginal() {
		return ori;
	}

	public static I3DOperation instance(int i) {
		return new LPColourMultiplier(i);
	}

}
