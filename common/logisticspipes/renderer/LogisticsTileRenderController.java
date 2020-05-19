package logisticspipes.renderer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import logisticspipes.config.Configs;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.block.PowerPacketLaser;
import logisticspipes.pipefxhandlers.PipeFXLaserPowerBall;
import logisticspipes.pipefxhandlers.PipeFXLaserPowerBeam;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;
import network.rs485.logisticspipes.world.DoubleCoordinates;

public class LogisticsTileRenderController {

	private static final int LASER_TIMEOUT_TICKS = 4;

	private final LogisticsTileGenericPipe pipe;
	private final Map<LaserKey, LaserBeamData> powerLasersBeam = new HashMap<>();
	private final Map<Integer, LaserBallData> powerLasersBall = new HashMap<>();

	@Data
	@AllArgsConstructor
	private static class LaserKey {

		final EnumFacing dir;
		final int color;
	}

	@Data
	@AllArgsConstructor
	private static class LaserBeamData {

		final float length;
		int timeout;
		final boolean reverse;

		boolean isDeadEntity() {
			return false;
		}

		void setDead() {}

		boolean sendPacket() {
			return true;
		}

		void tick() {
			timeout--;
		}
	}

	private class LaserBeamDataClient extends LaserBeamData {

		public LaserBeamDataClient(float length, int timeout, boolean reverse, EnumFacing dir, int color) {
			super(length, timeout, reverse);
			entity = new PipeFXLaserPowerBeam(pipe.getWorld(), new DoubleCoordinates((TileEntity) pipe), length, dir, color, pipe).setReverse(reverse);
			Minecraft.getMinecraft().effectRenderer.addEffect(entity);

		}

		@Getter
		@SideOnly(Side.CLIENT)
		final PipeFXLaserPowerBeam entity;

		@Override
		boolean isDeadEntity() {
			return entity == null || !entity.isAlive();
		}

		@Override
		void setDead() {
			if (entity != null) {
				entity.setExpired();
			}
		}

		@Override
		boolean sendPacket() {
			return false;
		}

		@Override
		void tick() {}
	}

	@Data
	@AllArgsConstructor
	private static class LaserBallData {

		final float length;
		int timeout;

		boolean isDeadEntity() {
			return false;
		}

		void setDead() {}

		boolean sendPacket() {
			return true;
		}

		void tick() {
			timeout--;
		}
	}

	private class LaserBallDataClient extends LaserBallData {

		public LaserBallDataClient(float length, int timeout, int color) {
			super(length, timeout);
			entity = new PipeFXLaserPowerBall(pipe.getWorld(), new DoubleCoordinates((TileEntity) pipe), color, pipe);
			Minecraft.getMinecraft().effectRenderer.addEffect(entity);
		}

		@Getter
		@SideOnly(Side.CLIENT)
		final PipeFXLaserPowerBall entity;

		@Override
		boolean isDeadEntity() {
			return entity == null || !entity.isAlive();
		}

		@Override
		void setDead() {
			if (entity != null) {
				entity.setExpired();
			}
		}

		@Override
		boolean sendPacket() {
			return false;
		}

		@Override
		void tick() {}
	}

	public LogisticsTileRenderController(LogisticsTileGenericPipe pipe) {
		this.pipe = pipe;
	}

	public void onUpdate() {
		{
			Iterator<LaserKey> iter = powerLasersBeam.keySet().iterator();
			while (iter.hasNext()) {
				LaserKey key = iter.next();
				LaserBeamData data = powerLasersBeam.get(key);
				data.tick();
				if (data.timeout < 0 || data.isDeadEntity()) {
					data.setDead();
					if (data.sendPacket()) {
						MainProxy.sendPacketToAllWatchingChunk(pipe, PacketHandler.getPacket(PowerPacketLaser.class).setColor(key.color).setRenderBall(false).setDir(key.dir).setRemove(true).setTilePos(pipe));
					}
					iter.remove();
				}
			}
		}
		{
			Iterator<Integer> iter = powerLasersBall.keySet().iterator();
			while (iter.hasNext()) {
				Integer key = iter.next();
				LaserBallData data = powerLasersBall.get(key);
				data.tick();
				if (data.timeout < 0 || data.isDeadEntity()) {
					data.setDead();
					if (data.sendPacket()) {
						MainProxy.sendPacketToAllWatchingChunk(pipe, PacketHandler.getPacket(PowerPacketLaser.class).setColor(key).setRenderBall(true).setDir(null).setRemove(true).setTilePos(pipe));
					}
					iter.remove();
				}
			}
		}
	}

	public void addLaser(EnumFacing dir, float length, int color, boolean reverse, boolean renderBall) {
		if (!Configs.ENABLE_PARTICLE_FX) {
			return;
		}
		boolean sendPacket = false;
		if (powerLasersBeam.containsKey(new LaserKey(dir, color))) {
			powerLasersBeam.get(new LaserKey(dir, color)).timeout = LASER_TIMEOUT_TICKS;
		} else {
			if (MainProxy.isClient(pipe.getWorld())) {
				powerLasersBeam.put(new LaserKey(dir, color), new LaserBeamDataClient(length, LASER_TIMEOUT_TICKS, reverse, dir, color));
			} else {
				powerLasersBeam.put(new LaserKey(dir, color), new LaserBeamData(length, LASER_TIMEOUT_TICKS, reverse));
				sendPacket = true;
			}
		}
		if (renderBall) {
			if (powerLasersBall.containsKey(color)) {
				powerLasersBall.get(color).timeout = LASER_TIMEOUT_TICKS;
			} else {
				if (MainProxy.isClient(pipe.getWorld())) {
					powerLasersBall.put(color, new LaserBallDataClient(length, LASER_TIMEOUT_TICKS, color));
				} else {
					powerLasersBall.put(color, new LaserBallData(length, LASER_TIMEOUT_TICKS));
					sendPacket = true;
				}
			}
		}
		if (sendPacket) {
			MainProxy.sendPacketToAllWatchingChunk(pipe, PacketHandler.getPacket(PowerPacketLaser.class).setColor(color).setRenderBall(renderBall).setDir(dir).setLength(length).setReverse(reverse).setTilePos(pipe));
		}
	}

	public void removeLaser(EnumFacing dir, int color, boolean isBall) {
		if (!MainProxy.isClient(pipe.getWorld())) {
			return;
		}
		if (!isBall) {
			LaserKey key = new LaserKey(dir, color);
			LaserBeamData beam = powerLasersBeam.get(key);
			if (beam != null) {
				beam.timeout = -1;
				if (MainProxy.isClient(pipe.getWorld())) {
					((LaserBeamDataClient) beam).entity.setExpired();
				}
				powerLasersBeam.remove(key);
			}
		} else {
			LaserBallData ball = powerLasersBall.get(color);
			if (ball != null) {
				ball.timeout = -1;
				if (MainProxy.isClient(pipe.getWorld())) {
					((LaserBallDataClient) ball).entity.setExpired();
				}
				powerLasersBall.remove(color);
			}
		}
	}

	public void sendInit() {
		for (LaserKey key : powerLasersBeam.keySet()) {
			LaserBeamData data = powerLasersBeam.get(key);
			boolean isBall = powerLasersBall.containsKey(key.color);
			MainProxy.sendPacketToAllWatchingChunk(pipe, PacketHandler.getPacket(PowerPacketLaser.class)
					.setColor(key.color).setRenderBall(isBall).setDir(key.dir).setLength(data.length).setReverse(data.reverse).setTilePos(pipe));
		}
	}
}
