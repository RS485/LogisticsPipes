package logisticspipes.network.packets.pipe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import logisticspipes.interfaces.ISpawnParticles.ParticleCount;
import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipefxhandlers.Particles;
import logisticspipes.pipefxhandlers.PipeFXRenderHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
public class ParticleFX extends CoordinatesPacket {

	public ParticleFX(int id) {
		super(id);
	}

	@Getter
	@Setter
	@NonNull
	private Collection<ParticleCount> particles;

	@Override
	public ModernPacket template() {
		return new ParticleFX(getId());
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		super.readData(data);
		int nparticles = data.readInt();
		particles = new ArrayList<ParticleCount>(nparticles);
		for (int i = 0; i < nparticles; i++) {
			int particle = data.readByte();
			int amount = data.readInt();
			particles.add(new ParticleCount(Particles.values()[particle], amount));
		}
	}

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeInt(particles.size());
		for (ParticleCount pc : particles) {
			data.writeByte(pc.getParticle().ordinal());
			data.writeInt(pc.getAmount());
		}
	}

	@Override
	public void processPacket(EntityPlayer player) {
		if (!Minecraft.isFancyGraphicsEnabled()) {
			return;
		}
		for (ParticleCount pc : particles) {
			PipeFXRenderHandler.spawnGenericParticle(pc.getParticle(), getPosX(), getPosY(), getPosZ(), pc.getAmount());
		}
	}
}
