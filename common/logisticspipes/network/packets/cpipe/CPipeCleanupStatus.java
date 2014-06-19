package logisticspipes.network.packets.cpipe;

import java.io.IOException;

import logisticspipes.asm.ClientSideOnlyMethodContent;
import logisticspipes.gui.GuiCraftingPipe;
import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.PipeItemsCraftingLogistics;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

@Accessors(chain=true)
public class CPipeCleanupStatus extends CoordinatesPacket {
	
	@Getter
	@Setter
	private boolean mode;
	
	public CPipeCleanupStatus(int id) {
		super(id);
	}
	
	@Override
	public ModernPacket template() {
		return new CPipeCleanupStatus(getId());
	}
	
	@Override
	@ClientSideOnlyMethodContent
	public void processPacket(EntityPlayer player) {
		final LogisticsTileGenericPipe pipe = getPipe(player.worldObj);
		if(pipe == null) {
			return;
		}
		
		if( !(pipe.pipe instanceof PipeItemsCraftingLogistics)) {
			return;
		}
		
		((PipeItemsCraftingLogistics) pipe.pipe).getLogisticsModule().cleanupModeIsExclude = mode;
		
		if(Minecraft.getMinecraft().currentScreen instanceof GuiCraftingPipe) {
			((GuiCraftingPipe)Minecraft.getMinecraft().currentScreen).onCleanupModeChange();
		}
	}

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeBoolean(mode);
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		super.readData(data);
		mode = data.readBoolean();
	}
}

