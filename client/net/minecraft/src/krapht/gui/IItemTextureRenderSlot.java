package net.minecraft.src.krapht.gui;

import net.minecraft.client.Minecraft;

public abstract class IItemTextureRenderSlot implements IRenderSlot {
	public abstract int getTextureId();
	
	public abstract String getTextureFile();
	
	public abstract boolean drawSlotIcon();
	
	public abstract boolean customRender(Minecraft mc, float zLevel);

	public int getSize() {
		return 18;
	}
}
