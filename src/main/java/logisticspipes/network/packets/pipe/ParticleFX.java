package logisticspipes.network.packets.pipe;

import logisticspipes.network.abstractpackets.Integer2CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.proxy.MainProxy;
import net.minecraft.entity.player.EntityPlayer;

public class ParticleFX extends Integer2CoordinatesPacket {

	public ParticleFX(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new ParticleFX(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		int particle = getInteger();
		int amount = getInteger2();
		MainProxy.spawnParticle(particle, getPosX(), getPosY(), getPosZ(), amount);
	}
}

