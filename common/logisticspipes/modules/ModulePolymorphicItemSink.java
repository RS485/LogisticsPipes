package logisticspipes.modules;

import logisticspipes.interfaces.IChassiePowerProvider;
import logisticspipes.interfaces.ILogisticsModule;
import logisticspipes.interfaces.ISendRoutedItem;
import logisticspipes.interfaces.IWorldProvider;
import logisticspipes.logisticspipes.IInventoryProvider;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.InventoryUtil;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.SinkReply.FixedPriority;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;

public class ModulePolymorphicItemSink implements ILogisticsModule {
	
	private IInventoryProvider _invProvider;
	private IChassiePowerProvider _power;
	
	public ModulePolymorphicItemSink() {}

	@Override
	public void registerHandler(IInventoryProvider invProvider, ISendRoutedItem itemSender, IWorldProvider world, IChassiePowerProvider powerprovider) {
		_invProvider = invProvider;
		_power = powerprovider;
	}

	@Override
	public SinkReply sinksItem(ItemStack item) {
		IInventory targetInventory = _invProvider.getInventory();
		if (targetInventory == null) return null;
		
		InventoryUtil invUtil = SimpleServiceLocator.inventoryUtilFactory.getInventoryUtil(targetInventory);
		if (!invUtil.containsItem(ItemIdentifier.get(item))) return null;
		
		SinkReply reply = new SinkReply();
		reply.fixedPriority = FixedPriority.ItemSink;
		reply.isDefault = false;
		reply.isPassive = true;
		if(_power.useEnergy(3)) {
			return reply;
		}
		return null;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound, String prefix) {}
	
	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound, String prefix) {}

	@Override
	public int getGuiHandlerID() {
		return -1;
	}
	
	@Override
	public ILogisticsModule getSubModule(int slot) {return null;}

	@Override
	public void tick() {}

	@Override
	public void registerPosition(int xCoord, int yCoord, int zCoord, int slot) {}
}
