package logisticspipes.renderer.state;

import logisticspipes.renderer.newpipe.GLRenderList;
import logisticspipes.renderer.newpipe.RenderEntry;

import java.util.List;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PipeSubRenderState {
	@SideOnly(Side.CLIENT)
	public List<RenderEntry> cachedRenderer = null;
	public GLRenderList renderList;
	public boolean forceRenderOldPipe = false;
}
