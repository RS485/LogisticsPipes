package logisticspipes.network.packets.cpipe;

import net.minecraft.entity.player.EntityPlayer;
import logisticspipes.network.abstractpackets.Integer2CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.PipeItemsCraftingLogistics;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;

public class CraftingFuzzyFlag extends Integer2CoordinatesPacket
{
	public CraftingFuzzyFlag(int id)
	{
		super(id);
	}

	@Override
	public void processPacket(EntityPlayer player)
	{
		final LogisticsTileGenericPipe pipe = this.getPipe(player.worldObj);
		if(pipe == null) {
			return;
		}
		if( !(pipe.pipe instanceof PipeItemsCraftingLogistics)) {
			return;
		}
		((PipeItemsCraftingLogistics) pipe.pipe).setFuzzyCraftingFlag(getInteger(), getInteger2(), player);
	}

	@Override
	public ModernPacket template()
	{
		return new CraftingFuzzyFlag(getId());
	}
	
}
