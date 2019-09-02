package logisticspipes.proxy.ccl;

import codechicken.lib.vec.Transformation;

import logisticspipes.proxy.object3d.interfaces.ITranslation;

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
