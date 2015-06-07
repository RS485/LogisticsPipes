package logisticspipes.proxy.ccl;

import logisticspipes.proxy.object3d.interfaces.ITranslation;

import codechicken.lib.vec.Transformation;

public class TransformationProxy implements ITranslation {

	private final Transformation transformation;

	public TransformationProxy(Transformation transformation) {
		this.transformation = transformation;
	}

	@Override
	public ITranslation inverse() {
		return new TransformationProxy(transformation.inverse());
	}

	@Override
	public Object getOriginal() {
		return transformation;
	}
}
