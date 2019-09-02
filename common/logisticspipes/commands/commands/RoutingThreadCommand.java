package logisticspipes.commands.commands;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.TextComponentString;

import logisticspipes.commands.abstracts.ICommandHandler;
import logisticspipes.ticks.RoutingTableUpdateThread;

public class RoutingThreadCommand implements ICommandHandler {

	@Override
	public String[] getNames() {
		return new String[] { "routingthread", "rt" };
	}

	@Override
	public boolean isCommandUsableBy(ICommandSender sender) {
		return true;
	}

	@Override
	public String[] getDescription() {
		return new String[] { "Display Routing thread status information" };
	}

	@Override
	public void executeCommand(ICommandSender sender, String[] args) {
		sender.sendMessage(new TextComponentString("RoutingTableUpdateThread: Queued: " + RoutingTableUpdateThread.size()));
		sender.sendMessage(new TextComponentString("RoutingTableUpdateThread: Average: " + RoutingTableUpdateThread.getAverage() + "ns"));
	}
}
