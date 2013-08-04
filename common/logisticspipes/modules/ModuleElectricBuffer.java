package logisticspipes.modules;

import java.util.List;

import logisticspipes.api.IRoutedPowerProvider;
import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.interfaces.ISendRoutedItem;
import logisticspipes.interfaces.IWorldProvider;
import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.logisticspipes.IInventoryProvider;
import logisticspipes.pipefxhandlers.Particles;
import logisticspipes.pipes.basic.CoreRoutedPipe.ItemSendMode;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.Pair3;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.SinkReply.FixedPriority;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Icon;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ModuleElectricBuffer extends LogisticsModule {
	private IInventoryProvider _invProvider;
	private IRoutedPowerProvider _power;
	private ISendRoutedItem _itemSender;



	private IWorldProvider _world;

	private int currentTickCount = 0;
	private int ticksToAction = 80;

	public ModuleElectricBuffer() {}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {}

	@Override
	public void registerHandler(IInventoryProvider invProvider, ISendRoutedItem itemSender, IWorldProvider world, IRoutedPowerProvider powerProvider) {		
		_invProvider = invProvider;
		_power = powerProvider;
		_world = world;
		_itemSender = itemSender;
	}


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

	private final SinkReply _sinkReply = new SinkReply(FixedPriority.ElectricBuffer, 0, true, false, 1, 0);
	@Override
	public SinkReply sinksItem(ItemIdentifier stack, int bestPriority, int bestCustomPriority, boolean allowDefault, boolean includeInTransit) {
		if (bestPriority >= FixedPriority.ElectricBuffer.ordinal()) return null;
		if (SimpleServiceLocator.IC2Proxy.isElectricItem(stack.makeNormalStack(1))) {
			if (_power.canUseEnergy(1)) {
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
	public void tick() {
		if (++currentTickCount < ticksToAction) return;
		currentTickCount = 0;

		IInventoryUtil inv = _invProvider.getPointedInventory(true);
		if (inv == null) return;
		for (int i = 0; i < inv.getSizeInventory(); i++) {
			ItemStack stack = inv.getStackInSlot(i);
			if (stack == null) continue;
			if (SimpleServiceLocator.IC2Proxy.isElectricItem(stack)) {
				Pair3<Integer, SinkReply, List<IFilter>> reply = SimpleServiceLocator.logisticsManager.hasDestinationWithMinPriority(ItemIdentifier.get(stack), _itemSender.getSourceID(), true, FixedPriority.ElectricManager);
				if(reply == null) continue;
				MainProxy.sendSpawnParticlePacket(Particles.OrangeParticle, this.getX(), this.getY(), this.getZ(), _world.getWorld(), 2);
				_itemSender.sendStack(inv.decrStackSize(i, 1), reply, ItemSendMode.Normal);
				return;
			}
			continue;
		}
	}
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
		return true;
	}

	@Override
	public boolean recievePassive() {
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIconTexture(IconRegister register) {
		return register.registerIcon("logisticspipes:itemModule/ModuleElectricBuffer");
	}
}
