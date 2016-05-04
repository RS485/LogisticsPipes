package logisticspipes.network.packets.pipe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import logisticspipes.interfaces.ISpawnParticles.ParticleCount;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipefxhandlers.Particles;
import logisticspipes.pipefxhandlers.PipeFXRenderHandler;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

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
	public void readData(LPDataInput input) throws IOException {
		super.readData(input);
		int nparticles = input.readInt();
		particles = new ArrayList<>(nparticles);
		for (int i = 0; i < nparticles; i++) {
			int particle = input.readByte();
			int amount = input.readInt();
			particles.add(new ParticleCount(Particles.values()[particle], amount));
		}
	}

	@Override
	public void writeData(LPDataOutput output) throws IOException {
		super.writeData(output);
		output.writeInt(particles.size());
		for (ParticleCount pc : particles) {
			output.writeByte(pc.getParticle().ordinal());
			output.writeInt(pc.getAmount());
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
