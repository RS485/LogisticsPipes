package logisticspipes.modules;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import lombok.Getter;

import logisticspipes.gui.hud.modules.HUDCCBasedQuickSort;
import logisticspipes.interfaces.IClientInformationProvider;
import logisticspipes.interfaces.IHUDModuleHandler;
import logisticspipes.interfaces.IHUDModuleRenderer;
import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.interfaces.IModuleWatchReciver;
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
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.computers.objects.CCSinkResponder;
import logisticspipes.proxy.specialinventoryhandler.SpecialInventoryHandler;
import logisticspipes.routing.ExitRoute;
import logisticspipes.routing.IRouter;
import logisticspipes.routing.PipeRoutingConnectionType;
import logisticspipes.routing.ServerRouter;
import logisticspipes.utils.PlayerCollectionList;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.tuples.Pair;
import logisticspipes.utils.tuples.Triplet;

public class ModuleCCBasedQuickSort extends ModuleQuickSort implements IClientInformationProvider, IHUDModuleHandler, IModuleWatchReciver {

	private Map<Integer, Pair<Integer, List<CCSinkResponder>>> sinkResponses = new HashMap<Integer, Pair<Integer, List<CCSinkResponder>>>();

	@Getter
	private int timeout = 100;

	@Getter
	private int sinkSize = 0;

	private final PlayerCollectionList localModeWatchers = new PlayerCollectionList();

	private IHUDModuleRenderer HUD = new HUDCCBasedQuickSort(this);

	private void createSinkMessage(int slot, ItemIdentifierStack stack) {
		List<CCSinkResponder> respones = new ArrayList<CCSinkResponder>();
		IRouter sourceRouter = _service.getRouter();
		if (sourceRouter == null) {
			return;
		}
		BitSet routersIndex = ServerRouter.getRoutersInterestedIn((ItemIdentifier) null); // get only pipes with generic interest
		List<ExitRoute> validDestinations = new ArrayList<ExitRoute>(); // get the routing table
		for (int i = routersIndex.nextSetBit(0); i >= 0; i = routersIndex.nextSetBit(i + 1)) {
			IRouter r = SimpleServiceLocator.routerManager.getRouterUnsafe(i, false);
			List<ExitRoute> exits = sourceRouter.getDistanceTo(r);
			if (exits != null) {
				for (ExitRoute e : exits) {
					if (e.containsFlag(PipeRoutingConnectionType.canRouteTo)) {
						validDestinations.add(e);
					}
				}
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
		sinkResponses.put(slot, new Pair<Integer, List<CCSinkResponder>>(0, respones));
	}

	@Override
	public void tick() {
		IInventoryUtil invUtil = _service.getPointedInventory(true);
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

		//Extract Item

		if (!_service.canUseEnergy(500)) {
			stalled = true;
			return;
		}

		if ((!(invUtil instanceof SpecialInventoryHandler) && invUtil.getSizeInventory() == 0) || !_service.canUseEnergy(500)) {
			stalled = true;
			return;
		}

		//incremented at the end of the previous loop.
		if (lastStackLookedAt >= invUtil.getSizeInventory()) {
			lastStackLookedAt = 0;
		}

		ItemStack slot = invUtil.getStackInSlot(lastStackLookedAt);

		while (slot == null) {
			lastStackLookedAt++;
			if (lastStackLookedAt >= invUtil.getSizeInventory()) {
				lastStackLookedAt = 0;
			}
			slot = invUtil.getStackInSlot(lastStackLookedAt);
			if (lastStackLookedAt == lastSuceededStack) {
				stalled = true;
				send();
				return; // then we have been around the list without sending, halt for now
			}
		}
		send();

		if (!sinkResponses.containsKey(lastStackLookedAt)) {
			createSinkMessage(lastStackLookedAt, ItemIdentifierStack.getFromStack(slot));
		}

		lastStackLookedAt++;
		checkSize();
	}

	private void handleSinkResponses(IInventoryUtil invUtil) {
		boolean changed = false;
		Iterator<Entry<Integer, Pair<Integer, List<CCSinkResponder>>>> iter = sinkResponses.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<Integer, Pair<Integer, List<CCSinkResponder>>> pair = iter.next();
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
				boolean slotInInventory = pair.getKey() < invUtil.getSizeInventory();

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

	private boolean handle(IInventoryUtil invUtil, int slot, List<CCSinkResponder> list) {
		if (list.isEmpty()) {
			return false;
		}
		ItemIdentifier ident = list.get(0).getStack().getItem();
		ItemStack stack = invUtil.getStackInSlot(slot);
		if (stack == null || !ItemIdentifier.get(stack).equals(ident)) {
			return false;
		}
		final IRouter source = _service.getRouter();
		List<Triplet<Integer, Double, CCSinkResponder>> posibilities = new ArrayList<Triplet<Integer, Double, CCSinkResponder>>();
		for (CCSinkResponder sink : list) {
			if (!sink.isDone()) {
				continue;
			}
			if (sink.getCanSink() < 1) {
				continue;
			}
			IRouter r = SimpleServiceLocator.routerManager.getRouter(sink.getRouterId());
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
				posibilities.add(new Triplet<Integer, Double, CCSinkResponder>(sink.getPriority(), minDistance, sink));
			}
		}
		if (posibilities.isEmpty()) {
			return false;
		}
		Collections.sort(posibilities, new Comparator<Triplet<Integer, Double, CCSinkResponder>>() {

			@Override
			public int compare(Triplet<Integer, Double, CCSinkResponder> o1, Triplet<Integer, Double, CCSinkResponder> o2) {
				int c = o2.getValue1() - o1.getValue1();
				if (c != 0) {
					return c;
				}
				double e = o1.getValue2() - o2.getValue2();
				return e < 0 ? -1 : 1;
			}
		});
		boolean sended = false;
		for (Triplet<Integer, Double, CCSinkResponder> triple : posibilities) {
			CCSinkResponder sink = triple.getValue3();
			if (sink.getCanSink() < 0) {
				continue;
			}
			stack = invUtil.getStackInSlot(slot);
			if (stack == null || stack.stackSize <= 0) {
				continue;
			}
			int amount = Math.min(stack.stackSize, sink.getCanSink());
			ItemStack extracted = invUtil.decrStackSize(slot, amount);
			_service.sendStack(extracted, sink.getRouterId(), ItemSendMode.Fast, null);
			sended = true;
		}
		return sended;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIconTexture(IIconRegister register) {
		return register.registerIcon("logisticspipes:itemModule/ModuleCCQuickSort");
	}

	@Override
	public boolean hasGui() {
		return true;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);
		timeout = nbttagcompound.getInteger("Timeout");
		if (timeout == 0) {
			timeout = 100;
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);
		nbttagcompound.setInteger("Timeout", timeout);
	}

	@Override
	public List<String> getClientInformation() {
		List<String> list = new ArrayList<String>(5);
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
		if(MainProxy.isServer(this._world.getWorld())) {
			MainProxy.sendToPlayerList(PacketHandler.getPacket(CCBasedQuickSortMode.class).setTimeOut(timeout).setModulePos(this), localModeWatchers);
		}
	}

	public void setSinkSize(int integer) {
		if (MainProxy.isClient(_world.getWorld())) {
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
