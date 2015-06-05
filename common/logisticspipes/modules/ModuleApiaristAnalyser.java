package logisticspipes.modules;

import java.util.ArrayList;
import java.util.List;

import logisticspipes.interfaces.IClientInformationProvider;
import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.interfaces.IModuleWatchReciver;
import logisticspipes.modules.abstractmodules.LogisticsGuiModule;
import logisticspipes.modules.abstractmodules.LogisticsModule;
import logisticspipes.network.NewGuiHandler;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractguis.ModuleCoordinatesGuiProvider;
import logisticspipes.network.abstractguis.ModuleInHandGuiProvider;
import logisticspipes.network.guis.module.inhand.ApiaristAnalyserModuleInHand;
import logisticspipes.network.guis.module.inpipe.ApiaristAnalyzerModuleSlot;
import logisticspipes.network.packets.module.ApiaristAnalyserMode;
import logisticspipes.pipes.PipeLogisticsChassi.ChassiTargetInformation;
import logisticspipes.pipes.basic.CoreRoutedPipe.ItemSendMode;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.PlayerCollectionList;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.SinkReply.FixedPriority;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.tuples.Pair;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ModuleApiaristAnalyser extends LogisticsGuiModule implements IClientInformationProvider, IModuleWatchReciver {

	private int ticksToAction = 100;
	private int currentTick = 0;

	private final PlayerCollectionList localModeWatchers = new PlayerCollectionList();

	public boolean extractMode = true;

	public ModuleApiaristAnalyser() {

	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		extractMode = nbt.getBoolean("extractMode");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		nbt.setBoolean("extractMode", extractMode);
	}

	private SinkReply _sinkReply;

	@Override
	public void registerPosition(ModulePositionType slot, int positionInt) {
		super.registerPosition(slot, positionInt);
		_sinkReply = new SinkReply(FixedPriority.APIARIST_Analyser, 0, true, false, 3, 0, new ChassiTargetInformation(getPositionInt()));
	}

	@Override
	public SinkReply sinksItem(ItemIdentifier itemID, int bestPriority, int bestCustomPriority, boolean allowDefault, boolean includeInTransit) {
		if (bestPriority > _sinkReply.fixedPriority.ordinal() || (bestPriority == _sinkReply.fixedPriority.ordinal() && bestCustomPriority >= _sinkReply.customPriority)) {
			return null;
		}
		ItemStack item = itemID.makeNormalStack(1);
		if (SimpleServiceLocator.forestryProxy.isBee(item)) {
			if (!SimpleServiceLocator.forestryProxy.isAnalysedBee(item)) {
				if (_service.canUseEnergy(3)) {
					return _sinkReply;
				}
			}
		}
		return null;
	}

	@Override
	public LogisticsModule getSubModule(int slot) {
		return null;
	}

	@Override
	public void tick() {
		if (extractMode) {
			if (++currentTick < ticksToAction) {
				return;
			}
			currentTick = 0;
			IInventoryUtil inv = _service.getUnsidedInventory();
			if (inv == null) {
				return;
			}
			for (int i = 0; i < inv.getSizeInventory(); i++) {
				ItemStack item = inv.getStackInSlot(i);
				if (SimpleServiceLocator.forestryProxy.isBee(item)) {
					if (SimpleServiceLocator.forestryProxy.isAnalysedBee(item)) {
						Pair<Integer, SinkReply> reply = _service.hasDestination(ItemIdentifier.get(item), true, new ArrayList<Integer>());
						if (reply == null) {
							continue;
						}
						if (_service.useEnergy(6)) {
							_service.sendStack(inv.decrStackSize(i, 1), reply, ItemSendMode.Normal);
						}
					}
				}
			}
		}
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
		return false;
	}

	@Override
	public boolean interestedInUndamagedID() {
		return false;
	}

	@Override
	public boolean recievePassive() {
		return true;
	}

	public void setExtractMode(int mode) {
		if (getExtractMode() == mode) {
			return;
		}

		if (mode == 1) {
			extractMode = true;
		} else if (mode == 0) {
			extractMode = false;
		}
		modeChanged();
	}

	public int getExtractMode() {
		return extractMode ? 1 : 0;
	}

	public void modeChanged() {
		if (MainProxy.isServer(_world.getWorld())) {
			if (getSlot().isInWorld()) {
				MainProxy.sendToPlayerList(PacketHandler.getPacket(ApiaristAnalyserMode.class).setMode(getExtractMode()).setModulePos(this), localModeWatchers);
			}
		} else {
			MainProxy.sendPacketToServer(PacketHandler.getPacket(ApiaristAnalyserMode.class).setMode(getExtractMode()).setModulePos(this));
		}
	}

	@Override
	public List<String> getClientInformation() {
		List<String> info = new ArrayList<String>();
		info.add("Extract Mode:");
		info.add(" - " + (extractMode ? "on" : "off"));
		return info;
	}

	@Override
	public void startWatching(EntityPlayer player) {
		localModeWatchers.add(player);
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(ApiaristAnalyserMode.class).setMode(getExtractMode()).setModulePos(this), player);
	}

	@Override
	public void stopWatching(EntityPlayer player) {
		localModeWatchers.remove(player);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIconTexture(IIconRegister register) {
		return register.registerIcon("logisticspipes:itemModule/ModuleApiaristAnalyser");
	}

	@Override
	protected ModuleCoordinatesGuiProvider getPipeGuiProvider() {
		return NewGuiHandler.getGui(ApiaristAnalyzerModuleSlot.class).setExtractorMode(getExtractMode());
	}

	@Override
	protected ModuleInHandGuiProvider getInHandGuiProvider() {
		return NewGuiHandler.getGui(ApiaristAnalyserModuleInHand.class);
	}
}
