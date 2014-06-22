package logisticspipes.modules;

import java.util.List;

import logisticspipes.interfaces.IPipeServiceProvider;
import logisticspipes.interfaces.IWorldProvider;
import logisticspipes.logisticspipes.IInventoryProvider;
import logisticspipes.modules.abstractmodules.LogisticsModule;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.SinkReply.FixedPriority;
import logisticspipes.utils.item.ItemIdentifier;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Icon;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ModuleApiaristTerminus extends LogisticsModule {

	private IPipeServiceProvider _service;
	
	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {}

	@Override
	public void registerHandler(IInventoryProvider invProvider, IWorldProvider world, IPipeServiceProvider service) {
		_service = service;
	}
	
	@Override 
	public final int getX() {
		return this._service.getX();
	}
	@Override 
	public final int getY() {
		return this._service.getY();
	}
	
	@Override 
	public final int getZ() {
		return this._service.getZ();
	}	
	private boolean replyCheck(ItemStack item) {
		if (SimpleServiceLocator.forestryProxy.isDrone(item)) {
			return true;
		}
		return false;
	}

	private static final SinkReply _sinkReply = new SinkReply(FixedPriority.Terminus, 0, true, false, 5, 0);
	@Override
	public SinkReply sinksItem(ItemIdentifier item, int bestPriority, int bestCustomPriority, boolean allowDefault, boolean includeInTransit) {
		if(bestPriority > _sinkReply.fixedPriority.ordinal() || (bestPriority == _sinkReply.fixedPriority.ordinal() && bestCustomPriority >= _sinkReply.customPriority)) return null;
		boolean decision = replyCheck(item.makeNormalStack(1));
		if (decision) {
			if (_service.canUseEnergy(5)) {
				return _sinkReply;
			}
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

	@Override
	public boolean recievePassive() {
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIconTexture(IconRegister register) {
		return register.registerIcon("logisticspipes:itemModule/ModuleApiaristTerminus");
	}
}
