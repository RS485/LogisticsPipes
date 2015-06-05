package logisticspipes.renderer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import logisticspipes.config.Configs;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.block.PowerPacketLaser;
import logisticspipes.pipefxhandlers.PipeFXLaserPowerBall;
import logisticspipes.pipefxhandlers.PipeFXLaserPowerBeam;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.tuples.LPPosition;

import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

public class LogisticsTileRenderController {

	private final LogisticsTileGenericPipe pipe;
	private final int LASER_TIMEOUT_TICKS = 4;
	private final Map<LaserKey, LaserBeamData> powerLasersBeam = new HashMap<LaserKey, LaserBeamData>();
	private final Map<Integer, LaserBallData> powerLasersBall = new HashMap<Integer, LaserBallData>();

	@Data
	@AllArgsConstructor
	private class LaserKey {

		final ForgeDirection dir;
		final int color;
	}

	@Data
	@AllArgsConstructor
	private class LaserBeamData {

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

		public LaserBeamDataClient(float length, int timeout, boolean reverse, ForgeDirection dir, int color) {
			super(length, timeout, reverse);
			entity = new PipeFXLaserPowerBeam(pipe.getWorldObj(), new LPPosition((TileEntity) pipe), length, dir, color, pipe).setReverse(reverse);
			Minecraft.getMinecraft().effectRenderer.addEffect(entity);

		}

		@Getter
		@SideOnly(Side.CLIENT)
		final PipeFXLaserPowerBeam entity;

		@Override
		boolean isDeadEntity() {
			return entity == null || entity.isDead;
		}

		@Override
		void setDead() {
			if (entity != null) {
				entity.setDead();
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
	private class LaserBallData {

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
			entity = new PipeFXLaserPowerBall(pipe.getWorldObj(), new LPPosition((TileEntity) pipe), color, pipe);
			Minecraft.getMinecraft().effectRenderer.addEffect(entity);
		}

		@Getter
		@SideOnly(Side.CLIENT)
		final PipeFXLaserPowerBall entity;

		@Override
		boolean isDeadEntity() {
			return entity == null || entity.isDead;
		}

		@Override
		void setDead() {
			if (entity != null) {
				entity.setDead();
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
						MainProxy.sendPacketToAllWatchingChunk(pipe.getX(), pipe.getZ(), MainProxy.getDimensionForWorld(pipe.getWorld()), PacketHandler.getPacket(PowerPacketLaser.class).setColor(key.color).setRenderBall(false).setDir(key.dir).setRemove(true).setTilePos(pipe));
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
						MainProxy.sendPacketToAllWatchingChunk(pipe.getX(), pipe.getZ(), MainProxy.getDimensionForWorld(pipe.getWorld()), PacketHandler.getPacket(PowerPacketLaser.class).setColor(key).setRenderBall(true).setDir(ForgeDirection.UNKNOWN).setRemove(true).setTilePos(pipe));
					}
					iter.remove();
				}
			}
		}
	}

	public void addLaser(ForgeDirection dir, float length, int color, boolean reverse, boolean renderBall) {
		if (!Configs.ENABLE_PARTICLE_FX) {
			return;
		}
		boolean sendPacket = false;
		if (powerLasersBeam.containsKey(new LaserKey(dir, color))) {
			powerLasersBeam.get(new LaserKey(dir, color)).timeout = LASER_TIMEOUT_TICKS;
		} else {
			if (MainProxy.isClient(pipe.getWorldObj())) {
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
				if (MainProxy.isClient(pipe.getWorldObj())) {
					powerLasersBall.put(color, new LaserBallDataClient(length, LASER_TIMEOUT_TICKS, color));
				} else {
					powerLasersBall.put(color, new LaserBallData(length, LASER_TIMEOUT_TICKS));
					sendPacket = true;
				}
			}
		}
		if (sendPacket) {
			MainProxy.sendPacketToAllWatchingChunk(pipe.getX(), pipe.getZ(), MainProxy.getDimensionForWorld(pipe.getWorld()), PacketHandler.getPacket(PowerPacketLaser.class).setColor(color).setRenderBall(renderBall).setDir(dir).setLength(length).setReverse(reverse).setTilePos(pipe));
		}
	}

	public void removeLaser(ForgeDirection dir, int color, boolean isBall) {
		if (!MainProxy.isClient(pipe.getWorldObj())) {
			return;
		}
		if (!isBall) {
			LaserKey key = new LaserKey(dir, color);
			LaserBeamData beam = powerLasersBeam.get(key);
			if (beam != null) {
				beam.timeout = -1;
				if (MainProxy.isClient(pipe.getWorldObj())) {
					((LaserBeamDataClient) beam).entity.setDead();
				}
				powerLasersBeam.remove(key);
			}
		} else {
			LaserBallData ball = powerLasersBall.get(color);
			if (ball != null) {
				ball.timeout = -1;
				if (MainProxy.isClient(pipe.getWorldObj())) {
					((LaserBallDataClient) ball).entity.setDead();
				}
				powerLasersBall.remove(color);
			}
		}
	}

	public void sendInit() {
		Iterator<LaserKey> iter = powerLasersBeam.keySet().iterator();
		while (iter.hasNext()) {
			LaserKey key = iter.next();
			LaserBeamData data = powerLasersBeam.get(key);
			boolean isBall = powerLasersBall.containsKey(key.color);
			MainProxy.sendPacketToAllWatchingChunk(pipe.getX(), pipe.getZ(), MainProxy.getDimensionForWorld(pipe.getWorld()), PacketHandler.getPacket(PowerPacketLaser.class).setColor(key.color).setRenderBall(isBall).setDir(key.dir).setLength(data.length).setReverse(data.reverse).setTilePos(pipe));
		}
	}
}
