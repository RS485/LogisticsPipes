package logisticspipes;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.awt.Graphics2D;
import java.awt.AlphaComposite;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
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
	private static boolean inited = false;

	public int widthColourTransition  = 3;
	public int heightColourTransition = 3;
	public int widthPadding  = 20;
	public int heightPadding = 4;


	public void loadTexture(IResourceManager resourceManager) throws IOException{};

	public RuntimeTextureCreator()
	{
		inited = true;
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
		int partsWidth = 128;//0;
		int partHeight = 128;//0;
		for(ArrayList<guiPart> row: partLists){
			int maxX = 0;
			int maxY = 0;
			for(guiPart part: row){
				if(part.getWidth() > maxX)
					maxX = part.getWidth();

				if(part.getHeight() > maxY)
					maxY = part.getHeight();
			}
			partsWidth += maxX;
			partHeight += maxY;
		}

		//Allocatefinalize
		this.guiWidth = partsWidth + 2*(widthColourTransition + widthPadding);
		this.guiHeight = partHeight + 2*(heightColourTransition + heightPadding);
		this.dynamicTextureData = new int[guiWidth * guiHeight];
		TextureUtil.allocateTexture(this.getGlTextureId(), partsWidth, guiHeight);


		//create
		BufferedImage bi = new BufferedImage(guiWidth, guiHeight, TYPE_INT_ARGB);


		System.out.println("this.getGlTextureId()" + this.getGlTextureId());
		for(int i=0; i<guiWidth && i<guiHeight; i++) {
			/*System.out.println("AAHHHHHH" + i +", "+  i);*/
			bi.setRGB(i, i, 1123);
		}


		//upload
		bi.getRGB(0, 0, bi.getWidth(), bi.getHeight(), this.dynamicTextureData, 0, bi.getWidth());
		TextureUtil.uploadTexture(this.getGlTextureId(), this.dynamicTextureData, this.guiWidth, this.guiHeight);

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
