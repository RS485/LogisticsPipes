package logisticspipes.modules;

import logisticspipes.interfaces.IChassiePowerProvider;
import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.interfaces.ILogisticsModule;
import logisticspipes.interfaces.ISendRoutedItem;
import logisticspipes.interfaces.IWorldProvider;
import logisticspipes.logisticspipes.IInventoryProvider;
import logisticspipes.pipefxhandlers.Particles;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.SinkReply.FixedPriority;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class ModulePolymorphicItemSink implements ILogisticsModule {
	
	private IInventoryProvider _invProvider;
	private IChassiePowerProvider _power;
	private int xCoord;
	private int yCoord;
	private int zCoord;
	private IWorldProvider _world;
	
	public ModulePolymorphicItemSink() {}

	@Override
	public void registerHandler(IInventoryProvider invProvider, ISendRoutedItem itemSender, IWorldProvider world, IChassiePowerProvider powerprovider) {
		_invProvider = invProvider;
		_power = powerprovider;
		_world = world;
	}

	@Override
	public SinkReply sinksItem(ItemStack item) {
		IInventory targetInventory = _invProvider.getInventory();
		if (targetInventory == null) return null;
		
		IInventoryUtil invUtil = SimpleServiceLocator.inventoryUtilFactory.getInventoryUtil(targetInventory);
		if (!invUtil.containsItem(ItemIdentifier.get(item))) return null;
		
		SinkReply reply = new SinkReply();
		reply.fixedPriority = FixedPriority.ItemSink;
		reply.isDefault = false;
		reply.isPassive = true;
		if(_power.useEnergy(3)) {
			MainProxy.sendSpawnParticlePacket(Particles.BlueParticle, xCoord, yCoord, this.zCoord, _world.getWorld(), 2);
			return reply;
		}
		return null;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {}
	
	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {}
	
	@Override
	public ILogisticsModule getSubModule(int slot) {return null;}

	@Override
	public void tick() {}

	@Override
	public void registerPosition(int xCoord, int yCoord, int zCoord, int slot) {
		this.xCoord = xCoord;
		this.yCoord = yCoord;
		this.zCoord = zCoord;
	}
}
