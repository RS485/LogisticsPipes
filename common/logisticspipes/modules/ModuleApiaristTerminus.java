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
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class ModuleApiaristTerminus implements ILogisticsModule {

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

	private final SinkReply _sinkReply = new SinkReply(FixedPriority.Terminus, 0, true, false, 5, 0);
	@Override
	public SinkReply sinksItem(ItemStack item, int bestPriority, int bestCustomPriority) {
		if (bestPriority >= FixedPriority.Terminus.ordinal()) return null;
		boolean decision = replyCheck(item);
		if (decision) {
			if (_power.canUseEnergy(5)) {
				MainProxy.sendSpawnParticlePacket(Particles.BlueParticle, xCoord, yCoord, zCoord, _world.getWorld(), 2);
				return _sinkReply;
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
