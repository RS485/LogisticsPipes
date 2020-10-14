package logisticspipes.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import lombok.AllArgsConstructor;

import logisticspipes.asm.addinfo.IAddInfo;
import logisticspipes.asm.addinfo.IAddInfoProvider;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.computers.interfaces.ILPCCTypeHolder;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;

public class FluidIdentifier implements Comparable<FluidIdentifier>, ILPCCTypeHolder {

	private final Object[] ccTypeHolder = new Object[1];
	private final static ReadWriteLock dblock = new ReentrantReadWriteLock();
	private final static Lock rlock = FluidIdentifier.dblock.readLock();
	private final static Lock wlock = FluidIdentifier.dblock.writeLock();

	//map uniqueID -> FluidIdentifier
	private final static HashMap<Integer, FluidIdentifier> _fluidIdentifierIdCache = new HashMap<>(256, 0.5f);

	//for fluids with tags, map fluidID -> map tag -> FluidIdentifier
	private final static Map<String, HashMap<FinalNBTTagCompound, FluidIdentifier>> _fluidIdentifierTagCache = new HashMap<>(256);

	//for fluids without tags, map fluidID -> FluidIdentifier
	private final static Map<String, FluidIdentifier> _fluidIdentifierCache = new HashMap<>(256);

	public final String fluidID;
	public final String name;
	public final FinalNBTTagCompound tag;
	public final int uniqueID;

	@Override
	public int compareTo(FluidIdentifier o) {
		int c = fluidID.compareTo(o.fluidID);
		if (c != 0) {
			return c;
		}
		c = uniqueID - o.uniqueID;
		return c;
	}

	@AllArgsConstructor
	private static class FluidStackAddInfo implements IAddInfo {

		private final FluidIdentifier fluid;
	}

	@AllArgsConstructor
	private static class FluidAddInfo implements IAddInfo {

		private final FluidIdentifier fluid;
	}

	private FluidIdentifier(String fluidID, String name, FinalNBTTagCompound tag, int uniqueID) {
		this.fluidID = fluidID;
		this.name = name;
		this.tag = tag;
		this.uniqueID = uniqueID;
	}

	public static FluidIdentifier get(Fluid fluid, NBTTagCompound tag, FluidIdentifier proposal) {
		String fluidID = fluid.getName();
		if (tag == null) {
			if (proposal != null) {
				if (proposal.fluidID.equals(fluidID) && proposal.tag == null) {
					return proposal;
				}
			}
			proposal = null;
			IAddInfoProvider prov = null;
			if (fluid instanceof IAddInfoProvider) {
				prov = (IAddInfoProvider) fluid;
				FluidAddInfo info = prov.getLogisticsPipesAddInfo(FluidAddInfo.class);
				if (info != null) {
					proposal = info.fluid;
				}
			}
			FluidIdentifier ident = getFluidIdentifierWithoutTag(fluid, fluidID, proposal);
			if (proposal != ident && prov != null) {
				prov.setLogisticsPipesAddInfo(new FluidAddInfo(ident));
			}
			return ident;
		} else {
			FluidIdentifier.rlock.lock();
			{
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
			{
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
			HashMap<FinalNBTTagCompound, FluidIdentifier> fluidNBTList = FluidIdentifier._fluidIdentifierTagCache
					.computeIfAbsent(fluidID, k -> new HashMap<>(16, 0.5f));
			FinalNBTTagCompound finaltag = new FinalNBTTagCompound(tag);
			int id = FluidIdentifier.getUnusedId();
			FluidIdentifier unknownFluid = new FluidIdentifier(fluidID, FluidRegistry.getFluidName(fluid), finaltag, id);
			fluidNBTList.put(finaltag, unknownFluid);
			FluidIdentifier._fluidIdentifierIdCache.put(id, unknownFluid);
			FluidIdentifier.wlock.unlock();
			return (unknownFluid);
		}
	}

	private static FluidIdentifier getFluidIdentifierWithoutTag(Fluid fluid, String fluidID, FluidIdentifier proposal) {
		if (proposal != null) {
			if (proposal.fluidID.equals(fluidID) && proposal.tag == null) {
				return proposal;
			}
		}
		FluidIdentifier.rlock.lock();
		{
			FluidIdentifier unknownFluid = FluidIdentifier._fluidIdentifierCache.get(fluidID);
			if (unknownFluid != null) {
				FluidIdentifier.rlock.unlock();
				return unknownFluid;
			}
		}
		FluidIdentifier.rlock.unlock();
		FluidIdentifier.wlock.lock();
		{
			FluidIdentifier unknownFluid = FluidIdentifier._fluidIdentifierCache.get(fluidID);
			if (unknownFluid != null) {
				FluidIdentifier.wlock.unlock();
				return unknownFluid;
			}
		}
		int id = FluidIdentifier.getUnusedId();
		FluidIdentifier unknownFluid = new FluidIdentifier(fluidID, FluidRegistry.getFluidName(fluid), null, id);
		FluidIdentifier._fluidIdentifierCache.put(fluidID, unknownFluid);
		FluidIdentifier._fluidIdentifierIdCache.put(id, unknownFluid);
		FluidIdentifier.wlock.unlock();
		return (unknownFluid);
	}

	public static FluidIdentifier get(FluidStack stack) {
		if (stack == null) {
			return null;
		}
		FluidIdentifier proposal = null;
		IAddInfoProvider prov = null;
		if (stack instanceof IAddInfoProvider) {
			prov = (IAddInfoProvider) stack;
			FluidStackAddInfo info = prov.getLogisticsPipesAddInfo(FluidStackAddInfo.class);
			if (info != null) {
				proposal = info.fluid;
			}
		}
		FluidIdentifier ident = FluidIdentifier.get(stack.getFluid(), stack.tag, proposal);
		if (proposal != ident && stack.tag == null && prov != null) {
			prov.setLogisticsPipesAddInfo(new FluidStackAddInfo(ident));
		}
		return ident;
	}

	public static FluidIdentifier get(ItemIdentifier stack) {
		return FluidIdentifier.get(stack.makeStack(1));
	}

	public static FluidIdentifier get(@Nonnull ItemStack stack) {
		return FluidIdentifier.get(ItemIdentifierStack.getFromStack(stack));
	}

	public static FluidIdentifier get(ItemIdentifierStack stack) {
		FluidStack f = null;
		FluidIdentifierStack fstack = SimpleServiceLocator.logisticsFluidManager.getFluidFromContainer(stack);
		if (fstack != null) {
			f = fstack.makeFluidStack();
		}
		if (f == null) {
			ItemStack itemStack = stack.unsafeMakeNormalStack();
			if (itemStack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
				IFluidHandlerItem capability = itemStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
				if (capability != null) {
					f = Arrays.stream(capability.getTankProperties()).map(IFluidTankProperties::getContents).filter(Objects::nonNull).findFirst().orElse(null);
				}
			}
		}
		if (f == null) {
			f = FluidUtil.getFluidContained(stack.unsafeMakeNormalStack());
		}
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
		return new FluidStack(getFluid(), amount, tag);
	}

	public FluidIdentifierStack makeFluidIdentifierStack(int amount) {
		//FluidStack constructor does the tag.copy(), so this is safe
		return new FluidIdentifierStack(this, amount);
	}

	public Fluid getFluid() {
		return FluidRegistry.getFluid(fluidID);
	}

	public int getFreeSpaceInsideTank(IFluidTank tank) {
		FluidStack liquid = tank.getFluid();
		if (liquid == null || liquid.getFluid() == null) {
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
		fluids.values().forEach(FluidIdentifier::get);
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
		for (FluidIdentifier i : FluidIdentifier._fluidIdentifierCache.values()) {
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
		for (FluidIdentifier i : FluidIdentifier._fluidIdentifierCache.values()) {
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
		for (FluidIdentifier i : FluidIdentifier._fluidIdentifierCache.values()) {
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
		for (FluidIdentifier i : FluidIdentifier._fluidIdentifierCache.values()) {
			if (i != null) {
				last = i;
			}
		}
		FluidIdentifier.rlock.unlock();
		return last;
	}

	public static Collection<FluidIdentifier> all() {
		FluidIdentifier.rlock.lock();
		Collection<FluidIdentifier> list = Collections.unmodifiableCollection(FluidIdentifier._fluidIdentifierCache.values());
		FluidIdentifier.rlock.unlock();
		return list;
	}

	public ItemIdentifier getItemIdentifier() {
		return SimpleServiceLocator.logisticsFluidManager.getFluidContainer(this.makeFluidIdentifierStack(1)).getItem();
	}

	@Override
	public Object[] getTypeHolder() {
		return ccTypeHolder;
	}

}
