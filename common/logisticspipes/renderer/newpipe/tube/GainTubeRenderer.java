package logisticspipes.renderer.newpipe.tube;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.ITubeOrientation;
import logisticspipes.pipes.basic.CoreUnroutedPipe;
import logisticspipes.pipes.tubes.HSTubeCurve;
import logisticspipes.pipes.tubes.HSTubeCurve.TurnDirection;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.object3d.interfaces.I3DOperation;
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

public class GainTubeRenderer implements ISpecialPipeRenderer, IHighlightPlacementRenderer {

	private GainTubeRenderer() {}

	public static final GainTubeRenderer instance = new GainTubeRenderer();

	enum TubeMount {
		UP_LEFT,
		UP_RIGHT,
		DOWN_LEFT,
		DOWN_RIGHT
	}

	//Tube Models
	static Map<TurnDirection, List<IModel3D>> tubeTurnBase = new HashMap<TurnDirection, List<IModel3D>>();
	static Map<TurnDirection, Map<Pair<TubeMount, Integer>, IModel3D>> tubeTurnMounts = new HashMap<TurnDirection, Map<Pair<TubeMount, Integer>, IModel3D>>();

	//Tube global Access
	public static Map<TurnDirection, IModel3D> tubeCurve = new HashMap<TurnDirection, IModel3D>();

	private static final ResourceLocation TEXTURE = new ResourceLocation("logisticspipes", "textures/blocks/pipes/HS-Tube.png");

	static {
		GainTubeRenderer.loadModels();
	}

	public static void loadModels() {
		try {
			Map<String, IModel3D> pipePartModels = SimpleServiceLocator.cclProxy.parseObjModels(LogisticsPipes.class.getResourceAsStream("/logisticspipes/models/HSTube-Gain_result.obj"), 7, new LPScale(1 / 100f));

			//tubeTurnMounts
			for (TurnDirection turn : TurnDirection.values()) {
				GainTubeRenderer.tubeTurnBase.put(turn, new ArrayList<IModel3D>());
			}
			for (Entry<String, IModel3D> entry : pipePartModels.entrySet()) {
				if (entry.getKey().startsWith("Lane ") || entry.getKey().contains(" Lane ") || entry.getKey().endsWith(" Lane")) {
					GainTubeRenderer.tubeTurnBase.get(TurnDirection.SOUTH_WEST).add(LogisticsNewRenderPipe.compute(entry.getValue().twoFacedCopy().apply(new LPTranslation(0.0, 0.0, 0.0)).apply(new LPRotation(-Math.PI / 2, 0, 1, 0))));
					GainTubeRenderer.tubeTurnBase.get(TurnDirection.EAST_SOUTH).add(LogisticsNewRenderPipe.compute(entry.getValue().twoFacedCopy().apply(new LPTranslation(0.0, 0.0, 1.0))));
					GainTubeRenderer.tubeTurnBase.get(TurnDirection.NORTH_EAST).add(LogisticsNewRenderPipe.compute(entry.getValue().twoFacedCopy().apply(new LPTranslation(-1.0, 0.0, 1.0)).apply(new LPRotation(Math.PI / 2, 0, 1, 0))));
					GainTubeRenderer.tubeTurnBase.get(TurnDirection.WEST_NORTH).add(LogisticsNewRenderPipe.compute(entry.getValue().twoFacedCopy().apply(new LPTranslation(-1.0, 0.0, 0.0)).apply(new LPRotation(Math.PI, 0, 1, 0))));
				}
			}
			if (GainTubeRenderer.tubeTurnBase.get(TurnDirection.NORTH_EAST).size() != 4) {
				throw new RuntimeException("Couldn't load Tube Lanes. Only loaded " + GainTubeRenderer.tubeTurnBase.get(TurnDirection.NORTH_EAST).size());
			}

			for (TurnDirection turn : TurnDirection.values()) {
				GainTubeRenderer.tubeCurve.put(turn, SimpleServiceLocator.cclProxy.combine(GainTubeRenderer.tubeTurnBase.get(turn)));
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	public void renderToList(CoreUnroutedPipe pipe, List<RenderEntry> objectsToRender) {
		if (pipe instanceof HSTubeCurve) {
			HSTubeCurve tube = (HSTubeCurve) pipe;
			if (tube.getOrientation() != null) {
				for (IModel3D model : GainTubeRenderer.tubeTurnBase.get(tube.getOrientation().getRenderOrientation())) {
					objectsToRender.add(new RenderEntry(model, new I3DOperation[] { new LPUVTransformationList(new LPUVTranslation(0, 0)) }, GainTubeRenderer.TEXTURE));
				}
			}
		}
	}

	@Override
	public void renderHighlight(ITubeOrientation orientation) {
		GainTubeRenderer.tubeCurve.get(orientation.getRenderOrientation()).copy().render(new I3DOperation[] { LPColourMultiplier.instance(LogisticsPipes.LogisticsPipeBlock.getBlockColor() << 8 | 0xFF) });

	}
}
