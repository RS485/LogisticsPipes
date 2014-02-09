package logisticspipes.gui.orderer;

import java.util.ArrayList;
import java.util.Map.Entry;

import logisticspipes.interfaces.ISlotClick;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.orderer.OrdererRefreshRequestPacket;
import logisticspipes.network.packets.orderer.RequestSubmitPacket;
import logisticspipes.pipes.PipeBlockRequestTable;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.gui.BasicGuiHelper;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.SmallGuiButton;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class GuiRequestTable extends GuiOrderer {

	private enum DisplayOptions {
		Both,
		SupplyOnly,
		CraftOnly,
	}

	protected DisplayOptions displayOptions = DisplayOptions.Both;
	public final PipeBlockRequestTable _table;
	
	public GuiRequestTable(EntityPlayer entityPlayer, PipeBlockRequestTable table) {
		super(table.getX(), table.getY(), table.getZ(), MainProxy.getDimensionForWorld(table.getWorld()), entityPlayer);
		_table = table;
		
		DummyContainer dummy = new DummyContainer(entityPlayer.inventory, _table.matrix);
		int i = 0;
		for(int y = 0;y < 3;y++) {
			for(int x = 0;x < 9;x++) {
				dummy.addNormalSlot(i++, _table.inv, guiLeft + (x * 18) + 20, guiTop + (y * 18) + 80);
			}
		}
		i = 0;
		for(int y = 0;y < 3;y++) {
			for(int x = 0;x < 3;x++) {
				dummy.addDummySlot(i++, guiLeft + (x * 18) + 20, guiTop + (y * 18) + 15);
			}
		}
		dummy.addCallableSlotHandler(0, _table.resultInv, guiLeft + 101, guiTop + 33, new ISlotClick() {
			@Override
			public ItemStack getResultForClick() {
				return _table.getResultForClick();
			}
		});
		dummy.addNormalSlot(0, _table.toSortInv, guiLeft + 164, guiTop + 51);
		dummy.addNormalSlotsForPlayerInventory(20, 150);
		this.inventorySlots = dummy;
		refreshItems();
	}

	@Override
	@SuppressWarnings("unchecked")
	public void initGui() {
		this.xSize = 420;
		
		super.initGui();
		
		buttonList.add(new SmallGuiButton(BUTTON_REFRESH, guiLeft + 10 + this.getRenderOffsetX(), bottom - 15 + this.getRenderOffsetY(), 46, 10, "Refresh")); // Refresh
		buttonList.add(new SmallGuiButton(13, guiLeft + 10 + this.getRenderOffsetX(), bottom - 28 + this.getRenderOffsetY(), 46, 10, "Content")); // Component
		buttonList.add(new SmallGuiButton(9, guiLeft + 10 + this.getRenderOffsetX(), bottom - 41 + this.getRenderOffsetY(), 46, 10, "Both"));
		
		buttonList.add(new SmallGuiButton(14, guiLeft + 96, guiTop + 53, 10, 10, "+")); // +1
		buttonList.add(new SmallGuiButton(15, guiLeft + 108, guiTop + 53, 15, 10, "++")); // +10
		buttonList.add(new SmallGuiButton(16, guiLeft + 96, guiTop + 64, 26, 10, "+++")); // +64
	}
	
	@Override
	public void drawGuiContainerBackgroundLayer(float f, int i, int j) {
		super.drawGuiContainerBackgroundLayer(f, i, j);
		
		for(int x = 0;x < 9;x++) {
			for(int y = 0;y < 3;y++) {
				BasicGuiHelper.drawSlotBackground(mc, guiLeft + (x * 18) + 19, guiTop + (y * 18) + 79);
			}
		}
		for(int x = 0;x < 3;x++) {
			for(int y = 0;y < 3;y++) {
				BasicGuiHelper.drawSlotBackground(mc, guiLeft + (x * 18) + 19, guiTop + (y * 18) + 14);
			}
		}
		BasicGuiHelper.drawSlotBackground(mc, guiLeft + 100, guiTop + 32);
		BasicGuiHelper.drawSlotBackground(mc, guiLeft + 163, guiTop + 50);
		drawRect(guiLeft + 75, guiTop + 38, guiLeft + 95, guiTop + 43, Colors.DarkGrey);
		for(int a = 0; a < 10;a++) {
			drawRect(guiLeft + 97 - a, guiTop + 40 - a, guiLeft + 98 - a, guiTop + 41 + a, Colors.DarkGrey);
		}
		for(int a = 0; a < 15;a++) {
			drawRect(guiLeft + 164 + a, guiTop + 51 + a, guiLeft + 166 + a, guiTop + 53 + a, Colors.DarkGrey);
			drawRect(guiLeft + 164 + a, guiTop + 65 - a, guiLeft + 166 + a, guiTop + 67 - a, Colors.DarkGrey);
		}
		BasicGuiHelper.drawPlayerInventoryBackground(mc, guiLeft + 20, guiTop + 150);
	}
	
	@Override
	public int getRenderOffsetX() {
		return 190;
	}
	
	@Override
	public void refreshItems(){
			int integer;
			switch(displayOptions) {
			case Both:
				integer = 0;
				break;
			case SupplyOnly:
				integer = 1;
				break;
			case CraftOnly:
				integer = 2;
				break;
			default: 
				integer = 3;
			}
			integer += (dimension * 10);
			MainProxy.sendPacketToServer(PacketHandler.getPacket(OrdererRefreshRequestPacket.class).setInteger(integer).setPosX(xCoord).setPosY(yCoord).setPosZ(zCoord));
	}

	@Override
	protected void actionPerformed(GuiButton guibutton) {
		super.actionPerformed(guibutton);
		if (guibutton.id == 9) {
			String displayString = "";
			switch (displayOptions){
			case Both:
				displayOptions = DisplayOptions.CraftOnly;
				displayString = "Craft";
				break;
			case CraftOnly:
				displayOptions = DisplayOptions.SupplyOnly;
				displayString = "Supply";
				break;
			case SupplyOnly:
				displayOptions = DisplayOptions.Both;
				displayString = "Both";
				break;
			}
			guibutton.displayString = displayString;
			refreshItems();
		} else if(guibutton.id == 14) {
			requestMatrix(1);
		} else if(guibutton.id == 15) {
			requestMatrix(10);
		} else if(guibutton.id == 16) {
			requestMatrix(64);
		}
	}

	private void requestMatrix(int multiplier) {
		ArrayList<ItemIdentifierStack> list = new ArrayList<ItemIdentifierStack>(9);
		
		for(Entry<ItemIdentifier,Integer> e : _table.matrix.getItemsAndCount().entrySet()) {
			list.add(e.getKey().makeStack(e.getValue() * multiplier));
		}
		
		MainProxy.sendPacketToServer(PacketHandler.getPacket(RequestSubmitPacket.class).setStacks(list.toArray(new ItemIdentifierStack[list.size()])).setPosX(xCoord).setPosY(yCoord).setPosZ(zCoord));
		refreshItems();
	}

	@Override
	public void specialItemRendering(ItemIdentifier item, int x, int y) {}
}
