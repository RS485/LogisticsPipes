/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.gui;

import logisticspipes.interfaces.IGuiIDHandlerProvider;
import logisticspipes.items.ItemModule;
import logisticspipes.modules.LogisticsGuiModule;
import logisticspipes.modules.LogisticsModule;
import logisticspipes.network.GuiIDs;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.packets.chassis.ChassisGUI;
import logisticspipes.pipes.PipeLogisticsChassi;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.KraphtBaseGuiScreen;
import logisticspipes.utils.gui.SmallGuiButton;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

public class GuiChassiPipe extends KraphtBaseGuiScreen implements IGuiIDHandlerProvider {

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
	
	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		super.initGui();
		
		left = width / 2 - xSize / 2;
		top = height /2 - ySize / 2;
		
		buttonList.clear();
		for (int i = 0; i < _chassiPipe.getChassiSize(); i++){
			buttonList.add(new SmallGuiButton(i, left + 5, top + 12 + 20 * i, 10, 10, "!"));
			if(_moduleInventory == null) continue;
			ItemStack module = _moduleInventory.getStackInSlot(i);
			if(module == null || _chassiPipe.getLogisticsModule().getSubModule(i) == null) {
				((SmallGuiButton)buttonList.get(i)).drawButton = false;
			} else {
				((SmallGuiButton)buttonList.get(i)).drawButton = _chassiPipe.getLogisticsModule().getSubModule(i) instanceof LogisticsGuiModule;
			}
		}
	}
	
	@Override
	protected void actionPerformed(GuiButton guibutton) {
		
		if (guibutton.id >= 0 && guibutton.id <= 7){
			LogisticsModule module = _chassiPipe.getLogisticsModule().getSubModule(guibutton.id);
			if (module != null){
				final ModernPacket packet = PacketHandler
						.getPacket(ChassisGUI.class).setButtonID(guibutton.id)
						.setPosX(_chassiPipe.getX())
						.setPosY(_chassiPipe.getY())
						.setPosZ(_chassiPipe.getZ());
//TODO Must be handled manualy
				MainProxy.sendPacketToServer(packet);
			}
		}
	}

	
	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		super.drawGuiContainerForegroundLayer(par1, par2);
		for (int i = 0; i < _chassiPipe.getChassiSize(); i++) {
			ItemStack module = _moduleInventory.getStackInSlot(i);
			if(module == null || _chassiPipe.getLogisticsModule().getSubModule(i) == null) {
				((SmallGuiButton)buttonList.get(i)).drawButton = false;
			} else {
				((SmallGuiButton)buttonList.get(i)).drawButton = _chassiPipe.getLogisticsModule().getSubModule(i) instanceof LogisticsGuiModule;
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
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.func_110577_a(_chassiPipe.getChassiGUITexture());
		int j = guiLeft;
		int k = guiTop;
		drawTexturedModalRect(j, k, 0, 0, xSize, ySize);
	}


	@Override
	public int getGuiID() {
		return GuiIDs.GUI_ChassiModule_ID;
	}
}
