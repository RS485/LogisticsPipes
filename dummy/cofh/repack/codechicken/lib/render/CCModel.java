package cofh.repack.codechicken.lib.render;

import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

import cofh.repack.codechicken.lib.lighting.LightModel;
import cofh.repack.codechicken.lib.render.CCRenderState.IVertexOperation;
import cofh.repack.codechicken.lib.render.uv.UVTransformation;
import cofh.repack.codechicken.lib.vec.Cuboid6;
import cofh.repack.codechicken.lib.vec.Transformation;

public class CCModel {
	
	public Vertex5[] verts;  
	
	public void render(IVertexOperation[] iVertexOperations) {
		// TODO Auto-generated method stub

	}

	public CCModel backfacedCopy() {
		// TODO Auto-generated method stub
		return null;
	}

	public CCModel copy() {
		// TODO Auto-generated method stub
		return null;
	}

	public CCModel computeLighting(LightModel standardLightModel) {
		// TODO Auto-generated method stub
		return null;
	}

	public CCModel computeNormals() {
		// TODO Auto-generated method stub
		return null;
	}

	public Cuboid6 bounds() {
		// TODO Auto-generated method stub
		return null;
	}

	public static Map<String, CCModel> parseObjModels(InputStream resourceAsStream, int i, Transformation original) {
		// TODO Auto-generated method stub
		return null;
	}

	public CCModel apply(UVTransformation original) {
		// TODO Auto-generated method stub
		return null;
	}

	public CCModel apply(Transformation original) {
		// TODO Auto-generated method stub
		return null;
	}

	public CCModel twoFacedCopy() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public static CCModel combine(Collection<CCModel> paramCollection) {
		// TODO Auto-generated method stub
		return null;
	}
}
