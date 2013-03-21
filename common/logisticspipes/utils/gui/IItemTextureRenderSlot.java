package logisticspipes.utils.gui;

import net.minecraft.client.Minecraft;

public abstract class IItemTextureRenderSlot implements IRenderSlot {
	public abstract int getTextureId();
	
	public abstract Icon getTextureIcon() ;
	
	public abstract boolean drawSlotIcon();
	
	public abstract boolean customRender(Minecraft mc, float zLevel);

	@Override
	public int getSize() {
		return 18;
	}
}
