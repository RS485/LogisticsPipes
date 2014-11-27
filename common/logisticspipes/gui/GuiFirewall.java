package logisticspipes.gui;

import logisticspipes.pipes.PipeItemsFirewall;
import logisticspipes.utils.gui.BasicGuiHelper;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.GuiStringHandlerButton;
import logisticspipes.utils.gui.GuiStringHandlerButton.StringHandler;
import logisticspipes.utils.gui.LogisticsBaseGuiScreen;
import logisticspipes.utils.string.StringUtil;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;

public class GuiFirewall extends LogisticsBaseGuiScreen {
	private static final String PREFIX = "gui.firewall.";
	
	private PipeItemsFirewall pipe;
	
	public GuiFirewall(PipeItemsFirewall pipe, EntityPlayer player) {
		super(230, 260, 0, 0);
		this.pipe = pipe;
		DummyContainer dummy = new DummyContainer(player.inventory, pipe.inv);
		dummy.addNormalSlotsForPlayerInventory(33, 175);
		for(int x = 0;x < 6;x++) {
			for(int y = 0;y < 6;y++) {
				dummy.addDummySlot(x*6 + y, x*18 + 17, y*18 + 41);
			}
		}
		this.inventorySlots = dummy;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		super.initGui();
		buttonList.clear();
		final String blocked = StringUtil.translate(PREFIX + "Blocked");
		final String allowed = StringUtil.translate(PREFIX + "Allowed");
		buttonList.add(new GuiStringHandlerButton(0, width / 2 + 23, height / 2 + 27 - 139, 60 , 20, new StringHandler() {@Override public String getContent() {return pipe.isBlocking() ? blocked : allowed;}}));
		buttonList.add(new GuiStringHandlerButton(1, width / 2 + 23, height / 2 + 60 - 139, 60 , 20, new StringHandler() {@Override public String getContent() {return pipe.isBlockProvider() ? blocked : allowed;}}));
		buttonList.add(new GuiStringHandlerButton(2, width / 2 + 23, height / 2 + 93 - 139, 60 , 20, new StringHandler() {@Override public String getContent() {return pipe.isBlockCrafer() ? blocked : allowed;}}));
		buttonList.add(new GuiStringHandlerButton(3, width / 2 + 23, height / 2 + 126- 139, 60 , 20, new StringHandler() {@Override public String getContent() {return pipe.isBlockSorting() ? blocked : allowed;}}));
		buttonList.add(new GuiStringHandlerButton(4, width / 2 + 23, height / 2 + 160- 139, 60 , 20, new StringHandler() {@Override public String getContent() {return pipe.isBlockPower() ? blocked : allowed;}}));
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		switch (button.id) {
		case 0:
			pipe.setBlocking(!pipe.isBlocking());
			break;
		case 1:
			pipe.setBlockProvider(!pipe.isBlockProvider());
			break;
		case 2:
			pipe.setBlockCrafer(!pipe.isBlockCrafer());
			break;
		case 3:
			pipe.setBlockSorting(!pipe.isBlockSorting());
			break;
		case 4:
			pipe.setBlockPower(!pipe.isBlockPower());
			break;
		default:
			break;
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		BasicGuiHelper.drawGuiBackGround(mc, guiLeft, guiTop, right, bottom, zLevel, true);
		BasicGuiHelper.drawPlayerInventoryBackground(mc, guiLeft + 33, guiTop + 175);
		mc.fontRenderer.drawString(StringUtil.translate(PREFIX + "Firewall"), guiLeft + 45, guiTop + 8, 0x404040);
		mc.fontRenderer.drawString(StringUtil.translate(PREFIX + "Filter") + ":", guiLeft + 14, guiTop + 28, 0x404040);
		for(int x = 0;x < 6;x++) {
			for(int y = 0;y < 6;y++) {
				BasicGuiHelper.drawSlotBackground(mc, guiLeft + x*18 + 16, guiTop + y*18 + 40);
			}
		}
		mc.fontRenderer.drawString(StringUtil.translate(PREFIX + "Filtereditemsare") + ":", guiLeft + 125, guiTop + 8, 0x404040);
		mc.fontRenderer.drawString(StringUtil.translate(PREFIX + "Providing") + ":", guiLeft + 144, guiTop + 41, 0x404040);
		mc.fontRenderer.drawString(StringUtil.translate(PREFIX + "Crafting") + ":", guiLeft + 146, guiTop + 74, 0x404040);
		mc.fontRenderer.drawString(StringUtil.translate(PREFIX + "Sorting") + ":", guiLeft + 150, guiTop + 107, 0x404040);
		mc.fontRenderer.drawString(StringUtil.translate(PREFIX + "Powerflow") + ":", guiLeft + 142, guiTop + 141, 0x404040);
	}
}
