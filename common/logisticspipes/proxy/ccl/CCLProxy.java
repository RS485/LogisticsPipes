package logisticspipes.proxy.ccl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import logisticspipes.proxy.DontLoadProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.interfaces.ICCLProxy;
import logisticspipes.proxy.object3d.interfaces.I3DOperation;
import logisticspipes.proxy.object3d.interfaces.IIconTransformation;
import logisticspipes.proxy.object3d.interfaces.IModel3D;
import logisticspipes.proxy.object3d.interfaces.IRenderState;
import logisticspipes.proxy.object3d.interfaces.ITranslation;
import logisticspipes.proxy.object3d.interfaces.IVec3;
import logisticspipes.proxy.object3d.operation.LPScale;

import net.minecraft.util.IIcon;

import codechicken.lib.render.CCModel;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.uv.IconTransformation;
import codechicken.lib.render.uv.UVScale;
import codechicken.lib.render.uv.UVTransformation;
import codechicken.lib.render.uv.UVTransformationList;
import codechicken.lib.render.uv.UVTranslation;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Scale;
import codechicken.lib.vec.Transformation;
import codechicken.lib.vec.Translation;
import codechicken.lib.vec.Vector3;

public class CCLProxy implements ICCLProxy {

	public CCLProxy() {
		try {
			CCModel.class.getName();
		} catch (Throwable e) {
			throw new DontLoadProxy();
		}
	}

	@Override
	public IIconTransformation createIconTransformer(IIcon registerIcon) {
		final IconTransformation icon = new IconTransformation(registerIcon);
		return new IIconTransformation() {

			@Override
			public Object getOriginal() {
				return icon;
			}

			@Override
			public void update(IIcon registerIcon) {
				icon.icon = registerIcon;
			}

		};
	}

	@Override
	public IRenderState getRenderState() {
		return new IRenderState() {

			@Override
			public void reset() {
				CCRenderState.reset();
			}

			@Override
			public void setUseNormals(boolean b) {
				CCRenderState.useNormals = b;
			}

			@Override
			public void setAlphaOverride(int i) {
				CCRenderState.alphaOverride = i;
			}

		};
	}

	@Override
	public Map<String, IModel3D> parseObjModels(InputStream resourceAsStream, int i, LPScale scale) throws IOException {
		Map<String, IModel3D> target = new HashMap<String, IModel3D>();
		Map<String, CCModel> source = CCModel.parseObjModels(resourceAsStream, i, (Transformation) scale.getOriginal());
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
		List<UVTransformation> transforms = new ArrayList<UVTransformation>();
		for (I3DOperation op : uvTranslation) {
			transforms.add((UVTransformation) op.getOriginal());
		}
		return new UVTransformationList(transforms.toArray(new UVTransformation[transforms.size()]));
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
}
