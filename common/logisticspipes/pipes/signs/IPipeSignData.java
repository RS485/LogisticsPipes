package logisticspipes.pipes.signs;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import logisticspipes.renderer.LogisticsRenderPipe;

public interface IPipeSignData {
	@SideOnly(Side.CLIENT)
	boolean isListCompatible(LogisticsRenderPipe render);
}
