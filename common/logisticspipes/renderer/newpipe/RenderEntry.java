package logisticspipes.renderer.newpipe;

import net.minecraft.util.ResourceLocation;
import codechicken.lib.render.CCModel;
import codechicken.lib.render.CCRenderState.IVertexOperation;
import lombok.Data;

@Data
public class RenderEntry {
	private static final ResourceLocation	BLOCKS	= new ResourceLocation("textures/atlas/blocks.png");

	public RenderEntry(CCModel model, IVertexOperation[] operations, ResourceLocation texture) {
		this.model = model;
		this.operations = operations;
		this.texture = texture;
	}
	
	public RenderEntry(CCModel model, IVertexOperation[] operations) {
		this(model, operations, BLOCKS);
	}
	
	public RenderEntry(CCModel model) {
		this(model, new IVertexOperation[]{});
	}
	
	public RenderEntry(CCModel model, ResourceLocation texture) {
		this(model, new IVertexOperation[]{}, texture);
	}
	
	private final CCModel model;
	private final IVertexOperation[] operations;
	private final ResourceLocation texture;
}
