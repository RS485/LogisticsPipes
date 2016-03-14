package logisticspipes.pipes.signs;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import logisticspipes.renderer.LogisticsRenderPipe;

public interface IPipeSignData {
	@SideOnly(Side.CLIENT)
	boolean isListCompatible(LogisticsRenderPipe render);
}
