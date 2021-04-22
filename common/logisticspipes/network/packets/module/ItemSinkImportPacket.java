package logisticspipes.network.packets.module;

import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

import logisticspipes.gui.modules.GuiItemSink;
import logisticspipes.modules.ModuleItemSink;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.abstractpackets.ModuleCoordinatesPacket;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.StaticResolve;
import logisticspipes.utils.item.ItemIdentifier;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

@StaticResolve
public class ItemSinkImportPacket extends ModuleCoordinatesPacket {

	@Nullable
	public List<ItemIdentifier> importedItems = null;

	public ItemSinkImportPacket setImportedItems(@Nullable List<ItemIdentifier> importedItems) {
		this.importedItems = importedItems;
		return this;
	}

	public ItemSinkImportPacket(int id) {
		super(id);
	}

	@Override
	public void writeData(LPDataOutput output) {
		super.writeData(output);
		output.writeCollection(importedItems, LPDataOutput::writeItemIdentifier);
	}

	@Override
	public void readData(LPDataInput input) {
		super.readData(input);
		importedItems = input.readArrayList(LPDataInput::readItemIdentifier);
	}

	@Override
	public ModernPacket template() {
		return new ItemSinkImportPacket(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		if (MainProxy.isServer(player.world)) {
			ModuleItemSink module = this.getLogisticsModule(player, ModuleItemSink.class);
			if (module == null) {
				return;
			}
			MainProxy.sendPacketToPlayer(PacketHandler.getPacket(ItemSinkImportPacket.class)
					.setImportedItems(module.getAdjacentInventoriesItems()
							.limit(module.filterInventory.getSizeInventory())
							.collect(Collectors.toList()))
					.setPacketPos(this), player);
		} else if (MainProxy.isClient(player.world)) {
			if (importedItems == null) return;
			if (Minecraft.getMinecraft().currentScreen instanceof GuiItemSink) {
				((GuiItemSink) Minecraft.getMinecraft().currentScreen).importFromInventory(importedItems.stream());
			}
		}
	}

}
