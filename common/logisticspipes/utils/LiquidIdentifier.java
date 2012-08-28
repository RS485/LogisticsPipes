package logisticspipes.utils;

import java.util.LinkedList;

import buildcraft.api.liquids.LiquidStack;

public class LiquidIdentifier {
	
	private final static LinkedList<LiquidIdentifier> _liquidIdentifierCache = new LinkedList<LiquidIdentifier>();
	
	private int itemId;
	private int itemMeta;
	
	private LiquidIdentifier(int itemId, int itemMeta) {
		this.itemId = itemId;
		this.itemMeta = itemMeta;
		_liquidIdentifierCache.add(this);
	}
	
	public static LiquidIdentifier get(LiquidStack stack) {
		for(LiquidIdentifier ident:_liquidIdentifierCache) {
			if(stack.itemID == ident.itemId && stack.itemMeta == ident.itemMeta) {
				return ident;
			}
		}
		return new LiquidIdentifier(stack.itemID,stack.itemMeta);
	}
	
	public String toString() {
		return itemId+":"+itemMeta;
	}
}
