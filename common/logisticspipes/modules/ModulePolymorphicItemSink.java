package logisticspipes.modules;

import logisticspipes.interfaces.ILogisticsModule;
import logisticspipes.interfaces.ISendRoutedItem;
import logisticspipes.interfaces.IWorldProvider;
import logisticspipes.logisticspipes.IInventoryProvider;
import logisticspipes.logisticspipes.modules.SinkReply;
import logisticspipes.logisticspipes.modules.SinkReply.FixedPriority;
import logisticspipes.main.SimpleServiceLocator;
import logisticspipes.utils.InventoryUtil;
import logisticspipes.utils.ItemIdentifier;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;

public class ModulePolymorphicItemSink implements ILogisticsModule {
	
	private IInventoryProvider _invProvider;
	
	public ModulePolymorphicItemSink() {}

	@Override
	public void registerHandler(IInventoryProvider invProvider, ISendRoutedItem itemSender, IWorldProvider world) {
		_invProvider = invProvider;
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
		//reply.speedBoost = 20F;
		return reply;
		
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
