package logisticspipes.renderer.newpipe.tube;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.ITubeOrientation;
import logisticspipes.pipes.basic.CoreUnroutedPipe;
import logisticspipes.pipes.tubes.HSTubeSCurve;
import logisticspipes.pipes.tubes.HSTubeSCurve.TurnSDirection;
import logisticspipes.renderer.newpipe.IHighlightPlacementRenderer;
import logisticspipes.renderer.newpipe.ISpecialPipeRenderer;
import logisticspipes.renderer.newpipe.LogisticsNewRenderPipe;
import logisticspipes.renderer.newpipe.RenderEntry;
import logisticspipes.utils.tuples.Pair;

import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;

import codechicken.lib.render.CCModel;
import codechicken.lib.render.CCRenderState.IVertexOperation;
import codechicken.lib.render.ColourMultiplier;
import codechicken.lib.render.Vertex5;
import codechicken.lib.render.uv.UVTransformationList;
import codechicken.lib.render.uv.UVTranslation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Scale;
import codechicken.lib.vec.Translation;

public class SCurveTubeRenderer implements ISpecialPipeRenderer, IHighlightPlacementRenderer {

	private SCurveTubeRenderer() {}

	public static final SCurveTubeRenderer instance = new SCurveTubeRenderer();

	enum TubeMount {
		UP_LEFT,
		UP_RIGHT,
		DOWN_LEFT,
		DOWN_RIGHT
	}

	//Tube Models
	static Map<TurnSDirection, List<CCModel>> tubeSCurveBase = new HashMap<TurnSDirection, List<CCModel>>();
	static Map<TurnSDirection, Map<Pair<TubeMount, Integer>, CCModel>> tubeSCurveMounts = new HashMap<TurnSDirection, Map<Pair<TubeMount, Integer>, CCModel>>();

	//Tube global Access
	public static Map<TurnSDirection, CCModel> tubeSCurve = new HashMap<TurnSDirection, CCModel>();

	private static final ResourceLocation TEXTURE = new ResourceLocation("logisticspipes", "textures/blocks/pipes/HS-Tube.png");

	static {
		SCurveTubeRenderer.loadModels();
	}

	public static void loadModels() {
		try {
			Map<String, CCModel> pipePartModels = CCModel.parseObjModels(LogisticsPipes.class.getResourceAsStream("/logisticspipes/models/HSTube-Gain_result.obj"), 7, new Scale(1 / 100f));

			//tubeTurnMounts
			for (TurnSDirection turn : TurnSDirection.values()) {
				SCurveTubeRenderer.tubeSCurveBase.put(turn, new ArrayList<CCModel>());
			}
			for (Entry<String, CCModel> entry : pipePartModels.entrySet()) {
				if (entry.getKey().startsWith("Lane ") || entry.getKey().contains(" Lane ") || entry.getKey().endsWith(" Lane")) {
					SCurveTubeRenderer.tubeSCurveBase.get(TurnSDirection.EAST).add(LogisticsNewRenderPipe.compute(entry.getValue().twoFacedCopy().apply(new Rotation(Math.PI / 2, 0, 0, 1)).apply(new Translation(1.0, 0.0, 0.0)).apply(new Rotation(-Math.PI / 2, 0, 1, 0))));
					SCurveTubeRenderer.tubeSCurveBase.get(TurnSDirection.NORTH).add(LogisticsNewRenderPipe.compute(entry.getValue().twoFacedCopy().apply(new Rotation(Math.PI / 2, 0, 0, 1)).apply(new Translation(1.0, 0.0, 1.0))));
					SCurveTubeRenderer.tubeSCurveBase.get(TurnSDirection.EAST_INV).add(LogisticsNewRenderPipe.compute(entry.getValue().twoFacedCopy().apply(new Rotation(-Math.PI / 2, 0, 0, 1)).apply(new Translation(-2.0, 1.0, 4.0)).apply(new Rotation(Math.PI / 2, 0, 1, 0))));
					SCurveTubeRenderer.tubeSCurveBase.get(TurnSDirection.NORTH_INV).add(LogisticsNewRenderPipe.compute(entry.getValue().twoFacedCopy().apply(new Rotation(-Math.PI / 2, 0, 0, 1)).apply(new Translation(-2.0, 1.0, 3.0)).apply(new Rotation(Math.PI, 0, 1, 0))));
				}
			}
			if (SCurveTubeRenderer.tubeSCurveBase.get(TurnSDirection.NORTH).size() != 4) {
				throw new RuntimeException("Couldn't load Tube Lanes. Only loaded " + SCurveTubeRenderer.tubeSCurveBase.get(TurnSDirection.NORTH).size());
			}

			for (TurnSDirection turn : TurnSDirection.values()) {
				SCurveTubeRenderer.tubeSCurve.put(turn, CCModel.combine(SCurveTubeRenderer.tubeSCurveBase.get(turn)));
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	public void renderToList(CoreUnroutedPipe pipe, List<RenderEntry> objectsToRender) {
		if (pipe instanceof HSTubeSCurve) {
			HSTubeSCurve tube = (HSTubeSCurve) pipe;
			if (tube.getOrientation() != null) {
				for (CCModel model : SCurveTubeRenderer.tubeSCurveBase.get(tube.getOrientation().getRenderOrientation())) {
					objectsToRender.add(new RenderEntry(model, new IVertexOperation[] { new UVTransformationList(new UVTranslation(0, 0)) }, SCurveTubeRenderer.TEXTURE));
				}
			}
		}
	}

	@Override
	public void renderHighlight(ITubeOrientation orientation) {
		SCurveTubeRenderer.tubeSCurve.get(orientation.getRenderOrientation()).render(new IVertexOperation[] { ColourMultiplier.instance(LogisticsPipes.LogisticsPipeBlock.getBlockColor() << 8 | 0xFF) });
	}

	public static AxisAlignedBB getObjectBoundsAt(AxisAlignedBB boundingBox, ITubeOrientation orientation) {
		CCModel model = SCurveTubeRenderer.tubeSCurve.get(orientation.getRenderOrientation());
		Cuboid6 c = null;
		for (Vertex5 v : model.verts) {
			if (boundingBox.isVecInside(Vec3.createVectorHelper(v.vec.x, v.vec.y, v.vec.z))) {
				if (c == null) {
					c = new Cuboid6(v.vec.copy(), v.vec.copy());
				} else {
					c.enclose(v.vec);
				}
			}
		}
		if (c != null) {
			return c.toAABB();
		}
		return null;
	}
}
