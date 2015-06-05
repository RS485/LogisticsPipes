package logisticspipes.network.packets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.item.ItemIdentifier;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.client.FMLClientHandler;

public class RequestUpdateNamesPacket extends ModernPacket {

	public RequestUpdateNamesPacket(int id) {
		super(id);
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {}

	@Override
	public void processPacket(EntityPlayer player) {
		//XXX stubbed out. How do you enumerate every item now?
		//Item[] itemList = Item.itemsList;
		Item[] itemList = new Item[0];
		List<ItemIdentifier> identList = new LinkedList<ItemIdentifier>();
		for (Item item : itemList) {
			if (item != null) {
				for (CreativeTabs tab : item.getCreativeTabs()) {
					List<ItemStack> list = new ArrayList<ItemStack>();
					item.getSubItems(item, tab, list);
					if (list.size() > 0) {
						for (ItemStack stack : list) {
							identList.add(ItemIdentifier.get(stack));
						}
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
	public void writeData(LPDataOutputStream data) throws IOException {}

	@Override
	public ModernPacket template() {
		return new RequestUpdateNamesPacket(getId());
	}
}
