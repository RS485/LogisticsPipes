package logisticspipes.modules;

import logisticspipes.interfaces.IChassiePowerProvider;
import logisticspipes.interfaces.ILogisticsModule;
import logisticspipes.interfaces.ISendRoutedItem;
import logisticspipes.interfaces.IWorldProvider;
import logisticspipes.logisticspipes.IInventoryProvider;
import logisticspipes.pipefxhandlers.Particles;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.SinkReply.FixedPriority;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class ModuleApiaristRefiller implements ILogisticsModule {
	
	private IInventoryProvider _invProvider;
	private IChassiePowerProvider _power;
	private int maxInvSize = 12;
	private int currentTicksEmpty = 0;
	private int maxTicksEmpty = 50;
	private boolean functionalStatus = true;
	private int xCoord;
	private int yCoord;
	private int zCoord;
	private IWorldProvider _world;
	
	public ModuleApiaristRefiller() {}
	
	@Override
	public void registerHandler(IInventoryProvider invProvider, ISendRoutedItem itemSender, IWorldProvider world, IChassiePowerProvider powerProvider) {
		_invProvider = invProvider;
		_power = powerProvider;
		_world = world;
	}
	
	private boolean apiaryCheck(ItemStack item) {
		if (!SimpleServiceLocator.forestryProxy.isBee(item)) {
			return false;
		}
		IInventory saidInventory = _invProvider.getInventory();
		if (saidInventory == null) {
			return false;
		}
		//TODO implement better method of limiting function to only apiary
		if ((saidInventory.getSizeInventory() < 2) || (saidInventory.getSizeInventory() > maxInvSize)) {
			return false;
		}
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
	
	@Override
	public SinkReply sinksItem(ItemStack item) {
		if (functionalStatus) {
			if (apiaryCheck(item)) {
				if (_power.useEnergy(50)) {
					SinkReply reply = new SinkReply();
					reply.fixedPriority = FixedPriority.APIARIST_Refiller;
					reply.isDefault = false;
					reply.isPassive = true;
					MainProxy.sendSpawnParticlePacket(Particles.BlueParticle, xCoord, yCoord, this.zCoord, _world.getWorld(), 2);
					return reply;
				}
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
	public void registerPosition(int xCoord, int yCoord, int zCoord, int slot) {
		this.xCoord = xCoord;
		this.yCoord = yCoord;
		this.zCoord = zCoord;
	}

	@Override
	public void tick() {
		/* Disables modules if inventory has been empty for too long */
		IInventory saidInventory = _invProvider.getInventory();
		if (saidInventory == null) {
			return;
		}
		if ((saidInventory.getSizeInventory() < 2) || (saidInventory.getSizeInventory() > maxInvSize)) {
			return;
		}
		ItemStack apiarySlot1 = saidInventory.getStackInSlot(0);
		ItemStack apiarySlot2 = saidInventory.getStackInSlot(1);
		if (functionalStatus == true) {
			if (apiarySlot1 == null && apiarySlot2 == null) {
				currentTicksEmpty++;
			}
		}
		if (currentTicksEmpty > maxTicksEmpty) {
			currentTicksEmpty = 0;
			functionalStatus = false;
		}
		if (apiarySlot1 != null || apiarySlot2 != null) {
			functionalStatus = true;
			currentTicksEmpty = 0;
		}
	}
	
	

}
