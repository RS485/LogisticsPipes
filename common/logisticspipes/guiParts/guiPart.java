package logisticspipes.guiParts;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.Field;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.client.FMLClientHandler;

public class guiPart implements guiPartInterface{
	private static final MetadataSerializer metadataSerializer_ = new MetadataSerializer();
	BufferedImage bufferedimage;
	guiPart(ResourceLocation rl){
		bufferedimage = resourceToBufferedImage(rl);
	}

	public int getWidth() {
		return bufferedimage.getWidth();
	}

	public int getHeight() {
		return bufferedimage.getHeight();
	}

	public BufferedImage getBufferedImage(){
		return bufferedimage;
	}

	protected static BufferedImage resourceToBufferedImage(ResourceLocation rl){
		Minecraft mc  = FMLClientHandler.instance().getClient();
		IResourceManager IRM = mc.getResourceManager();

		BufferedImage bufferedimage = null;
		System.out.println("resourceToBufferedImage: " + rl);
		try{
			bufferedimage = TextureUtil.readBufferedImage(IRM.getResource(rl).getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		assert(bufferedimage!=null);
		return bufferedimage;
	}
}
