package logisticspipes.modules;

import java.util.List;

import logisticspipes.api.IRoutedPowerProvider;
import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.interfaces.ISendRoutedItem;
import logisticspipes.interfaces.IWorldProvider;
import logisticspipes.logisticspipes.IInventoryProvider;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.SinkReply.FixedPriority;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Icon;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ModulePolymorphicItemSink extends LogisticsModule {
	
	private IInventoryProvider _invProvider;
	private IRoutedPowerProvider _power;
	
	public ModulePolymorphicItemSink() {}

	@Override
	public void registerHandler(IInventoryProvider invProvider, ISendRoutedItem itemSender, IWorldProvider world, IRoutedPowerProvider powerprovider) {
		_invProvider = invProvider;
		_power = powerprovider;
	}

	private static final SinkReply _sinkReply = new SinkReply(FixedPriority.ItemSink, 0, true, false, 3, 0);
	@Override
	public SinkReply sinksItem(ItemIdentifier item, int bestPriority, int bestCustomPriority, boolean allowDefault, boolean includeInTransit) {
		if(bestPriority > _sinkReply.fixedPriority.ordinal() || (bestPriority == _sinkReply.fixedPriority.ordinal() && bestCustomPriority >= _sinkReply.customPriority)) return null;
		IInventoryUtil targetInventory = _invProvider.getSneakyInventory(false);
		if (targetInventory == null) return null;
		
		if (!targetInventory.containsUndamagedItem(item.getUndamaged())) return null;
		
		if(_power.canUseEnergy(3)) {
			return _sinkReply;
		}
		return null;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {}
	
	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {}
	
	@Override
	public LogisticsModule getSubModule(int slot) {return null;}

	@Override
	public void tick() {}

	@Override 
	public void registerSlot(int slot) {
	}
	
	@Override 
	public final int getX() {
		return this._power.getX();
	}
	@Override 
	public final int getY() {
		return this._power.getY();
	}
	
	@Override 
	public final int getZ() {
		return this._power.getZ();
	}
	@Override
	public boolean hasGenericInterests() {
		return false;
	}
	//TODO: SINK UNDAMAGED MATCH CORRECTLY!
	
	@Override
	public List<ItemIdentifier> getSpecificInterests() {
		return null;
	}

	@Override
	public boolean interestedInAttachedInventory() {		
		return true; // by definition :)
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
		return register.registerIcon("logisticspipes:itemModule/ModulePolymorphicItemSink");
	}
}
