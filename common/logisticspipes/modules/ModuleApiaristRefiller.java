package logisticspipes.modules;

import java.util.ArrayList;
import java.util.List;

import logisticspipes.api.IRoutedPowerProvider;
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
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Icon;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.inventory.ISpecialInventory;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ModuleApiaristRefiller extends LogisticsModule {

	private IInventoryProvider _invProvider;
	private IRoutedPowerProvider _power;
	private ISendRoutedItem _itemSender;



	private IWorldProvider _world;

	private int currentTickCount = 0;
	private int ticksToOperation = 200;
	public ModuleApiaristRefiller() {}

	@Override
	public void registerHandler(IInventoryProvider invProvider, ISendRoutedItem itemSender, IWorldProvider world, IRoutedPowerProvider powerProvider) {
		_invProvider = invProvider;
		_power = powerProvider;
		_world = world;
		_itemSender = itemSender;
	}

	@Override
	public SinkReply sinksItem(ItemIdentifier item, int bestPriority, int bestCustomPriority, boolean allowDefault, boolean includeInTransit) {
		return null;
	}

	@Override
	public LogisticsModule getSubModule(int slot) {
		return null;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {}


	@Override 
	public void registerSlot(int slot) {
	}
	
	@Override 
	public final int getX() {
		return this._invProvider.getX();
	}
	@Override 
	public final int getY() {
		return this._invProvider.getY();
	}
	
	@Override 
	public final int getZ() {
		return this._invProvider.getZ();
	}


	@Override
	public void tick() {
		if (++currentTickCount < ticksToOperation) return;
		currentTickCount = 0;
		IInventory inv = _invProvider.getRealInventory();
		if (!(inv instanceof ISpecialInventory)) return;
		ISpecialInventory sinv = (ISpecialInventory) inv;
		ForgeDirection direction = _invProvider.inventoryOrientation().getOpposite();
		ItemStack[] stack = sinv.extractItem(false, direction, 1);
		if (stack == null || stack.length < 1 || stack[0] == null) return;
		if (!(_power.canUseEnergy(100))) return;
		
		currentTickCount = ticksToOperation;

		if(reinsertBee(stack[0], sinv, direction))
			return;

		Pair3<Integer, SinkReply, List<IFilter>> reply = _itemSender.hasDestination(ItemIdentifier.get(stack[0]), true, new ArrayList<Integer>());
		if(reply == null) return;
		_power.useEnergy(20);
		sinv.extractItem(true, direction, 1);
		_itemSender.sendStack(stack[0], reply, ItemSendMode.Normal);
	}

	private boolean reinsertBee(ItemStack stack, ISpecialInventory inv, ForgeDirection direction) {
		if ((inv.getStackInSlot(0) == null)) {
			if (SimpleServiceLocator.forestryProxy.isPrincess(stack)) {
				if (SimpleServiceLocator.forestryProxy.isPurebred(stack)) {
					int inserted = inv.addItem(stack, true, direction);
					if (inserted == 0) {
						return false;
					}
					_power.useEnergy(100);
					inv.extractItem(true, direction, 1);
					MainProxy.sendSpawnParticlePacket(Particles.VioletParticle, this.getX(), this.getY(), this.getZ(), _world.getWorld(), 5);
					MainProxy.sendSpawnParticlePacket(Particles.BlueParticle, this.getX(), this.getY(), this.getZ(), _world.getWorld(), 5);
					return true;
				}
			}
		}
		if ((inv.getStackInSlot(1) == null) && !(SimpleServiceLocator.forestryProxy.isQueen(inv.getStackInSlot(0)))) {
			if (SimpleServiceLocator.forestryProxy.isDrone(stack)) {
				if (SimpleServiceLocator.forestryProxy.isPurebred(stack)) {
					int inserted = inv.addItem(stack, true, direction);
					if (inserted == 0) {
						return false;
					}
					_power.useEnergy(100);
					inv.extractItem(true, direction, 1);
					MainProxy.sendSpawnParticlePacket(Particles.VioletParticle, this.getX(), this.getY(), this.getZ(), _world.getWorld(), 5);
					MainProxy.sendSpawnParticlePacket(Particles.BlueParticle, this.getX(), this.getY(), this.getZ(), _world.getWorld(), 5);
					return true;
				}
			}
		}
		return false;
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
		return false;
	}

	@Override
	public boolean recievePassive() {
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIconTexture(IconRegister register) {
		return register.registerIcon("logisticspipes:itemModule/ModuleApiaristRefiller");
	}
}
