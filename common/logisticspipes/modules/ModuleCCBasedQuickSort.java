package logisticspipes.modules;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

import lombok.Getter;

import logisticspipes.gui.hud.modules.HUDCCBasedQuickSort;
import logisticspipes.interfaces.IClientInformationProvider;
import logisticspipes.interfaces.IHUDModuleHandler;
import logisticspipes.interfaces.IHUDModuleRenderer;
import logisticspipes.interfaces.IModuleWatchReciver;
import logisticspipes.interfaces.WrappedInventory;
import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.network.NewGuiHandler;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractguis.ModuleCoordinatesGuiProvider;
import logisticspipes.network.abstractguis.ModuleInHandGuiProvider;
import logisticspipes.network.guis.module.inhand.CCBasedQuickSortInHand;
import logisticspipes.network.guis.module.inpipe.CCBasedQuickSortSlot;
import logisticspipes.network.packets.hud.HUDStartModuleWatchingPacket;
import logisticspipes.network.packets.hud.HUDStopModuleWatchingPacket;
import logisticspipes.network.packets.modules.CCBasedQuickSortMode;
import logisticspipes.network.packets.modules.CCBasedQuickSortSinkSize;
import logisticspipes.pipes.basic.CoreRoutedPipe.ItemSendMode;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.computers.objects.CCSinkResponder;
import logisticspipes.proxy.specialinventoryhandler.SpecialInventoryHandler;
import logisticspipes.routing.ExitRoute;
import logisticspipes.routing.PipeRoutingConnectionType;
import logisticspipes.routing.Router;
import logisticspipes.routing.RouterManager;
import logisticspipes.routing.ServerRouter;
import logisticspipes.utils.PlayerCollectionList;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.tuples.Tuple2;
import logisticspipes.utils.tuples.Tuple3;

public class ModuleCCBasedQuickSort extends ModuleQuickSort implements IClientInformationProvider, IHUDModuleHandler, IModuleWatchReciver {

	private Map<Integer, Tuple2<Integer, List<CCSinkResponder>>> sinkResponses = new HashMap<>();

	@Getter
	private int timeout = 100;

	@Getter
	private int sinkSize = 0;

	private final PlayerCollectionList localModeWatchers = new PlayerCollectionList();

	private IHUDModuleRenderer HUD = new HUDCCBasedQuickSort(this);

	private void createSinkMessage(int slot, ItemStack stack) {
		List<CCSinkResponder> respones = new ArrayList<>();
		Router sourceRouter = service.getRouter();
		if (sourceRouter == null) {
			return;
		}
		BitSet routersIndex = ServerRouter.getRoutersInterestedIn((ItemIdentifier) null); // get only pipes with generic interest
		List<ExitRoute> validDestinations = new ArrayList<>(); // get the routing table
		for (int i = routersIndex.nextSetBit(0); i >= 0; i = routersIndex.nextSetBit(i + 1)) {
			Router r = RouterManager.getInstance().getRouterUnsafe(i, false);
			List<ExitRoute> exits = sourceRouter.getDistanceTo(r);
			if (exits != null) {
				validDestinations.addAll(exits.stream()
						.filter(e -> e.containsFlag(PipeRoutingConnectionType.canRouteTo))
						.collect(Collectors.toList()));
			}
		}
		Collections.sort(validDestinations);

		outer:
		for (ExitRoute candidateRouter : validDestinations) {
			if (candidateRouter.destination.getId().equals(sourceRouter.getId())) {
				continue;
			}

			for (IFilter filter : candidateRouter.filters) {
				if (filter.blockRouting() || (filter.isBlocked() == filter.isFilteredItem(stack.getItem()))) {
					continue outer;
				}
			}
			if (candidateRouter.destination != null && candidateRouter.destination.getLogisticsModule() != null) {
				respones.addAll(candidateRouter.destination.getLogisticsModule().queueCCSinkEvent(stack));
			}
		}
		sinkResponses.put(slot, new Tuple2<>(0, respones));
	}

	@Override
	public void tick() {
		WrappedInventory invUtil = service.getPointedInventory();
		if (invUtil == null) {
			return;
		}
		handleSinkResponses(invUtil);

		if (--currentTick > 0) {
			return;
		}
		if (stalled) {
			currentTick = stalledDelay;
		} else {
			currentTick = normalDelay;
		}

		// Extract Item

		if (!service.canUseEnergy(500)) {
			stalled = true;
			return;
		}

		if ((!(invUtil instanceof SpecialInventoryHandler) && invUtil.getSlotCount() == 0) || !service.canUseEnergy(500)) {
			stalled = true;
			return;
		}

		// incremented at the end of the previous loop.
		if (lastStackLookedAt >= invUtil.getSlotCount()) {
			lastStackLookedAt = 0;
		}

		ItemStack slot = invUtil.getInvStack(lastStackLookedAt);

		while (slot.isEmpty()) {
			lastStackLookedAt++;
			if (lastStackLookedAt >= invUtil.getSlotCount()) {
				lastStackLookedAt = 0;
			}
			slot = invUtil.getInvStack(lastStackLookedAt);
			if (lastStackLookedAt == lastSuceededStack) {
				stalled = true;
				send();
				return; // then we have been around the list without sending, halt for now
			}
		}
		send();

		if (!sinkResponses.containsKey(lastStackLookedAt)) {
			createSinkMessage(lastStackLookedAt, ItemStack.getFromStack(slot));
		}

		lastStackLookedAt++;
		checkSize();
	}

	private void handleSinkResponses(WrappedInventory invUtil) {
		boolean changed = false;
		Iterator<Entry<Integer, Tuple2<Integer, List<CCSinkResponder>>>> iter = sinkResponses.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<Integer, Tuple2<Integer, List<CCSinkResponder>>> pair = iter.next();
			pair.getValue().setValue1(pair.getValue().getValue1() + 1);
			boolean canBeHandled = true;
			for (CCSinkResponder response : pair.getValue().getValue2()) {
				if (!response.isDone()) {
					canBeHandled = false;
					break;
				}
			}
			if (canBeHandled || pair.getValue().getValue1() > timeout) {
				// skip entry, if slot is not in the inventory (too high).
				boolean slotInInventory = pair.getKey() < invUtil.getSlotCount();

				if (slotInInventory && handle(invUtil, pair.getKey(), pair.getValue().getValue2())) {
					stalled = false;
					lastSuceededStack = pair.getKey();
				}
				iter.remove();
				changed = true;
			}
		}
		if (changed) {
			checkSize();
		}
	}

	private boolean handle(WrappedInventory invUtil, int slot, List<CCSinkResponder> list) {
		if (list.isEmpty()) {
			return false;
		}
		ItemIdentifier ident = list.get(0).getStack().getItem();
		ItemStack stack = invUtil.getInvStack(slot);
		if (stack.isEmpty() || !ItemIdentifier.get(stack).equals(ident)) {
			return false;
		}
		final Router source = service.getRouter();

		// list of triplets: priority: Integer, distance: Double, sink: CCSinkResponder
		List<Tuple3<Integer, Double, CCSinkResponder>> possibilities = new ArrayList<>();

		for (CCSinkResponder sink : list) {
			if (!sink.isDone()) {
				continue;
			}
			if (sink.getCanSink() < 1) {
				continue;
			}
			Router r = RouterManager.getInstance().getRouter(sink.getRouterId());
			if (r == null) {
				continue;
			}
			List<ExitRoute> ways = source.getDistanceTo(r);
			double minDistance = Double.MAX_VALUE;
			outer:
			for (ExitRoute route : ways) {
				for (IFilter filter : route.filters) {
					if (filter.blockRouting() || filter.isFilteredItem(ident) == filter.isBlocked()) {
						continue outer;
					}
				}
				minDistance = Math.min(route.distanceToDestination, minDistance);
			}
			if (minDistance != Integer.MAX_VALUE) {
				possibilities.add(new Tuple3<>(sink.getPriority(), minDistance, sink));
			}
		}
		if (possibilities.isEmpty()) {
			return false;
		}
		possibilities.sort((left, right) -> {
			int prioritySign = Integer.compare(right.getValue1(), left.getValue1());
			if (prioritySign == 0) {
				// if priority is equal, compare distance
				return Double.compare(left.getValue2(), right.getValue2());
			} else {
				return prioritySign;
			}
		});

		boolean isSent = false;
		for (Tuple3<Integer, Double, CCSinkResponder> triple : possibilities) {
			CCSinkResponder sink = triple.getValue3();
			if (sink.getCanSink() < 0) {
				continue;
			}
			stack = invUtil.getInvStack(slot);
			if (stack.isEmpty()) {
				continue;
			}
			int amount = Math.min(stack.getCount(), sink.getCanSink());
			ItemStack extracted = invUtil.takeInvStack(slot, amount);
			service.sendStack(extracted, sink.getRouterId(), ItemSendMode.Fast, null);
			isSent = true;
		}
		return isSent;
	}

	@Override
	public boolean hasGui() {
		return true;
	}

	@Override
	public void readFromNBT(CompoundTag nbttagcompound) {
		super.readFromNBT(nbttagcompound);
		timeout = nbttagcompound.getInteger("Timeout");
		if (timeout == 0) {
			timeout = 100;
		}
	}

	@Override
	public void writeToNBT(CompoundTag nbttagcompound) {
		super.writeToNBT(nbttagcompound);
		nbttagcompound.setInteger("Timeout", timeout);
	}

	@Override
	public List<String> getClientInformation() {
		List<String> list = new ArrayList<>(5);
		list.add("Timeout: " + timeout);
		return list;
	}

	private void checkSize() {
		if (sinkSize != sinkResponses.size()) {
			sinkSize = sinkResponses.size();
			MainProxy.sendToPlayerList(PacketHandler.getPacket(CCBasedQuickSortSinkSize.class).setSinkSize(sinkSize).setModulePos(this), localModeWatchers);
		}
	}

	@Override
	public void startHUDWatching() {
		MainProxy.sendPacketToServer(PacketHandler.getPacket(HUDStartModuleWatchingPacket.class).setModulePos(this));
	}

	@Override
	public void stopHUDWatching() {
		MainProxy.sendPacketToServer(PacketHandler.getPacket(HUDStopModuleWatchingPacket.class).setModulePos(this));
	}

	@Override
	public void startWatching(EntityPlayer player) {
		localModeWatchers.add(player);
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(CCBasedQuickSortMode.class).setTimeOut(timeout).setModulePos(this), player);
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(CCBasedQuickSortSinkSize.class).setSinkSize(sinkSize).setModulePos(this), player);
	}

	@Override
	public void stopWatching(EntityPlayer player) {
		localModeWatchers.remove(player);
	}

	@Override
	public IHUDModuleRenderer getHUDRenderer() {
		return HUD;
	}

	public void setTimeout(int time) {
		timeout = time;
		if (MainProxy.isServer(this.world.getWorld())) {
			MainProxy.sendToPlayerList(PacketHandler.getPacket(CCBasedQuickSortMode.class).setTimeOut(timeout).setModulePos(this), localModeWatchers);
		}
	}

	public void setSinkSize(int integer) {
		if (MainProxy.isClient(world.getWorld())) {
			sinkSize = integer;
		}
	}

	@Override
	protected ModuleCoordinatesGuiProvider getPipeGuiProvider() {
		return NewGuiHandler.getGui(CCBasedQuickSortSlot.class).setTimeOut(timeout);
	}

	@Override
	protected ModuleInHandGuiProvider getInHandGuiProvider() {
		return NewGuiHandler.getGui(CCBasedQuickSortInHand.class);
	}
}
