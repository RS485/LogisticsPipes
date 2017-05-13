package logisticspipes.proxy.ccl;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Vector3f;

import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.object3d.interfaces.I3DOperation;
import logisticspipes.proxy.object3d.interfaces.IBounds;
import logisticspipes.proxy.object3d.interfaces.IModel3D;
import logisticspipes.proxy.object3d.interfaces.IVec3;
import logisticspipes.proxy.object3d.interfaces.TextureTransformation;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

import net.minecraftforge.client.model.obj.OBJModel;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import codechicken.lib.model.bakery.CCModelBakery;
import codechicken.lib.lighting.LightModel;
import codechicken.lib.render.CCModel;
import codechicken.lib.render.CCRenderState;
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

		return CCModelBakery.bakeModel(model, format, list.toArray(new IVertexOperation[0]));
		/*
		ArrayList<BakedQuad> quads = Lists.newArrayList();

		CCRenderState.setPipeline(model, 0, model.verts.length, list.toArray(new IVertexOperation[list.size()]));
		Vertex5[] verts = CCRenderState.model.getVertices();
		UnpackedBakedQuad.Builder builder = null;
		Vector3f faceNormal = null;
		int counter = 0;
		for (CCRenderState.vertexIndex = CCRenderState.firstVertexIndex; CCRenderState.vertexIndex < CCRenderState.lastVertexIndex; CCRenderState.vertexIndex++) {
			if(counter++ % model.vp == 0) {
				if(builder != null) quads.add(builder.build());
				builder = new UnpackedBakedQuad.Builder(format);
				Vertex5 vert0 = verts[CCRenderState.vertexIndex + 0];
				Vertex5 vert1 = verts[CCRenderState.vertexIndex + 1];
				Vertex5 vert2 = verts[CCRenderState.vertexIndex + 2];
				Vertex5 vert3 = verts[CCRenderState.vertexIndex + ((model.vp == 3) ? 2: 3)]; //TODO is this the right vector to copy or does the normal invert?
				Vector3f a = new Vector3f((float) vert2.vec.x, (float)vert2.vec.y, (float)vert2.vec.z);
				a.sub(new Vector3f((float) vert0.vec.x, (float)vert0.vec.y, (float)vert0.vec.z));
				Vector3f b = new Vector3f((float) vert3.vec.x, (float)vert3.vec.y, (float)vert3.vec.z);
				b.sub(new Vector3f((float) vert1.vec.x, (float)vert1.vec.y, (float)vert1.vec.z));
				a.cross(a, b);
				a.normalize();
				faceNormal = a;
				//builder.setContractUVs(true);
				builder.setTexture(sprite);
				builder.setQuadOrientation(EnumFacing.getFacingFromVector(faceNormal.x, faceNormal.y, faceNormal.z));
			}
			CCRenderState.model.prepareVertex();
			CCRenderState.vert.set(verts[CCRenderState.vertexIndex]);
			CCRenderState.runPipeline();
			writeVert(builder);
			if(model.vp == 3 && model.vp % model.vp == 2) {
				writeVert(builder);
			}
		}
		quads.add(builder.build());
		return ImmutableList.copyOf(quads);
		*/
	}
/*
	@SideOnly(Side.CLIENT)
	public static void writeVert(UnpackedBakedQuad.Builder builder) {
		for (int e = 0; e < CCRenderState.fmt.getElementCount(); e++) {
			VertexFormatElement fmte = CCRenderState.fmt.getElement(e);
			switch (fmte.getUsage()) {
				case POSITION:
					builder.put(e, (float) CCRenderState.vert.vec.x, (float) CCRenderState.vert.vec.y, (float) CCRenderState.vert.vec.z);
					break;
				case UV:
					if (fmte.getIndex() == 0) {
						CCRenderState.r.tex(CCRenderState.vert.uv.u, CCRenderState.vert.uv.v);
					} else {
						CCRenderState.r.lightmap(CCRenderState.brightness >> 16 & 65535, CCRenderState.brightness & 65535);
					}
					break;
				case COLOR:
					builder.put(e,CCRenderState.colour >>> 24, CCRenderState.colour >> 16 & 0xFF, CCRenderState.colour >> 8 & 0xFF, CCRenderState.alphaOverride >= 0 ? CCRenderState.alphaOverride : CCRenderState.colour & 0xFF);
					break;
				case NORMAL: // TODO Check if normals need to be computed from the face manualy?
					CCRenderState.r.normal((float) CCRenderState.normal.x, (float) CCRenderState.normal.y, (float) CCRenderState.normal.z);
					break;
				case PADDING:
					break;
				default:
					throw new UnsupportedOperationException("Generic vertex format element");
			}
		}
	}
*/
	/*
	private final void putVertexData(UnpackedBakedQuad.Builder builder, OBJModel.Vertex v, OBJModel.Normal faceNormal, OBJModel.TextureCoordinate defUV, TextureAtlasSprite sprite)
	{
		for (int e = 0; e < format.getElementCount(); e++)
		{
			switch (format.getElement(e).getUsage())
			{
				case POSITION:
					builder.put(e, v.getPos().x, v.getPos().y, v.getPos().z, v.getPos().w);
					break;
				case COLOR:
					if (v.getMaterial() != null)
						builder.put(e,
								v.getMaterial().getColor().x,
								v.getMaterial().getColor().y,
								v.getMaterial().getColor().z,
								v.getMaterial().getColor().w);
					else
						builder.put(e, 1, 1, 1, 1);
					break;
				case UV:
					if (!v.hasTextureCoordinate())
						builder.put(e,
								sprite.getInterpolatedU(defUV.u * 16),
								sprite.getInterpolatedV((model.customData.flipV ? 1 - defUV.v: defUV.v) * 16),
								0, 1);
					else
						builder.put(e,
								sprite.getInterpolatedU(v.getTextureCoordinate().u * 16),
								sprite.getInterpolatedV((model.customData.flipV ? 1 - v.getTextureCoordinate().v : v.getTextureCoordinate().v) * 16),
								0, 1);
					break;
				case NORMAL:
					if (!v.hasNormal())
						builder.put(e, faceNormal.x, faceNormal.y, faceNormal.z, 0);
					else
						builder.put(e, v.getNormal().x, v.getNormal().y, v.getNormal().z, 0);
					break;
				default:
					builder.put(e);
			}
		}
	}
	*/

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
			if (boundingBox.isVecInside(new Vec3d(v.vec.x, v.vec.y, v.vec.z))) {
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
