package logisticspipes.modules;

import java.util.Collection;

import logisticspipes.api.IRoutedPowerProvider;
import logisticspipes.interfaces.IWorldProvider;
import logisticspipes.logisticspipes.IInventoryProvider;
import logisticspipes.modules.abstractmodules.LogisticsModule;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.SinkReply.FixedPriority;
import logisticspipes.utils.item.ItemIdentifier;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Icon;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ModuleEnchantmentSink extends LogisticsModule {
	
	private IRoutedPowerProvider _power;
	
	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {}

	@Override
	public void registerHandler(IInventoryProvider invProvider,
			IWorldProvider world,
			IRoutedPowerProvider powerProvider) {
		_power = powerProvider;
	}

	@Override
	public int getX() {
		if(slot.isInWorld())
			return this._power.getX();
		else 
			return 0;
	}

	@Override
	public int getY() {
		if(slot.isInWorld())
			return this._power.getY();
		else 
			return 0;
	}

	@Override
	public int getZ() {
		if(slot.isInWorld())
			return this._power.getZ();
		else 
			return 0;
	}


	private static final SinkReply _sinkReply = new SinkReply(FixedPriority.EnchantmentItemSink, 0, true, false, 1, 0);

	@Override
	public SinkReply sinksItem(ItemIdentifier item, int bestPriority, int bestCustomPriority, boolean allowDefault, boolean includeInTransit) {
		// check to see if a better route is already found
		// Note: Higher MKs are higher priority  
		if(bestPriority > _sinkReply.fixedPriority.ordinal() || (bestPriority == _sinkReply.fixedPriority.ordinal() && bestCustomPriority >= _sinkReply.customPriority)) return null;
		
		//check to see if item is enchanted
		if (item.makeNormalStack(1).isItemEnchanted())
		{
			return _sinkReply;
		}
		return null;
	}
	
	@Override
	public LogisticsModule getSubModule(int slot) {
		return null;
	}

	@Override
	public void tick() {}

	@Override
	/*
	 * We will check every item return true
	 * @see logisticspipes.modules.LogisticsModule#hasGenericInterests()
	 */
	public boolean hasGenericInterests() {
		return true;
	}

	@Override
	/*
	 * Null return as checking all items
	 * @see logisticspipes.modules.LogisticsModule#getSpecificInterests()
	 */
	public Collection<ItemIdentifier> getSpecificInterests() {
		return null;
	}

	@Override
	public boolean interestedInAttachedInventory() {
		return false;
	}

	@Override
	public boolean interestedInUndamagedID() {
		return true;
	}

	@Override
	public boolean recievePassive() {
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIconTexture(IconRegister register) {
		return register.registerIcon("logisticspipes:itemModule/ModuleEnchantmentSink");
	}
	@Override
	public boolean hasEffect() {
		return true;
	}
}
