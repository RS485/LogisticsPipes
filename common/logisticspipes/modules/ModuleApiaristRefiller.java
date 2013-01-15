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
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.inventory.ISpecialInventory;

public class ModuleApiaristRefiller implements ILogisticsModule {
	
	private IInventoryProvider _invProvider;
	private IChassiePowerProvider _power;
	private ISendRoutedItem _itemSender;
	private int xCoord;
	private int yCoord;
	private int zCoord;
	private IWorldProvider _world;
	
	private int currentTickCount = 0;
	private int ticksToOperation = 200;
	
	public ModuleApiaristRefiller() {}
	
	@Override
	public void registerHandler(IInventoryProvider invProvider, ISendRoutedItem itemSender, IWorldProvider world, IChassiePowerProvider powerProvider) {
		_invProvider = invProvider;
		_power = powerProvider;
		_world = world;
		_itemSender = itemSender;
	}
		
	@Override
	public SinkReply sinksItem(ItemStack item) {
		return null;
	}
	
	@Override
	public ILogisticsModule getSubModule(int slot) {
		return null;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {}
	
	@Override
	public void registerPosition(int xCoord, int yCoord, int zCoord, int slot) {
		this.xCoord = xCoord;
		this.yCoord = yCoord;
		this.zCoord = zCoord;
	}

	@Override
	public void tick() {
		doOperation();
	}

	private void doOperation() {
		if (++currentTickCount < ticksToOperation) return;
		currentTickCount = 0;
		IInventory inv = _invProvider.getRawInventory();
		if (inv instanceof ISpecialInventory) {
			ForgeDirection direction = _invProvider.inventoryOrientation().getOpposite();
			ItemStack[] stack = ((ISpecialInventory) inv).extractItem(true, direction, 1);
			if (stack[0] == null) return;
			//if no queen/princess
			if ((inv.getStackInSlot(0) == null)) {
				if (SimpleServiceLocator.forestryProxy.isPrincess(stack[0])) {
					if (!(_power.useEnergy(100))) return;
					((ISpecialInventory) inv).addItem(stack[0], true, direction);
					MainProxy.sendSpawnParticlePacket(Particles.VioletParticle, this.xCoord, this.yCoord, this.zCoord, _world.getWorld(), 5);
					return;
				}
			}
			//if princess w/out drone
			if ((inv.getStackInSlot(1) == null) && !(SimpleServiceLocator.forestryProxy.isQueen(inv.getStackInSlot(0)))) {
				if (SimpleServiceLocator.forestryProxy.isDrone(stack[0])) {
					if (!(_power.useEnergy(100))) return;
					((ISpecialInventory) inv).addItem(stack[0], true, direction);
					MainProxy.sendSpawnParticlePacket(Particles.VioletParticle, this.xCoord, this.yCoord, this.zCoord, _world.getWorld(), 5);
					return;
				}
			}
			//Extract unneeded items
			if (!(_power.useEnergy(20))) return;
			_itemSender.sendStack(stack[0]);
		}
	}
}
