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
import logisticspipes.interfaces.ITubeRenderOrientation;
import logisticspipes.pipes.basic.CoreUnroutedPipe;
import logisticspipes.pipes.tubes.HSTubeSpeedup;
import logisticspipes.pipes.tubes.HSTubeSpeedup.SpeedupDirection;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.object3d.interfaces.IModel3D;
import logisticspipes.proxy.object3d.operation.LPColourMultiplier;
import logisticspipes.proxy.object3d.operation.LPRotation;
import logisticspipes.proxy.object3d.operation.LPScale;
import logisticspipes.proxy.object3d.operation.LPTranslation;
import logisticspipes.renderer.newpipe.IHighlightPlacementRenderer;
import logisticspipes.renderer.newpipe.ISpecialPipeRenderer;
import logisticspipes.renderer.newpipe.LogisticsNewRenderPipe;

public final class SpeedupTubeRenderer implements ISpecialPipeRenderer, IHighlightPlacementRenderer {

	private SpeedupTubeRenderer() {}

	public static final SpeedupTubeRenderer instance = new SpeedupTubeRenderer();

	static Map<SpeedupDirection, List<IModel3D>> tubeSpeedupBase = new HashMap<>();

	//Global Access
	public static Map<SpeedupDirection, IModel3D> tubeSpeedup = new HashMap<>();

	private static final ResourceLocation TEXTURE = new ResourceLocation("logisticspipes", "textures/blocks/pipes/HS-Speedup.png");

	public static void loadModels() {
		try {
			Map<String, IModel3D> pipePartModels = SimpleServiceLocator.cclProxy.parseObjModels(LogisticsPipes.class.getResourceAsStream("/logisticspipes/models/HSTube-Speedup_result.obj"), 7, new LPScale(1 / 100f));

			//tubeTurnMounts
			for (SpeedupDirection turn : SpeedupDirection.values()) {
				SpeedupTubeRenderer.tubeSpeedupBase.put(turn, new ArrayList<>());
			}
			pipePartModels.entrySet().stream()
					.filter(entry -> entry.getKey().startsWith("Side ") || entry.getKey().contains(" Side ") || entry.getKey().endsWith(" Side"))
					.forEach(entry -> {
						SpeedupTubeRenderer.tubeSpeedupBase.get(SpeedupDirection.EAST).add(LogisticsNewRenderPipe.compute(entry.getValue().twoFacedCopy().apply(new LPTranslation(0.0, 0.0, 0.0)).apply(new LPRotation(-Math.PI / 2, 0, 1, 0))));
						SpeedupTubeRenderer.tubeSpeedupBase.get(SpeedupDirection.NORTH).add(LogisticsNewRenderPipe.compute(entry.getValue().twoFacedCopy().apply(new LPTranslation(0.0, 0.0, 1.0))));
						SpeedupTubeRenderer.tubeSpeedupBase.get(SpeedupDirection.WEST).add(LogisticsNewRenderPipe.compute(entry.getValue().twoFacedCopy().apply(new LPTranslation(-1.0, 0.0, 1.0)).apply(new LPRotation(Math.PI / 2, 0, 1, 0))));
						SpeedupTubeRenderer.tubeSpeedupBase.get(SpeedupDirection.SOUTH).add(LogisticsNewRenderPipe.compute(entry.getValue().twoFacedCopy().apply(new LPTranslation(-1.0, 0.0, 0.0)).apply(new LPRotation(Math.PI, 0, 1, 0))));
					});
			if (SpeedupTubeRenderer.tubeSpeedupBase.get(SpeedupDirection.NORTH).size() != 4) {
				throw new RuntimeException("Couldn't load Tube Side. Only loaded " + SpeedupTubeRenderer.tubeSpeedupBase.get(SpeedupDirection.NORTH).size());
			}

			for (SpeedupDirection turn : SpeedupDirection.values()) {
				SpeedupTubeRenderer.tubeSpeedup.put(turn, SimpleServiceLocator.cclProxy.combine(SpeedupTubeRenderer.tubeSpeedupBase.get(turn)));
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	@Nonnull
	public List<IModel3D> getModelsWithoutPipe() {
		return SpeedupTubeRenderer.tubeSpeedupBase.get(SpeedupDirection.NORTH);
	}

	@Override
	@Nonnull
	public List<IModel3D> getModelsFromPipe(@Nonnull CoreUnroutedPipe pipe) {
		if (pipe instanceof HSTubeSpeedup && ((HSTubeSpeedup) pipe).getOrientation() != null) {
			final ITubeRenderOrientation orientation = ((HSTubeSpeedup) pipe).getOrientation().getRenderOrientation();
			return Objects.requireNonNull(SpeedupTubeRenderer.tubeSpeedupBase.get(orientation), "Could not fetch model for HSTubeSpeedup for orientation " + orientation);
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	@Nonnull
	public ResourceLocation getTexture() {
		return SpeedupTubeRenderer.TEXTURE;
	}

	@Override
	public void renderHighlight(ITubeOrientation orientation) {
		SpeedupDirection speedupDirection = (SpeedupDirection) orientation.getRenderOrientation();
		SpeedupTubeRenderer.tubeSpeedup.get(speedupDirection).copy().render(LPColourMultiplier.instance(0xFFFFFFFF));
		LogisticsNewRenderPipe.renderBoxWithDir(((SpeedupDirection) orientation.getRenderOrientation()).getDir1());
	}
}
