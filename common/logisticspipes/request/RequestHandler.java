package logisticspipes.request;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;
import java.util.stream.Collectors;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.text.TextComponentTranslation;

import logisticspipes.interfaces.IRequestWatcher;
import logisticspipes.interfaces.routing.FluidRequester;
import logisticspipes.logistics.LogisticsFluidManager;
import logisticspipes.logistics.LogisticsManager;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.orderer.ComponentList;
import logisticspipes.network.packets.orderer.MissingItems;
import logisticspipes.network.packets.orderer.OrdererContent;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.request.RequestTree.ActiveRequestType;
import logisticspipes.request.resources.Resource;
import logisticspipes.routing.order.LinkedLogisticsOrderList;
import logisticspipes.utils.FluidIdentifier;
import logisticspipes.utils.FluidIdentifierStack;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemStack;

public class RequestHandler {

	public enum DisplayOptions {
		Both,
		SupplyOnly,
		CraftOnly
	}

	public static void request(final EntityPlayer player, final ItemStack stack, final CoreRoutedPipe pipe) {
		if (!pipe.useEnergy(5)) {
			player.sendMessage(new TextComponentTranslation("lp.misc.noenergy"));
			return;
		}
		RequestTree.request(stack.clone(), pipe, new RequestLog() {

			@Override
			public void handleMissingItems(List<Resource> resources) {
				MainProxy.sendPacketToPlayer(PacketHandler.getPacket(MissingItems.class).setItems(resources).setFlag(true), player);
			}

			@Override
			public void handleSucessfullRequestOf(Resource item, LinkedLogisticsOrderList parts) {
				Collection<Resource> coll = new ArrayList<>(1);
				coll.add(item);
				MainProxy.sendPacketToPlayer(PacketHandler.getPacket(MissingItems.class).setItems(coll).setFlag(false), player);
				if (pipe instanceof IRequestWatcher) {
					((IRequestWatcher) pipe).handleOrderList(item, parts);
				}
			}

			@Override
			public void handleSucessfullRequestOfList(List<Resource> resources, LinkedLogisticsOrderList parts) {}
		}, null);
	}

	public static void simulate(final EntityPlayer player, final ItemStack stack, CoreRoutedPipe pipe) {
		final List<Resource> usedList = new ArrayList<>();
		final List<Resource> missingList = new ArrayList<>();
		RequestTree.simulate(stack.clone(), pipe, new RequestLog() {

			@Override
			public void handleMissingItems(List<Resource> resources) {
				missingList.addAll(resources);
			}

			@Override
			public void handleSucessfullRequestOf(Resource item, LinkedLogisticsOrderList parts) {}

			@Override
			public void handleSucessfullRequestOfList(List<Resource> resources, LinkedLogisticsOrderList parts) {
				usedList.addAll(resources);
			}
		});
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(ComponentList.class).setUsed(usedList).setMissing(missingList), player);
	}

	public static void refresh(EntityPlayer player, CoreRoutedPipe pipe, DisplayOptions option) {
		Map<ItemIdentifier, Integer> _availableItems;
		LinkedList<ItemIdentifier> _craftableItems;

		if (option == DisplayOptions.SupplyOnly || option == DisplayOptions.Both) {
			_availableItems = LogisticsManager.getInstance().getAvailableItems(pipe.getRouter().getIRoutersByCost());
		} else {
			_availableItems = new HashMap<>();
		}
		if (option == DisplayOptions.CraftOnly || option == DisplayOptions.Both) {
			_craftableItems = LogisticsManager.getInstance().getCraftableItems(pipe.getRouter().getIRoutersByCost());
		} else {
			_craftableItems = new LinkedList<>();
		}
		TreeSet<ItemStack> _allItems = new TreeSet<>();

		for (Entry<ItemIdentifier, Integer> item : _availableItems.entrySet()) {
			ItemStack newStack = item.getKey().makeStack(item.getValue());
			_allItems.add(newStack);
		}

		for (ItemIdentifier item : _craftableItems) {
			if (_availableItems.containsKey(item)) {
				continue;
			}
			_allItems.add(item.makeStack(0));
		}
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(OrdererContent.class).setIdentSet(_allItems), player);
	}

	public static void requestList(final EntityPlayer player, final List<ItemStack> list, final CoreRoutedPipe pipe) {
		if (!pipe.useEnergy(5)) {
			player.sendMessage(new TextComponentTranslation("lp.misc.noenergy"));
			return;
		}
		RequestTree.request(list, pipe, new RequestLog() {

			@Override
			public void handleMissingItems(List<Resource> resources) {
				MainProxy.sendPacketToPlayer(PacketHandler.getPacket(MissingItems.class).setItems(resources).setFlag(true), player);
			}

			@Override
			public void handleSucessfullRequestOf(Resource item, LinkedLogisticsOrderList parts) {}

			@Override
			public void handleSucessfullRequestOfList(List<Resource> resources, LinkedLogisticsOrderList parts) {
				MainProxy.sendPacketToPlayer(PacketHandler.getPacket(MissingItems.class).setItems(resources).setFlag(false), player);
				if (pipe instanceof IRequestWatcher) {
					((IRequestWatcher) pipe).handleOrderList(null, parts);
				}
			}
		}, RequestTree.defaultRequestFlags, null);
	}

	public static void requestMacrolist(CompoundTag itemlist, final CoreRoutedPipe requester, final EntityPlayer player) {
		if (!requester.useEnergy(5)) {
			player.sendMessage(new TextComponentTranslation("lp.misc.noenergy"));
			return;
		}
		ListTag list = itemlist.getTagList("inventar", 10);
		final List<ItemStack> transaction = new ArrayList<>(list.tagCount());
		for (int i = 0; i < list.tagCount(); i++) {
			CompoundTag itemnbt = list.getCompoundTagAt(i);
			CompoundTag itemNBTContent = itemnbt.getCompoundTag("nbt");
			if (!itemnbt.hasKey("nbt")) {
				itemNBTContent = null;
			}
			ItemStack stack = ItemIdentifier.get(Item.getItemById(itemnbt.getInteger("id")), itemnbt.getInteger("data"), itemNBTContent).makeStack(itemnbt.getInteger("amount"));
			transaction.add(stack);
		}
		RequestTree.request(transaction, requester, new RequestLog() {

			@Override
			public void handleMissingItems(List<Resource> resources) {
				MainProxy.sendPacketToPlayer(PacketHandler.getPacket(MissingItems.class).setItems(resources).setFlag(true), player);
			}

			@Override
			public void handleSucessfullRequestOf(Resource item, LinkedLogisticsOrderList parts) {}

			@Override
			public void handleSucessfullRequestOfList(List<Resource> resources, LinkedLogisticsOrderList parts) {
				MainProxy.sendPacketToPlayer(PacketHandler.getPacket(MissingItems.class).setItems(resources).setFlag(false), player);
				if (requester instanceof IRequestWatcher) {
					((IRequestWatcher) requester).handleOrderList(null, parts);
				}
			}
		}, RequestTree.defaultRequestFlags, null);
	}

	public static Object[] computerRequest(final ItemStack makeStack, final CoreRoutedPipe pipe, boolean craftingOnly) {

		EnumSet<ActiveRequestType> requestFlags;
		if (craftingOnly) {
			requestFlags = EnumSet.of(ActiveRequestType.Craft);
		} else {
			requestFlags = EnumSet.of(ActiveRequestType.Craft, ActiveRequestType.Provide);
		}
		if (!pipe.useEnergy(15)) {
			return new Object[] { "NO_POWER" };
		}
		final Object[] status = new Object[2];
		RequestTree.request(makeStack, pipe, new RequestLog() {

			@Override
			public void handleMissingItems(List<Resource> resources) {
				status[0] = "MISSING";
				status[1] = resources;
			}

			@Override
			public void handleSucessfullRequestOf(Resource item, LinkedLogisticsOrderList parts) {
				status[0] = "DONE";
				List<Resource> itemList = new LinkedList<>();
				itemList.add(item);
				status[1] = itemList;
			}

			@Override
			public void handleSucessfullRequestOfList(List<Resource> resources, LinkedLogisticsOrderList parts) {}
		}, false, false, true, false, requestFlags, null);
		return status;
	}

	public static void refreshFluid(EntityPlayer player, CoreRoutedPipe pipe) {
		TreeSet<FluidIdentifierStack> _allItems = LogisticsFluidManager.getInstance().getAvailableFluid(pipe.getRouter().getIRoutersByCost());
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(OrdererContent.class)
						.setIdentSet(
								_allItems.stream()
										.map(item -> new ItemStack(item.getFluid().getItemIdentifier(), item.getAmount()))
										.collect(Collectors.toCollection(TreeSet::new))
						)
				, player);
	}

	public static void requestFluid(final EntityPlayer player, final ItemStack stack, CoreRoutedPipe pipe, FluidRequester requester) {
		if (!pipe.useEnergy(10)) {
			player.sendMessage(new TextComponentTranslation("lp.misc.noenergy"));
			return;
		}

		RequestTree.requestFluid(FluidIdentifier.get(stack.getItem()), stack.getCount(), requester, new RequestLog() {

			@Override
			public void handleMissingItems(List<Resource> resources) {
				MainProxy.sendPacketToPlayer(PacketHandler.getPacket(MissingItems.class).setItems(resources).setFlag(true), player);
			}

			@Override
			public void handleSucessfullRequestOf(Resource item, LinkedLogisticsOrderList parts) {
				Collection<Resource> coll = new ArrayList<>(1);
				coll.add(item);
				MainProxy.sendPacketToPlayer(PacketHandler.getPacket(MissingItems.class).setItems(coll).setFlag(false), player);
			}

			@Override
			public void handleSucessfullRequestOfList(List<Resource> resources, LinkedLogisticsOrderList parts) {}
		});
	}
}
