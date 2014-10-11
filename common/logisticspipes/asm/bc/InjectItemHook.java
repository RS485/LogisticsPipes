package logisticspipes.asm.bc;

import java.util.logging.Level;

import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.buildcraft.LPRoutedBCTravelingItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.BuildCraftTransport;
import buildcraft.api.core.BCLog;
import buildcraft.core.DefaultProps;
import buildcraft.core.utils.BlockUtil;
import buildcraft.core.utils.MathUtils;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TravelingItem;
import buildcraft.transport.network.PacketPipeTransportTraveler;
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


		if (!pipe.container.getWorldObj().isRemote) {
			item.output = pipe.resolveDestination(item);
		}

		PipeEventItem.Entered event = new PipeEventItem.Entered(item);
		pipe.container.pipe.handlePipeEvent(event);
		if (event.cancelled)
			return;

		pipe.items.add(item);

		if (!pipe.container.getWorldObj().isRemote) {
			sendTravelerPacket(pipe, item, false);

			int stackCount = 0;
			int numItems = 0;
			for (TravelingItem travellingItem : pipe.items) {
				if(!(travellingItem instanceof LPRoutedBCTravelingItem)) {
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
				if(!(travellingItem instanceof LPRoutedBCTravelingItem)) {
					ItemStack stack = travellingItem.getItemStack();
					if (stack != null && stack.stackSize > 0) {
						numItems += stack.stackSize;
						stackCount++;
					}
				}
			}

			if (stackCount > PipeTransportItems.MAX_PIPE_STACKS) {
				SimpleServiceLocator.buildCraftProxy.logWarning(String.format("Pipe exploded at %d,%d,%d because it had too many stacks: %d", pipe.container.xCoord, pipe.container.yCoord, pipe.container.zCoord, stackCount));
				destroyPipe(pipe);
				return;
			}

			if (numItems > PipeTransportItems.MAX_PIPE_ITEMS) {
				SimpleServiceLocator.buildCraftProxy.logWarning(String.format("Pipe exploded at %d,%d,%d because it had too many items: %d", pipe.container.xCoord, pipe.container.yCoord, pipe.container.zCoord, numItems));
				destroyPipe(pipe);
			}
		}
	}

	private static void destroyPipe(PipeTransportItems pipe) {
		BlockUtil.explodeBlock(pipe.container.getWorldObj(), pipe.container.xCoord, pipe.container.yCoord, pipe.container.zCoord);
		pipe.container.getWorldObj().setBlockToAir(pipe.container.xCoord, pipe.container.yCoord, pipe.container.zCoord);
	}

	private static void sendTravelerPacket(PipeTransportItems pipe, TravelingItem data, boolean forceStackRefresh) {
		PacketPipeTransportTraveler packet = new PacketPipeTransportTraveler(data, forceStackRefresh);
		BuildCraftTransport.instance.sendToPlayers(packet, pipe.container.getWorldObj(), pipe.container.xCoord, pipe.container.yCoord, pipe.container.zCoord, DefaultProps.PIPE_CONTENTS_RENDER_DIST);
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
