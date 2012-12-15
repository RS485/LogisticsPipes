package logisticspipes.modules;

import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import logisticspipes.interfaces.IChassiePowerProvider;
import logisticspipes.interfaces.ILogisticsModule;
import logisticspipes.interfaces.ISendRoutedItem;
import logisticspipes.interfaces.IWorldProvider;
import logisticspipes.logisticspipes.IInventoryProvider;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.SinkReply.FixedPriority;

public class ModuleApiaristTerminus implements ILogisticsModule {

	private IInventoryProvider _invProvider;
	private IChassiePowerProvider _power;
	private int xCoord;
	private int yCoord;
	private int zCoord;
	
	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound, String prefix) {}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound, String prefix) {}

	@Override
	public void registerHandler(IInventoryProvider invProvider, ISendRoutedItem itemSender, IWorldProvider world, IChassiePowerProvider powerProvider) {
		_invProvider = invProvider;
		_power = powerProvider;	
	}

	@Override
	public void registerPosition(int xCoord, int yCoord, int zCoord, int slot) {
		this.xCoord = xCoord;
		this.yCoord = yCoord;
		this.zCoord = zCoord;
	}

	@Override
	public int getGuiHandlerID() {
		return -1;
	}
	
	private boolean replyCheck(ItemStack item) {
		if (SimpleServiceLocator.forestryProxy.isDrone(item)) {
			return true;
		}
		return false;
	}

	@Override
	public SinkReply sinksItem(ItemStack item) {
		boolean decision = replyCheck(item);
		if (decision) {
			if (_power.useEnergy(5)) {
				SinkReply reply = new SinkReply();
				reply.fixedPriority = FixedPriority.Terminus;
				reply.isDefault = false;
				reply.isPassive = true;
				MainProxy.proxy.spawnGenericParticle("BlueParticle", this.xCoord, this.yCoord, this.zCoord, 2);
				return reply;
			}
		}
		return null;
	}

	@Override
	public ILogisticsModule getSubModule(int slot) {
		return null;
	}

	@Override
	public void tick() {}
	

}
