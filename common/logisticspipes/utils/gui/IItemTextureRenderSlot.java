package logisticspipes.utils.gui;

import javax.swing.Icon;

import net.minecraft.client.Minecraft;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class IItemTextureRenderSlot implements IRenderSlot {
	@SideOnly(Side.CLIENT)
	public abstract Icon getTextureIcon();
	
	public abstract boolean drawSlotIcon();
	
	public abstract boolean customRender(Minecraft mc, float zLevel);

	@Override
	public int getSize() {
		return 18;
	}
}
