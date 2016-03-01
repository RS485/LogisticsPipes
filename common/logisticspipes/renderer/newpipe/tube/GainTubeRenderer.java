package logisticspipes.renderer.newpipe.tube;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.ITubeOrientation;
import logisticspipes.pipes.basic.CoreUnroutedPipe;
import logisticspipes.pipes.tubes.HSTubeGain;
import logisticspipes.pipes.tubes.HSTubeGain.TubeGainRenderOrientation;
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

import net.minecraft.util.AxisAlignedBB;
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
	static Map<TubeGainRenderOrientation, List<IModel3D>> tubeTurnBase = new HashMap<>();
	static Map<TubeGainRenderOrientation, Map<Pair<TubeMount, Integer>, IModel3D>> tubeTurnMounts = new HashMap<>();

	//Tube global Access
	public static Map<TubeGainRenderOrientation, IModel3D> tubeGain = new HashMap<TubeGainRenderOrientation, IModel3D>();

	private static final ResourceLocation TEXTURE = new ResourceLocation("logisticspipes", "textures/blocks/pipes/HS-Tube.png");

	static {
		GainTubeRenderer.loadModels();
	}

	public static void loadModels() {
		try {
			Map<String, IModel3D> pipePartModels = SimpleServiceLocator.cclProxy.parseObjModels(LogisticsPipes.class.getResourceAsStream("/logisticspipes/models/HSTube-Gain_result.obj"), 7, new LPScale(1 / 100f));

			//tubeTurnMounts
			for (TubeGainRenderOrientation turn : TubeGainRenderOrientation.values()) {
				GainTubeRenderer.tubeTurnBase.put(turn, new ArrayList<>());
			}
			pipePartModels.entrySet().stream().filter(entry -> entry.getKey().startsWith("Lane ") || entry.getKey().contains(" Lane ") || entry.getKey().endsWith(" Lane")).forEach(entry -> {
				GainTubeRenderer.tubeTurnBase.get(TubeGainRenderOrientation.EAST).add(LogisticsNewRenderPipe.compute(entry.getValue().twoFacedCopy().apply(new LPTranslation(0.0, 0.0, 0.0)).apply(new LPRotation(-Math.PI / 2, 0, 1, 0))));
				GainTubeRenderer.tubeTurnBase.get(TubeGainRenderOrientation.NORTH).add(LogisticsNewRenderPipe.compute(entry.getValue().twoFacedCopy().apply(new LPTranslation(0.0, 0.0, 1.0))));
				GainTubeRenderer.tubeTurnBase.get(TubeGainRenderOrientation.WEST).add(LogisticsNewRenderPipe.compute(entry.getValue().twoFacedCopy().apply(new LPTranslation(-1.0, 0.0, 1.0)).apply(new LPRotation(Math.PI / 2, 0, 1, 0))));
				GainTubeRenderer.tubeTurnBase.get(TubeGainRenderOrientation.SOUTH).add(LogisticsNewRenderPipe.compute(entry.getValue().twoFacedCopy().apply(new LPTranslation(-1.0, 0.0, 0.0)).apply(new LPRotation(Math.PI, 0, 1, 0))));
			});
			if (GainTubeRenderer.tubeTurnBase.get(TubeGainRenderOrientation.NORTH).size() != 4) {
				throw new RuntimeException("Couldn't load Tube Lanes. Only loaded " + GainTubeRenderer.tubeTurnBase.get(TubeGainRenderOrientation.NORTH).size());
			}

			for (TubeGainRenderOrientation turn : TubeGainRenderOrientation.values()) {
				GainTubeRenderer.tubeGain.put(turn, SimpleServiceLocator.cclProxy.combine(GainTubeRenderer.tubeTurnBase.get(turn)));
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	public void renderToList(CoreUnroutedPipe pipe, List<RenderEntry> objectsToRender) {
		if (pipe instanceof HSTubeGain) {
			HSTubeGain tube = (HSTubeGain) pipe;
			if (tube.getOrientation() != null) {
				objectsToRender.addAll(GainTubeRenderer.tubeTurnBase.get(tube.getOrientation().getRenderOrientation()).stream().map(model -> new RenderEntry(model, new I3DOperation[]{new LPUVTransformationList(new LPUVTranslation(0, 0))}, GainTubeRenderer.TEXTURE)).collect(Collectors.toList()));
			}
		}
	}

	@Override
	public void renderHighlight(ITubeOrientation orientation) {
		GainTubeRenderer.tubeGain.get(orientation.getRenderOrientation()).copy().render(new I3DOperation[] { LPColourMultiplier.instance(LogisticsPipes.LogisticsPipeBlock.getBlockColor() << 8 | 0xFF) });
	}

	public static AxisAlignedBB getObjectBoundsAt(AxisAlignedBB boundingBox, ITubeOrientation orientation) {
		IModel3D model = GainTubeRenderer.tubeGain.get(orientation.getRenderOrientation());
		IBounds c = model.getBoundsInside(boundingBox);
		if (c != null) {
			return c.toAABB();
		}
		return null;
	}
}
