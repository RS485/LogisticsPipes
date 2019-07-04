package logisticspipes;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.awt.Graphics2D;
import java.awt.AlphaComposite;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;

import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.IResourceManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import logisticspipes.config.Configs;
import logisticspipes.guiParts.guiPart;
import logisticspipes.guiParts.guiParts;

@SideOnly(Side.CLIENT)
public class RuntimeTextureCreator
{
	private ArrayList<Integer> guiWidths      = new ArrayList<>();
	private ArrayList<Integer> guiHeights     = new ArrayList<>();
	private ArrayList<DynamicTexture> dtArray = new ArrayList<>();


	boolean finalized = false;

	public final int widthColourTransition  = 3;
	public final int heightColourTransition = 3;
	//between the edges of the GUI
	public final int widthPaddingLeft  = 18;
	public final int widthPaddingRight = 3;
	public final int heightPadding = 5;
	//Between opbjects
	public final int standardYPadding = 2;
	public final int standardXPadding = 4;

	public RuntimeTextureCreator()
	{
		createAll();
	}

	public void createAll(){
		for(int i=0; i<Configs.CHASSI_SLOTS_ARRAY.length; i++){
			ArrayList<ArrayList<guiPart>> partLists = new ArrayList<ArrayList<guiPart>>();
			ArrayList<guiPart> al_PI = new ArrayList<>();
			al_PI.add(guiParts.playerInventory);
			partLists.add(al_PI);
			for(int j=0; j<Configs.CHASSI_SLOTS_ARRAY[i]; j++) {
				ArrayList<guiPart> al = new ArrayList<>();
				al.add(guiParts.normalSlot);
				partLists.add(al);
			}
			create(partLists);
		}
		finalized = true;
	}

	//Note: partLists start from the bottomleft corner
	public void create(ArrayList<ArrayList<guiPart>> partLists){
		int partsWidth = 0;
		int partHeight = 0;
		for(ArrayList<guiPart> row: partLists){
			int currX = 0;
			int maxY  = 0;
			for(guiPart part: row){
				assert(part != null);
				assert(part.getBufferedImage() != null);
				currX += part.getWidth() + standardXPadding;
				if(part.getHeight() > maxY)
					maxY = part.getHeight();
			}
			currX -= standardXPadding;
			if(currX > partsWidth)
				partsWidth = currX;
			partHeight += maxY + standardYPadding;
		}
		partHeight -= standardYPadding;

		//Allocate
		int guiWidth  = partsWidth + 2*(widthColourTransition) + widthPaddingLeft + widthPaddingRight;
		int guiHeight = partHeight + 2*(heightColourTransition + heightPadding);

		int realGuiHeight = getNextPowerOf2(guiHeight)>256? getNextPowerOf2(guiHeight): 256;
		int realGuiWidth  = getNextPowerOf2(guiWidth) >256? getNextPowerOf2(guiWidth):  256;
		//create
		BufferedImage bi = new BufferedImage(realGuiWidth, realGuiHeight, TYPE_INT_ARGB);

		//remember (0,0) is top left >.>
		int y=guiHeight-heightPadding;
		for(ArrayList<guiPart> parts: partLists){
			int x = widthPaddingLeft;
			int locMaxY = 0;
			for(guiPart part: parts){
				if(locMaxY < part.getHeight())
					locMaxY = part.getHeight();
			}
			y -= locMaxY + standardYPadding;
			for(guiPart part: parts){
				addImage(bi, part.getBufferedImage(), x,y, 1);
				x += part.getWidth() + standardXPadding;
			}


		}

		DynamicTexture dt = new DynamicTexture(bi);

		guiWidths.add(guiWidth);
		guiHeights.add(guiHeight);
		dtArray.add(dt);
	}

	public static int getNextPowerOf2(int x){
		int x_ = x;
		int i=0;
		while(x_>0){
			x_ = x_ >> 1;
			i++;
		}
		return 1 << i;
	}

	public DynamicTexture getDynamicTexture(int chassiSize){
		return dtArray.get(chassiSizeToIndex(chassiSize));
	}
	public int getWidth(int chassiSize){
		return guiWidths.get(chassiSizeToIndex(chassiSize));
	}
	public int getHeight(int chassiSize){
		return guiHeights.get(chassiSizeToIndex(chassiSize));
	}

	protected int chassiSizeToIndex(int chassiSize) {
		for (int i = 0; i < Configs.CHASSI_SLOTS_ARRAY.length; i++){
			if (chassiSize == Configs.CHASSI_SLOTS_ARRAY[i]) {
				return i;
			}
		}

		return -1;
	}

	private void addImage(BufferedImage buff1, BufferedImage buff2, int x, int y, float alpha) {
		Graphics2D g2d = buff1.createGraphics();
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
		g2d.drawImage(buff2, x, y, null);
		g2d.dispose();
	}
}