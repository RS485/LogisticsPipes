package logisticspipes.renderer.newpipe.tube;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;

import net.minecraft.util.ResourceLocation;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.ITubeOrientation;
import logisticspipes.pipes.basic.CoreUnroutedPipe;
import logisticspipes.pipes.tubes.HSTubeCurve;
import logisticspipes.pipes.tubes.HSTubeCurve.TurnDirection;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.object3d.interfaces.IModel3D;
import logisticspipes.proxy.object3d.operation.LPColourMultiplier;
import logisticspipes.proxy.object3d.operation.LPRotation;
import logisticspipes.proxy.object3d.operation.LPScale;
import logisticspipes.proxy.object3d.operation.LPTranslation;
import logisticspipes.renderer.newpipe.IHighlightPlacementRenderer;
import logisticspipes.renderer.newpipe.ISpecialPipeRenderer;
import logisticspipes.renderer.newpipe.LogisticsNewRenderPipe;
import logisticspipes.utils.tuples.Pair;

public class CurveTubeRenderer implements ISpecialPipeRenderer, IHighlightPlacementRenderer {

	private CurveTubeRenderer() {}

	public static final CurveTubeRenderer instance = new CurveTubeRenderer();

	enum TubeMount {
		UP_LEFT,
		UP_RIGHT,
		DOWN_LEFT,
		DOWN_RIGHT
	}

	//Tube Models
	static Map<TurnDirection, List<IModel3D>> tubeTurnBase = new HashMap<>();
	static Map<TurnDirection, Map<Pair<TubeMount, Integer>, IModel3D>> tubeTurnMounts = new HashMap<>();

	//Tube global Access
	public static Map<TurnDirection, IModel3D> tubeCurve = new HashMap<>();

	private static final ResourceLocation TEXTURE = new ResourceLocation("logisticspipes", "textures/blocks/pipes/HS-Tube.png");

	public static void loadModels() {
		try {
			Map<String, IModel3D> pipePartModels = SimpleServiceLocator.cclProxy.parseObjModels(LogisticsPipes.class.getResourceAsStream("/logisticspipes/models/HSTube-Turn_result.obj"), 7, new LPScale(1 / 100f));

			//tubeTurnMounts
			for (TurnDirection turn : TurnDirection.values()) {
				CurveTubeRenderer.tubeTurnBase.put(turn, new ArrayList<>());
			}
			pipePartModels.entrySet().stream()
					.filter(entry -> entry.getKey().startsWith("Lane ") || entry.getKey().contains(" Lane ") || entry.getKey().endsWith(" Lane"))
					.forEach(entry -> {
						CurveTubeRenderer.tubeTurnBase.get(TurnDirection.SOUTH_WEST).add(LogisticsNewRenderPipe.compute(entry.getValue().twoFacedCopy().apply(new LPTranslation(0.0, 0.0, 0.0)).apply(new LPRotation(-Math.PI / 2, 0, 1, 0))));
						CurveTubeRenderer.tubeTurnBase.get(TurnDirection.EAST_SOUTH).add(LogisticsNewRenderPipe.compute(entry.getValue().twoFacedCopy().apply(new LPTranslation(0.0, 0.0, 1.0))));
						CurveTubeRenderer.tubeTurnBase.get(TurnDirection.NORTH_EAST).add(LogisticsNewRenderPipe.compute(entry.getValue().twoFacedCopy().apply(new LPTranslation(-1.0, 0.0, 1.0)).apply(new LPRotation(Math.PI / 2, 0, 1, 0))));
						CurveTubeRenderer.tubeTurnBase.get(TurnDirection.WEST_NORTH).add(LogisticsNewRenderPipe.compute(entry.getValue().twoFacedCopy().apply(new LPTranslation(-1.0, 0.0, 0.0)).apply(new LPRotation(Math.PI, 0, 1, 0))));
					});
			if (CurveTubeRenderer.tubeTurnBase.get(TurnDirection.NORTH_EAST).size() != 4) {
				throw new RuntimeException("Couldn't load Tube Lanes. Only loaded " + CurveTubeRenderer.tubeTurnBase.get(TurnDirection.NORTH_EAST).size());
			}

			for (TurnDirection turn : TurnDirection.values()) {
				CurveTubeRenderer.tubeCurve.put(turn, SimpleServiceLocator.cclProxy.combine(CurveTubeRenderer.tubeTurnBase.get(turn)));
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}

	}

	@Nonnull
	@Override
	public List<IModel3D> getModelsWithoutPipe() {
		return CurveTubeRenderer.tubeTurnBase.get(TurnDirection.NORTH_EAST);
	}

	@Nonnull
	@Override
	public List<IModel3D> getModelsFromPipe(@Nonnull CoreUnroutedPipe pipe) {
		if (pipe instanceof HSTubeCurve && ((HSTubeCurve) pipe).getOrientation() != null) {
			final TurnDirection orientation = ((HSTubeCurve) pipe).getOrientation().getRenderOrientation();
			return Objects.requireNonNull(CurveTubeRenderer.tubeTurnBase.get(orientation), "Could not fetch model for CurveTubeRenderer for orientation " + orientation);
		} else {
			return Collections.emptyList();
		}
	}

	@Nonnull
	@Override
	public ResourceLocation getTexture() {
		return CurveTubeRenderer.TEXTURE;
	}

	@Override
	public void renderHighlight(ITubeOrientation orientation) {
		CurveTubeRenderer.tubeCurve.get(orientation.getRenderOrientation()).copy().render(LPColourMultiplier.instance(0xFFFFFFFF));
	}
}
