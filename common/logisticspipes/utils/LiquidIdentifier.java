package logisticspipes.utils;

import java.util.LinkedList;
import java.util.Map;

import net.minecraftforge.liquids.LiquidDictionary;
import net.minecraftforge.liquids.LiquidStack;

public class LiquidIdentifier {
	
	private final static LinkedList<LiquidIdentifier> _liquidIdentifierCache = new LinkedList<LiquidIdentifier>();
	
	public final int itemId;
	public final int itemMeta;
	public final String name;
	
	private LiquidIdentifier(int itemId, int itemMeta) {
		this.itemId = itemId;
		this.itemMeta = itemMeta;
		this.name = "";
		_liquidIdentifierCache.add(this);
	}
	
	private LiquidIdentifier(int itemId, int itemMeta, String name) {
		this.itemId = itemId;
		this.itemMeta = itemMeta;
		if(name == null) {
			name = "";
		}
		this.name = name;
		_liquidIdentifierCache.add(this);
	}
	
	public String getName() {
		return name;
	}
	
	public ItemIdentifier getItemIdentifier() {
		return ItemIdentifier.get(itemId, itemMeta, null);
	}
	
	public static LiquidIdentifier get(LiquidStack stack) {
		for(LiquidIdentifier ident:_liquidIdentifierCache) {
			if(stack.itemID == ident.itemId && stack.itemMeta == ident.itemMeta) {
				return ident;
			}
		}
		return new LiquidIdentifier(stack.itemID,stack.itemMeta);
	}
	
	public static LiquidIdentifier get(LiquidStack stack, String name) {
		for(LiquidIdentifier ident:_liquidIdentifierCache) {
			if(stack.itemID == ident.itemId && stack.itemMeta == ident.itemMeta) {
				return ident;
			}
		}
		return new LiquidIdentifier(stack.itemID,stack.itemMeta, name);
	}
	
	public static LiquidIdentifier get(int itemID, int itemMeta) {
		for(LiquidIdentifier ident:_liquidIdentifierCache) {
			if(itemID == ident.itemId && itemMeta == ident.itemMeta) {
				return ident;
			}
		}
		return new LiquidIdentifier(itemID, itemMeta);
	}
	
	public static LiquidIdentifier get(int itemID, int itemMeta, String name) {
		for(LiquidIdentifier ident:_liquidIdentifierCache) {
			if(itemID == ident.itemId && itemMeta == ident.itemMeta) {
				return ident;
			}
		}
		return new LiquidIdentifier(itemID, itemMeta, name);
	}
	
	private static boolean init = false;
	public static void initFromForge(boolean flag) {
		if(init) return;
		Map<String, LiquidStack> liquids = LiquidDictionary.getLiquids();
		for(String name: liquids.keySet()) {
			get(liquids.get(name), name);
		}
		if(flag) {
			init = true;
		}
	}
	
	public String toString() {
		return itemId+":"+itemMeta;
	}
}
