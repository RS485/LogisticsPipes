package logisticspipes.renderer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import logisticspipes.Configs;
import logisticspipes.pipefxhandlers.PipeFXLaserPowerBall;
import logisticspipes.pipefxhandlers.PipeFXLaserPowerBeam;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.utils.tuples.LPPosition;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
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
		final PipeFXLaserPowerBeam entity;
	}
	
	@Data
	@AllArgsConstructor
	private class LaserBallData {
		final float length;
		int timeout;
		final PipeFXLaserPowerBall entity;
	}
	
	public LogisticsTileRenderController(LogisticsTileGenericPipe pipe) {
		this.pipe = pipe;
	}
	
	public void onUpdate() {
		{
			Iterator<LaserBeamData> iter = powerLasersBeam.values().iterator();
			while(iter.hasNext()) {
				LaserBeamData data = iter.next();
				data.timeout -= 1;
				if(data.timeout < 0 || data.entity == null || data.entity.isDead) {
					if(data.entity != null) data.entity.setDead();
					iter.remove();
				}
			}
		}{
			Iterator<LaserBallData> iter = powerLasersBall.values().iterator();
			while(iter.hasNext()) {
				LaserBallData data = iter.next();
				data.timeout -= 1;
				if(data.timeout < 0 || data.entity == null || data.entity.isDead) {
					if(data.entity != null) data.entity.setDead();
					iter.remove();
				}
			}
		}
	}

	public void addLaser(ForgeDirection dir, float length, int color, boolean reverse, boolean renderBall) {
		if(!Configs.ENABLE_PARTICLE_FX) return; 
		if(powerLasersBeam.containsKey(new LaserKey(dir, color))) {
			powerLasersBeam.get(new LaserKey(dir, color)).timeout = LASER_TIMEOUT_TICKS;
		} else {
			PipeFXLaserPowerBeam fx = new PipeFXLaserPowerBeam(pipe.worldObj, new LPPosition((TileEntity)pipe), length, dir, color, this.pipe).setReverse(reverse);
			powerLasersBeam.put(new LaserKey(dir, color), new LaserBeamData(length, LASER_TIMEOUT_TICKS, fx));
			Minecraft.getMinecraft().effectRenderer.addEffect(fx);
		}
		if(renderBall) {
			if(powerLasersBall.containsKey(color)) {
				powerLasersBall.get(color).timeout = LASER_TIMEOUT_TICKS;
			} else {
				PipeFXLaserPowerBall fx = new PipeFXLaserPowerBall(pipe.worldObj, new LPPosition((TileEntity)pipe), color, this.pipe);
				powerLasersBall.put(color, new LaserBallData(length, LASER_TIMEOUT_TICKS, fx));
				Minecraft.getMinecraft().effectRenderer.addEffect(fx);
			}
		}
	}
}
