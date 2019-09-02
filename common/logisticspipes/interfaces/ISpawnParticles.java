package logisticspipes.interfaces;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import logisticspipes.pipefxhandlers.Particles;

public interface ISpawnParticles {

	@RequiredArgsConstructor
	class ParticleCount {

		@Getter
		private final Particles particle;
		@Getter
		private final int amount;
	}

	void spawnParticle(Particles particle, int amount);
}
