package logisticspipes.proxy.object3d.interfaces;

import net.minecraft.util.math.AxisAlignedBB;

public interface IBounds {

	IVec3 max();

	IVec3 min();

	AxisAlignedBB toAABB();

}
