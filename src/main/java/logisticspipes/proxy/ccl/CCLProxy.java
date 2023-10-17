package logisticspipes.proxy.ccl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import codechicken.lib.render.CCModel;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.OBJParser;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.util.TransformUtils;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Scale;
import codechicken.lib.vec.Transformation;
import codechicken.lib.vec.Translation;
import codechicken.lib.vec.Vector3;
import codechicken.lib.vec.uv.IconTransformation;
import codechicken.lib.vec.uv.UVScale;
import codechicken.lib.vec.uv.UVTransformation;
import codechicken.lib.vec.uv.UVTransformationList;
import codechicken.lib.vec.uv.UVTranslation;

import logisticspipes.proxy.DontLoadProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.interfaces.ICCLProxy;
import logisticspipes.proxy.object3d.interfaces.I3DOperation;
import logisticspipes.proxy.object3d.interfaces.IModel3D;
import logisticspipes.proxy.object3d.interfaces.IRenderState;
import logisticspipes.proxy.object3d.interfaces.ITranslation;
import logisticspipes.proxy.object3d.interfaces.IVec3;
import logisticspipes.proxy.object3d.interfaces.TextureTransformation;
import logisticspipes.proxy.object3d.operation.LPScale;

public class CCLProxy implements ICCLProxy {

	public CCLProxy() {
		try {
			CCModel.class.getName();
		} catch (Throwable e) {
			throw new DontLoadProxy();
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public TextureTransformation createIconTransformer(TextureAtlasSprite registerIcon) {
		final IconTransformation icon = new IconTransformation(registerIcon);
		return new TextureTransformation() {

			@Override
			public Object getOriginal() {
				return icon;
			}

			@Override
			public void update(TextureAtlasSprite registerIcon) {
				icon.icon = registerIcon;
			}

			@Override
			public TextureAtlasSprite getTexture() {
				return icon.icon;
			}

		};
	}

	@Override
	public IRenderState getRenderState() {
		return new IRenderState() {

			@Override
			public void reset() {
				CCRenderState.instance().reset();
				CCRenderState.instance().computeLighting = false;
			}

			@Override
			public void setAlphaOverride(int i) {
				CCRenderState.instance().alphaOverride = i;
			}

			@Override
			public void draw() {
				CCRenderState.instance().draw();
			}

			@Override
			public void setBrightness(IBlockAccess world, BlockPos pos) {
				CCRenderState.instance().setBrightness(world, pos);
			}

			@Override
			@SideOnly(Side.CLIENT)
			public void startDrawing(int mode, VertexFormat format) {
				CCRenderState.instance().startDrawing(mode, format);
			}

		};
	}

	@Override
	public Map<String, IModel3D> parseObjModels(InputStream resourceAsStream, int i, LPScale scale) throws IOException {
		Map<String, IModel3D> target = new HashMap<>();
		Map<String, CCModel> source = OBJParser.parseModels(resourceAsStream, i, (Transformation) scale.getOriginal());
		for (Entry<String, CCModel> entry : source.entrySet()) {
			target.put(entry.getKey(), SimpleServiceLocator.cclProxy.wrapModel(entry.getValue()));
		}
		return target;
	}

	@Override
	public Object getRotation(int i, int j) {
		return Rotation.sideOrientation(i, j);
	}

	@Override
	public Object getScale(double d, double e, double f) {
		return new Scale(d, e, f);
	}

	@Override
	public Object getScale(double d) {
		return new Scale(d);
	}

	@Override
	public ITranslation getTranslation(double d, double e, double f) {
		return new TransformationProxy(new Translation(d, e, f));
	}

	@Override
	public ITranslation getTranslation(IVec3 vec) {
		final Translation translation;
		if (vec.getOriginal() instanceof Vector3) {
			translation = new Translation((Vector3) vec.getOriginal());
		} else {
			translation = new Translation(vec.x(), vec.y(), vec.z());
		}
		return new TransformationProxy(translation);
	}

	@Override
	public Object getUVScale(double i, double d) {
		return new UVScale(i, d);
	}

	@Override
	public Object getUVTranslation(float i, float f) {
		return new UVTranslation(i, f);
	}

	@Override
	public Object getUVTransformationList(I3DOperation[] uvTranslation) {
		List<UVTransformation> transforms = new ArrayList<>();
		for (I3DOperation op : uvTranslation) {
			transforms.add((UVTransformation) op.getOriginal());
		}
		return new UVTransformationList(transforms.toArray(new UVTransformation[0]));
	}

	@Override
	public IModel3D wrapModel(Object oModel) {
		final CCModel model = (CCModel) oModel;
		return new Model3D(model);
	}

	@Override
	public boolean isActivated() {
		return true;
	}

	@Override
	public Object getRotation(double d, int i, int j, int k) {
		return new Rotation(d, i, j, k);
	}

	@Override
	public IModel3D combine(Collection<IModel3D> list) {
		List<CCModel> collection = new ArrayList<>(list.size());
		collection.addAll(list.stream().map(model -> (CCModel) model.getOriginal()).collect(Collectors.toList()));
		return SimpleServiceLocator.cclProxy.wrapModel(CCModel.combine(collection));
	}

	@Override
	public Object getColourMultiplier(int i) {
		return ColourMultiplier.instance(i);
	}

	@Override
	public IModelState getDefaultBlockState() {
		return TransformUtils.DEFAULT_BLOCK;
	}
}
