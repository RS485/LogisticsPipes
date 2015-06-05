package logisticspipes.interfaces;

import logisticspipes.pipefxhandlers.Particles;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

public interface ISpawnParticles {

	@RequiredArgsConstructor
	public class ParticleCount {

		@Getter
		private final Particles particle;
		@Getter
		private final int amount;
	}

	public void spawnParticle(Particles particle, int amount);
}
