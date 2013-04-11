package logisticspipes.textures;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.IOException;

import net.minecraft.client.renderer.RenderEngine;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.FMLTextureFX;
import cpw.mods.fml.client.TextureFXManager;

public class LogisticsPipesTextureStatic extends FMLTextureFX {
    private boolean oldanaglyph = false;
    private int[] pixels = null;
    private String targetTex = null;
    private int storedSize;
    private String sourceTex = null;
    private String overlayTex = null;
    private int needApply = 10;

    public LogisticsPipesTextureStatic(int icon, String source, String overlay)
    {
        super(icon);
        RenderEngine re = FMLClientHandler.instance().getClient().renderEngine;

        targetTex = Textures.BASE_TEXTURE_FILE;
        storedSize = 1;
        tileSize = 1;
        tileImage = re.getTexture(Textures.BASE_TEXTURE_FILE);
        sourceTex = source;
        overlayTex = overlay;
    }

    @Override
    public void setup() {
        super.setup();
       
		try {
	        RenderEngine re = FMLClientHandler.instance().getClient().renderEngine;
			BufferedImage overrideData = TextureFXManager.instance().loadImageFromTexturePack(re, sourceTex);
			BufferedImage overlayData = TextureFXManager.instance().loadImageFromTexturePack(re, overlayTex);
	        int sWidth  = overrideData.getWidth();
	        int sHeight = overrideData.getHeight();

	        int[] pixelbuffera = new int[tileSizeSquare];
	        if (tileSizeBase == sWidth && tileSizeBase == sHeight)
	        {
	            overrideData.getRGB(0, 0, sWidth, sHeight, pixelbuffera, 0, sWidth);
	        }
	        else
	        {
	            BufferedImage tmp = new BufferedImage(tileSizeBase, tileSizeBase, 6);
	            Graphics2D gfx = tmp.createGraphics();
	            gfx.drawImage(overrideData, 0, 0, tileSizeBase, tileSizeBase, 0, 0, sWidth, sHeight, (ImageObserver)null);
	            tmp.getRGB(0, 0, tileSizeBase, tileSizeBase, pixelbuffera, 0, tileSizeBase);
	            gfx.dispose();
	        }
	        sWidth  = overlayData.getWidth();
	        sHeight = overlayData.getHeight();
	
	        int[] pixelbufferb = new int[tileSizeSquare];
	        if (tileSizeBase == sWidth && tileSizeBase == sHeight)
	        {
	        	overlayData.getRGB(0, 0, sWidth, sHeight, pixelbufferb, 0, sWidth);
	        }
	        else
	        {
	            BufferedImage tmp = new BufferedImage(tileSizeBase, tileSizeBase, 6);
	            Graphics2D gfx = tmp.createGraphics();
	            gfx.drawImage(overlayData, 0, 0, tileSizeBase, tileSizeBase, 0, 0, sWidth, sHeight, (ImageObserver)null);
	            tmp.getRGB(0, 0, tileSizeBase, tileSizeBase, pixelbufferb, 0, tileSizeBase);
	            gfx.dispose();
	        }

	        pixels = new int[tileSizeSquare];
	        //Add Overlay
	        for (int idx = 0; idx < pixels.length; idx++) {
	        	double a1 = ((double)(pixelbufferb[idx] >> 24 & 255));
	        	double r1 = ((double)(pixelbufferb[idx] >> 16 & 255));
	        	double g1 = ((double)(pixelbufferb[idx] >> 8 & 255));
	        	double b1 = ((double)(pixelbufferb[idx] >> 0 & 255));
	        	double a2 = ((double)(pixelbuffera[idx] >> 24 & 255));
	        	double r2 = ((double)(pixelbuffera[idx] >> 16 & 255));
	        	double g2 = ((double)(pixelbuffera[idx] >> 8 & 255));
	        	double b2 = ((double)(pixelbuffera[idx] >> 0 & 255));

	        	a1 /= 255;
	        	r1 /= 255;
	        	g1 /= 255;
	        	b1 /= 255;
	        	a2 /= 255;
	        	r2 /= 255;
	        	g2 /= 255;
	        	b2 /= 255;

	            double a = (a1 + a2 * (1 - a1));
	            double r = (r1 * a1 + r2 * a2 * (1 - a1));
	            double g = (g1 * a1 + g2 * a2 * (1 - a1));
	            double b = (b1 * a1 + b2 * a2 * (1 - a1));
	            
	            a *= 255;
	            r *= 255;
	            g *= 255;
	            b *= 255;
	            
	            pixels[idx] = (((int)a) << 24) | (((int)r) << 16) | (((int)g) << 8) | ((int)b);
	        }
	        update();
		} catch (IOException e) {
			throw new UnsupportedOperationException();
		}
    }

    @Override
    public void onTick()
    {
        if (oldanaglyph != anaglyphEnabled)
        {
            update();
        }
        // This makes it so we only apply the texture to the target texture when we need to,
        //due to the fact that update is called when the Effect is first registered, we actually
        //need to wait for the next one.
        tileSize = (needApply == 0 ? 0 : storedSize);
        if (needApply > 0)
        {
            needApply--;
        }
    }

    @Override
    public void bindImage(RenderEngine par1RenderEngine)
    {
        GL11.glBindTexture(GL_TEXTURE_2D, par1RenderEngine.getTexture(targetTex));
    }

    public void update()
    {
        needApply = 10;
        for (int idx = 0; idx < pixels.length; idx++)
        {
            int i = idx * 4;
            int a = pixels[idx] >> 24 & 255;
            int r = pixels[idx] >> 16 & 255;
            int g = pixels[idx] >> 8 & 255;
            int b = pixels[idx] >> 0 & 255;

            if (anaglyphEnabled)
            {
                r = g = b = (r + g + b) / 3;
            }

            imageData[i + 0] = (byte)r;
            imageData[i + 1] = (byte)g;
            imageData[i + 2] = (byte)b;
            imageData[i + 3] = (byte)a;
        }

        oldanaglyph = anaglyphEnabled;
    }
    
    @Override
    public String toString() {
        return String.format("LogisticsPipesTextureStatic %s @ %d", targetTex, iconIndex);
    }
}
