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

	@Override
	public SinkReply sinksItem(ItemStack stack) {
		if (SimpleServiceLocator.IC2Proxy.isElectricItem(stack)) {
			return positiveSinkReply();
		}
		return null;
	}

	private SinkReply positiveSinkReply() {
		if (!_power.useEnergy(1)) return null;
		SinkReply reply = new SinkReply();
		reply.fixedPriority = FixedPriority.ElectricNetwork;
		reply.isPassive = true;
		MainProxy.sendSpawnParticlePacket(Particles.BlueParticle, xCoord, yCoord, zCoord, _world.getWorld(), 2);
		return reply;
	}

	@Override
	public ILogisticsModule getSubModule(int slot) {
		return null;
	}

	@Override
	public void tick() {
		if (++currentTickCount < ticksToAction) return;
		currentTickCount = 0;
		
		IInventory inv = _invProvider.getInventory();
		if (inv == null) return;
		for (int i = 0; i < inv.getSizeInventory(); i++) {
			ItemStack stack = inv.getStackInSlot(i);
			if (stack == null) continue;
			if (SimpleServiceLocator.IC2Proxy.isElectricItem(stack)) {
				if (SimpleServiceLocator.logisticsManager.hasDestinationWithPriority(stack, _itemSender.getSourceUUID(), true, FixedPriority.ElectricNetwork)) {
					MainProxy.sendSpawnParticlePacket(Particles.OrangeParticle, this.xCoord, this.yCoord, this.zCoord, _world.getWorld(), 2);
					_itemSender.sendStack(inv.decrStackSize(i, 1));
					return;
				}
			}
			continue;
		}
	}

}
