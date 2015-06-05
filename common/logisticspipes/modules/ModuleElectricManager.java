package logisticspipes.modules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import logisticspipes.gui.hud.modules.HUDElectricManager;
import logisticspipes.interfaces.IClientInformationProvider;
import logisticspipes.interfaces.IHUDModuleHandler;
import logisticspipes.interfaces.IHUDModuleRenderer;
import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.interfaces.IModuleInventoryReceive;
import logisticspipes.interfaces.IModuleWatchReciver;
import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.modules.abstractmodules.LogisticsGuiModule;
import logisticspipes.modules.abstractmodules.LogisticsModule;
import logisticspipes.network.NewGuiHandler;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractguis.ModuleCoordinatesGuiProvider;
import logisticspipes.network.abstractguis.ModuleInHandGuiProvider;
import logisticspipes.network.guis.module.inhand.ElectricModuleInHand;
import logisticspipes.network.guis.module.inpipe.ElectricModuleSlot;
import logisticspipes.network.packets.hud.HUDStartModuleWatchingPacket;
import logisticspipes.network.packets.hud.HUDStopModuleWatchingPacket;
import logisticspipes.network.packets.module.ElectricManagetMode;
import logisticspipes.network.packets.module.ModuleInventory;
import logisticspipes.pipefxhandlers.Particles;
import logisticspipes.pipes.PipeLogisticsChassi.ChassiTargetInformation;
import logisticspipes.pipes.basic.CoreRoutedPipe.ItemSendMode;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.ISimpleInventoryEventHandler;
import logisticspipes.utils.PlayerCollectionList;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.SinkReply.FixedPriority;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierInventory;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.tuples.Triplet;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ModuleElectricManager extends LogisticsGuiModule implements IClientInformationProvider, IHUDModuleHandler, IModuleWatchReciver, ISimpleInventoryEventHandler, IModuleInventoryReceive {

	private final ItemIdentifierInventory _filterInventory = new ItemIdentifierInventory(9, "Electric Items", 1);
	private boolean _dischargeMode;

	private int ticksToAction = 100;
	private int currentTick = 0;

	private IHUDModuleRenderer HUD = new HUDElectricManager(this);

	private final PlayerCollectionList localModeWatchers = new PlayerCollectionList();

	public ModuleElectricManager() {
		_filterInventory.addListener(this);
	}

	public IInventory getFilterInventory() {
		return _filterInventory;
	}

	public boolean isDischargeMode() {
		return _dischargeMode;
	}

	public void setDischargeMode(boolean isDischargeMode) {
		_dischargeMode = isDischargeMode;
		if (!localModeWatchers.isEmpty()) {
			MainProxy.sendToPlayerList(PacketHandler.getPacket(ElectricManagetMode.class).setFlag(isDischargeMode()).setModulePos(this), localModeWatchers);
		}
	}

	private SinkReply _sinkReply;

	@Override
	public void registerPosition(ModulePositionType slot, int positionInt) {
		super.registerPosition(slot, positionInt);
		_sinkReply = new SinkReply(FixedPriority.ElectricManager, 0, true, false, 1, 1, new ChassiTargetInformation(getPositionInt()));
	}

	@Override
	public SinkReply sinksItem(ItemIdentifier stackID, int bestPriority, int bestCustomPriority, boolean allowDefault, boolean includeInTransit) {
		if (bestPriority >= FixedPriority.ElectricManager.ordinal()) {
			return null;
		}
		if (!_service.canUseEnergy(1)) {
			return null;
		}
		ItemStack stack = stackID.makeNormalStack(1);
		if (isOfInterest(stack)) {
			//If item is full and in discharge mode, sink.
			if (_dischargeMode && SimpleServiceLocator.IC2Proxy.isFullyCharged(stack)) {
				return _sinkReply;
			}

			//If item is empty and in charge mode, sink.
			if (!_dischargeMode && SimpleServiceLocator.IC2Proxy.isFullyDischarged(stack)) {
				return _sinkReply;
			}

			//If item is partially charged, sink.
			if (SimpleServiceLocator.IC2Proxy.isPartiallyCharged(stack)) {
				return _sinkReply;
			}
		}
		return null;
	}

	@Override
	protected ModuleCoordinatesGuiProvider getPipeGuiProvider() {
		return NewGuiHandler.getGui(ElectricModuleSlot.class).setFlag(isDischargeMode());
	}

	@Override
	protected ModuleInHandGuiProvider getInHandGuiProvider() {
		return NewGuiHandler.getGui(ElectricModuleInHand.class);
	}

	@Override
	public LogisticsModule getSubModule(int slot) {
		return null;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		_filterInventory.readFromNBT(nbttagcompound, "");
		setDischargeMode(nbttagcompound.getBoolean("discharge"));
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		_filterInventory.writeToNBT(nbttagcompound, "");
		nbttagcompound.setBoolean("discharge", isDischargeMode());
	}

	@Override
	public void tick() {
		if (++currentTick < ticksToAction) {
			return;
		}
		currentTick = 0;

		IInventoryUtil inv = _service.getSneakyInventory(true, slot, positionInt);
		if (inv == null) {
			return;
		}
		for (int i = 0; i < inv.getSizeInventory(); i++) {
			ItemStack stack = inv.getStackInSlot(i);
			if (stack == null) {
				return;
			}
			if (isOfInterest(stack)) {
				//If item set to discharge and its fully discharged, then extract it.
				if (_dischargeMode && SimpleServiceLocator.IC2Proxy.isFullyDischarged(stack)) {
					Triplet<Integer, SinkReply, List<IFilter>> reply = SimpleServiceLocator.logisticsManager.hasDestinationWithMinPriority(ItemIdentifier.get(stack), _service.getSourceID(), true, FixedPriority.ElectricBuffer);
					if (reply == null) {
						continue;
					}
					if (_service.useEnergy(10)) {
						_service.spawnParticle(Particles.OrangeParticle, 2);
						_service.sendStack(inv.decrStackSize(i, 1), reply, ItemSendMode.Normal);
						return;
					}
				}
				//If item set to charge  and its fully charged, then extract it.
				if (!_dischargeMode && SimpleServiceLocator.IC2Proxy.isFullyCharged(stack)) {
					Triplet<Integer, SinkReply, List<IFilter>> reply = SimpleServiceLocator.logisticsManager.hasDestinationWithMinPriority(ItemIdentifier.get(stack), _service.getSourceID(), true, FixedPriority.ElectricBuffer);
					if (reply == null) {
						continue;
					}
					if (_service.useEnergy(10)) {
						_service.spawnParticle(Particles.OrangeParticle, 2);
						_service.sendStack(inv.decrStackSize(i, 1), reply, ItemSendMode.Normal);
						return;
					}
				}
			}
		}
	}

	private boolean isOfInterest(ItemStack stack) {
		if (!SimpleServiceLocator.IC2Proxy.isElectricItem(stack)) {
			return false;
		}
		for (int i = 0; i < _filterInventory.getSizeInventory(); i++) {
			ItemStack fStack = _filterInventory.getStackInSlot(i);
			if (fStack == null) {
				continue;
			}
			if (SimpleServiceLocator.IC2Proxy.isSimilarElectricItem(stack, fStack)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public List<String> getClientInformation() {
		List<String> list = new ArrayList<String>();
		list.add("Mode: " + (isDischargeMode() ? "Discharge Items" : "Charge Items"));
		list.add("Supplied: ");
		list.add("<inventory>");
		list.add("<that>");
		return list;
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
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(ModuleInventory.class).setIdentList(ItemIdentifierStack.getListFromInventory(_filterInventory)).setModulePos(this), player);
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(ElectricManagetMode.class).setFlag(isDischargeMode()).setModulePos(this), player);
	}

	@Override
	public void stopWatching(EntityPlayer player) {
		localModeWatchers.remove(player);
	}

	@Override
	public IHUDModuleRenderer getHUDRenderer() {
		return HUD;
	}

	@Override
	public void InventoryChanged(IInventory inventory) {
		if (MainProxy.isServer(_world.getWorld())) {
			MainProxy.sendToPlayerList(PacketHandler.getPacket(ModuleInventory.class).setIdentList(ItemIdentifierStack.getListFromInventory(inventory)).setModulePos(this), localModeWatchers);
		}
	}

	@Override
	public void handleInvContent(Collection<ItemIdentifierStack> list) {
		_filterInventory.handleItemIdentifierList(list);
	}

	@Override
	public boolean hasGenericInterests() {
		return true;
	}

	@Override
	public List<ItemIdentifier> getSpecificInterests() {
		return null;
	}

	@Override
	public boolean interestedInAttachedInventory() {
		return false; // would be true, but hasGenericInterests means this is interested anyway.
	}

	@Override
	public boolean interestedInUndamagedID() {
		return false;
	}

	@Override
	public boolean recievePassive() {
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIconTexture(IIconRegister register) {
		return register.registerIcon("logisticspipes:itemModule/ModuleElectricManager");
	}
}
