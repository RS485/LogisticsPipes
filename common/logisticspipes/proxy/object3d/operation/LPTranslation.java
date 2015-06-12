package logisticspipes.proxy.object3d.operation;

import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.object3d.interfaces.I3DOperation;
import logisticspipes.proxy.object3d.interfaces.ITranslation;
import logisticspipes.proxy.object3d.interfaces.IVec3;

public class LPTranslation implements I3DOperation {

	private final ITranslation trans;

	public LPTranslation(double d, double e, double f) {
		trans = SimpleServiceLocator.cclProxy.getTranslation(d, e, f);
	}

	public LPTranslation(IVec3 min) {
		trans = SimpleServiceLocator.cclProxy.getTranslation(min);
	}

	private LPTranslation(ITranslation trans) {
		this.trans = trans;
	}

	public LPTranslation inverse() {
		return new LPTranslation(trans.inverse());
	}

	@Override
	public Object getOriginal() {
		return trans.getOriginal();
	}

}
