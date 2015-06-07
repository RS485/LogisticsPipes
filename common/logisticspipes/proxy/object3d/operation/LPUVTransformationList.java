package logisticspipes.proxy.object3d.operation;

import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.object3d.interfaces.I3DOperation;

public class LPUVTransformationList implements I3DOperation {

	private final Object obj;

	public LPUVTransformationList(I3DOperation... uvTranslation) {
		obj = SimpleServiceLocator.cclProxy.getUVTransformationList(uvTranslation);
	}

	@Override
	public Object getOriginal() {
		return obj;
	}

}
