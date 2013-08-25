package logisticspipes.utils;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import logisticspipes.LogisticsPipes;
import logisticspipes.proxy.SimpleServiceLocator;
import net.minecraft.item.Item;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import com.google.common.base.Objects;

public class FluidIdentifier {

	private static class ItemKey implements Comparable<ItemKey>{

		public int fluidID;
		public int itemDamage;
		
		public ItemKey(int id, int d){
			fluidID=id;
			itemDamage=d;
		}
		
		@Override 
		public boolean equals(Object that){
			if (!(that instanceof ItemKey))
				return false;
			ItemKey i = (ItemKey)that;
			return this.fluidID== i.fluidID && this.itemDamage == i.itemDamage;
			
		}
		
		@Override
		public int hashCode(){
			//1000001 chosen because 1048576 is 2^20, moving the bits for the item ID to the top of the integer
			// not exactly 2^20 was chosen so that when the has is used mod power 2, there arn't repeated collisions on things with the same damage id.
			//return ((fluidID)*1000001)+itemDamage;
			return Objects.hashCode(fluidID, itemDamage);
		}
		
		@Override
		public int compareTo(ItemKey o) {
			if(fluidID==o.fluidID)
				return itemDamage-o.itemDamage;
			return fluidID-o.fluidID;
		}
	}
	
	private final static ConcurrentHashMap<ItemKey, FluidIdentifier> _liquidIdentifierCache = new ConcurrentHashMap<ItemKey, FluidIdentifier>();
	
	private final static ConcurrentSkipListSet<ItemKey> _liquidIdentifierKeyQueue = new ConcurrentSkipListSet<ItemKey>();
	
	public final int fluidID;
	public final int itemMeta;
	public final String name;
	
	private FluidIdentifier(int fluidID, int itemMeta, String name) {
		this.fluidID = fluidID;
		this.itemMeta = itemMeta;
		if(name == null) {
			name = "";
		}
		this.name = name;
		ItemKey key = new ItemKey(fluidID, itemMeta);
		_liquidIdentifierCache.put(key, this);
		if(this.isVaild()) {
			_liquidIdentifierKeyQueue.add(key);
		}
	}
	
	public String getName() {
		return name;
	}
	
	public static FluidIdentifier get(FluidStack stack) {
		if(stack.tag != null) {
			LogisticsPipes.log.warning("Found liquidStack with NBT tag. LP doesn't know how to handle it.");
			new Exception().printStackTrace();
		}
		return get(stack.fluidID, 0);
	}
	
	public static FluidIdentifier get(Fluid fluid, String name) {
		/*if(fluid.extra != null) {
			LogisticsPipes.log.warning("Found liquidStack with NBT tag. LP doesn't know how to handle it.");
			new Exception().printStackTrace();
		}*/
		return get(fluid.getID(), 0);
	}
	
	public static FluidIdentifier get(int fluidID, int itemMeta) {
		return get(fluidID, itemMeta, "");
	}
	
	public static FluidIdentifier get(int fluidID, int itemMeta, String name) {
		if(_liquidIdentifierCache.containsKey(new ItemKey(fluidID, itemMeta))) {
			return _liquidIdentifierCache.get(new ItemKey(fluidID, itemMeta));
		}
		return new FluidIdentifier(fluidID, itemMeta, name);
	}
	
	public FluidStack makeFluidStack(int amount) {
		return new FluidStack(fluidID, amount);
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
	
	public int getFreeSpaceInsideTank(FluidTankInfo tanks) {
		FluidStack liquid = tanks.fluid;
		if(liquid == null || liquid.fluidID <= 0) {
			return tanks.capacity;
		}
		if(get(liquid) == this) {
			return tanks.capacity - liquid.amount;
		}
		return 0;
	}
	
	private static boolean init = false;
	public static void initFromForge(boolean flag) {
		if(init) return;
		Map<String, Fluid> liquids = FluidRegistry.getRegisteredFluids();
		for(Entry<String, Fluid> name: liquids.entrySet()) {
			get(name.getValue(), name.getKey());
		}
		if(flag) {
			init = true;
		}
	}
	
	@Override
	public String toString() {
		return name + "/" + fluidID + ":" + itemMeta;
	}
	
	public boolean isVaild() {
		return Item.itemsList.length > fluidID && Item.itemsList[fluidID] != null;
	}
	
	public FluidIdentifier next() {
		ItemKey key = new ItemKey(fluidID, itemMeta);
		if(!_liquidIdentifierKeyQueue.contains(key)) return first();
		key = _liquidIdentifierKeyQueue.higher(key);
		if(key == null) {
			return null;
		}
		return _liquidIdentifierCache.get(key);
	}
	
	public FluidIdentifier prev() {
		ItemKey key = new ItemKey(fluidID, itemMeta);
		if(!_liquidIdentifierKeyQueue.contains(key)) return last();
		key = _liquidIdentifierKeyQueue.lower(key);
		if(key == null) {
			return null;
		}
		return _liquidIdentifierCache.get(key);
	}
	
	public static FluidIdentifier first() {
		ItemKey key = _liquidIdentifierKeyQueue.first();
		if(key == null) {
			return null;
		}
		return _liquidIdentifierCache.get(key);
	}
	
	public static FluidIdentifier last() {
		ItemKey key = _liquidIdentifierKeyQueue.last();
		if(key == null) {
			return null;
		}
		return _liquidIdentifierCache.get(key);
	}

	public ItemIdentifier getItemIdentifier() {
		return ItemIdentifier.get(SimpleServiceLocator.logisticsFluidManager.getFluidContainer(makeFluidStack(0)));
	}
}
