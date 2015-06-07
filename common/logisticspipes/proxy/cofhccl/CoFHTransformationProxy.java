package logisticspipes.proxy.cofhccl;

import logisticspipes.proxy.object3d.interfaces.ITranslation;

import cofh.repack.codechicken.lib.vec.Transformation;

public class CoFHTransformationProxy implements ITranslation {

	private final Transformation transformation;

	public CoFHTransformationProxy(Transformation transformation) {
		this.transformation = transformation;
	}

	@Override
	public ITranslation inverse() {
		return new CoFHTransformationProxy(transformation.inverse());
	}

	@Override
	public Object getOriginal() {
		return transformation;
	}
}
