package logisticspipes.modules;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import logisticspipes.interfaces.IClientInformationProvider;
import logisticspipes.interfaces.IModuleWatchReciver;
import logisticspipes.modules.abstractmodules.LogisticsGuiModule;
import logisticspipes.modules.abstractmodules.LogisticsModule;
import logisticspipes.network.NewGuiHandler;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractguis.ModuleCoordinatesGuiProvider;
import logisticspipes.network.abstractguis.ModuleInHandGuiProvider;
import logisticspipes.network.guis.module.inhand.ThaumicAspectSinkModuleInHand;
import logisticspipes.network.guis.module.inpipe.ThaumicAspectSinkModuleSlot;
import logisticspipes.network.packets.module.ThaumicAspectsSinkList;
import logisticspipes.pipes.PipeLogisticsChassi.ChassiTargetInformation;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.PlayerCollectionList;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.SinkReply.FixedPriority;
import logisticspipes.utils.item.ItemIdentifier;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ModuleThaumicAspectSink extends LogisticsGuiModule implements IClientInformationProvider, IModuleWatchReciver {

	public final List<String> aspectList = new LinkedList<String>();

	private final PlayerCollectionList localModeWatchers = new PlayerCollectionList();

	private SinkReply _sinkReply;

	@Override
	public void registerPosition(ModulePositionType slot, int positionInt) {
		super.registerPosition(slot, positionInt);
		_sinkReply = new SinkReply(FixedPriority.ItemSink, -2, true, false, 5, 0, new ChassiTargetInformation(getPositionInt()));
	}

	@Override
	public SinkReply sinksItem(ItemIdentifier item, int bestPriority, int bestCustomPriority, boolean allowDefault, boolean includeInTransit) {
		if (bestPriority > _sinkReply.fixedPriority.ordinal() || (bestPriority == _sinkReply.fixedPriority.ordinal() && bestCustomPriority >= _sinkReply.customPriority)) {
			return null;
		}
		if (isOfInterest(item)) {
			return _sinkReply;
		}
		return null;
	}

	private boolean isOfInterest(ItemIdentifier itemID) {
		if (itemID == null || aspectList.size() == 0) {
			return false;
		}
		ItemStack item = itemID.makeNormalStack(1);
		List<String> itemAspectList = SimpleServiceLocator.thaumCraftProxy.getListOfTagsForStack(item);
		if (itemAspectList == null || itemAspectList.size() == 0) {
			return false;
		}
		for (int i = 0; i < itemAspectList.size(); i++) {
			if (aspectList.contains(itemAspectList.get(i))) {
				return true;
			}
		}
		return false;
	}

	@Override
	public LogisticsModule getSubModule(int slot) {
		return null;
	}

	@Override
	public void tick() {}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		aspectList.clear();
		int size = nbttagcompound.getInteger("aspectTagListSize");
		if (size <= 0) {
			return;
		}
		for (int i = 0; i < size; i++) {
			aspectList.add(nbttagcompound.getString("aspectTag" + i));
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		nbttagcompound.setInteger("aspectTagListSize", aspectList.size());
		if (aspectList.size() <= 0) {
			return;
		}
		for (int i = 0; i < aspectList.size(); i++) {
			nbttagcompound.setString("aspectTag" + i, aspectList.get(i));
		}
	}

	@Override
	public void startWatching(EntityPlayer player) {
		localModeWatchers.add(player);
		NBTTagCompound nbt = new NBTTagCompound();
		writeToNBT(nbt);
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(ThaumicAspectsSinkList.class).setTag(nbt).setModulePos(this), player);
	}

	@Override
	public void stopWatching(EntityPlayer player) {
		localModeWatchers.remove(player);
	}

	public void aspectListChanged() {
		if (MainProxy.isServer(_world.getWorld())) {
			NBTTagCompound nbt = new NBTTagCompound();
			writeToNBT(nbt);
			MainProxy.sendToPlayerList(PacketHandler.getPacket(ThaumicAspectsSinkList.class).setTag(nbt).setModulePos(this), localModeWatchers);
		} else {
			NBTTagCompound nbt = new NBTTagCompound();
			writeToNBT(nbt);
			MainProxy.sendPacketToServer(PacketHandler.getPacket(ThaumicAspectsSinkList.class).setTag(nbt).setModulePos(this));
		}
	}

	@Override
	public List<String> getClientInformation() {
		List<String> info = new ArrayList<String>();
		info.add("Aspects: ");
		if (aspectList.size() == 0) {
			info.add("none");
		}
		for (int i = 0; i < aspectList.size(); i++) {
			//info.add(" - " + SimpleServiceLocator.thaumCraftProxy.getNameForTagID(aspectList.get(i)));
			info.add(" - " + aspectList.get(i));
		}
		return info;
	}

	@Override
	protected ModuleCoordinatesGuiProvider getPipeGuiProvider() {
		NBTTagCompound tag = new NBTTagCompound();
		writeToNBT(tag);
		return NewGuiHandler.getGui(ThaumicAspectSinkModuleSlot.class).setNbt(tag);
	}

	@Override
	protected ModuleInHandGuiProvider getInHandGuiProvider() {
		return NewGuiHandler.getGui(ThaumicAspectSinkModuleInHand.class);
	}

	public void guiAddAspect(String aspect) {
		if (aspectList.contains(aspect) || aspectList.size() >= 9) {
			return;
		}
		aspectList.add(aspect);
		aspectListChanged();
	}

	public void guiRemoveAspect(String aspect) {
		if (!aspectList.contains(aspect)) {
			return;
		}
		aspectList.remove(aspect);
		aspectListChanged();
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

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIconTexture(IIconRegister register) {
		return register.registerIcon("logisticspipes:itemModule/ModuleThaumicAspectSink");
	}
}
