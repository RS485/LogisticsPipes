package logisticspipes.proxy.ccl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.object3d.interfaces.I3DOperation;
import logisticspipes.proxy.object3d.interfaces.IBounds;
import logisticspipes.proxy.object3d.interfaces.IModel3D;
import logisticspipes.proxy.object3d.interfaces.IVec3;
import logisticspipes.utils.math.Vector3f;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import codechicken.lib.lighting.LightModel;
import codechicken.lib.render.CCModel;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.buffer.BakingVertexBuffer;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Transformation;
import codechicken.lib.vec.Vertex5;
import codechicken.lib.vec.uv.UVTransformation;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class Model3D implements IModel3D {

	private final CCModel model;

	public Model3D(CCModel model) {
		if (model == null) {
			throw new NullPointerException();
		}
		this.model = model;
	}

	@Override
	public void render(I3DOperation... i3dOperations) {
		List<IVertexOperation> list = new ArrayList<>();
		for (I3DOperation op : i3dOperations) {
			list.add((IVertexOperation) op.getOriginal());
		}
		model.render(CCRenderState.instance(), list.toArray(new IVertexOperation[list.size()]));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public List<BakedQuad> renderToQuads(VertexFormat format, I3DOperation... i3dOperations) {
		List<IVertexOperation> list = new ArrayList<>();

		for (I3DOperation op : i3dOperations) {
			list.add((IVertexOperation) op.getOriginal());
		}

		BakingVertexBuffer buffer = BakingVertexBuffer.create();
		CCRenderState ccrs = CCRenderState.instance();
		ccrs.reset();
		ccrs.startDrawing(0x7, format, buffer);
		model.render(ccrs, list.toArray(new IVertexOperation[0]));
		buffer.finishDrawing();
		return buffer.bake();
	}

	@Override
	public IModel3D copy() {
		return SimpleServiceLocator.cclProxy.wrapModel(model.copy());
	}

	@Override
	public void computeStandardLighting() {
		model.computeLighting(LightModel.standardLightModel);
	}

	@Override
	public void computeNormals() {
		model.computeNormals();
	}

	@Override
	public IBounds bounds() {
		return wrap(model.bounds());
	}
	
	private IBounds wrap(final Cuboid6 bounds) {
		return new IBounds() {

			@Override
			public IVec3 min() {
				return new IVec3() {

					@Override
					public double z() {
						return bounds.min.z;
					}

					@Override
					public double y() {
						return bounds.min.y;
					}

					@Override
					public double x() {
						return bounds.min.x;
					}

					@Override
					public Object getOriginal() {
						return bounds.min;
					}
				};
			}

			@Override
			public IVec3 max() {
				return new IVec3() {

					@Override
					public double z() {
						return bounds.max.z;
					}

					@Override
					public double y() {
						return bounds.max.y;
					}

					@Override
					public double x() {
						return bounds.max.x;
					}

					@Override
					public Object getOriginal() {
						return bounds.max;
					}
				};
			}

			@Override
			public AxisAlignedBB toAABB() {
				return bounds.aabb();
			}
		};
	}
	
	@Override
	public IModel3D backfacedCopy() {
		return SimpleServiceLocator.cclProxy.wrapModel(model.backfacedCopy());
	}

	@Override
	public IModel3D apply(I3DOperation translation) {
		if (translation.getOriginal() instanceof UVTransformation) {
			return SimpleServiceLocator.cclProxy.wrapModel(model.apply((UVTransformation) translation.getOriginal()));
		} else if (translation.getOriginal() instanceof Transformation) {
			return SimpleServiceLocator.cclProxy.wrapModel(model.apply((Transformation) translation.getOriginal()));
		} else {
			throw new UnsupportedOperationException(translation.getOriginal().getClass().toString());
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Model3D) {
			return ((Model3D) obj).model.equals(model);
		}
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return model.hashCode();
	}

	@Override
	public IModel3D twoFacedCopy() {
		return SimpleServiceLocator.cclProxy.wrapModel(model.twoFacedCopy());
	}

	@Override
	public Object getOriginal() {
		return model;
	}

	@Override
	public IBounds getBoundsInside(AxisAlignedBB boundingBox) {
		Cuboid6 c = null;
		for (Vertex5 v : model.verts) {
			if (boundingBox.contains(new Vec3d(v.vec.x, v.vec.y, v.vec.z))) {
				if (c == null) {
					c = new Cuboid6(v.vec.copy(), v.vec.copy());
				} else {
					c.enclose(v.vec);
				}
			}
		}
		if(c == null) return null;
		return wrap(c);
	}
}
