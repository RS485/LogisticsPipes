package logisticspipes.network.packets.block;

import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

import net.minecraft.entity.player.EntityPlayer;

import logisticspipes.blocks.stats.LogisticsStatisticsTileEntity;
import logisticspipes.logistics.LogisticsManager;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.StaticResolve;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemStack;

@StaticResolve
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

		Map<ItemIdentifier, Integer> _availableItems = LogisticsManager.getInstance().getAvailableItems(pipe.getRouter().getIRoutersByCost());
		LinkedList<ItemIdentifier> _craftableItems = LogisticsManager.getInstance().getCraftableItems(pipe.getRouter().getIRoutersByCost());

		TreeSet<ItemStack> _allItems = new TreeSet<>();

		for (Entry<ItemIdentifier, Integer> item : _availableItems.entrySet()) {
			ItemStack newStack = item.getKey().makeStack(item.getValue());
			_allItems.add(newStack);
		}

		for (ItemIdentifier item : _craftableItems) {
			if (_availableItems.containsKey(item)) {
				continue;
			}
			_allItems.add(item.makeStack(1));
		}

		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(AmountTaskSubGui.class).setIdentSet(_allItems), player);
	}

	@Override
	public ModernPacket template() {
		return new RequestAmountTaskSubGui(getId());
	}
}
