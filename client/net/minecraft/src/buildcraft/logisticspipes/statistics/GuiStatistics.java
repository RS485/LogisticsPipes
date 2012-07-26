package net.minecraft.src.buildcraft.logisticspipes.statistics;

import java.awt.Color;
import java.util.HashMap;
import java.util.LinkedList;

import org.lwjgl.opengl.GL11;

import net.minecraft.src.Block;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.ItemStack;
import net.minecraft.src.ModLoader;
import net.minecraft.src.Tessellator;
import net.minecraft.src.mod_LogisticsPipes;
import net.minecraft.src.buildcraft.api.APIProxy;
import net.minecraft.src.buildcraft.core.CoreProxy;
import net.minecraft.src.buildcraft.krapht.GuiIDs;
import net.minecraft.src.buildcraft.krapht.network.NetworkConstants;
import net.minecraft.src.buildcraft.krapht.network.PacketPipeInteger;
import net.minecraft.src.buildcraft.logisticspipes.modules.IGuiIDHandlerProvider;
import net.minecraft.src.buildcraft.transport.Pipe;
import net.minecraft.src.krapht.ItemIdentifier;
import net.minecraft.src.krapht.gui.KraphtBaseGuiScreen;

public class GuiStatistics extends KraphtBaseGuiScreen {

	private final LinkedList<HashMap<ItemIdentifier, Integer>> _stats;
	private final ItemIdentifier _targetItem;
	private final GuiScreen _previousGui;
	private final EntityPlayer _player;
	private int prevGuiID = -1;
	private Pipe pipe;
	
	public GuiStatistics(LinkedList<HashMap<ItemIdentifier, Integer>> stats, ItemIdentifier targetItem, GuiScreen previousGui, EntityPlayer player, Pipe pipe) {
		super(250, 200, 0, 0);
		_targetItem = targetItem;
		_stats = stats;
		_previousGui = previousGui;
		_player = player;
		
		if(previousGui instanceof IGuiIDHandlerProvider) {
			this.prevGuiID = ((IGuiIDHandlerProvider)previousGui).getGuiID();
		}
		if(pipe == null) {
			throw new NullPointerException("A pipe can't be null");
		}
		this.pipe = pipe;
	}
	
	@Override
	protected void keyTyped(char c, int i) {
		if (i == 1 || c == 'e'){
			if (prevGuiID != -1){
				if(!APIProxy.isClient(mc.theWorld)) {
					_player.openGui(mod_LogisticsPipes.instance, prevGuiID, mc.theWorld, pipe.xCoord, pipe.yCoord, pipe.zCoord);
				} else {
					CoreProxy.sendToServer(new PacketPipeInteger(NetworkConstants.CHASSI_GUI_PACKET_ID, pipe.xCoord, pipe.yCoord, pipe.zCoord, prevGuiID).getPacket());
					super.keyTyped(c, i);
				}
			}
		}
	}
	
	@Override
	public void initGui() {
		super.initGui();
	}
	
	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

	@Override
	public void drawScreen(int i, int j, float f) {
		drawGuiBackGround();
		String s = this._targetItem.getFriendlyName() + " - History";
		fontRenderer.drawString(s, xCenter - fontRenderer.getStringWidth(s)/2, guiTop + 10, 0x404040);
		
		int xOrigo = xCenter - 95;
		int yOrigo = yCenter + 80;
		drawLine(xOrigo, yOrigo, xOrigo + 190, yOrigo, Colors.DarkGrey);
		drawLine(xOrigo, yOrigo, xOrigo, yOrigo - 160, Colors.DarkGrey);
		for(int k = -4; k < 5; k++){
			drawLine(xOrigo - 4, yCenter + k * 20, xOrigo + 5, yCenter + k * 20, Colors.DarkGrey);
		}
		for(int k = 0; k < 20; k++){
			drawLine(xOrigo + k * 10, yOrigo - 4, xOrigo + k * 10, yOrigo + 4, Colors.DarkGrey);
		}
		
		fontRenderer.drawString("20 minutes", xOrigo + 2, yOrigo + 6, 0x404040);
		fontRenderer.drawString("Now", xOrigo + 193, yOrigo + 6, 0x404040);
		

		if (_stats.size() < 1){
			return;
		}
		
		int lowest = Integer.MAX_VALUE;
		int highest = Integer.MIN_VALUE;
		for (HashMap<ItemIdentifier, Integer> dataPoint : _stats){
			
			int dataValue = dataPoint.containsKey(_targetItem)?dataPoint.get(_targetItem):0; 
			if (dataValue > highest) highest = dataValue;
			if (dataValue < lowest) lowest = dataValue;
		}
		
		float averagey = ((float)highest + lowest) / 2;
		
		fontRenderer.drawString(highest + "", xOrigo - 2 - fontRenderer.getStringWidth(highest + ""), guiTop + 12, 0x404040);
		fontRenderer.drawString((int)averagey + "", xOrigo - 2 - fontRenderer.getStringWidth((int)averagey +""), yCenter - 9, 0x404040);
		fontRenderer.drawString(lowest + "", xOrigo - 2 - fontRenderer.getStringWidth(lowest +""), bottom - 30, 0x404040);
		
		float yScale = 160F / Math.max(highest - lowest,1);
		
		int x = xOrigo;
		float yOff = (_stats.getFirst().containsKey(_targetItem)?_stats.getFirst().get(_targetItem):0) - averagey ;
		int y = (yOrigo - 160/2) - (int) (yOff * yScale);
		
		for (HashMap<ItemIdentifier, Integer> dataPoint : _stats){
			if (dataPoint == _stats.getFirst()) continue; 
			int x1 = x + 10;
			int dataValue = (dataPoint.containsKey(_targetItem)?dataPoint.get(_targetItem):0); 
			yOff =  dataValue - averagey;
			int y1 = (yOrigo - 160/2) - (int)(yOff * yScale);
			
			drawLine(x, y, x1, y1, Colors.Red);
			drawRect(x-1, y-1, x+2, y+2, Colors.Black);
			
			x = x1;
			y = y1;
		}
		drawRect(x-1, y-1, x+2, y+2, Colors.Black);
	}

	@Override
	public int getGuiID() {
		return GuiIDs.GUI_OrdererStats_ID;
	}
	
	
}
