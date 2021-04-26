package logisticspipes.gui;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import logisticspipes.LPItems;
import logisticspipes.LogisticsPipes;
import logisticspipes.items.ItemUpgrade;
import logisticspipes.items.LogisticsItemCard;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.block.LogicControllerPacket;
import logisticspipes.network.packets.gui.OpenUpgradePacket;
import logisticspipes.network.packets.pipe.PipeManagerWatchingPacket;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.upgrades.IPipeUpgrade;
import logisticspipes.pipes.upgrades.SneakyUpgradeConfig;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.routing.order.IOrderInfoProvider;
import logisticspipes.utils.Color;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.GuiGraphics;
import logisticspipes.utils.gui.ItemDisplay;
import logisticspipes.utils.gui.LogisticsBaseTabGuiScreen;
import logisticspipes.utils.gui.SmallGuiButton;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.string.ChatColor;
import logisticspipes.utils.string.StringUtils;
import network.rs485.logisticspipes.util.TextUtil;
import network.rs485.logisticspipes.world.DoubleCoordinates;

public class GuiPipeController extends LogisticsBaseTabGuiScreen {

	private final String PREFIX = "gui.pipecontroller.";

	private final CoreRoutedPipe pipe;

	public GuiPipeController(final EntityPlayer player, final CoreRoutedPipe pipe) {
		super(180, 220);
		this.pipe = pipe;
		DummyContainer dummy = new DummyContainer(player, null, pipe.getOriginalUpgradeManager().getGuiController());
		dummy.addNormalSlotsForPlayerInventory(10, 135);

		//Order is important here: (Slot Server/Client sync)
		Upgrades upgrades = new Upgrades(dummy);
		Security security = new Security(dummy);
		Statistics statistics = new Statistics();
		//Logic logic = new Logic();
		addHiddenSlot(dummy.addRestrictedSlot(0, pipe.container.logicController.diskInv, 14, 36, LPItems.disk)); //Keep it for now, but hidden. Maybe it will be used again later
		Tasks tasks = new Tasks();

		//Here order doesn't matter/can be changed to reorganise tabs
		if (LogisticsPipes.isDEBUG()) {
			addTab(upgrades);
			addTab(security);
			addTab(statistics);
			//addTab(logic);
			addTab(tasks);
		} else {
			addTab(statistics);
			addTab(upgrades);
			addTab(security);
			//addTab(logic);
			addTab(tasks);
		}

		inventorySlots = dummy;
	}

	private class Upgrades extends TabSubGui {

		private final List<Slot> TAB_SLOTS_SNEAKY_INV = new ArrayList<>();
		private final Slot[] upgradeslot = new Slot[18];
		private GuiButton[] upgradeConfig = new GuiButton[18];

		private Upgrades(DummyContainer dummy) {
			for (int pipeSlot = 0; pipeSlot < 9; pipeSlot++) {
				addSlot(upgradeslot[pipeSlot] = dummy.addUpgradeSlot(pipeSlot, pipe.getOriginalUpgradeManager(), pipeSlot, 10 + pipeSlot * 18, 42, itemStack ->
						!itemStack.isEmpty() && itemStack.getItem() instanceof ItemUpgrade && ((ItemUpgrade) itemStack.getItem()).getUpgradeForItem(itemStack, null).isAllowedForPipe(pipe)));
			}

			for (int pipeSlot = 0; pipeSlot < 9; pipeSlot++) {
				TAB_SLOTS_SNEAKY_INV.add(addSlot(upgradeslot[pipeSlot + 9] = dummy.addSneakyUpgradeSlot(pipeSlot, pipe.getOriginalUpgradeManager(), pipeSlot + 9, 10 + pipeSlot * 18, 88, itemStack -> {
					if (itemStack.isEmpty()) {
						return false;
					}
					if (itemStack.getItem() instanceof ItemUpgrade) {
						IPipeUpgrade upgrade = ((ItemUpgrade) itemStack.getItem()).getUpgradeForItem(itemStack, null);
						return upgrade instanceof SneakyUpgradeConfig && upgrade.isAllowedForPipe(pipe);
					} else {
						return false;
					}
				})));
			}
		}

		@Override
		public void initTab() {
			int x = 0;
			int y = 0;
			for (int i = 0; i < upgradeConfig.length; i++) {
				upgradeConfig[i] = addButton(new SmallGuiButton(20 + i, guiLeft + 13 + x, guiTop + 61 + y, 10, 10, "!"));
				upgradeConfig[i].visible = pipe.getOriginalUpgradeManager().hasGuiUpgrade(i);
				x += 18;
				if (x > 160 && y == 0) {
					x = 0;
					y = 46;
				}
			}
		}

		@Override
		public void checkButton(GuiButton button, boolean isTabActive) {
			super.checkButton(button, isTabActive);
			for (int i = 0; i < upgradeConfig.length; i++) {
				upgradeConfig[i].visible &= pipe.getOriginalUpgradeManager().hasGuiUpgrade(i);
			}
		}

		@Override
		public boolean showSlot(Slot slot) {
			return pipe.getOriginalUpgradeManager().hasCombinedSneakyUpgrade() || !TAB_SLOTS_SNEAKY_INV.contains(slot);
		}

		@Override
		public void renderIcon(int x, int y) {
			GL11.glEnable(GL12.GL_RESCALE_NORMAL);
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240 / 1.0F, 240 / 1.0F);
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			RenderHelper.enableGUIStandardItemLighting();
			ItemStack stack = new ItemStack(ItemUpgrade.getAndCheckUpgrade(LPItems.upgrades.get(SneakyUpgradeConfig.getName())));
			itemRender.renderItemAndEffectIntoGUI(stack, x, y);
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			itemRender.zLevel = 0.0F;
		}

		@Override
		public void buttonClicked(GuiButton button) {
			for (int i = 0; i < upgradeConfig.length; i++) {
				if (upgradeConfig[i] == button) {
					MainProxy.sendPacketToServer(PacketHandler.getPacket(OpenUpgradePacket.class).setSlot(upgradeslot[i]));
				}
			}
		}

		@Override
		public void renderBackgroundContent() {
			for (int pipeSlot = 0; pipeSlot < 9; pipeSlot++) {
				GuiGraphics.drawSlotBackground(mc, guiLeft + 9 + pipeSlot * 18, guiTop + 41);
			}
			if (pipe.getOriginalUpgradeManager().hasCombinedSneakyUpgrade()) {
				for (int pipeSlot = 0; pipeSlot < 9; pipeSlot++) {
					GuiGraphics.drawSlotBackground(mc, guiLeft + 9 + pipeSlot * 18, guiTop + 87);
				}
			}
		}

		@Override
		public void renderForgroundContent() {
			fontRenderer.drawString(TextUtil.translate(PREFIX + "upgrade"), 10, 28, Color.getValue(Color.DARKER_GREY), false);
			if (pipe.getOriginalUpgradeManager().hasCombinedSneakyUpgrade()) {
				fontRenderer.drawString(TextUtil.translate(PREFIX + "sneakyUpgrades"), 10, 74, Color.getValue(Color.DARKER_GREY), false);
			}
		}
	}

	private class Security extends TabSubGui {

		public Security(DummyContainer dummy) {
			addSlot(dummy
					.addStaticRestrictedSlot(0, pipe.getOriginalUpgradeManager().secInv, 10, 42, itemStack -> {
						if (itemStack.isEmpty()) {
							return false;
						}
						if (itemStack.getItem() != LPItems.itemCard) {
							return false;
						}
						if (itemStack.getItemDamage() != LogisticsItemCard.SEC_CARD) {
							return false;
						}
						return SimpleServiceLocator.securityStationManager
								.isAuthorized(UUID.fromString(itemStack.getTagCompound().getString("UUID")));
					}, 1));
		}

		@Override
		public void renderIcon(int x, int y) {
			GuiGraphics.drawLockBackground(mc, x + 1, y);
		}

		@Override
		public void renderBackgroundContent() {
			GuiGraphics.drawSlotBackground(mc, guiLeft + 9, guiTop + 41);
		}

		@Override
		public void renderForgroundContent() {
			fontRenderer.drawString(TextUtil.translate(PREFIX + "security"), 10, 28, Color.getValue(Color.DARKER_GREY), false);
			ItemStack itemStack = pipe.getOriginalUpgradeManager().secInv.getStackInSlot(0);
			if (!itemStack.isEmpty()) {
				UUID id = UUID.fromString(itemStack.getTagCompound().getString("UUID"));
				fontRenderer.drawString("Id: ", 10, 68, Color.getValue(Color.DARKER_GREY), false);
				GL11.glTranslated(10, 80, 0);
				GL11.glScaled(0.75D, 0.75D, 1.0D);
				fontRenderer.drawString(ChatColor.BLUE.toString() + id.toString(), 0, 0, Color.getValue(Color.DARKER_GREY), false);
				GL11.glScaled(1 / 0.75D, 1 / 0.75D, 1.0D);
				GL11.glTranslated(-10, -80, 0);
				fontRenderer.drawString("Authorization: " + (SimpleServiceLocator.securityStationManager.isAuthorized(id) ? ChatColor.GREEN + "Authorized" : ChatColor.RED + "Deauthorized"), 10, 94, Color.getValue(Color.DARKER_GREY), false);
			}
		}
	}

	private class Statistics extends TabSubGui {

		@Override
		public void renderIcon(int x, int y) {
			GuiGraphics.drawStatsBackground(mc, x, y);
		}

		@Override
		public void renderBackgroundContent() {

		}

		@Override
		public void renderForgroundContent() {
			String pipeName = ItemIdentifier.get(pipe.item, 0, null).getFriendlyName();
			fontRenderer.drawString(pipeName, (170 - fontRenderer.getStringWidth(pipeName)) / 2, 28, 0x83601c);

			int sessionxCenter = 85;
			int lifetimexCenter = 140;
			String s;

			fontRenderer.drawString(TextUtil.translate(PREFIX + "Session"), sessionxCenter - fontRenderer
					.getStringWidth(TextUtil.translate(PREFIX + "Session")) / 2, 40, 0x303030);
			fontRenderer.drawString(TextUtil.translate(PREFIX + "Lifetime"), lifetimexCenter - fontRenderer
					.getStringWidth(TextUtil.translate(PREFIX + "Lifetime")) / 2, 40, 0x303030);
			fontRenderer.drawString(TextUtil.translate(PREFIX + "Sent") + ":", 55 - fontRenderer
					.getStringWidth(TextUtil.translate(PREFIX + "Sent") + ":"), 55, 0x303030);
			fontRenderer.drawString(TextUtil.translate(PREFIX + "Recieved") + ":", 55 - fontRenderer
					.getStringWidth(TextUtil.translate(PREFIX + "Recieved") + ":"), 70, 0x303030);
			fontRenderer.drawString(TextUtil.translate(PREFIX + "Relayed") + ":", 55 - fontRenderer
					.getStringWidth(TextUtil.translate(PREFIX + "Relayed") + ":"), 85, 0x303030);

			s = StringUtils.getStringWithSpacesFromLong(pipe.stat_session_sent);
			fontRenderer.drawString(s, sessionxCenter - fontRenderer.getStringWidth(s) / 2, 55, 0x303030);

			s = StringUtils.getStringWithSpacesFromLong(pipe.stat_session_recieved);
			fontRenderer.drawString(s, sessionxCenter - fontRenderer.getStringWidth(s) / 2, 70, 0x303030);

			s = StringUtils.getStringWithSpacesFromLong(pipe.stat_session_relayed);
			fontRenderer.drawString(s, sessionxCenter - fontRenderer.getStringWidth(s) / 2, 85, 0x303030);

			s = StringUtils.getStringWithSpacesFromLong(pipe.stat_lifetime_sent);
			fontRenderer.drawString(s, lifetimexCenter - fontRenderer.getStringWidth(s) / 2, 55, 0x303030);

			s = StringUtils.getStringWithSpacesFromLong(pipe.stat_lifetime_recieved);
			fontRenderer.drawString(s, lifetimexCenter - fontRenderer.getStringWidth(s) / 2, 70, 0x303030);

			s = StringUtils.getStringWithSpacesFromLong(pipe.stat_lifetime_relayed);
			fontRenderer.drawString(s, lifetimexCenter - fontRenderer.getStringWidth(s) / 2, 85, 0x303030);

			fontRenderer.drawString(TextUtil.translate(PREFIX + "RoutingTableSize") + ":", 110 - fontRenderer
					.getStringWidth(TextUtil.translate(PREFIX + "RoutingTableSize") + ":"), 110, 0x303030);

			s = StringUtils.getStringWithSpacesFromLong(pipe.server_routing_table_size);
			fontRenderer.drawString(s, 130 - fontRenderer.getStringWidth(s) / 2, 110, 0x303030);
		}
	}

	private class Logic extends TabSubGui {

		private GuiButton editButton;

		@Override
		public void initTab() {
			editButton = addButton(new GuiButton(0, guiLeft + 10, guiTop + 70, 160, 20, "Edit Logic Controller"));
		}

		@Override
		public void renderIcon(int x, int y) {
			GL11.glEnable(GL12.GL_RESCALE_NORMAL);
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240 / 1.0F, 240 / 1.0F);
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			RenderHelper.enableGUIStandardItemLighting();
			ItemStack stack2 = new ItemStack(Blocks.REDSTONE_TORCH);
			itemRender.renderItemAndEffectIntoGUI(stack2, x, y);
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			itemRender.zLevel = 0.0F;
		}

		@Override
		public void buttonClicked(GuiButton button) {
			if (button == editButton) {
				MainProxy.sendPacketToServer(PacketHandler.getPacket(LogicControllerPacket.class)
						.setTilePos(pipe.container));
			}
		}

		@Override
		public void renderBackgroundContent() {

			drawRect(guiLeft + 12, guiTop + 34, guiLeft + 32, guiTop + 54, Color.BLACK);
			drawRect(guiLeft + 14, guiTop + 36, guiLeft + 30, guiTop + 52, Color.DARKER_GREY);
		}

		@Override
		public void checkButton(GuiButton button, boolean isTabActive) {
			if (isTabActive) {
				button.enabled = pipe.container.logicController.diskInv.getStackInSlot(0) != null;
			}
			super.checkButton(button, isTabActive);
		}

		@Override
		public void renderForgroundContent() {

		}
	}

	private class Tasks extends TabSubGui {

		private GuiButton leftButton;
		private GuiButton rightButton;
		private ItemDisplay _itemDisplay_5;
		private boolean managerWatching;

		@Override
		public void initTab() {
			Keyboard.enableRepeatEvents(true);

			leftButton = addButton(new SmallGuiButton(1, guiLeft + 95, guiTop + 26, 10, 10, "<"));
			rightButton = addButton(new SmallGuiButton(2, guiLeft + 165, guiTop + 26, 10, 10, ">"));
			if (_itemDisplay_5 == null) {
				_itemDisplay_5 = new ItemDisplay(null, fontRenderer, GuiPipeController.this, null, 10, 40, 20, 60, 0, 0, 0, new int[] { 1, 1, 1, 1 }, true);
			}
			_itemDisplay_5.reposition(10, 40, 20, 60, 0, 0);
		}

		@Override
		public void renderIcon(int x, int y) {
			GuiGraphics.drawLinesBackground(mc, x, y);
		}

		@Override
		public void renderBackgroundContent() {

		}

		@Override
		public void leavingTab() {
			if (managerWatching) {
				managerWatching = false;
				MainProxy.sendPacketToServer(PacketHandler.getPacket(PipeManagerWatchingPacket.class).setStart(false).setTilePos(pipe.container));
			}
		}

		@Override
		public void enteringTab() {
			if (!managerWatching) {
				managerWatching = true;
				MainProxy.sendPacketToServer(PacketHandler.getPacket(PipeManagerWatchingPacket.class).setStart(true).setTilePos(pipe.container));
			}
		}

		@Override
		public void buttonClicked(GuiButton button) {
			if (button == leftButton) {
				_itemDisplay_5.prevPage();
			} else if (button == rightButton) {
				_itemDisplay_5.nextPage();
			}
		}

		@Override
		public void renderForgroundContent() {
			List<ItemIdentifierStack> _allItems = pipe.getClientSideOrderManager().stream()
					.map(IOrderInfoProvider::getAsDisplayItem).collect(Collectors.toCollection(LinkedList::new));
			_itemDisplay_5.setItemList(_allItems);
			_itemDisplay_5.renderItemArea(zLevel);
			_itemDisplay_5.renderPageNumber(right - guiLeft - 45, 28);
			int start = _itemDisplay_5.getPage() * 3;
			int stringPos = 40;
			for (int i = start; i < start + 3 && i < pipe.getClientSideOrderManager().size(); i++) {
				IOrderInfoProvider order = pipe.getClientSideOrderManager().get(i);
				ItemIdentifier target = order.getTargetType();
				String s;
				if (target != null) {
					s = target.getFriendlyName();
					fontRenderer.drawString(s, 35, stringPos, 0x303030);
				}
				s = Integer.toString(i + 1);
				stringPos += 6;
				fontRenderer.drawString(s, 3, stringPos, 0x303030);
				stringPos += 4;
				DoubleCoordinates pos = order.getTargetPosition();
				if (pos != null) {
					s = pos.toIntBasedString();
					fontRenderer.drawString(s, 40, stringPos, 0x303030);
				}
				stringPos += 10;
			}
		}
	}
}
