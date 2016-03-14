package logisticspipes.utils.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class IItemTextureRenderSlot implements IRenderSlot {

	@SideOnly(Side.CLIENT)
	public abstract TextureAtlasSprite getTextureIcon();

	public abstract boolean drawSlotIcon();

	public abstract boolean customRender(Minecraft mc, float zLevel);

	@Override
	public int getSize() {
		return 18;
	}
}
