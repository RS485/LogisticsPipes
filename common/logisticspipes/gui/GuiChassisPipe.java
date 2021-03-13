/*
 * Copyright (c) Krapht, 2011
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.gui;

import java.util.LinkedList;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import logisticspipes.config.Configs;
import logisticspipes.items.ItemModule;
import logisticspipes.modules.LogisticsModule;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.guis.pipe.ChassisGuiProvider;
import logisticspipes.network.packets.chassis.ChassisGUI;
import logisticspipes.network.packets.gui.GuiClosePacket;
import logisticspipes.network.packets.gui.OpenUpgradePacket;
import logisticspipes.pipes.PipeLogisticsChassis;
import logisticspipes.pipes.upgrades.ModuleUpgradeManager;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.GuiGraphics;
import logisticspipes.utils.gui.LogisticsBaseGuiScreen;
import logisticspipes.utils.gui.SmallGuiButton;
import logisticspipes.utils.string.StringUtils;
import network.rs485.logisticspipes.module.Gui;

public class GuiChassisPipe extends LogisticsBaseGuiScreen {

	private final PipeLogisticsChassis _chassiPipe;
	private final IInventory _moduleInventory;
	//private final GuiScreen _previousGui;
	private final List<SmallGuiButton> moduleConfigButtons = new LinkedList<>();

	private final Slot[] upgradeslot = new Slot[2 * Configs.CHASSIS_SLOTS_ARRAY[4]];
	private GuiButton[] upgradeConfig;

	private final boolean hasUpgradeModuleUpgarde;

	public GuiChassisPipe(EntityPlayer player, PipeLogisticsChassis chassis, boolean hasUpgradeModuleUpgarde) { //, GuiScreen previousGui) {
		super(null);
		_chassiPipe = chassis;
		_moduleInventory = chassis.getModuleInventory();
		//_previousGui = previousGui;
		this.hasUpgradeModuleUpgarde = hasUpgradeModuleUpgarde;

		DummyContainer dummy = new DummyContainer(player.inventory, _moduleInventory);
		dummy.addNormalSlotsForPlayerInventory(18, 9 + 20* _chassiPipe.getChassisSize());
		for (int i = 0; i < _chassiPipe.getChassisSize(); i++)
			dummy.addModuleSlot(i, _moduleInventory, 18, 9 + 20 * i, _chassiPipe);

		if (hasUpgradeModuleUpgarde) {
			for (int i = 0; i < _chassiPipe.getChassisSize(); i++) {
				final int fI = i;
				ModuleUpgradeManager upgradeManager = _chassiPipe.getModuleUpgradeManager(i);
				upgradeslot[i * 2] = dummy.addUpgradeSlot(0, upgradeManager, 0, 145, 9 + i * 20, itemStack -> ChassisGuiProvider.checkStack(itemStack, _chassiPipe, fI));
				upgradeslot[i * 2 + 1] = dummy.addUpgradeSlot(1, upgradeManager, 1, 165, 9 + i * 20, itemStack -> ChassisGuiProvider.checkStack(itemStack, _chassiPipe, fI));
			}
		}

		inventorySlots = dummy;


		int playerInventoryWidth = 162;
		int playerInventoryHeight = 76;

		xSize = playerInventoryWidth + 26;
		ySize = playerInventoryHeight + 14 + 20*_chassiPipe.getChassisSize();

	}

	@Override
	public void initGui() {
		super.initGui();

		int left = width / 2 - xSize / 2;
		int top = height / 2 - ySize / 2;

		buttonList.clear();
		moduleConfigButtons.clear();
		upgradeConfig = new GuiButton[_chassiPipe.getChassisSize() * 2];
		for (int i = 0; i < _chassiPipe.getChassisSize(); i++) {
			moduleConfigButtons.add(addButton(new SmallGuiButton(i, left + 5, top + 12 + 20 * i, 10, 10, "!")));
			if (_moduleInventory == null) {
				continue;
			}
			updateModuleConfigButtonVisibility(i);

			if (hasUpgradeModuleUpgarde) {
				upgradeConfig[i * 2] = addButton(new SmallGuiButton(100 + i, guiLeft + 134, guiTop + 12 + i * 20, 10, 10, "!"));
				upgradeConfig[i * 2].visible = _chassiPipe.getModuleUpgradeManager(i).hasGuiUpgrade(0);
				upgradeConfig[i * 2 + 1] = addButton(new SmallGuiButton(120 + i, guiLeft + 182, guiTop + 12 + i * 20, 10, 10, "!"));
				upgradeConfig[i * 2 + 1].visible = _chassiPipe.getModuleUpgradeManager(i).hasGuiUpgrade(1);
			}
		}
	}

	private void updateModuleConfigButtonVisibility(int slot) {
		ItemStack module = _moduleInventory.getStackInSlot(slot);
		LogisticsModule subModule = _chassiPipe.getSubModule(slot);
		if (module.isEmpty() || subModule == null) {
			moduleConfigButtons.get(slot).visible = false;
		} else {
			moduleConfigButtons.get(slot).visible = subModule instanceof Gui;
		}
	}

	@Override
	public void onGuiClosed() {
		MainProxy.sendPacketToServer(PacketHandler.getPacket(GuiClosePacket.class).setTilePos(_chassiPipe.container));
		super.onGuiClosed();
	}

	@Override
	protected void actionPerformed(GuiButton guibutton) {

		if (guibutton.id >= 0 && guibutton.id <= _chassiPipe.getChassisSize()) {
			LogisticsModule module = _chassiPipe.getSubModule(guibutton.id);
			if (module != null) {
				final ModernPacket packet = PacketHandler.getPacket(ChassisGUI.class).setButtonID(guibutton.id).setPosX(_chassiPipe.getX()).setPosY(_chassiPipe.getY()).setPosZ(_chassiPipe.getZ());
				MainProxy.sendPacketToServer(packet);
			}
		}
		for (int i = 0; i < upgradeConfig.length; i++) {
			if (upgradeConfig[i] == guibutton) {
				MainProxy.sendPacketToServer(PacketHandler.getPacket(OpenUpgradePacket.class).setSlot(upgradeslot[i]));
			}
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		super.drawGuiContainerForegroundLayer(par1, par2);
		for (int i = 0; i < _chassiPipe.getChassisSize(); i++) {
			updateModuleConfigButtonVisibility(i);
		}
		if (hasUpgradeModuleUpgarde) {
			for (int i = 0; i < upgradeConfig.length; i++) {
				upgradeConfig[i].visible = _chassiPipe.getModuleUpgradeManager(i / 2).hasGuiUpgrade(i % 2);
			}
		}
		for (int i = 0; i < _chassiPipe.getChassisSize(); i++)
			mc.fontRenderer.drawString(getModuleName(i), 40, 14 + 20 * i, 0x404040);
	}

	private String getModuleName(int slot) {
		if (_moduleInventory == null) {
			return "";
		}
		if (_moduleInventory.getStackInSlot(slot).isEmpty()) {
			return "";
		}
		if (!(_moduleInventory.getStackInSlot(slot).getItem() instanceof ItemModule)) {
			return "";
		}
		String name = _moduleInventory.getStackInSlot(slot).getItem().getItemStackDisplayName(_moduleInventory.getStackInSlot(slot));
		if (!hasUpgradeModuleUpgarde) {
			return name;
		}
		return StringUtils.getWithMaxWidth(name, 100, fontRenderer);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		GuiGraphics.drawGuiBackGround(mc, guiLeft, guiTop, right, bottom, zLevel, true);
		for (int i = 0; i < _chassiPipe.getChassisSize(); i++)
			GuiGraphics.drawSlotBackground(mc, guiLeft + 17, guiTop + 8 + 20 * i);

		GuiGraphics.drawPlayerInventoryBackground(mc, guiLeft + 18, guiTop + 9 + 20 * _chassiPipe.getChassisSize());

		if (hasUpgradeModuleUpgarde) {
			for (int i = 0; i < _chassiPipe.getChassisSize(); i++) {
				GuiGraphics.drawSlotBackground(mc, guiLeft + 144, guiTop + 8 + i * 20);
				GuiGraphics.drawSlotBackground(mc, guiLeft + 164, guiTop + 8 + i * 20);
			}
		}
	}
}
