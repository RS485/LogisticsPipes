package logisticspipes.network.packets.orderer;

import logisticspipes.network.abstractpackets.ItemPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.utils.ItemIdentifier;
import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.client.FMLClientHandler;

public class CraftingLoop extends ItemPacket {

	public CraftingLoop(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new CraftingLoop(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		FMLClientHandler.instance().getClient().thePlayer.addChatMessage("Logistics: Possible crafting loop while trying to craft " + ItemIdentifier.get(getStack()).getFriendlyName() + " !! ABORTING !!");
	}
}

