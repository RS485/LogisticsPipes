package logisticspipes.asm.bc;

import java.util.logging.Level;

import cpw.mods.fml.common.network.PacketDispatcher;

import logisticspipes.logisticspipes.IRoutedItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.BuildCraftTransport;
import buildcraft.core.DefaultProps;
import buildcraft.core.utils.BCLog;
import buildcraft.core.utils.BlockUtil;
import buildcraft.core.utils.MathUtils;
import buildcraft.transport.IPipeTransportItemsHook;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TravelingItem;
import buildcraft.transport.pipes.events.PipeEventItem;
import buildcraft.transport.utils.TransportUtils;

public class InjectItemHook {
	public static void handleInjectItem(PipeTransportItems pipe, TravelingItem item, ForgeDirection inputOrientation) {
		if (item.isCorrupted())
			// Safe guard - if for any reason the item is corrupted at this
			// stage, avoid adding it to the pipe to avoid further exceptions.
			return;

		item.reset();
		item.input = inputOrientation;

		pipe.readjustSpeed(item);
		readjustPosition(pipe, item);


		if (!pipe.container.worldObj.isRemote) {
			item.output = pipe.resolveDestination(item);
		}

		if (pipe.container.pipe instanceof IPipeTransportItemsHook) {
			((IPipeTransportItemsHook) pipe.container.pipe).entityEntered(item, inputOrientation);
		}

		PipeEventItem.Entered event = new PipeEventItem.Entered(item);
		pipe.container.pipe.handlePipeEvent(event);
		if (event.cancelled)
			return;

		pipe.items.scheduleAdd(item);

		if (!pipe.container.worldObj.isRemote) {
			sendItemPacket(pipe, item);

			int stackCount = 0;
			int numItems = 0;
			for (TravelingItem travellingItem : pipe.items) {
				if(!(travellingItem instanceof IRoutedItem)) {
					ItemStack stack = travellingItem.getItemStack();
					if (stack != null && stack.stackSize > 0) {
						numItems += stack.stackSize;
						stackCount++;
					}
				}
			}

			if (stackCount > BuildCraftTransport.groupItemsTrigger) {
				pipe.groupEntities();
			}

			stackCount = 0;
			numItems = 0;
			for (TravelingItem travellingItem : pipe.items) {
				if(!(travellingItem instanceof IRoutedItem)) {
					ItemStack stack = travellingItem.getItemStack();
					if (stack != null && stack.stackSize > 0) {
						numItems += stack.stackSize;
						stackCount++;
					}
				}
			}

			if (stackCount > PipeTransportItems.MAX_PIPE_STACKS) {
				BCLog.logger.log(Level.WARNING, String.format("Pipe exploded at %d,%d,%d because it had too many stacks: %d", pipe.container.xCoord, pipe.container.yCoord, pipe.container.zCoord, stackCount));
				destroyPipe(pipe);
				return;
			}

			if (numItems > PipeTransportItems.MAX_PIPE_ITEMS) {
				BCLog.logger.log(Level.WARNING, String.format("Pipe exploded at %d,%d,%d because it had too many items: %d", pipe.container.xCoord, pipe.container.yCoord, pipe.container.zCoord, numItems));
				destroyPipe(pipe);
			}
		}
	}

	private static void destroyPipe(PipeTransportItems pipe) {
		BlockUtil.explodeBlock(pipe.container.worldObj, pipe.container.xCoord, pipe.container.yCoord, pipe.container.zCoord);
		pipe.container.worldObj.setBlockToAir(pipe.container.xCoord, pipe.container.yCoord, pipe.container.zCoord);
	}

	private static void sendItemPacket(PipeTransportItems pipe, TravelingItem data) {
		int dimension = pipe.container.worldObj.provider.dimensionId;
		PacketDispatcher.sendPacketToAllAround(pipe.container.xCoord, pipe.container.yCoord, pipe.container.zCoord, DefaultProps.PIPE_CONTENTS_RENDER_DIST, dimension, pipe.createItemPacket(data));
	}

	private static void readjustPosition(PipeTransportItems pipe, TravelingItem item) {
		double x = MathUtils.clamp(item.xCoord, pipe.container.xCoord + 0.01, pipe.container.xCoord + 0.99);
		double y = MathUtils.clamp(item.yCoord, pipe.container.yCoord + 0.01, pipe.container.yCoord + 0.99);
		double z = MathUtils.clamp(item.zCoord, pipe.container.zCoord + 0.01, pipe.container.zCoord + 0.99);

		if (item.input != ForgeDirection.UP && item.input != ForgeDirection.DOWN) {
			y = pipe.container.yCoord + TransportUtils.getPipeFloorOf(item.getItemStack());
		}

		item.setPosition(x, y, z);
	}
}
