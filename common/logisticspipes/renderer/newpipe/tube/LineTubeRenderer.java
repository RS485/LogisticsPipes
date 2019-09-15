package logisticspipes.renderer.newpipe.tube;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import net.minecraft.util.Identifier;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.ITubeOrientation;
import logisticspipes.pipes.basic.CoreUnroutedPipe;
import logisticspipes.pipes.tubes.HSTubeLine;
import logisticspipes.pipes.tubes.HSTubeLine.TubeLineRenderOrientation;
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

public final class LineTubeRenderer implements ISpecialPipeRenderer, IHighlightPlacementRenderer {

	private LineTubeRenderer() {}

	public static final LineTubeRenderer instance = new LineTubeRenderer();

	static Map<TubeLineRenderOrientation, List<IModel3D>> tubeLineBase = new HashMap<>();

	// Global Access
	public static Map<TubeLineRenderOrientation, IModel3D> tubeLine = new HashMap<>();

	private static final Identifier TEXTURE = new Identifier("logisticspipes", "textures/blocks/pipes/HS-Tube-Line.png");

	static {
		LineTubeRenderer.loadModels();
	}

	public static void loadModels() {
		try {
			Map<String, IModel3D> pipePartModels = SimpleServiceLocator.cclProxy.parseObjModels(LogisticsPipes.class.getResourceAsStream("/logisticspipes/models/HSTube-Line_result.obj"), 7, new LPScale(1 / 100f));

			// tubeTurnMounts
			for (TubeLineRenderOrientation turn : TubeLineRenderOrientation.values()) {
				LineTubeRenderer.tubeLineBase.put(turn, new ArrayList<>());
			}
			pipePartModels.entrySet().stream().filter(entry -> entry.getKey().startsWith("Side ") || entry.getKey().contains(" Side ") || entry.getKey().endsWith(" Side")).forEach(entry -> {
				LineTubeRenderer.tubeLineBase.get(TubeLineRenderOrientation.EAST_WEST).add(LogisticsNewRenderPipe.compute(entry.getValue().twoFacedCopy().apply(new LPTranslation(0.0, 0.0, 0.0)).apply(new LPRotation(-Math.PI / 2, 0, 1, 0))));
				LineTubeRenderer.tubeLineBase.get(TubeLineRenderOrientation.NORTH_SOUTH).add(LogisticsNewRenderPipe.compute(entry.getValue().twoFacedCopy().apply(new LPTranslation(0.0, 0.0, 1.0))));
			});
			if (LineTubeRenderer.tubeLineBase.get(TubeLineRenderOrientation.EAST_WEST).size() != 4) {
				throw new RuntimeException("Couldn't load Tube Side. Only loaded " + LineTubeRenderer.tubeLineBase.get(TubeLineRenderOrientation.EAST_WEST).size());
			}

			for (TubeLineRenderOrientation turn : TubeLineRenderOrientation.values()) {
				LineTubeRenderer.tubeLine.put(turn, SimpleServiceLocator.cclProxy.combine(LineTubeRenderer.tubeLineBase.get(turn)));
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	public void renderToList(CoreUnroutedPipe pipe, List<RenderEntry> objectsToRender) {
		if (pipe instanceof HSTubeLine) {
			HSTubeLine tube = (HSTubeLine) pipe;
			if (tube.getOrientation() != null) {
				TubeLineRenderOrientation speedupDirection = tube.getOrientation().getRenderOrientation();
				objectsToRender.addAll(LineTubeRenderer.tubeLineBase.get(speedupDirection).stream().map(model -> new RenderEntry(model, new I3DOperation[] { new LPUVTransformationList(new LPUVTranslation(0, 0)) }, LineTubeRenderer.TEXTURE)).collect(Collectors.toList()));
			}
		}
		if (pipe == null) {
			objectsToRender.addAll(LineTubeRenderer.tubeLineBase.get(TubeLineRenderOrientation.NORTH_SOUTH).stream().map(model -> new RenderEntry(model, new I3DOperation[] { new LPUVTransformationList(new LPUVTranslation(0, 0)) }, LineTubeRenderer.TEXTURE)).collect(Collectors.toList()));
		}
	}

	@Override
	public void renderHighlight(ITubeOrientation orientation) {
		TubeLineRenderOrientation direction = (TubeLineRenderOrientation) orientation.getRenderOrientation();
		LineTubeRenderer.tubeLine.get(direction).copy().render(LPColourMultiplier.instance(0xFFFFFFFF));
	}
}

