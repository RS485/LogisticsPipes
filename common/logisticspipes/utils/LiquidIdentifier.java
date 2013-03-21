package logisticspipes.utils;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import net.minecraftforge.liquids.LiquidDictionary;
import net.minecraftforge.liquids.LiquidStack;

import com.google.common.base.Objects;

public class LiquidIdentifier {

	private static class ItemKey implements Comparable<ItemKey>{

		public int itemID;
		public int itemDamage;
		
		public ItemKey(int id, int d){
			itemID=id;
			itemDamage=d;
		}
		
		@Override 
		public boolean equals(Object that){
			if (!(that instanceof ItemKey))
				return false;
			ItemKey i = (ItemKey)that;
			return this.itemID== i.itemID && this.itemDamage == i.itemDamage;
			
		}
		
		@Override
		public int hashCode(){
			//1000001 chosen because 1048576 is 2^20, moving the bits for the item ID to the top of the integer
			// not exactly 2^20 was chosen so that when the has is used mod power 2, there arn't repeated collisions on things with the same damage id.
			//return ((itemID)*1000001)+itemDamage;
			return Objects.hashCode(itemID, itemDamage);
		}
		
		@Override
		public int compareTo(ItemKey o) {
			if(itemID==o.itemID)
				return itemDamage-o.itemDamage;
			return itemID-o.itemID;
		}
	}
	
	private final static ConcurrentHashMap<ItemKey, LiquidIdentifier> _liquidIdentifierCache = new ConcurrentHashMap<ItemKey, LiquidIdentifier>();
	
	private final static ConcurrentSkipListSet<ItemKey> _liquidIdentifierKeyQueue = new ConcurrentSkipListSet<ItemKey>();
	
	public final int itemId;
	public final int itemMeta;
	public final String name;
	
	private LiquidIdentifier(int itemId, int itemMeta, String name) {
		this.itemId = itemId;
		this.itemMeta = itemMeta;
		if(name == null) {
			name = "";
		}
		this.name = name;
		ItemKey key = new ItemKey(itemId, itemMeta);
		_liquidIdentifierCache.put(key, this);
		_liquidIdentifierKeyQueue.add(key);
	}
	
	public String getName() {
		return name;
	}
	
	public ItemIdentifier getItemIdentifier() {
		return ItemIdentifier.get(itemId, itemMeta, null);
	}
	
	public static LiquidIdentifier get(LiquidStack stack) {
		return get(stack.itemID, stack.itemMeta);
	}
	
	public static LiquidIdentifier get(LiquidStack stack, String name) {
		return get(stack.itemID, stack.itemMeta, name);
	}
	
	public static LiquidIdentifier get(int itemID, int itemMeta) {
		return get(itemID, itemMeta, "");
	}
	
	public static LiquidIdentifier get(int itemID, int itemMeta, String name) {
		if(_liquidIdentifierCache.containsKey(new ItemKey(itemID, itemMeta))) {
			return _liquidIdentifierCache.get(new ItemKey(itemID, itemMeta));
		}
		return new LiquidIdentifier(itemID, itemMeta, name);
	}
	
	private static boolean init = false;
	public static void initFromForge(boolean flag) {
		if(init) return;
		Map<String, LiquidStack> liquids = LiquidDictionary.getLiquids();
		for(Entry<String, LiquidStack> name: liquids.entrySet()) {
			get(name.getValue(), name.getKey());
		}
		if(flag) {
			init = true;
		}
	}
	
	@Override
	public String toString() {
		return name + "/" + itemId + ":" + itemMeta;
	}
	
	public LiquidIdentifier next() {
		ItemKey key = new ItemKey(itemId, itemMeta);
		key = _liquidIdentifierKeyQueue.higher(key);
		if(key == null) {
			return null;
		}
		return _liquidIdentifierCache.get(key);
	}
	
	public LiquidIdentifier prev() {
		ItemKey key = new ItemKey(itemId, itemMeta);
		key = _liquidIdentifierKeyQueue.lower(key);
		if(key == null) {
			return null;
		}
		return _liquidIdentifierCache.get(key);
	}
	
	public static LiquidIdentifier first() {
		ItemKey key = _liquidIdentifierKeyQueue.first();
		if(key == null) {
			return null;
		}
		return _liquidIdentifierCache.get(key);
	}
	
	public static LiquidIdentifier last() {
		ItemKey key = _liquidIdentifierKeyQueue.last();
		if(key == null) {
			return null;
		}
		return _liquidIdentifierCache.get(key);
	}
}
