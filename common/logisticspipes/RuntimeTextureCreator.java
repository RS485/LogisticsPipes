package logisticspipes;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.awt.Graphics2D;
import java.awt.AlphaComposite;



import static org.lwjgl.opengl.GL11.*;

import net.minecraft.client.resources.IResourceManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.GlStateManager;

@SideOnly(Side.CLIENT)
public class RuntimeTextureCreator extends AbstractTexture
{
	private int[] dynamicTextureData;
	private int guiWidth;
	private int guiHeight;
	boolean finalized = false;

	public int widthColourTransition  = 3;
	public int heightColourTransition = 3;
	public int widthPadding  = 20;
	public int heightPadding = 4;

	public void loadTexture(IResourceManager resourceManager) throws IOException{};

	public RuntimeTextureCreator(int textureWidth, int textureHeight)
	{

	}


	//start from the bottomleft corner

	ArrayList<ArrayList<guiPart>> partLists = new ArrayList<ArrayList<guiPart>>();
	//Note: this works from bottom to top
	public void addGuipartlistHorizontal(ArrayList<guiPart> guiPartList){
		if(finalized)
			throw new RuntimeException();

		partLists.add(guiPartList);
	}


	public void finalize(){
		int partsWidth = 0;
		int partfor(int y=0;y<heightColourTransition)sHeight = 0;
		for(ArrayList<guiPart> row: partLists){
			int maxX, maxY =0;
			for(guiPart part: row){
				if(part.width > maxX)
					maxX = part.width;

				if(part.height > maxY)
					maxY = part.height;
			}
			textureWidth += maxX;
			textureHeight += maxY;
		}

		//Allocate
		this.guiWidth = textureWidth + 2*(widthColourTransition + widthPadding);
		this.guiHeight = textureHeight + 2*(heightColourTransition + heightPadding);
		this.dynamicTextureData = new int[textureWidth * textureHeight];
		TextureUtil.allocateTexture(this.getGlTextureId(), textureWidth, textureHeight);


		//create
		BufferedImage bi = new BufferedImage(guiWidth, guiHeight, TYPE_INT_ARGB);





		//upload
		bufferedImage.getRGB(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight(), this.dynamicTextureData, 0, bufferedImage.getWidth());
		TextureUtil.uploadTexture(this.getGlTextureId(), this.dynamicTextureData, this.width, this.height);

		//ib.wrap(dynamicTextureData,0, size);
		//GlStateManager.glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, width, height,  GL_RGBA,  GL_UNSIGNED_BYTE, ib);

		finalized= true;
	}

	public int[] getTextureData()
	{
		return this.dynamicTextureData;
	}

	/**
	 * prints the contents of buff2 on buff1 with the given opaque value.
	 */
	private void addImage(BufferedImage buff1, BufferedImage buff2, int x, int y) {
		Graphics2D g2d = buff1.createGraphics();
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0));
		g2d.drawImage(buff2, x, y, null);
		g2d.dispose();
	}
}

/*
	public DynamicTexture(BufferedImage bufferedImage)
	{
		this(bufferedImage.getWidth(), bufferedImage.getHeight());
		bufferedImage.getRGB(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight(), this.dynamicTextureData, 0, bufferedImage.getWidth());
		this.updateDynamicTexture();
	}

	public DynamicTexture(int textureWidth, int textureHeight)
	{
		this.width = textureWidth;
		this.height = textureHeight;
		this.dynamicTextureData = new int[textureWidth * textureHeight];
		TextureUtil.allocateTexture(this.getGlTextureId(), textureWidth, textureHeight);
	}

	public void loadTexture(IResourceManager resourceManager) throws IOException
	{
	}

	public void updateDynamicTexture()
	{
		TextureUtil.uploadTexture(this.getGlTextureId(), this.dynamicTextureData, this.width, this.height);
	}*/
