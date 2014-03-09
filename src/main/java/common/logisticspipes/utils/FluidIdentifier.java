package logisticspipes.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.item.ItemIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.fluids.IFluidTank;

public class FluidIdentifier {
	private final static ReadWriteLock dblock = new ReentrantReadWriteLock();
	private final static Lock rlock = dblock.readLock();
	private final static Lock wlock = dblock.writeLock();

	//map uniqueID -> FluidIdentifier
	private final static HashMap<Integer, FluidIdentifier> _fluidIdentifierIdCache = new HashMap<Integer, FluidIdentifier>(256, 0.5f);

	//for fluids with tags, map fluidID -> map tag -> FluidIdentifier 
	private final static ArrayList<HashMap<FinalNBTTagCompound,FluidIdentifier>> _fluidIdentifierTagCache = new ArrayList<HashMap<FinalNBTTagCompound,FluidIdentifier>>(256);

	//for fluids without tags, map fluidID -> FluidIdentifier
	private final static ArrayList<FluidIdentifier> _fluidIdentifierCache = new ArrayList<FluidIdentifier>(256);
	
	public final int fluidID;
	public final String name;
	public final FinalNBTTagCompound tag;
	public final int uniqueID;
	
	private FluidIdentifier(int fluidID, String name, FinalNBTTagCompound tag, int uniqueID) {
		this.fluidID = fluidID;
		this.name = name;
		this.tag = tag;
		this.uniqueID = uniqueID;
	}

	public static FluidIdentifier get(int fluidID, NBTTagCompound tag)	{
		if(tag == null) {
			rlock.lock();
			if(fluidID < _fluidIdentifierCache.size()) {
				FluidIdentifier unknownFluid = _fluidIdentifierCache.get(fluidID);
				if(unknownFluid != null) {
					rlock.unlock();
					return unknownFluid;
				}
			}
			rlock.unlock();
			wlock.lock();
			if(fluidID < _fluidIdentifierCache.size()) {
				FluidIdentifier unknownFluid = _fluidIdentifierCache.get(fluidID);
				if(unknownFluid != null) {
					wlock.unlock();
					return unknownFluid;
				}
			}
			int id = getUnusedId();
			FluidIdentifier unknownFluid = new FluidIdentifier(fluidID, FluidRegistry.getFluidName(fluidID), null, id);
			while(_fluidIdentifierCache.size() <= fluidID)
				_fluidIdentifierCache.add(null);
			_fluidIdentifierCache.set(fluidID, unknownFluid);
			_fluidIdentifierIdCache.put(id, unknownFluid);
			wlock.unlock();
			return(unknownFluid);
		} else {
			rlock.lock();
			if(fluidID < _fluidIdentifierTagCache.size()) {
				HashMap<FinalNBTTagCompound, FluidIdentifier> fluidNBTList = _fluidIdentifierTagCache.get(fluidID);
				if(fluidNBTList!=null){
					FinalNBTTagCompound tagwithfixedname = new FinalNBTTagCompound(tag);
					FluidIdentifier unknownFluid = fluidNBTList.get(tagwithfixedname);
					if(unknownFluid!=null) {
						rlock.unlock();
						return unknownFluid;
					}
				}
			}
			rlock.unlock();
			wlock.lock();
			if(fluidID < _fluidIdentifierTagCache.size()) {
				HashMap<FinalNBTTagCompound, FluidIdentifier> fluidNBTList = _fluidIdentifierTagCache.get(fluidID);
				if(fluidNBTList!=null){
					FinalNBTTagCompound tagwithfixedname = new FinalNBTTagCompound(tag);
					FluidIdentifier unknownFluid = fluidNBTList.get(tagwithfixedname);
					if(unknownFluid!=null) {
						wlock.unlock();
						return unknownFluid;
					}
				}
			}
			while(_fluidIdentifierTagCache.size() <= fluidID)
				_fluidIdentifierTagCache.add(null);
			HashMap<FinalNBTTagCompound, FluidIdentifier> fluidNBTList = _fluidIdentifierTagCache.get(fluidID);
			if(fluidNBTList == null) {
				fluidNBTList = new HashMap<FinalNBTTagCompound, FluidIdentifier>(16, 0.5f);
				_fluidIdentifierTagCache.set(fluidID, fluidNBTList);
			}
			FinalNBTTagCompound finaltag = new FinalNBTTagCompound((NBTTagCompound)tag.copy());
			int id = getUnusedId();
			FluidIdentifier unknownFluid = new FluidIdentifier(fluidID, FluidRegistry.getFluidName(fluidID), finaltag, id);
			fluidNBTList.put(finaltag, unknownFluid);
			_fluidIdentifierIdCache.put(id, unknownFluid);
			wlock.unlock();
			return(unknownFluid);
		}
	}

	public static FluidIdentifier get(FluidStack stack) {
		return get(stack.fluidID, stack.tag);
	}
	
	public static FluidIdentifier get(ItemStack stack) {
		FluidStack f = SimpleServiceLocator.logisticsFluidManager.getFluidFromContainer(stack);
		if(f == null)
			return null;
		return get(f);
	}
	
	public static FluidIdentifier get(ItemIdentifier stack) {
		return get(stack.unsafeMakeNormalStack(1));
	}
	
	private static FluidIdentifier get(Fluid fluid) {
		return get(fluid.getID(), null);
	}
	

	private static int getUnusedId() {
		int id = new Random().nextInt();
		while(isIdUsed(id)) {
			id = new Random().nextInt();
		}
		return id;
	}
	
	private static boolean isIdUsed(int id) {
		return _fluidIdentifierIdCache.containsKey(id);
	}

	public String getName() {
		return name;
	}
	
	public FluidStack makeFluidStack(int amount) {
		//FluidStack constructor does the tag.copy(), so this is safe
		return new FluidStack(fluidID, amount, tag);
	}
	
	public int getFreeSpaceInsideTank(IFluidHandler container, ForgeDirection dir) {
		int free = 0;
		FluidTankInfo[] tanks = container.getTankInfo(dir);
		if(tanks != null && tanks.length > 0) {
			for(int i=0;i<tanks.length;i++) {
				free += getFreeSpaceInsideTank(tanks[i]);
			}
		}
		return free;
	}
	
	private int getFreeSpaceInsideTank(FluidTankInfo tanks) {
		FluidStack liquid = tanks.fluid;
		if(liquid == null || liquid.fluidID <= 0) {
			return tanks.capacity;
		}
		if(get(liquid) == this) {
			return tanks.capacity - liquid.amount;
		}
		return 0;
	}

	public int getFreeSpaceInsideTank(IFluidTank tank) {
		FluidStack liquid = tank.getFluid();
		if(liquid == null || liquid.fluidID <= 0) {
			return tank.getCapacity();
		}
		if(get(liquid) == this) {
			return tank.getCapacity() - liquid.amount;
		}
		return 0;
	}
	
	private static boolean init = false;
	public static void initFromForge(boolean flag) {
		if(init) return;
		Map<String, Fluid> fluids = FluidRegistry.getRegisteredFluids();
		for(Fluid fluid: fluids.values()) {
			get(fluid);
		}
		if(flag) {
			init = true;
		}
	}
	
	@Override
	public String toString() {
		String t = tag != null ? tag.toString() : "null";
		return name + "/" + fluidID + ":" + t;
	}
	
	public FluidIdentifier next() {
		rlock.lock();
		boolean takeNext = false;
		for(FluidIdentifier i : _fluidIdentifierCache) {
			if(takeNext && i != null) {
				rlock.unlock();
				return i;
			}
			if(i == this)
				takeNext = true;
		}
		rlock.unlock();
		return null;
	}
	
	public FluidIdentifier prev() {
		rlock.lock();
		FluidIdentifier last = null;
		for(FluidIdentifier i : _fluidIdentifierCache) {
			if(i == this) {
				rlock.unlock();
				return last;
			}
			if(i != null) {
				last = i;
			}
		}
		rlock.unlock();
		return last;
	}
	
	public static FluidIdentifier first() {
		rlock.lock();
		for(FluidIdentifier i : _fluidIdentifierCache) {
			if(i != null) {
				rlock.unlock();
				return i;
			}
		}
		rlock.unlock();
		return null;
	}
	
	public static FluidIdentifier last() {
		rlock.lock();
		FluidIdentifier last = null;
		for(FluidIdentifier i : _fluidIdentifierCache) {
			if(i != null)
				last = i;
		}
		rlock.unlock();
		return last;
	}

	public ItemIdentifier getItemIdentifier() {
		return ItemIdentifier.get(SimpleServiceLocator.logisticsFluidManager.getFluidContainer(makeFluidStack(0)));
	}

	public static FluidIdentifier convertFromID(int id) {
		Fluid f = null;
		for(Fluid fluid:FluidRegistry.getRegisteredFluids().values()) {
			if(fluid.getBlockID() == id) {
				f = fluid;
				break;
			}
		}
		if(f == null) return null;
		return get(f);
	}
}
