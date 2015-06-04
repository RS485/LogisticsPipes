package logisticspipes.renderer.newpipe;

import java.util.List;

import logisticspipes.pipes.basic.CoreUnroutedPipe;

public interface ISpecialPipeRenderer {
	void renderToList(CoreUnroutedPipe pipe, List<RenderEntry> objectsToRender);
}
