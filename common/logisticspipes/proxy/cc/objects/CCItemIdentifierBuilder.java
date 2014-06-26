package logisticspipes.proxy.cc.objects;

import java.util.List;

import net.minecraft.item.Item;

import logisticspipes.proxy.cc.interfaces.CCCommand;
import logisticspipes.proxy.cc.interfaces.CCType;
import logisticspipes.utils.item.ItemIdentifier;

@CCType(name="ItemIdentifierBuilder")
public class CCItemIdentifierBuilder {
	private int itemID = 0;
	private int itemData = 0;
	
	@CCCommand(description="Set the itemID for this ItemIdentifierBuilder")
	public void setItemID(Double id) {
		this.itemID = id.intValue();
	}

	@CCCommand(description="Returns the itemID for this ItemIdentifierBuilder")
	public int getItemID() {
		return itemID;
	}

	@CCCommand(description="Set the item data/damage for this ItemIdentifierBuilder")
	public void setItemData(Double data) {
		this.itemData = data.intValue();
	}

	@CCCommand(description="Returns the item data/damage for this ItemIdentifierBuilder")
	public int getItemData() {
		return itemData;
	}

	@CCCommand(description="Returns the ItemIdentifier for this ItemIdentifierBuilder")
	public ItemIdentifier build() {
		if(itemID < 0 || itemID > Item.itemsList.length || Item.itemsList[itemID] == null) throw new UnsupportedOperationException("Not a valid ItemIdentifier");
		return ItemIdentifier.get(itemID, itemData, null);
	}

	@CCCommand(description="Returns a list of all ItemIdentifier with an NBT tag matching the givven Item ID and data")
	public List<ItemIdentifier> matchingNBTIdentifier() {
		if(itemID < 0 || itemID > Item.itemsList.length || Item.itemsList[itemID] == null) throw new UnsupportedOperationException("Not a valid ItemIdentifier");
		return ItemIdentifier.getMatchingNBTIdentifier(itemID, itemData);
	}
}
