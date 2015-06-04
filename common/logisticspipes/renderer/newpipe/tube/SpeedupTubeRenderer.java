package logisticspipes.renderer.newpipe.tube;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import codechicken.lib.render.CCModel;
import codechicken.lib.render.CCRenderState.IVertexOperation;
import codechicken.lib.render.ColourMultiplier;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Scale;
import codechicken.lib.vec.Translation;
import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.ITubeOrientation;
import logisticspipes.pipes.basic.CoreUnroutedPipe;
import logisticspipes.pipes.tubes.HSTubeSpeedup;
import logisticspipes.pipes.tubes.HSTubeSpeedup.SpeedupDirection;
import logisticspipes.renderer.newpipe.IHighlightPlacementRenderer;
import logisticspipes.renderer.newpipe.ISpecialPipeRenderer;
import logisticspipes.renderer.newpipe.LogisticsNewRenderPipe;
import logisticspipes.renderer.newpipe.RenderEntry;
import net.minecraft.util.ResourceLocation;

public class SpeedupTubeRenderer implements ISpecialPipeRenderer, IHighlightPlacementRenderer {
	
	private SpeedupTubeRenderer() {}
	
	public static final SpeedupTubeRenderer instance = new SpeedupTubeRenderer();
	
	
	static Map<SpeedupDirection, List<CCModel>> tubeSpeedupBase = new HashMap<SpeedupDirection, List<CCModel>>();

	
	//Global Access
	public static Map<SpeedupDirection, CCModel> tubeSpeedup = new HashMap<SpeedupDirection, CCModel>();
	
	private static final ResourceLocation TEXTURE = new ResourceLocation("logisticspipes", "textures/blocks/pipes/HS-Speedup.png");
	
	static {
		loadModels();
	}
	
	public static void loadModels() {
		try {
			Map<String, CCModel> pipePartModels = CCModel.parseObjModels(LogisticsPipes.class.getResourceAsStream("/logisticspipes/models/HSTube-Speedup_result.obj"), 7, new Scale(1/100f));
	
			//tubeTurnMounts
			for(SpeedupDirection turn:SpeedupDirection.values()) {
				tubeSpeedupBase.put(turn, new ArrayList<CCModel>());
			}
			for(Entry<String, CCModel> entry:pipePartModels.entrySet()) {
				if(entry.getKey().startsWith("Side ") || entry.getKey().contains(" Side ") || entry.getKey().endsWith(" Side")) {
					tubeSpeedupBase.get(SpeedupDirection.EAST).add(LogisticsNewRenderPipe.compute(entry.getValue().twoFacedCopy().apply(new Translation(0.0, 0.0, 0.0)).apply(new Rotation(- Math.PI / 2, 0, 1, 0))));
					tubeSpeedupBase.get(SpeedupDirection.NORTH).add(LogisticsNewRenderPipe.compute(entry.getValue().twoFacedCopy().apply(new Translation(0.0, 0.0, 1.0))));
					tubeSpeedupBase.get(SpeedupDirection.WEST).add(LogisticsNewRenderPipe.compute(entry.getValue().twoFacedCopy().apply(new Translation(-1.0, 0.0, 1.0)).apply(new Rotation(Math.PI / 2, 0, 1, 0))));
					tubeSpeedupBase.get(SpeedupDirection.SOUTH).add(LogisticsNewRenderPipe.compute(entry.getValue().twoFacedCopy().apply(new Translation(-1.0, 0.0, 0.0)).apply(new Rotation(Math.PI, 0, 1, 0))));
				}
			}
			if(tubeSpeedupBase.get(SpeedupDirection.NORTH).size() != 4) throw new RuntimeException("Couldn't load Tube Side. Only loaded " + tubeSpeedupBase.get(SpeedupDirection.NORTH).size());
			
			for(SpeedupDirection turn:SpeedupDirection.values()) {
				tubeSpeedup.put(turn, CCModel.combine(tubeSpeedupBase.get(turn)));
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
		
	}
	
	@Override
	public void renderToList(CoreUnroutedPipe pipe, List<RenderEntry> objectsToRender) {
		if(pipe != null && pipe.isMultiBlock()) {
			if(pipe instanceof HSTubeSpeedup) {
				HSTubeSpeedup tube = (HSTubeSpeedup) pipe;
				if(tube.getOrientation() != null) {
					for(CCModel model:tubeSpeedupBase.get(tube.getOrientation().getRenderOrientation())) {
						objectsToRender.add(new RenderEntry(model, TEXTURE));
					}
				}
			}
		}
	}

	@Override
	public void renderHighlight(ITubeOrientation orientation) {
		SpeedupTubeRenderer.tubeSpeedup.get(orientation.getRenderOrientation()).copy().render(new IVertexOperation[] {ColourMultiplier.instance(LogisticsPipes.LogisticsPipeBlock.getBlockColor() << 8 | 0xFF)});
	}
}
