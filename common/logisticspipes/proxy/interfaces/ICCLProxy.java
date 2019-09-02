package logisticspipes.proxy.interfaces;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import logisticspipes.proxy.object3d.interfaces.I3DOperation;
import logisticspipes.proxy.object3d.interfaces.IModel3D;
import logisticspipes.proxy.object3d.interfaces.IRenderState;
import logisticspipes.proxy.object3d.interfaces.ITranslation;
import logisticspipes.proxy.object3d.interfaces.IVec3;
import logisticspipes.proxy.object3d.interfaces.TextureTransformation;
import logisticspipes.proxy.object3d.operation.LPScale;

public interface ICCLProxy {

	@SideOnly(Side.CLIENT)
	TextureTransformation createIconTransformer(TextureAtlasSprite registerIcon);

	IRenderState getRenderState();

	Map<String, IModel3D> parseObjModels(InputStream resourceAsStream, int i, LPScale scale) throws IOException;

	Object getRotation(int i, int j);

	Object getScale(double d, double e, double f);

	Object getScale(double d);

	ITranslation getTranslation(double d, double e, double f);

	ITranslation getTranslation(IVec3 min);

	Object getUVScale(double i, double d);

	Object getUVTranslation(float i, float f);

	Object getUVTransformationList(I3DOperation[] uvTranslation);

	IModel3D wrapModel(Object model);

	boolean isActivated();

	Object getRotation(double d, int i, int j, int k);

	IModel3D combine(Collection<IModel3D> list);

	Object getColourMultiplier(int i);

	IModelState getDefaultBlockState();
}
