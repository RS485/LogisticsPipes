package logisticspipes.proxy.computers.objects;

import java.util.List;

import logisticspipes.proxy.computers.interfaces.CCCommand;
import logisticspipes.proxy.computers.interfaces.CCType;
import logisticspipes.utils.item.ItemIdentifier;
import net.minecraft.item.Item;

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
		Item item = Item.getItemById(itemID);
		if(item == null) throw new UnsupportedOperationException("Not a valid ItemIdentifier");
		return ItemIdentifier.get(item, itemData, null);
	}

	@CCCommand(description="Returns a list of all ItemIdentifier with an NBT tag matching the givven Item ID and data")
	public List<ItemIdentifier> matchingNBTIdentifier() {
		Item item = Item.getItemById(itemID);
		if(item == null) throw new UnsupportedOperationException("Not a valid ItemIdentifier");
		return ItemIdentifier.getMatchingNBTIdentifier(item, itemData);
	}
}
