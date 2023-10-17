package logisticspipes.proxy.object3d.operation;

import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.object3d.interfaces.I3DOperation;

public class LPUVTranslation implements I3DOperation {

	private final Object obj;

	public LPUVTranslation(float i, float f) {
		obj = SimpleServiceLocator.cclProxy.getUVTranslation(i, f);
	}

	@Override
	public Object getOriginal() {
		return obj;
	}

}
