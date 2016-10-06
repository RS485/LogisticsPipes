package logisticspipes.proxy.object3d.interfaces;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;

public interface IModel3D {

	IModel3D backfacedCopy();

	void render(I3DOperation... i3dOperations);

	void computeNormals();

	void computeStandardLighting();

	IBounds bounds();

	IModel3D apply(I3DOperation translation);

	IModel3D copy();

	IModel3D twoFacedCopy();

	Object getOriginal();

	IBounds getBoundsInside(AxisAlignedBB boundingBox);

}
