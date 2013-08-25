/*
 * @Author: gejzer
 */
package logisticspipes.textures;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import net.minecraft.client.Minecraft;

public class OverlayManager {
/*	public static BufferedImage generateOverlay(String bg, String fg) throws IOException
	{
		String basicpath="/mods/logisticspipes/textures/blocks";
		
		// load source images
		BufferedImage image = loadImageFromTexturePack(Minecraft.getMinecraft().renderEngine, basicpath+"/"+bg);
		BufferedImage overlay = loadImageFromTexturePack(Minecraft.getMinecraft().renderEngine, basicpath+"/"+fg);

		// create the new image, canvas size is the max. of both image sizes
		int w = Math.max(image.getWidth(), overlay.getWidth());
		int h = Math.max(image.getHeight(), overlay.getHeight());
		BufferedImage combined = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

		// paint both images, preserving the alpha channels
		
		Graphics2D g = combined.createGraphics();
		g.drawImage(image, 0, 0, null);
		g.drawImage(overlay, 0, 0, null);
		g.dispose();
	
		return combined;
	}
	public static BufferedImage loadImageFromTexturePack(RenderEngine renderEngine, String path) throws IOException
    {
        InputStream image=Minecraft.getMinecraft().texturePackList.getSelectedTexturePack().getResourceAsStream(path);
        if (image==null) {
            throw new RuntimeException(String.format("The requested image path %s is not found",path));
        }
        BufferedImage result=ImageIO.read(image);
        if (result==null)
        {
            throw new RuntimeException(String.format("The requested image path %s appears to be corrupted",path));
        }
        return result;
    }*/
}
