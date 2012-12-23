package logisticspipes.modules;

import logisticspipes.interfaces.IChassiePowerProvider;
import logisticspipes.interfaces.ILogisticsGuiModule;
import logisticspipes.interfaces.ILogisticsModule;
import logisticspipes.interfaces.ISendRoutedItem;
import logisticspipes.interfaces.IWorldProvider;
import logisticspipes.logisticspipes.IInventoryProvider;
import logisticspipes.pipefxhandlers.Particles;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.SimpleInventory;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.SinkReply.FixedPriority;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class ModuleApiaristTerminus implements ILogisticsModule {

	private IInventoryProvider _invProvider;
	private IChassiePowerProvider _power;
	private int xCoord;
	private int yCoord;
	private int zCoord;
	private IWorldProvider _world;
	
	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {}

	@Override
	public void registerHandler(IInventoryProvider invProvider, ISendRoutedItem itemSender, IWorldProvider world, IChassiePowerProvider powerProvider) {
		_invProvider = invProvider;
		_power = powerProvider;
		_world = world;
	}

	@Override
	public void registerPosition(int xCoord, int yCoord, int zCoord, int slot) {
		this.xCoord = xCoord;
		this.yCoord = yCoord;
		this.zCoord = zCoord;
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
				MainProxy.sendSpawnParticlePacket(Particles.BlueParticle, xCoord, yCoord, this.zCoord, _world.getWorld(), 2);
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
