/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.krapht.gui;

import org.lwjgl.opengl.GL11;

import net.minecraft.src.Container;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiContainer;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.ModLoader;
import net.minecraft.src.mod_LogisticsPipes;
import buildcraft.api.APIProxy;
import buildcraft.core.CoreProxy;
import net.minecraft.src.buildcraft.krapht.GuiIDs;
import net.minecraft.src.buildcraft.krapht.network.NetworkConstants;
import net.minecraft.src.buildcraft.krapht.network.PacketPipeInteger;
import net.minecraft.src.buildcraft.krapht.pipes.PipeLogisticsChassi;
import net.minecraft.src.buildcraft.logisticspipes.items.ItemModule;
import net.minecraft.src.buildcraft.logisticspipes.modules.IGuiIDHandlerProvider;
import net.minecraft.src.buildcraft.logisticspipes.modules.ILogisticsModule;
import net.minecraft.src.forge.ForgeHooks;
import net.minecraft.src.krapht.SimpleInventory;
import net.minecraft.src.krapht.gui.DummyContainer;
import net.minecraft.src.krapht.gui.ISubGuiControler;
import net.minecraft.src.krapht.gui.SmallGuiButton;

public class GuiChassiPipe extends GuiContainer implements IGuiIDHandlerProvider {
	
	private final PipeLogisticsChassi _chassiPipe;
	private final EntityPlayer _player;
	private final IInventory _moduleInventory;
	//private final GuiScreen _previousGui;
	
	private int left;
	private int top;
	
	public GuiChassiPipe(EntityPlayer player, PipeLogisticsChassi chassi) { //, GuiScreen previousGui) {
		super(null);
		_player = player;
		_chassiPipe = chassi;
		_moduleInventory = chassi.getModuleInventory();
		//_previousGui = previousGui;
		
		
		DummyContainer dummy = new DummyContainer(_player.inventory, _moduleInventory);
		if (_chassiPipe.getChassiSize() < 5){
			dummy.addNormalSlotsForPlayerInventory(18, 97);
		} else {
			dummy.addNormalSlotsForPlayerInventory(18, 174);
		}
		if (_chassiPipe.getChassiSize() > 0) dummy.addModuleSlot(0, _moduleInventory, 19, 9, _chassiPipe);
		if (_chassiPipe.getChassiSize() > 1) dummy.addModuleSlot(1, _moduleInventory, 19, 29, _chassiPipe);
		if (_chassiPipe.getChassiSize() > 2) dummy.addModuleSlot(2, _moduleInventory, 19, 49, _chassiPipe);
		if (_chassiPipe.getChassiSize() > 3) dummy.addModuleSlot(3, _moduleInventory, 19, 69, _chassiPipe);
		if (_chassiPipe.getChassiSize() > 4) {
			dummy.addModuleSlot(4, _moduleInventory, 19, 89, _chassiPipe);
			dummy.addModuleSlot(5, _moduleInventory, 19, 109, _chassiPipe);
			dummy.addModuleSlot(6, _moduleInventory, 19, 129, _chassiPipe);
			dummy.addModuleSlot(7, _moduleInventory, 19, 149, _chassiPipe);
		}
		
		this.inventorySlots = dummy;
		
		xSize = 194;
		ySize = 186;
		
		if (_chassiPipe.getChassiSize() > 4) ySize = 256;

	}
	
	@Override
	public void initGui() {
		super.initGui();
		
		left = width / 2 - xSize / 2;
		top = height /2 - ySize / 2;
		
		controlList.clear();
		for (int i = 0; i < _chassiPipe.getChassiSize(); i++){
			controlList.add(new SmallGuiButton(i, left + 5, top + 12 + 20 * i, 10, 10, "!"));
			if(_moduleInventory == null) continue;
			ItemStack module = _moduleInventory.getStackInSlot(i);
			if(module == null || _chassiPipe.getLogisticsModule().getSubModule(i) == null) {
				((SmallGuiButton)controlList.get(i)).drawButton = false;
			} else {
				((SmallGuiButton)controlList.get(i)).drawButton = _chassiPipe.getLogisticsModule().getSubModule(i).getGuiHandlerID() != -1;
			}
		}
	}
	
	@Override
	protected void actionPerformed(GuiButton guibutton) {
		
		if (guibutton.id >= 0 && guibutton.id <= 7){
			ILogisticsModule module = _chassiPipe.getLogisticsModule().getSubModule(guibutton.id);
			if (module != null){
				if(!APIProxy.isClient(_player.worldObj)) {
					_player.openGui(mod_LogisticsPipes.instance, module.getGuiHandlerID() + (100 * (guibutton.id + 1)), _chassiPipe.worldObj, _chassiPipe.xCoord, _chassiPipe.yCoord, _chassiPipe.zCoord);	
				} else {
					CoreProxy.sendToServer(new PacketPipeInteger(NetworkConstants.CHASSI_GUI_PACKET_ID,_chassiPipe.xCoord,_chassiPipe.yCoord,_chassiPipe.zCoord,guibutton.id).getPacket());
				}
			}
		}
	}

	
	@Override
	protected void drawGuiContainerForegroundLayer() {
		super.drawGuiContainerForegroundLayer();
		for (int i = 0; i < _chassiPipe.getChassiSize(); i++) {
			ItemStack module = _moduleInventory.getStackInSlot(i);
			if(module == null || _chassiPipe.getLogisticsModule().getSubModule(i) == null) {
				((SmallGuiButton)controlList.get(i)).drawButton = false;
			} else {
				((SmallGuiButton)controlList.get(i)).drawButton = _chassiPipe.getLogisticsModule().getSubModule(i).getGuiHandlerID() != -1;
			}
		}
		if (_chassiPipe.getChassiSize() > 0) {
			fontRenderer.drawString(getModuleName(0), 40, 14, 0x404040);
		}
		if (_chassiPipe.getChassiSize() > 1) {
			fontRenderer.drawString(getModuleName(1), 40, 34, 0x404040);
		}
		if (_chassiPipe.getChassiSize() > 2) {
			fontRenderer.drawString(getModuleName(2), 40, 54, 0x404040);
		}
		if (_chassiPipe.getChassiSize() > 3) {
			fontRenderer.drawString(getModuleName(3), 40, 74, 0x404040);
		}
		if (_chassiPipe.getChassiSize() > 4) {
			fontRenderer.drawString(getModuleName(4), 40, 94, 0x404040);
			fontRenderer.drawString(getModuleName(5), 40, 114, 0x404040);
			fontRenderer.drawString(getModuleName(6), 40, 134, 0x404040);
			fontRenderer.drawString(getModuleName(7), 40, 154, 0x404040);
		}
	}
	
	private String getModuleName(int slot){
		if (this._moduleInventory == null) return "";
		if (this._moduleInventory.getStackInSlot(slot) == null) return "";
		if (!(this._moduleInventory.getStackInSlot(slot).getItem() instanceof ItemModule)) return "";
		return ((ItemModule)this._moduleInventory.getStackInSlot(slot).getItem()).getItemDisplayName(this._moduleInventory.getStackInSlot(slot));
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		
		int i = mc.renderEngine.getTexture("/logisticspipes/gui/chassipipe_size"+ _chassiPipe.getChassiSize() +".png");
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(i);
		int j = (width - xSize) / 2;
		int k = (height - ySize) / 2;
		drawTexturedModalRect(j, k, 0, 0, xSize, ySize);
	}

	@Override
	public int getGuiID() {
		return GuiIDs.GUI_ChassiModule_ID;
	}
}
