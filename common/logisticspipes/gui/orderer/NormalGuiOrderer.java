package logisticspipes.gui.orderer;

import java.util.HashMap;
import java.util.LinkedList;

import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.main.CoreRoutedPipe;
import logisticspipes.network.NetworkConstants;
import logisticspipes.network.PacketPipeInteger;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.gui.SmallGuiButton;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.GuiButton;
import cpw.mods.fml.common.network.PacketDispatcher;

public class NormalGuiOrderer extends GuiOrderer {

	private enum DisplayOptions {
		Both,
		SupplyOnly,
		CraftOnly,
	}

	private HashMap<ItemIdentifier, Integer> _availableItems;
	private LinkedList<ItemIdentifier> _craftableItems;
	
	protected DisplayOptions displayOptions = DisplayOptions.Both;
	
	public NormalGuiOrderer(IRequestItems itemRequester, EntityPlayer entityPlayer) {
		super(itemRequester, entityPlayer);
		refreshItems();
	}
	
	public void initGui() {
		super.initGui();
		controlList.add(new SmallGuiButton(9, guiLeft + 10, bottom - 41, 46, 10, "Both"));
	}
	
	protected void refreshItems(){
		/*if(!CoreProxy.isRemote()) {
			if (displayOptions == DisplayOptions.SupplyOnly || displayOptions == DisplayOptions.Both){
				_availableItems = mod_LogisticsPipes.logisticsManager.getAvailableItems(_itemRequester.getRouter().getRouteTable().keySet());
			} else {
				_availableItems = new HashMap<ItemIdentifier, Integer>();
			}
			if (displayOptions == DisplayOptions.CraftOnly || displayOptions == DisplayOptions.Both){
				_craftableItems = mod_LogisticsPipes.logisticsManager.getCraftableItems(_itemRequester.getRouter().getRouteTable().keySet());
			} else {
				_craftableItems = new LinkedList<ItemIdentifier>();
			}
			_allItems.clear();
			
			outer:
			for (ItemIdentifier item : _availableItems.keySet()){
				for (int i = 0; i <_allItems.size(); i++){
					if (item.itemID < _allItems.get(i).getItem().itemID || item.itemID == _allItems.get(i).getItem().itemID && item.itemDamage < _allItems.get(i).getItem().itemDamage){
						_allItems.add(i, item.makeStack(_availableItems.get(item)));
						continue outer;
					}
				}
				_allItems.addLast(item.makeStack(_availableItems.get(item)));
			}
			
			outer:
			for (ItemIdentifier item : _craftableItems){
				if (_availableItems.containsKey(item)) continue;
				for (int i = 0; i <_allItems.size(); i++){
					if (item.itemID < _allItems.get(i).getItem().itemID || item.itemID == _allItems.get(i).getItem().itemID && item.itemDamage < _allItems.get(i).getItem().itemDamage){
						_allItems.add(i, item.makeStack(0));
						continue outer;
					}
				}
				_allItems.addLast(item.makeStack(0));
			}
		} else {*/
			CoreRoutedPipe requestPipe = (CoreRoutedPipe)_itemRequester;
			int integer;
			switch(displayOptions) {
			case Both:
				integer = 0;
				break;
			case SupplyOnly:
				integer = 1;
				break;
			case CraftOnly:
				integer = 2;
				break;
			default: 
				integer = 3;
			}
			PacketDispatcher.sendPacketToServer(new PacketPipeInteger(NetworkConstants.ORDERER_REFRESH_REQUEST,requestPipe.xCoord,requestPipe.yCoord,requestPipe.zCoord,integer).getPacket());
		//}
	}

	protected void actionPerformed(GuiButton guibutton) {
		super.actionPerformed(guibutton);
		if (guibutton.id == 9) {
			String displayString = "";
			switch (displayOptions){
			case Both:
				displayOptions = DisplayOptions.CraftOnly;
				displayString = "Craft";
				break;
			case CraftOnly:
				displayOptions = DisplayOptions.SupplyOnly;
				displayString = "Supply";
				break;
			case SupplyOnly:
				displayOptions = DisplayOptions.Both;
				displayString = "Both";
				break;
			}
			guibutton.displayString = displayString;
			refreshItems();
		}
	}
}
