package logisticspipes.network.packets;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.StaticResolve;
import logisticspipes.utils.item.ItemIdentifier;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

@StaticResolve
public class RequestUpdateNamesPacket extends ModernPacket {

	public RequestUpdateNamesPacket(int id) {
		super(id);
	}

	@Override
	public void readData(LPDataInput input) {}

	@Override
	public void processPacket(EntityPlayer player) {
		Collection<Item> itemList = ForgeRegistries.ITEMS.getValuesCollection();
		List<ItemIdentifier> identList = new LinkedList<>();
		for (Item item : itemList) {
			if (item != null) {
				for (CreativeTabs tab : item.getCreativeTabs()) {
					NonNullList<ItemStack> list = NonNullList.create();
					item.getSubItems(tab, list);
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
		FMLClientHandler.instance().getClient().player.sendChatMessage("Names in send Queue");
	}

	@Override
	public void writeData(LPDataOutput output) {}

	@Override
	public ModernPacket template() {
		return new RequestUpdateNamesPacket(getId());
	}
}
