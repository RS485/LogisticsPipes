package logisticspipes.gui;

import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.upgrades.UpgradeManager;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.GuiGraphics;
import logisticspipes.utils.gui.LogisticsBaseGuiScreen;
import logisticspipes.utils.string.StringUtils;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

public class GuiUpgradeManager extends LogisticsBaseGuiScreen {

	private static final String PREFIX = "gui.upgrademanager.";

	private final UpgradeManager upgrade;
	private static final int SMALL_SIZE = 142;
	private static final int BIG_SIZE = 172;
	private final EntityPlayer player;
	private boolean init = false;

	public GuiUpgradeManager(EntityPlayer player, CoreRoutedPipe pipe) {
		super(175, GuiUpgradeManager.SMALL_SIZE, 0, 0);
		upgrade = pipe.getOriginalUpgradeManager();
		this.player = player;
		inventorySlots = upgrade.getDummyContainer(player);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		mc.fontRenderer.drawString(StringUtils.translate(GuiUpgradeManager.PREFIX + "Upgrades"), 8, 6, 0x404040);
		mc.fontRenderer.drawString(StringUtils.translate(GuiUpgradeManager.PREFIX + "Inventory"), 8, ySize - 92, 0x404040);
		if (upgrade.hasCombinedSneakyUpgrade()) {
			mc.fontRenderer.drawString(StringUtils.translate(GuiUpgradeManager.PREFIX + "SneakyUpgrades"), 8, 47, 0x404040);
		}
	}

	private static final ResourceLocation TEXTURE = new ResourceLocation("logisticspipes", "textures/gui/upgrade_manager.png");

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		if (upgrade.isNeedingContainerUpdate() || !init) {
			init = true;
			if (upgrade.hasCombinedSneakyUpgrade()) {
				ySize = GuiUpgradeManager.BIG_SIZE;
			} else {
				ySize = GuiUpgradeManager.SMALL_SIZE;
			}
			DummyContainer newSlots = upgrade.getDummyContainer(player);
			for (int i = 0; i < newSlots.inventorySlots.size(); i++) {
				Slot oldSlot = (Slot) inventorySlots.inventorySlots.get(i);
				Slot newSlot = (Slot) newSlots.inventorySlots.get(i);
				oldSlot.xDisplayPosition = newSlot.xDisplayPosition;
				oldSlot.yDisplayPosition = newSlot.yDisplayPosition;
			}
		}
		mc.renderEngine.bindTexture(GuiUpgradeManager.TEXTURE);
		if (!upgrade.hasCombinedSneakyUpgrade()) {
			drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		} else {
			drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, 55);
			drawTexturedModalRect(guiLeft, guiTop + 85, 0, 55, xSize, ySize);
			GuiGraphics.drawGuiBackGround(mc, guiLeft, guiTop + 40, right + 2, guiTop + 100, zLevel, true, false, true, false, true);
			for (int i = 0; i < 9; i++) {
				GuiGraphics.drawSlotBackground(mc, guiLeft + i * 18 + 7, guiTop + 57);
			}
		}
	}
}
