package logisticspipes.modules;

import java.util.ArrayList;
import java.util.List;

import logisticspipes.api.IRoutedPowerProvider;
import logisticspipes.interfaces.ISendRoutedItem;
import logisticspipes.interfaces.IWorldProvider;
import logisticspipes.logisticspipes.IInventoryProvider;
import logisticspipes.pipefxhandlers.Particles;
import logisticspipes.pipes.basic.CoreRoutedPipe.ItemSendMode;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.tuples.Pair;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Icon;
import net.minecraftforge.common.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ModuleApiaristRefiller extends LogisticsModule {
	
	private IInventoryProvider		_invProvider;
	private IRoutedPowerProvider	_power;
	
	private IWorldProvider			_world;
	
	private int						currentTickCount	= 0;
	private int						ticksToOperation	= 200;
	
	public ModuleApiaristRefiller() {}
	
	@Override
	public void registerHandler(IInventoryProvider invProvider, IWorldProvider world, IRoutedPowerProvider powerProvider) {
		_invProvider = invProvider;
		_power = powerProvider;
		_world = world;

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
	public void registerSlot(int slot) {}
	
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
		if(++currentTickCount < ticksToOperation) return;
		currentTickCount = 0;
		IInventory inv = _invProvider.getRealInventory();
		if(!(inv instanceof ISidedInventory)) return;
		ISidedInventory sinv = (ISidedInventory)inv;
		ForgeDirection direction = _invProvider.inventoryOrientation().getOpposite();
		ItemStack stack = extractItem(sinv, false, direction, 1);
		if(stack == null) return;
		if(!(_power.canUseEnergy(100))) return;
		
		currentTickCount = ticksToOperation;
		
		if(reinsertBee(stack, sinv, direction)) return;
		
		Pair<Integer, SinkReply> reply = _invProvider.hasDestination(ItemIdentifier.get(stack), true, new ArrayList<Integer>());
		if(reply == null) return;
		_power.useEnergy(20);
		extractItem(sinv, true, direction, 1);
		_invProvider.sendStack(stack, reply, ItemSendMode.Normal);
	}
	
	private ItemStack extractItem(ISidedInventory inv, boolean remove, ForgeDirection dir, int amount) {
		for(int i=0;i<inv.getSizeInventory();i++) {
			if(inv.getStackInSlot(i) != null && inv.canExtractItem(i, inv.getStackInSlot(i), dir.ordinal())) {
				if(remove) {
					return inv.decrStackSize(i, amount);
				} else {
					ItemStack extracted = inv.getStackInSlot(i).copy();
					extracted.stackSize = amount;
					return extracted;
				}
			}
		}
		return null;
	}
	
	private int addItem(ISidedInventory inv, ItemStack stack, ForgeDirection dir) {
		for(int i=0;i<inv.getSizeInventory();i++) {
			if(inv.getStackInSlot(i) == null && inv.canInsertItem(i, stack, dir.ordinal())) {
				inv.setInventorySlotContents(i, stack);
				return stack.stackSize;
			}
		}
		return 0;
	}
	
	private boolean reinsertBee(ItemStack stack, ISidedInventory inv, ForgeDirection direction) {
		if((inv.getStackInSlot(0) == null)) {
			if(SimpleServiceLocator.forestryProxy.isPrincess(stack)) {
				if(SimpleServiceLocator.forestryProxy.isPurebred(stack)) {
					int inserted = addItem(inv, stack, direction);
					if(inserted == 0) { return false; }
					_power.useEnergy(100);
					extractItem(inv, true, direction, 1);
					MainProxy.sendSpawnParticlePacket(Particles.VioletParticle, this.getX(), this.getY(), this.getZ(), _world.getWorld(), 5);
					MainProxy.sendSpawnParticlePacket(Particles.BlueParticle, this.getX(), this.getY(), this.getZ(), _world.getWorld(), 5);
					return true;
				}
			}
		}
		if((inv.getStackInSlot(1) == null) && !(SimpleServiceLocator.forestryProxy.isQueen(inv.getStackInSlot(0)))) {
			if(SimpleServiceLocator.forestryProxy.isDrone(stack)) {
				if(SimpleServiceLocator.forestryProxy.isPurebred(stack)) {
					int inserted = addItem(inv, stack, direction);
					if(inserted == 0) { return false; }
					_power.useEnergy(100);
					extractItem(inv, true, direction, 1);
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
