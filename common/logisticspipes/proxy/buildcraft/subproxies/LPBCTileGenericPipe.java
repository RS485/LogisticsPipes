package logisticspipes.proxy.buildcraft.subproxies;

import java.lang.reflect.InvocationTargetException;

import org.apache.logging.log4j.Level;

import scala.actors.threadpool.Arrays;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.utils.ReflectionHelper;
import lombok.SneakyThrows;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.BuildCraftCore;
import buildcraft.api.core.BCLog;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.core.TileBuffer;
import buildcraft.core.network.BuildCraftPacket;
import buildcraft.transport.BlockGenericPipe;
import buildcraft.transport.TileGenericPipe;
import buildcraft.transport.render.FakeBlock;

public class LPBCTileGenericPipe extends TileGenericPipe implements IBCTilePart {
	
	private final LPBCPipe bcPipe;
	private final LPBCPluggableState bcPlugState;
	private final LPBCPipeRenderState bcRenderState;
	private final LogisticsTileGenericPipe lpPipe;
	
	private boolean blockPluggableAccess = false;
	
	public LPBCTileGenericPipe(LPBCPipe pipe, LogisticsTileGenericPipe lpPipe) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		this.pipe = this.bcPipe = pipe;
		bcPipe.setTile(this);
		this.lpPipe = lpPipe;
		bcPlugState = new LPBCPluggableState();
		bcRenderState = new LPBCPipeRenderState();
		ReflectionHelper.setFinalField(TileGenericPipe.class, "pluggableState", this, bcPlugState);
		ReflectionHelper.setFinalField(TileGenericPipe.class, "renderState", this, bcRenderState);
	}

	@Override
	public void writeToNBT_LP(NBTTagCompound nbt) {
		NBTTagCompound bcNBT = new NBTTagCompound();

		for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
			final String key = "redstoneInputSide[" + i + "]";
			bcNBT.setByte(key, (byte) redstoneInputSide[i]);
		}

		pipe.writeToNBT(bcNBT);

		sideProperties.writeToNBT(bcNBT);
		nbt.setTag("BC_Pipe_NBT", bcNBT);
	}

	@Override
	public void readFromNBT_LP(NBTTagCompound nbt) {
		if(!nbt.hasKey("BC_Pipe_NBT")) {
			redstoneInput = 0;
			for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
				final String key = "redstoneInputSide[" + i + "]";
				if (nbt.hasKey(key)) {
					redstoneInputSide[i] = nbt.getByte(key);

					if (redstoneInputSide[i] > redstoneInput) {
						redstoneInput = redstoneInputSide[i];
					}
				} else {
					redstoneInputSide[i] = 0;
				}
			}
			//Import PipePart
			pipe.readFromNBT(nbt);
			//Import TilePart
			pipeBound = true;
			sideProperties.readFromNBT(nbt);
			attachPluggables = true;
			return;
		}
		NBTTagCompound bcNBT = nbt.getCompoundTag("BC_Pipe_NBT");

		redstoneInput = 0;

		for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
			final String key = "redstoneInputSide[" + i + "]";
			if (bcNBT.hasKey(key)) {
				redstoneInputSide[i] = bcNBT.getByte(key);

				if (redstoneInputSide[i] > redstoneInput) {
					redstoneInput = redstoneInputSide[i];
				}
			} else {
				redstoneInputSide[i] = 0;
			}
		}

		pipeBound = true;

		pipe.readFromNBT(bcNBT);

		sideProperties.readFromNBT(bcNBT);
		attachPluggables = true;
	}

	@Override
	public void invalidate_LP() {
		this.invalidate();
	}

	@Override
	public void validate_LP() {
		this.validate();
	}

	@Override
	@SneakyThrows({NoSuchFieldException.class, SecurityException.class, IllegalArgumentException.class, IllegalAccessException.class, NoSuchMethodException.class, InvocationTargetException.class})
	public void updateEntity_LP() {
		//Make sure we still have the same TE values
		this.xCoord = lpPipe.xCoord;
		this.yCoord = lpPipe.yCoord;
		this.zCoord = lpPipe.zCoord;
		
		if (attachPluggables) {
			attachPluggables = false;
			// Attach callback
			PipePluggable[] pluggables = ReflectionHelper.getPrivateField(PipePluggable[].class, SideProperties.class, "pluggables", sideProperties);
			for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
				if (pluggables[i] != null) {
					pipe.eventBus.registerHandler(pluggables[i]);
					pluggables[i].onAttachedPipe(this, ForgeDirection.getOrientation(i));
				}
			}
			notifyBlockChanged();
		}

		if (!BlockGenericPipe.isValid(pipe)) {
			return;
		}

		pipe.updateEntity();

		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
			PipePluggable p = getPipePluggable(direction);
			if (p != null) {
				p.update(this, direction);
			}
		}

		if (worldObj.isRemote) {
			if (resyncGateExpansions) {
				ReflectionHelper.invokePrivateMethod(Object.class, TileGenericPipe.class, this, "syncGateExpansions", new Class[]{}, new Object[]{});
			}

			return;
		}

		if (blockNeighborChange) {
			ReflectionHelper.invokePrivateMethod(Object.class, TileGenericPipe.class, this, "computeConnections", new Class[]{}, new Object[]{});
			pipe.onNeighborBlockChange(0);
			blockNeighborChange = false;
			refreshRenderState = true;
		}

		if (refreshRenderState) {
			refreshRenderState();
			refreshRenderState = false;
		}
	}

	@Override
	public IBCRenderState getBCRenderState() {
		return bcRenderState;
	}

	@Override
	public IBCPipePart getBCPipePart() {
		return bcPipe;
	}

	@Override
	public IBCPluggableState getBCPlugableState() {
		return bcPlugState;
	}

	@Override
	public void readOldRedStone(NBTTagCompound nbt) {
		for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
			final String key = "redstoneInputSide[" + i + "]";
			if (nbt.hasKey(key)) {
				redstoneInputSide[i] = nbt.getByte(key);
				
				if (redstoneInputSide[i] > redstoneInput) {
					redstoneInput = redstoneInputSide[i];
				}
			} else {
				redstoneInputSide[i] = 0;
			}
		}
	}

	@Override
	public IBCPipePluggable getBCPipePluggable(final ForgeDirection sideHit) {
		final PipePluggable plug = this.getPipePluggable(sideHit);
		if(plug == null) return null;
		return new IBCPipePluggable() {
			@Override
			public ItemStack[] getDropItems(LogisticsTileGenericPipe container) {
				return plug.getDropItems(container);
			}

			@Override
			public boolean isBlocking() {
				return plug.isBlocking(pipe.container, sideHit);
			}

			@Override
			public Object getOriginal() {
				return plug;
			}

			@Override
			public void renderPluggable(RenderBlocks renderblocks, ForgeDirection dir, int renderPass, int x, int y, int z) {
				if(plug.getRenderer() == null) return;
				plug.getRenderer().renderPluggable(renderblocks, bcPipe, dir, plug, FakeBlock.INSTANCE, renderPass, x, y, z);
			}
		};
	}

	@Override
	public PipePluggable getPipePluggable(ForgeDirection side) {
		if(blockPluggableAccess) {
			StackTraceElement[] trace = Thread.currentThread().getStackTrace();
			if(trace.length > 2 && (trace[2].getMethodName().equals("onBlockActivated") || trace[2].getMethodName().equals("func_149727_a")) && trace[2].getClassName().equals("buildcraft.transport.BlockGenericPipe") && trace[2].getLineNumber() > 680) {
				return null;
			}
		}
		return super.getPipePluggable(side);
	}

	@Override
	public void disablePluggableAccess() {
		blockPluggableAccess = true;
	}

	@Override
	public void reenablePluggableAccess() {
		blockPluggableAccess = false;
	}

	@Override
	public void afterStateUpdated() {
		if(this.worldObj == null) this.worldObj = this.lpPipe.getWorldObj();
		this.afterStateUpdated((byte)2);
	}

	@Override
	public Object getOriginal() {
		return this;
	}

	@Override
	public Block getBlock(ForgeDirection to) {
		return lpPipe.getBlock(to);
	}

	@Override
	public TileEntity getTile(ForgeDirection to) {
		return lpPipe.getTile(to);
	}

	@Override
	public void sendUpdateToClient() {
		super.sendUpdateToClient();
		lpPipe.sendUpdateToClient();
	}

	@Override
	public void setWorldObj_LP(World world) {
		this.setWorldObj(world);
		this.xCoord = lpPipe.xCoord;
		this.yCoord = lpPipe.yCoord;
		this.zCoord = lpPipe.zCoord;
	}

	@Override
	public PipeType getPipeType() {
		StackTraceElement[] trace = Thread.currentThread().getStackTrace();
		if(trace.length > 2 && trace[2].getClassName().equals("buildcraft.transport.pluggable.ItemLens") && trace[2].getMethodName().equals("createPipePluggable")) {
			return PipeType.ITEM;
		}
		return PipeType.STRUCTURE;
	}

	@Override
	public boolean isPipeConnected(ForgeDirection with) {
		return lpPipe.isPipeConnected(with);
	}
}
