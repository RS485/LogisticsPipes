package cofh.repack.codechicken.lib.vec;

public abstract class ITransformation<Vector, Transformation extends ITransformation> {

	public abstract Transformation inverse();
}
