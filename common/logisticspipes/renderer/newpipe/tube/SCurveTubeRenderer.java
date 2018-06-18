package logisticspipes.renderer.newpipe.tube;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.ITubeOrientation;
import logisticspipes.pipes.basic.CoreUnroutedPipe;
import logisticspipes.pipes.tubes.HSTubeSCurve;
import logisticspipes.pipes.tubes.HSTubeSCurve.TurnSDirection;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.object3d.interfaces.I3DOperation;
import logisticspipes.proxy.object3d.interfaces.IBounds;
import logisticspipes.proxy.object3d.interfaces.IModel3D;
import logisticspipes.proxy.object3d.operation.LPColourMultiplier;
import logisticspipes.proxy.object3d.operation.LPRotation;
import logisticspipes.proxy.object3d.operation.LPScale;
import logisticspipes.proxy.object3d.operation.LPTranslation;
import logisticspipes.proxy.object3d.operation.LPUVTransformationList;
import logisticspipes.proxy.object3d.operation.LPUVTranslation;
import logisticspipes.renderer.newpipe.IHighlightPlacementRenderer;
import logisticspipes.renderer.newpipe.ISpecialPipeRenderer;
import logisticspipes.renderer.newpipe.LogisticsNewRenderPipe;
import logisticspipes.renderer.newpipe.RenderEntry;
import logisticspipes.utils.tuples.Pair;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;

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
	static Map<TurnSDirection, List<IModel3D>> tubeSCurveBase = new HashMap<>();
	static Map<TurnSDirection, Map<Pair<TubeMount, Integer>, IModel3D>> tubeSCurveMounts = new HashMap<>();

	//Tube global Access
	public static Map<TurnSDirection, IModel3D> tubeSCurve = new HashMap<>();

	private static final ResourceLocation TEXTURE = new ResourceLocation("logisticspipes", "textures/blocks/pipes/HS-Tube.png");

	static {
		SCurveTubeRenderer.loadModels();
	}

	public static void loadModels() {
		try {
			Map<String, IModel3D> pipePartModels = SimpleServiceLocator.cclProxy.parseObjModels(LogisticsPipes.class.getResourceAsStream("/logisticspipes/models/HSTube-Gain_result.obj"), 7, new LPScale(1 / 100f));

			//tubeTurnMounts
			for (TurnSDirection turn : TurnSDirection.values()) {
				SCurveTubeRenderer.tubeSCurveBase.put(turn, new ArrayList<>());
			}
			pipePartModels.entrySet().stream()
					.filter(entry -> entry.getKey().startsWith("Lane ") || entry.getKey().contains(" Lane ") || entry.getKey().endsWith(" Lane"))
					.forEach(entry -> {
						SCurveTubeRenderer.tubeSCurveBase.get(TurnSDirection.EAST).add(LogisticsNewRenderPipe.compute(entry.getValue().twoFacedCopy().apply(new LPRotation(Math.PI / 2, 0, 0, 1)).apply(new LPTranslation(1.0, 0.0, 0.0)).apply(new LPRotation(-Math.PI / 2, 0, 1, 0))));
						SCurveTubeRenderer.tubeSCurveBase.get(TurnSDirection.NORTH).add(LogisticsNewRenderPipe.compute(entry.getValue().twoFacedCopy().apply(new LPRotation(Math.PI / 2, 0, 0, 1)).apply(new LPTranslation(1.0, 0.0, 1.0))));
						SCurveTubeRenderer.tubeSCurveBase.get(TurnSDirection.EAST_INV).add(LogisticsNewRenderPipe.compute(entry.getValue().twoFacedCopy().apply(new LPRotation(-Math.PI / 2, 0, 0, 1)).apply(new LPTranslation(-2.0, 1.0, 4.0)).apply(new LPRotation(Math.PI / 2, 0, 1, 0))));
						SCurveTubeRenderer.tubeSCurveBase.get(TurnSDirection.NORTH_INV).add(LogisticsNewRenderPipe.compute(entry.getValue().twoFacedCopy().apply(new LPRotation(-Math.PI / 2, 0, 0, 1)).apply(new LPTranslation(-2.0, 1.0, 3.0)).apply(new LPRotation(Math.PI, 0, 1, 0))));
					});
			if (SCurveTubeRenderer.tubeSCurveBase.get(TurnSDirection.NORTH).size() != 4) {
				throw new RuntimeException("Couldn't load Tube Lanes. Only loaded " + SCurveTubeRenderer.tubeSCurveBase.get(TurnSDirection.NORTH).size());
			}

			for (TurnSDirection turn : TurnSDirection.values()) {
				SCurveTubeRenderer.tubeSCurve.put(turn, SimpleServiceLocator.cclProxy.combine(SCurveTubeRenderer.tubeSCurveBase.get(turn)));
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
				objectsToRender
						.addAll(SCurveTubeRenderer.tubeSCurveBase.get(tube.getOrientation().getRenderOrientation())
								.stream()
								.map(model -> new RenderEntry(model, new I3DOperation[]{new LPUVTransformationList(new LPUVTranslation(0, 0))}, SCurveTubeRenderer.TEXTURE))
								.collect(Collectors.toList()));
			}
		}
		if(pipe == null) {
			objectsToRender
					.addAll(SCurveTubeRenderer.tubeSCurveBase.get(TurnSDirection.NORTH)
							.stream()
							.map(model -> new RenderEntry(model, new I3DOperation[]{new LPUVTransformationList(new LPUVTranslation(0, 0))}, SCurveTubeRenderer.TEXTURE))
							.collect(Collectors.toList()));
		}
	}

	@Override
	public void renderHighlight(ITubeOrientation orientation) {
		SCurveTubeRenderer.tubeSCurve.get(orientation.getRenderOrientation()).render(new I3DOperation[] { LPColourMultiplier.instance(0xFFFFFFFF) });
	}

	public static AxisAlignedBB getObjectBoundsAt(AxisAlignedBB boundingBox, ITubeOrientation orientation) {
		IModel3D model = SCurveTubeRenderer.tubeSCurve.get(orientation.getRenderOrientation());
		IBounds c = model.getBoundsInside(boundingBox);
		if (c != null) {
			return c.toAABB();
		}
		return null;
	}
}
