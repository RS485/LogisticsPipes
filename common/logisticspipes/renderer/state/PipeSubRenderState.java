package logisticspipes.renderer.state;

import java.util.List;

import logisticspipes.renderer.newpipe.GLRenderList;
import logisticspipes.renderer.newpipe.RenderEntry;

public class PipeSubRenderState {

	public List<RenderEntry> cachedRenderer = null;
	public GLRenderList renderList;
	public boolean forceRenderOldPipe = false;
}
