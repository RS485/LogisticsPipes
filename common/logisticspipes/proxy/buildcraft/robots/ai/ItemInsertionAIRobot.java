package logisticspipes.proxy.buildcraft.robots.ai;

import java.util.Iterator;

import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.buildcraft.robots.boards.LogisticsRoutingBoardRobot;
import logisticspipes.transport.LPTravelingItem;
import logisticspipes.transport.LPTravelingItem.LPTravelingItemServer;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;

public class ItemInsertionAIRobot extends AIRobot {

	private LogisticsTileGenericPipe pipe;
	private LogisticsRoutingBoardRobot board;
	private ForgeDirection insertion;
	private IInventoryUtil robotInv;
	private int tick = 0;

	public ItemInsertionAIRobot(EntityRobotBase iRobot, LogisticsTileGenericPipe pipe, LogisticsRoutingBoardRobot board, ForgeDirection insertion) {
		super(iRobot);
		this.pipe = pipe;
		this.board = board;
		this.insertion = insertion;
	}

	@Override
	public void start() {
		if (pipe == null || board == null) {
			terminate();
			return;
		}
		robotInv = SimpleServiceLocator.inventoryUtilFactory.getInventoryUtil(robot, ForgeDirection.UNKNOWN);
		if (robotInv == null) {
			terminate();
		}
	}

	@Override
	public void update() {
		if (tick++ % 2 == 0) {
			Iterator<LPTravelingItemServer> iter = board.getItems().iterator();
			if (iter.hasNext()) {
				LPTravelingItemServer item = iter.next();
				LPTravelingItem.clientSideKnownIDs.set(item.getId(), false);
				pipe.pipe.transport.injectItem(item, insertion);
				robotInv.getMultipleItems(item.getItemIdentifierStack().getItem(), item.getItemIdentifierStack().getStackSize()); // We clear the Inv at the end so we don't care if this fails
				iter.remove();
			} else {
				terminate();
			}
		}
	}

	@Override
	public boolean success() {
		return board.getItems().isEmpty();
	}
}
