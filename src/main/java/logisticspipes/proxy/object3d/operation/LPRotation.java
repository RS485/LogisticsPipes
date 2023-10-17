package logisticspipes.proxy.object3d.operation;

import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.object3d.interfaces.I3DOperation;

public class LPRotation implements I3DOperation {

	private final Object ori;

	private LPRotation(int i, int j) {
		ori = SimpleServiceLocator.cclProxy.getRotation(i, j);
	}

	public LPRotation(double d, int i, int j, int k) {
		ori = SimpleServiceLocator.cclProxy.getRotation(d, i, j, k);
	}

	public static I3DOperation sideOrientation(int i, int j) {
		return new LPRotation(i, j);
	}

	@Override
	public Object getOriginal() {
		return ori;
	}

}
