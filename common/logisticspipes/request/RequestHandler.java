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
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.TextComponentTranslation;

import logisticspipes.interfaces.IRequestWatcher;
import logisticspipes.interfaces.routing.IRequestFluid;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.orderer.ComponentList;
import logisticspipes.network.packets.orderer.MissingItems;
import logisticspipes.network.packets.orderer.OrdererContent;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.request.RequestTree.ActiveRequestType;
import logisticspipes.request.resources.IResource;
import logisticspipes.routing.order.LinkedLogisticsOrderList;
import logisticspipes.utils.FluidIdentifier;
import logisticspipes.utils.FluidIdentifierStack;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;

public class RequestHandler {

	public enum DisplayOptions {
		Both,
		SupplyOnly,
		CraftOnly
	}

	public static void request(final EntityPlayer player, final ItemIdentifierStack stack, final CoreRoutedPipe pipe) {
		if (!pipe.useEnergy(5)) {
			player.sendMessage(new TextComponentTranslation("lp.misc.noenergy"));
			return;
		}
		RequestTree.request(stack.clone(), pipe, new RequestLog() {

			@Override
			public void handleMissingItems(List<IResource> resources) {
				MainProxy.sendPacketToPlayer(PacketHandler.getPacket(MissingItems.class).setItems(resources).setFlag(true), player);
			}

			@Override
			public void handleSucessfullRequestOf(IResource item, LinkedLogisticsOrderList parts) {
				Collection<IResource> coll = new ArrayList<>(1);
				coll.add(item);
				MainProxy.sendPacketToPlayer(PacketHandler.getPacket(MissingItems.class).setItems(coll).setFlag(false), player);
				if (pipe instanceof IRequestWatcher) {
					((IRequestWatcher) pipe).handleOrderList(item, parts);
				}
			}

			@Override
			public void handleSucessfullRequestOfList(List<IResource> resources, LinkedLogisticsOrderList parts) {}
		}, null);
	}

	public static void simulate(final EntityPlayer player, final ItemIdentifierStack stack, CoreRoutedPipe pipe) {
		final List<IResource> usedList = new ArrayList<>();
		final List<IResource> missingList = new ArrayList<>();
		RequestTree.simulate(stack.clone(), pipe, new RequestLog() {

			@Override
			public void handleMissingItems(List<IResource> resources) {
				missingList.addAll(resources);
			}

			@Override
			public void handleSucessfullRequestOf(IResource item, LinkedLogisticsOrderList parts) {}

			@Override
			public void handleSucessfullRequestOfList(List<IResource> resources, LinkedLogisticsOrderList parts) {
				usedList.addAll(resources);
			}
		});
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(ComponentList.class).setUsed(usedList).setMissing(missingList), player);
	}

	public static void refresh(EntityPlayer player, CoreRoutedPipe pipe, DisplayOptions option) {
		Map<ItemIdentifier, Integer> _availableItems;
		LinkedList<ItemIdentifier> _craftableItems;

		if (option == DisplayOptions.SupplyOnly || option == DisplayOptions.Both) {
			_availableItems = SimpleServiceLocator.logisticsManager.getAvailableItems(pipe.getRouter().getIRoutersByCost());
		} else {
			_availableItems = new HashMap<>();
		}
		if (option == DisplayOptions.CraftOnly || option == DisplayOptions.Both) {
			_craftableItems = SimpleServiceLocator.logisticsManager.getCraftableItems(pipe.getRouter().getIRoutersByCost());
		} else {
			_craftableItems = new LinkedList<>();
		}
		TreeSet<ItemIdentifierStack> _allItems = new TreeSet<>();

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
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(OrdererContent.class).setIdentSet(_allItems), player);
	}

	public static void requestList(final EntityPlayer player, final List<ItemIdentifierStack> list, final CoreRoutedPipe pipe) {
		if (!pipe.useEnergy(5)) {
			player.sendMessage(new TextComponentTranslation("lp.misc.noenergy"));
			return;
		}
		RequestTree.request(list, pipe, new RequestLog() {

			@Override
			public void handleMissingItems(List<IResource> resources) {
				MainProxy.sendPacketToPlayer(PacketHandler.getPacket(MissingItems.class).setItems(resources).setFlag(true), player);
			}

			@Override
			public void handleSucessfullRequestOf(IResource item, LinkedLogisticsOrderList parts) {}

			@Override
			public void handleSucessfullRequestOfList(List<IResource> resources, LinkedLogisticsOrderList parts) {
				MainProxy.sendPacketToPlayer(PacketHandler.getPacket(MissingItems.class).setItems(resources).setFlag(false), player);
				if (pipe instanceof IRequestWatcher) {
					((IRequestWatcher) pipe).handleOrderList(null, parts);
				}
			}
		}, RequestTree.defaultRequestFlags, null);
	}

	public static void requestMacrolist(NBTTagCompound itemlist, final CoreRoutedPipe requester, final EntityPlayer player) {
		if (!requester.useEnergy(5)) {
			player.sendMessage(new TextComponentTranslation("lp.misc.noenergy"));
			return;
		}
		NBTTagList list = itemlist.getTagList("inventar", 10);
		final List<ItemIdentifierStack> transaction = new ArrayList<>(list.tagCount());
		for (int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound itemnbt = list.getCompoundTagAt(i);
			NBTTagCompound itemNBTContent = itemnbt.getCompoundTag("nbt");
			if (!itemnbt.hasKey("nbt")) {
				itemNBTContent = null;
			}
			ItemIdentifierStack stack = ItemIdentifier.get(Item.getItemById(itemnbt.getInteger("id")), itemnbt.getInteger("data"), itemNBTContent).makeStack(itemnbt.getInteger("amount"));
			transaction.add(stack);
		}
		RequestTree.request(transaction, requester, new RequestLog() {

			@Override
			public void handleMissingItems(List<IResource> resources) {
				MainProxy.sendPacketToPlayer(PacketHandler.getPacket(MissingItems.class).setItems(resources).setFlag(true), player);
			}

			@Override
			public void handleSucessfullRequestOf(IResource item, LinkedLogisticsOrderList parts) {}

			@Override
			public void handleSucessfullRequestOfList(List<IResource> resources, LinkedLogisticsOrderList parts) {
				MainProxy.sendPacketToPlayer(PacketHandler.getPacket(MissingItems.class).setItems(resources).setFlag(false), player);
				if (requester instanceof IRequestWatcher) {
					((IRequestWatcher) requester).handleOrderList(null, parts);
				}
			}
		}, RequestTree.defaultRequestFlags, null);
	}

	public static Object[] computerRequest(final ItemIdentifierStack makeStack, final CoreRoutedPipe pipe, boolean craftingOnly) {

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
			public void handleMissingItems(List<IResource> resources) {
				status[0] = "MISSING";
				status[1] = resources;
			}

			@Override
			public void handleSucessfullRequestOf(IResource item, LinkedLogisticsOrderList parts) {
				status[0] = "DONE";
				List<IResource> itemList = new LinkedList<>();
				itemList.add(item);
				status[1] = itemList;
			}

			@Override
			public void handleSucessfullRequestOfList(List<IResource> resources, LinkedLogisticsOrderList parts) {}
		}, false, false, true, false, requestFlags, null);
		return status;
	}

	public static void refreshFluid(EntityPlayer player, CoreRoutedPipe pipe) {
		TreeSet<FluidIdentifierStack> _allItems = SimpleServiceLocator.logisticsFluidManager.getAvailableFluid(pipe.getRouter().getIRoutersByCost());
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(OrdererContent.class)
						.setIdentSet(
								_allItems.stream()
										.map(item -> new ItemIdentifierStack(item.getFluid().getItemIdentifier(), item.getAmount()))
										.collect(Collectors.toCollection(TreeSet::new))
						)
				, player);
	}

	public static void requestFluid(final EntityPlayer player, final ItemIdentifierStack stack, CoreRoutedPipe pipe, IRequestFluid requester) {
		if (!pipe.useEnergy(10)) {
			player.sendMessage(new TextComponentTranslation("lp.misc.noenergy"));
			return;
		}

		RequestTree.requestFluid(FluidIdentifier.get(stack.getItem()), stack.getStackSize(), requester, new RequestLog() {

			@Override
			public void handleMissingItems(List<IResource> resources) {
				MainProxy.sendPacketToPlayer(PacketHandler.getPacket(MissingItems.class).setItems(resources).setFlag(true), player);
			}

			@Override
			public void handleSucessfullRequestOf(IResource item, LinkedLogisticsOrderList parts) {
				Collection<IResource> coll = new ArrayList<>(1);
				coll.add(item);
				MainProxy.sendPacketToPlayer(PacketHandler.getPacket(MissingItems.class).setItems(coll).setFlag(false), player);
			}

			@Override
			public void handleSucessfullRequestOfList(List<IResource> resources, LinkedLogisticsOrderList parts) {}
		});
	}
}
