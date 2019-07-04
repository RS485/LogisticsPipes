package logisticspipes.guiParts;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.Field;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureUtil;
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
		SimpleReloadableResourceManager SRRM = null; //= new SimpleReloadableResourceManager(metadataSerializer_);
		Minecraft mc  = FMLClientHandler.instance().getClient();
		try {
			Field f = mc.getClass().getDeclaredField("mcResourceManager");
			f.setAccessible(true);
			SRRM = (SimpleReloadableResourceManager) f.get(mc);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		BufferedImage bufferedimage = null;
		System.out.println("resourceToBufferedImage: " + rl);
		try{
			bufferedimage = TextureUtil.readBufferedImage(SRRM.getResource(rl).getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		assert(bufferedimage!=null);
		return bufferedimage;
	}
}
