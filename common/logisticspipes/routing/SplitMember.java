package logisticspipes.routing;

import java.util.UUID;

public class SplitMember {
	
	public int groupNumber;
	public final int totalAmount;
	public int amountSinked;
	public boolean myTurn;
		
	public SplitMember(int group, int amount, boolean turn) {
		this.groupNumber = group;
		this.totalAmount = amount;
		myTurn = turn;
	}

	public int reduceSink(int par1) {
		//not my turn
		if (!myTurn) return 0;

		//cant sink that many
		if (par1 > totalAmount-amountSinked) {
			int numberSinked = totalAmount-amountSinked;
			giveUpTurn();
			return numberSinked;
		}
		
		//can sink that many
		if (par1 <= totalAmount-amountSinked) {
			amountSinked = amountSinked + par1;
			//if no more sinks left
			if (amountSinked == totalAmount) {
				giveUpTurn();
			}
			return par1;
		}
		return 0;
	}
	
	public void giveUpTurn() {
		myTurn = false;
		amountSinked = 0;
	}
	
	
}
