package logisticspipes.proxy.interfaces;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelSign;

public interface IBetterSignProxy {
	public @SideOnly(Side.CLIENT) void hideSignSticks(ModelSign model);
}
