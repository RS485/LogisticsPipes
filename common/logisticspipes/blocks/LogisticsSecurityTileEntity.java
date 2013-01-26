package logisticspipes.blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import logisticspipes.interfaces.IGuiOpenControler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

public class LogisticsSecurityTileEntity extends TileEntity implements IGuiOpenControler {
	
	private List<EntityPlayer> listener = new ArrayList<EntityPlayer>();
	private UUID secId = null;
	
	@Override
	public void guiOpenedByPlayer(EntityPlayer player) {
		listener.add(player);
	}

	@Override
	public void guiClosedByPlayer(EntityPlayer player) {
		listener.remove(player);
	}

	public UUID getSecId() {
		if(secId == null) {
			secId = UUID.randomUUID();
		}
		return secId;
	}
}
