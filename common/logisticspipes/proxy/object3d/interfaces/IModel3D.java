package logisticspipes.proxy.object3d.interfaces;

import java.util.List;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.math.AxisAlignedBB;

public interface IModel3D {

	IModel3D backfacedCopy();

	void render(I3DOperation... i3dOperations);

	List<BakedQuad> renderToQuads(VertexFormat format, I3DOperation... i3dOperations);

	void computeNormals();

	void computeStandardLighting();

	IBounds bounds();

	IModel3D apply(I3DOperation translation);

	IModel3D copy();

	IModel3D twoFacedCopy();

	Object getOriginal();

	IBounds getBoundsInside(AxisAlignedBB boundingBox);

}
