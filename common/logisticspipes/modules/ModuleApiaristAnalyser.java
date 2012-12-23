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

public class ModuleApiaristAnalyser implements ILogisticsModule {
	
	private IInventoryProvider _invProvider;
	private ISendRoutedItem _itemSender;
	private int ticksToAction = 100;
	private int currentTick = 0;
	
	private int xCoord;
	private int yCoord;
	private int zCoord;
	private IWorldProvider _world;
	
	private IChassiePowerProvider _power;
	
	public ModuleApiaristAnalyser() {
		
	}

	@Override
	public void registerHandler(IInventoryProvider invProvider, ISendRoutedItem itemSender, IWorldProvider world, IChassiePowerProvider powerprovider) {
		_invProvider = invProvider;
		_itemSender = itemSender;
		_power = powerprovider;
		_world = world;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		
	}

	@Override
	public SinkReply sinksItem(ItemStack item) {
		if(SimpleServiceLocator.forestryProxy.isBee(item)) {
			if(!SimpleServiceLocator.forestryProxy.isAnalysedBee(item)) {
				SinkReply reply = new SinkReply();
				reply.fixedPriority = SinkReply.FixedPriority.APIARIST_Analyser;
				reply.isPassive = true;
				if(_power.useEnergy(3)) {
					MainProxy.sendSpawnParticlePacket(Particles.BlueParticle, this.xCoord, this.yCoord, this.zCoord, _world.getWorld(), 2);
					return reply;
				}
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
		if(MainProxy.isClient()) return;
		if (++currentTick  < ticksToAction) return;
		currentTick = 0;
		
		IInventory inv = _invProvider.getRawInventory();
		if(inv == null) return;
		for(int i=0; i < inv.getSizeInventory(); i++) {
			ItemStack item = inv.getStackInSlot(i);
			if(SimpleServiceLocator.forestryProxy.isBee(item)) {
				if(SimpleServiceLocator.forestryProxy.isAnalysedBee(item)) {
					if(_power.useEnergy(6)) {
						_itemSender.sendStack(inv.decrStackSize(i,1));
					}
				}
			}
		}
	}

	@Override
	public void registerPosition(int xCoord, int yCoord, int zCoord, int slot) {
		this.xCoord = xCoord;
		this.yCoord = yCoord;
		this.zCoord = zCoord;
	}
}
