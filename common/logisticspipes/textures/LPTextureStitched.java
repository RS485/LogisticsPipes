package logisticspipes.textures;

import net.minecraft.client.renderer.texture.TextureStitched;

public class LPTextureStitched extends TextureStitched {

	protected LPTextureStitched(String par1) {
		super(par1);
	}
	public void setAnimationFrame(int i)
	{
		this.frameCounter=i;
	}
}
