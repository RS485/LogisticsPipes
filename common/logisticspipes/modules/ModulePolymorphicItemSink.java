package logisticspipes.modules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import logisticspipes.interfaces.IChassiePowerProvider;
import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.interfaces.ILogisticsModule;
import logisticspipes.interfaces.ISendRoutedItem;
import logisticspipes.interfaces.IWorldProvider;
import logisticspipes.logisticspipes.IInventoryProvider;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.SinkReply.FixedPriority;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class ModulePolymorphicItemSink implements ILogisticsModule {
	
	private IInventoryProvider _invProvider;
	private IChassiePowerProvider _power;
	
	public ModulePolymorphicItemSink() {}

	@Override
	public void registerHandler(IInventoryProvider invProvider, ISendRoutedItem itemSender, IWorldProvider world, IChassiePowerProvider powerprovider) {
		_invProvider = invProvider;
		_power = powerprovider;
	}

	private static final SinkReply _sinkReply = new SinkReply(FixedPriority.ItemSink, 0, true, false, 3, 0);
	@Override
	public SinkReply sinksItem(ItemIdentifier item, int bestPriority, int bestCustomPriority) {
		if(bestPriority > _sinkReply.fixedPriority.ordinal() || (bestPriority == _sinkReply.fixedPriority.ordinal() && bestCustomPriority >= _sinkReply.customPriority)) return null;
		IInventory targetInventory = _invProvider.getSneakyInventory();
		if (targetInventory == null) return null;
		
		IInventoryUtil invUtil = SimpleServiceLocator.inventoryUtilFactory.getInventoryUtil(targetInventory);
		if (!invUtil.containsUndamagedItem(item.toUndamaged())) return null;
		
		if(_power.canUseEnergy(3)) {
			return _sinkReply;
		}
		return null;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {}
	
	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {}
	
	@Override
	public ILogisticsModule getSubModule(int slot) {return null;}

	@Override
	public void tick() {}

	@Override
	public void registerPosition(int xCoord, int yCoord, int zCoord, int slot) {}

	@Override
	public boolean hasGenericInterests() {
		return false;
	}
	//TODO: SINK UNDAMAGED MATCH CORRECTLY!
	
	@Override
	public List<ItemIdentifier> getSpecificInterests() {
		return null;
	}

	@Override
	public boolean interestedInAttachedInventory() {		
		return true; // by definition :)
	}

	@Override
	public boolean interestedInUndamagedID() {
		return true;
	}
}
