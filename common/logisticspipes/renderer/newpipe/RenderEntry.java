package logisticspipes.renderer.newpipe;

import logisticspipes.proxy.object3d.interfaces.I3DOperation;
import logisticspipes.proxy.object3d.interfaces.IModel3D;

import net.minecraft.util.ResourceLocation;

import lombok.Data;

@Data
public class RenderEntry {

	private static final ResourceLocation BLOCKS = new ResourceLocation("textures/atlas/blocks.png");

	public RenderEntry(IModel3D model, I3DOperation[] operations, ResourceLocation texture) {
		this.model = model;
		this.operations = operations;
		this.texture = texture;
	}

	public RenderEntry(IModel3D model, I3DOperation[] operations) {
		this(model, operations, RenderEntry.BLOCKS);
	}

	public RenderEntry(IModel3D model) {
		this(model, new I3DOperation[] {});
	}

	public RenderEntry(IModel3D model, ResourceLocation texture) {
		this(model, new I3DOperation[] {}, texture);
	}

	private final IModel3D model;
	private final I3DOperation[] operations;
	private final ResourceLocation texture;
}
