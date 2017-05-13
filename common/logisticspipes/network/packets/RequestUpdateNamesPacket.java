package logisticspipes.network.packets;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fml.client.FMLClientHandler;

import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.item.ItemIdentifier;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

public class RequestUpdateNamesPacket extends ModernPacket {

	public RequestUpdateNamesPacket(int id) {
		super(id);
	}

	@Override
	public void readData(LPDataInput input) {}

	@Override
	public void processPacket(EntityPlayer player) {
		//XXX stubbed out. How do you enumerate every item now?
		//Item[] itemList = Item.itemsList;
		Item[] itemList = new Item[0];
		List<ItemIdentifier> identList = new LinkedList<>();
		for (Item item : itemList) {
			if (item != null) {
				for (CreativeTabs tab : item.getCreativeTabs()) {
					List<ItemStack> list = new ArrayList<>();
					item.getSubItems(item, tab, list);
					if (list.size() > 0) {
						identList.addAll(list.stream().map(ItemIdentifier::get).collect(Collectors.toList()));
					} else {
						identList.add(ItemIdentifier.get(item, 0, null));
					}
				}
			}
		}
		SimpleServiceLocator.clientBufferHandler.setPause(true);
		for (ItemIdentifier item : identList) {
			MainProxy.sendPacketToServer(PacketHandler.getPacket(UpdateName.class).setIdent(item).setName(item.getFriendlyName()));
		}
		SimpleServiceLocator.clientBufferHandler.setPause(false);
		FMLClientHandler.instance().getClient().thePlayer.sendChatMessage("Names in send Queue");
	}

	@Override
	public void writeData(LPDataOutput output) {}

	@Override
	public ModernPacket template() {
		return new RequestUpdateNamesPacket(getId());
	}
}
