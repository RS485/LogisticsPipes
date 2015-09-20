package logisticspipes.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import logisticspipes.asm.addinfo.IAddInfo;
import logisticspipes.asm.addinfo.IAddInfoProvider;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.computers.interfaces.ILPCCTypeHolder;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;

import lombok.AllArgsConstructor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.fluids.IFluidTank;

public class FluidIdentifier implements ILPCCTypeHolder {

	private final static ReadWriteLock dblock = new ReentrantReadWriteLock();
	private final static Lock rlock = FluidIdentifier.dblock.readLock();
	private final static Lock wlock = FluidIdentifier.dblock.writeLock();

	//map uniqueID -> FluidIdentifier
	private final static HashMap<Integer, FluidIdentifier> _fluidIdentifierIdCache = new HashMap<Integer, FluidIdentifier>(256, 0.5f);

	//for fluids with tags, map fluidID -> map tag -> FluidIdentifier
	private final static ArrayList<HashMap<FinalNBTTagCompound, FluidIdentifier>> _fluidIdentifierTagCache = new ArrayList<HashMap<FinalNBTTagCompound, FluidIdentifier>>(256);

	//for fluids without tags, map fluidID -> FluidIdentifier
	private final static ArrayList<FluidIdentifier> _fluidIdentifierCache = new ArrayList<FluidIdentifier>(256);

	public final int fluidID;
	public final String name;
	public final FinalNBTTagCompound tag;
	public final int uniqueID;

	@AllArgsConstructor
	private static class FluidStackAddInfo implements IAddInfo {
		private final FluidIdentifier fluid;
	}

	@AllArgsConstructor
	private static class FluidAddInfo implements IAddInfo {
		private final FluidIdentifier fluid;
	}

	private FluidIdentifier(int fluidID, String name, FinalNBTTagCompound tag, int uniqueID) {
		this.fluidID = fluidID;
		this.name = name;
		this.tag = tag;
		this.uniqueID = uniqueID;
	}

	public static FluidIdentifier get(Fluid fluid, NBTTagCompound tag, FluidIdentifier proposal) {
		int fluidID = fluid.getID();
		if (tag == null) {
			if(proposal != null) {
				if(proposal.fluidID == fluidID && proposal.tag == null) {
					return proposal;
				}
			}
			proposal = null;
			IAddInfoProvider prov = null;
			if(fluid instanceof IAddInfoProvider) {
				prov = (IAddInfoProvider) fluid;
				FluidAddInfo info = prov.getLogisticsPipesAddInfo(FluidAddInfo.class);
				if(info != null) {
					proposal = info.fluid;
				}
			}
			FluidIdentifier ident = getFluidIdentifierWithoutTag(fluid, fluidID, proposal);
			if(proposal != ident && prov != null) {
				prov.setLogisticsPipesAddInfo(new FluidAddInfo(ident));
			}
			return ident;
		} else {
			FluidIdentifier.rlock.lock();
			if (fluidID < FluidIdentifier._fluidIdentifierTagCache.size()) {
				HashMap<FinalNBTTagCompound, FluidIdentifier> fluidNBTList = FluidIdentifier._fluidIdentifierTagCache.get(fluidID);
				if (fluidNBTList != null) {
					FinalNBTTagCompound tagwithfixedname = new FinalNBTTagCompound(tag);
					FluidIdentifier unknownFluid = fluidNBTList.get(tagwithfixedname);
					if (unknownFluid != null) {
						FluidIdentifier.rlock.unlock();
						return unknownFluid;
					}
				}
			}
			FluidIdentifier.rlock.unlock();
			FluidIdentifier.wlock.lock();
			if (fluidID < FluidIdentifier._fluidIdentifierTagCache.size()) {
				HashMap<FinalNBTTagCompound, FluidIdentifier> fluidNBTList = FluidIdentifier._fluidIdentifierTagCache.get(fluidID);
				if (fluidNBTList != null) {
					FinalNBTTagCompound tagwithfixedname = new FinalNBTTagCompound(tag);
					FluidIdentifier unknownFluid = fluidNBTList.get(tagwithfixedname);
					if (unknownFluid != null) {
						FluidIdentifier.wlock.unlock();
						return unknownFluid;
					}
				}
			}
			while (FluidIdentifier._fluidIdentifierTagCache.size() <= fluidID) {
				FluidIdentifier._fluidIdentifierTagCache.add(null);
			}
			HashMap<FinalNBTTagCompound, FluidIdentifier> fluidNBTList = FluidIdentifier._fluidIdentifierTagCache.get(fluidID);
			if (fluidNBTList == null) {
				fluidNBTList = new HashMap<FinalNBTTagCompound, FluidIdentifier>(16, 0.5f);
				FluidIdentifier._fluidIdentifierTagCache.set(fluidID, fluidNBTList);
			}
			FinalNBTTagCompound finaltag = new FinalNBTTagCompound((NBTTagCompound) tag.copy());
			int id = FluidIdentifier.getUnusedId();
			FluidIdentifier unknownFluid = new FluidIdentifier(fluidID, FluidRegistry.getFluidName(fluid), finaltag, id);
			fluidNBTList.put(finaltag, unknownFluid);
			FluidIdentifier._fluidIdentifierIdCache.put(id, unknownFluid);
			FluidIdentifier.wlock.unlock();
			return (unknownFluid);
		}
	}

	private static FluidIdentifier getFluidIdentifierWithoutTag(Fluid fluid, int fluidID, FluidIdentifier proposal) {
		if(proposal != null) {
			if(proposal.fluidID == fluidID && proposal.tag == null) {
				return proposal;
			}
		}
		FluidIdentifier.rlock.lock();
		if (fluidID < FluidIdentifier._fluidIdentifierCache.size()) {
			FluidIdentifier unknownFluid = FluidIdentifier._fluidIdentifierCache.get(fluidID);
			if (unknownFluid != null) {
				FluidIdentifier.rlock.unlock();
				return unknownFluid;
			}
		}
		FluidIdentifier.rlock.unlock();
		FluidIdentifier.wlock.lock();
		if (fluidID < FluidIdentifier._fluidIdentifierCache.size()) {
			FluidIdentifier unknownFluid = FluidIdentifier._fluidIdentifierCache.get(fluidID);
			if (unknownFluid != null) {
				FluidIdentifier.wlock.unlock();
				return unknownFluid;
			}
		}
		int id = FluidIdentifier.getUnusedId();
		FluidIdentifier unknownFluid = new FluidIdentifier(fluidID, FluidRegistry.getFluidName(fluid), null, id);
		while (FluidIdentifier._fluidIdentifierCache.size() <= fluidID) {
			FluidIdentifier._fluidIdentifierCache.add(null);
		}
		FluidIdentifier._fluidIdentifierCache.set(fluidID, unknownFluid);
		FluidIdentifier._fluidIdentifierIdCache.put(id, unknownFluid);
		FluidIdentifier.wlock.unlock();
		return (unknownFluid);
	}

	public static FluidIdentifier get(FluidStack stack) {
		FluidIdentifier proposal = null;
		IAddInfoProvider prov = null;
		if(stack instanceof IAddInfoProvider) {
			prov = (IAddInfoProvider) stack;
			FluidStackAddInfo info = prov.getLogisticsPipesAddInfo(FluidStackAddInfo.class);
			if(info != null) {
				proposal = info.fluid;
			}
		}
		FluidIdentifier ident = FluidIdentifier.get(stack.getFluid(), stack.tag, proposal);
		if(proposal != ident && stack.tag == null && prov != null) {
			prov.setLogisticsPipesAddInfo(new FluidStackAddInfo(ident));
		}
		return ident;
	}

	public static FluidIdentifier get(ItemIdentifier stack) {
		return FluidIdentifier.get(stack.makeStack(1));
	}

	public static FluidIdentifier get(ItemStack stack) {
		return FluidIdentifier.get(ItemIdentifierStack.getFromStack(stack));
	}

	public static FluidIdentifier get(ItemIdentifierStack stack) {
		FluidStack f = SimpleServiceLocator.logisticsFluidManager.getFluidFromContainer(stack);
		if (f == null) {
			return null;
		}
		return FluidIdentifier.get(f);
	}

	private static FluidIdentifier get(Fluid fluid) {
		return FluidIdentifier.get(fluid, null, null);
	}

	private static int getUnusedId() {
		int id = new Random().nextInt();
		while (FluidIdentifier.isIdUsed(id)) {
			id = new Random().nextInt();
		}
		return id;
	}

	private static boolean isIdUsed(int id) {
		return FluidIdentifier._fluidIdentifierIdCache.containsKey(id);
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
		if (tanks != null && tanks.length > 0) {
			for (FluidTankInfo tank : tanks) {
				free += getFreeSpaceInsideTank(tank);
			}
		}
		return free;
	}

	private int getFreeSpaceInsideTank(FluidTankInfo tanks) {
		if (tanks == null) {
			return 0;
		}
		FluidStack liquid = tanks.fluid;
		if (liquid == null || liquid.getFluidID() <= 0) {
			return tanks.capacity;
		}
		if (FluidIdentifier.get(liquid).equals(this)) {
			return tanks.capacity - liquid.amount;
		}
		return 0;
	}

	public int getFreeSpaceInsideTank(IFluidTank tank) {
		FluidStack liquid = tank.getFluid();
		if (liquid == null || liquid.getFluidID() <= 0) {
			return tank.getCapacity();
		}
		if (FluidIdentifier.get(liquid).equals(this)) {
			return tank.getCapacity() - liquid.amount;
		}
		return 0;
	}

	private static boolean init = false;

	public static void initFromForge(boolean flag) {
		if (FluidIdentifier.init) {
			return;
		}
		Map<String, Fluid> fluids = FluidRegistry.getRegisteredFluids();
		for (Fluid fluid : fluids.values()) {
			FluidIdentifier.get(fluid);
		}
		if (flag) {
			FluidIdentifier.init = true;
		}
	}

	@Override
	public String toString() {
		String t = tag != null ? tag.toString() : "null";
		return name + "/" + fluidID + ":" + t;
	}

	public FluidIdentifier next() {
		FluidIdentifier.rlock.lock();
		boolean takeNext = false;
		for (FluidIdentifier i : FluidIdentifier._fluidIdentifierCache) {
			if (takeNext && i != null) {
				FluidIdentifier.rlock.unlock();
				return i;
			}
			if (equals(i)) {
				takeNext = true;
			}
		}
		FluidIdentifier.rlock.unlock();
		return null;
	}

	public FluidIdentifier prev() {
		FluidIdentifier.rlock.lock();
		FluidIdentifier last = null;
		for (FluidIdentifier i : FluidIdentifier._fluidIdentifierCache) {
			if (equals(i)) {
				FluidIdentifier.rlock.unlock();
				return last;
			}
			if (i != null) {
				last = i;
			}
		}
		FluidIdentifier.rlock.unlock();
		return last;
	}

	public static FluidIdentifier first() {
		FluidIdentifier.rlock.lock();
		for (FluidIdentifier i : FluidIdentifier._fluidIdentifierCache) {
			if (i != null) {
				FluidIdentifier.rlock.unlock();
				return i;
			}
		}
		FluidIdentifier.rlock.unlock();
		return null;
	}

	public static FluidIdentifier last() {
		FluidIdentifier.rlock.lock();
		FluidIdentifier last = null;
		for (FluidIdentifier i : FluidIdentifier._fluidIdentifierCache) {
			if (i != null) {
				last = i;
			}
		}
		FluidIdentifier.rlock.unlock();
		return last;
	}

	public static List<FluidIdentifier> all() {
		FluidIdentifier.rlock.lock();
		List<FluidIdentifier> list = Collections.unmodifiableList(FluidIdentifier._fluidIdentifierCache);
		FluidIdentifier.rlock.unlock();
		return list;
	}

	public ItemIdentifier getItemIdentifier() {
		return SimpleServiceLocator.logisticsFluidManager.getFluidContainer(makeFluidStack(0)).getItem();
	}

	private Object ccObject;

	@Override
	public void setCCType(Object type) {
		ccObject = type;
	}

	@Override
	public Object getCCType() {
		return ccObject;
	}
}
