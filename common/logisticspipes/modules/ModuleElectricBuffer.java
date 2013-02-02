package logisticspipes.modules;

import java.util.List;

import logisticspipes.interfaces.IChassiePowerProvider;
import logisticspipes.interfaces.ILogisticsModule;
import logisticspipes.interfaces.ISendRoutedItem;
import logisticspipes.interfaces.IWorldProvider;
import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.logisticspipes.IInventoryProvider;
import logisticspipes.pipefxhandlers.Particles;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.Pair3;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.SinkReply.FixedPriority;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class ModuleElectricBuffer implements ILogisticsModule {
	private IInventoryProvider _invProvider;
	private IChassiePowerProvider _power;
	private ISendRoutedItem _itemSender;
	private int xCoord;
	private int yCoord;
	private int zCoord;
	private IWorldProvider _world;

	private int currentTickCount = 0;
	private int ticksToAction = 80;

	public ModuleElectricBuffer() {}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {}

	@Override
	public void registerHandler(IInventoryProvider invProvider, ISendRoutedItem itemSender, IWorldProvider world, IChassiePowerProvider powerProvider) {		
		_invProvider = invProvider;
		_power = powerProvider;
		_world = world;
		_itemSender = itemSender;
	}

	@Override
	public void registerPosition(int xCoord, int yCoord, int zCoord, int slot) {
		this.xCoord = xCoord;
		this.yCoord = yCoord;
		this.zCoord = zCoord;
	}

	private final SinkReply _sinkReply = new SinkReply(FixedPriority.ElectricNetwork, 0, true, false, 1, 0);
	@Override
	public SinkReply sinksItem(ItemStack stack, int bestPriority, int bestCustomPriority) {
		if (bestPriority >= FixedPriority.ElectricNetwork.ordinal()) return null;
		if (SimpleServiceLocator.IC2Proxy.isElectricItem(stack)) {
			if (_power.canUseEnergy(1)) {
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
	public void tick() {
		if (++currentTickCount < ticksToAction) return;
		currentTickCount = 0;

		IInventory inv = _invProvider.getPointedInventory();
		if (inv == null) return;
		for (int i = 0; i < inv.getSizeInventory(); i++) {
			ItemStack stack = inv.getStackInSlot(i);
			if (stack == null) continue;
			if (SimpleServiceLocator.IC2Proxy.isElectricItem(stack)) {
				Pair3<Integer, SinkReply, List<IFilter>> reply = SimpleServiceLocator.logisticsManager.hasDestinationWithMinPriority(stack, _itemSender.getSourceID(), true, FixedPriority.ElectricNetwork);
				if(reply == null) continue;
				MainProxy.sendSpawnParticlePacket(Particles.OrangeParticle, this.xCoord, this.yCoord, this.zCoord, _world.getWorld(), 2);
				_itemSender.sendStack(inv.decrStackSize(i, 1), reply);
				return;
			}
			continue;
		}
	}

}
