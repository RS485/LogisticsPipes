package logisticspipes.modules;

import java.util.Collection;
import java.util.List;

import javax.swing.Icon;

import logisticspipes.api.IRoutedPowerProvider;
import logisticspipes.interfaces.IQueueCCEvent;
import logisticspipes.interfaces.ISendRoutedItem;
import logisticspipes.interfaces.IWorldProvider;
import logisticspipes.logisticspipes.IInventoryProvider;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.cc.CCSinkResponder;
import logisticspipes.utils.OneList;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.nbt.NBTTagCompound;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.util.IIcon;

public class ModuleCCBasedItemSink extends LogisticsModule {
	
	private IInventoryProvider	coords;
	private IQueueCCEvent	eventQueuer;
	private ISendRoutedItem	itemSender;

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {}
	
	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {}
	
	@Override
	public void registerHandler(IInventoryProvider invProvider, ISendRoutedItem itemSender, IWorldProvider world, IRoutedPowerProvider powerProvider) {
		coords = invProvider;
		this.itemSender = itemSender;
	}
	
	@Override
	public void registerCCEventQueuer(IQueueCCEvent eventQueuer) {
		this.eventQueuer = eventQueuer;
	}

	@Override
	public void registerSlot(int slot) {}
	
	@Override
	public int getX() {
		return coords.getX();
	}
	
	@Override
	public int getY() {
		return coords.getY();
	}
	
	@Override
	public int getZ() {
		return coords.getZ();
	}
	
	@Override
	public SinkReply sinksItem(ItemIdentifier stack, int bestPriority, int bestCustomPriority, boolean allowDefault, boolean includeInTransit) {
		return null;
	}
	
	@Override
	public LogisticsModule getSubModule(int slot) {
		return null;
	}
	
	@Override
	public void tick() {}
	
	@Override
	public boolean hasGenericInterests() {
		return true;
	}
	
	@Override
	public Collection<ItemIdentifier> getSpecificInterests() {
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
		return false;
	}
	
	@Override
	public List<CCSinkResponder> queueCCSinkEvent(ItemIdentifierStack item) {
		CCSinkResponder resonse = new CCSinkResponder(item, itemSender.getSourceID(), eventQueuer);
		eventQueuer.queueEvent("ItemSink", new Object[]{SimpleServiceLocator.ccProxy.getAnswer(resonse)});
		return new OneList<CCSinkResponder>(resonse);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIconTexture(IIconRegister register) {
		return register.registerIcon("logisticspipes:itemModule/ModuleCCBasedItemSink");
	}
}
