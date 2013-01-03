package logisticspipes.request;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import logisticspipes.interfaces.routing.IRequestLiquid;
import logisticspipes.routing.LiquidLogisticsPromise;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.ItemMessage;
import logisticspipes.utils.LiquidIdentifier;

public class LiquidRequest {
	
	private final LiquidIdentifier liquid;
	private final int amount;
	private List<LiquidLogisticsPromise> promises = new ArrayList<LiquidLogisticsPromise>();
	
	public LiquidRequest(LiquidIdentifier liquid, int amount) {
		this.liquid = liquid;
		this.amount = amount;
	}

	public int getAmount() {
		return amount;
	}

	public LiquidIdentifier getLiquid() {
		return liquid;
	}

	public ItemIdentifierStack getStack() {
		return liquid.getItemIdentifier().makeStack(amount);
	}
	
	public int amountLeft() {
		int promised = 0;
		for(LiquidLogisticsPromise promise: promises){
			promised += promise.amount;
		}
		return amount - promised;
	}

	public boolean isAllDone() {
		return amountLeft() <= 0;
	}

	public void fullFill(IRequestLiquid destination) {
		for(LiquidLogisticsPromise promise: promises){
			promise.sender.fullFill(promise, destination);
		}
	}

	public void sendMissingMessage(RequestLog log) {
		LinkedList<ItemMessage> mes = new LinkedList<ItemMessage>();
		mes.add(new ItemMessage(liquid.getItemIdentifier(), amountLeft()));
		log.handleMissingItems(mes);
	}
}
