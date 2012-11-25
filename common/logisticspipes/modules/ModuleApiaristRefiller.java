package logisticspipes.modules;

import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import logisticspipes.interfaces.IChassiePowerProvider;
import logisticspipes.interfaces.ILogisticsModule;
import logisticspipes.interfaces.ISendRoutedItem;
import logisticspipes.interfaces.IWorldProvider;
import logisticspipes.logisticspipes.IInventoryProvider;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.SinkReply.FixedPriority;

public class ModuleApiaristRefiller implements ILogisticsModule {
	
	private IInventoryProvider _invProvider;
	private IChassiePowerProvider _power;
	private int maxInvSize = 12;
	
	public ModuleApiaristRefiller() {}
	
	@Override
	public void registerHandler(IInventoryProvider invProvider, ISendRoutedItem itemSender, IWorldProvider world, IChassiePowerProvider powerProvider) {
		_invProvider = invProvider;
		_power = powerProvider;
	}
	
	private boolean apiaryCheck(ItemStack item) {
		if (SimpleServiceLocator.forestryProxy.isBee(item)) {
			IInventory saidInventory = _invProvider.getInventory();
			//TODO implement better method of limiting function to only apiary
			if ((saidInventory.getSizeInventory()) <= maxInvSize) {
				ItemStack apiarySlot1 = saidInventory.getStackInSlot(0);
				ItemStack apiarySlot2 = saidInventory.getStackInSlot(1);
				if (SimpleServiceLocator.forestryProxy.isQueen(apiarySlot1)) {
					return false;
				}
				if (SimpleServiceLocator.forestryProxy.isDrone(item) && (apiarySlot2 != null)) {
					return false;
				}
				if (SimpleServiceLocator.forestryProxy.isPrincess(item) && (apiarySlot1 !=null)) {
					return false;
				}
				return true;
			}
		}
		return false;
	}
	
	@Override
	public SinkReply sinksItem(ItemStack item) {
		boolean decision = apiaryCheck(item);
		if (decision) {
			if (_power.useEnergy(50)) {
				SinkReply reply = new SinkReply();
				reply.fixedPriority = FixedPriority.APIARIST_Refiller;
				reply.isDefault = false;
				reply.isPassive = true;
				return reply;
			}
		}
		
		return null;
	}
	
	@Override
	public int getGuiHandlerID() {
		return -1;
	}
	
	@Override
	public ILogisticsModule getSubModule(int slot) {
		return null;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound, String prefix) {}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound, String prefix) {}
	
	@Override
	public void registerPosition(int xCoord, int yCoord, int zCoord, int slot) {}

	@Override
	public void tick() {}
	
	

}
