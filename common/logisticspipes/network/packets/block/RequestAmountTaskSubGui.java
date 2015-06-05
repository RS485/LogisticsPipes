package logisticspipes.network.packets.block;

import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

import logisticspipes.blocks.stats.LogisticsStatisticsTileEntity;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;

import net.minecraft.entity.player.EntityPlayer;

public class RequestAmountTaskSubGui extends CoordinatesPacket {

	public RequestAmountTaskSubGui(int id) {
		super(id);
	}

	@Override
	public void processPacket(EntityPlayer player) {
		LogisticsStatisticsTileEntity tile = this.getTile(player.getEntityWorld(), LogisticsStatisticsTileEntity.class);
		CoreRoutedPipe pipe = tile.getConnectedPipe();
		if (pipe == null) {
			return;
		}

		Map<ItemIdentifier, Integer> _availableItems = SimpleServiceLocator.logisticsManager.getAvailableItems(pipe.getRouter().getIRoutersByCost());
		LinkedList<ItemIdentifier> _craftableItems = SimpleServiceLocator.logisticsManager.getCraftableItems(pipe.getRouter().getIRoutersByCost());

		TreeSet<ItemIdentifierStack> _allItems = new TreeSet<ItemIdentifierStack>();

		for (Entry<ItemIdentifier, Integer> item : _availableItems.entrySet()) {
			ItemIdentifierStack newStack = item.getKey().makeStack(item.getValue());
			_allItems.add(newStack);
		}

		for (ItemIdentifier item : _craftableItems) {
			if (_availableItems.containsKey(item)) {
				continue;
			}
			_allItems.add(item.makeStack(0));
		}

		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(AmountTaskSubGui.class).setIdentSet(_allItems), player);
	}

	@Override
	public ModernPacket template() {
		return new RequestAmountTaskSubGui(getId());
	}
}
