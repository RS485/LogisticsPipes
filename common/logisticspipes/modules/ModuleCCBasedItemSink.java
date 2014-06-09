package logisticspipes.modules;

import java.util.Collection;
import java.util.List;

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
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Icon;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ModuleCCBasedItemSink extends LogisticsModule {
	
	private IQueueCCEvent	eventQueuer;
	private IInventoryProvider	invProvider;

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {}
	
	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {}
	
	@Override
	public void registerHandler(IInventoryProvider invProvider, IWorldProvider world, IRoutedPowerProvider powerProvider) {
		this.invProvider = invProvider;
	}
	
	@Override
	public void registerCCEventQueuer(IQueueCCEvent eventQueuer) {
		this.eventQueuer = eventQueuer;
	}
	
	@Override
	public int getX() {
		return invProvider.getX();
	}
	
	@Override
	public int getY() {
		return invProvider.getY();
	}
	
	@Override
	public int getZ() {
		return invProvider.getZ();
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
		CCSinkResponder resonse = new CCSinkResponder(item, invProvider.getSourceID(), eventQueuer);
		eventQueuer.queueEvent("ItemSink", new Object[]{SimpleServiceLocator.ccProxy.getAnswer(resonse)});
		return new OneList<CCSinkResponder>(resonse);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIconTexture(IconRegister register) {
		return register.registerIcon("logisticspipes:itemModule/ModuleCCBasedItemSink");
	}
}
