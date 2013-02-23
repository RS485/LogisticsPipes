package logisticspipes.modules;

import java.util.ArrayList;
import java.util.List;

import logisticspipes.interfaces.IChassiePowerProvider;
import logisticspipes.interfaces.ILogisticsModule;
import logisticspipes.interfaces.ISendRoutedItem;
import logisticspipes.interfaces.IWorldProvider;
import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.logisticspipes.IInventoryProvider;
import logisticspipes.pipes.basic.CoreRoutedPipe.ItemSendMode;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.Pair3;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.SinkReply.FixedPriority;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class ModuleApiaristAnalyser implements ILogisticsModule {

	private IInventoryProvider _invProvider;
	private ISendRoutedItem _itemSender;
	private int ticksToAction = 100;
	private int currentTick = 0;

	private IChassiePowerProvider _power;

	public ModuleApiaristAnalyser() {

	}

	@Override
	public void registerHandler(IInventoryProvider invProvider, ISendRoutedItem itemSender, IWorldProvider world, IChassiePowerProvider powerprovider) {
		_invProvider = invProvider;
		_itemSender = itemSender;
		_power = powerprovider;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {

	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {

	}

	private static final SinkReply _sinkReply = new SinkReply(FixedPriority.APIARIST_Analyser, 0, true, false, 3, 0);
	@Override
	public SinkReply sinksItem(ItemIdentifier itemID, int bestPriority, int bestCustomPriority) {
		if(bestPriority > _sinkReply.fixedPriority.ordinal() || (bestPriority == _sinkReply.fixedPriority.ordinal() && bestCustomPriority >= _sinkReply.customPriority)) return null;
		ItemStack item = itemID.makeNormalStack(1);
		if(SimpleServiceLocator.forestryProxy.isBee(item)) {
			if(!SimpleServiceLocator.forestryProxy.isAnalysedBee(item)) {
				if(_power.canUseEnergy(3)) {
					return _sinkReply;
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
		if (++currentTick  < ticksToAction) return;
		currentTick = 0;

		IInventory inv = _invProvider.getRawInventory();
		if(inv == null) return;
		for(int i=0; i < inv.getSizeInventory(); i++) {
			ItemStack item = inv.getStackInSlot(i);
			if(SimpleServiceLocator.forestryProxy.isBee(item)) {
				if(SimpleServiceLocator.forestryProxy.isAnalysedBee(item)) {
					Pair3<Integer, SinkReply, List<IFilter>> reply = _itemSender.hasDestination(ItemIdentifier.get(item), true, new ArrayList<Integer>());
					if(reply == null) continue;
					if(_power.useEnergy(6)) {
						_itemSender.sendStack(inv.decrStackSize(i,1), reply, ItemSendMode.Normal);
					}
				}
			}
		}
	}

	@Override
	public void registerPosition(int xCoord, int yCoord, int zCoord, int slot) {}

	@Override
	public boolean hasGenericInterests() {
		return true;
	}

	@Override
	public List<ItemIdentifier> getSpecificInterests() {
		return null;
	}

	@Override
	public boolean interestedInAttachedInventory() {		
		return false;
	}
	
	@Override
	public boolean interestedInUndamagedID() {
		return false;
	}
}
