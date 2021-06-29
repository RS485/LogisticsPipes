package logisticspipes.commands.commands;

import java.util.Arrays;

import net.minecraft.command.ICommandSender;

import logisticspipes.LogisticsPipes;
import logisticspipes.commands.abstracts.SubCommandHandler;
import logisticspipes.commands.commands.debug.HandCommand;
import logisticspipes.commands.commands.debug.MeCommand;
import logisticspipes.commands.commands.debug.PipeCommand;
import logisticspipes.commands.commands.debug.RoutingTableCommand;
import logisticspipes.commands.commands.debug.TargetCommand;

public class DebugCommand extends SubCommandHandler {

	private static final String[] allowedPlayers = new String[] { "davboecki", "theZorro266" };

	public DebugCommand() {
		registerSubCommand(new MeCommand());
		registerSubCommand(new TargetCommand());
		registerSubCommand(new RoutingTableCommand());
		registerSubCommand(new PipeCommand());
		registerSubCommand(new HandCommand());
	}

	@Override
	public String[] getNames() {
		return new String[] { "debug" };
	}

	@Override
	public boolean isCommandUsableBy(ICommandSender sender) {
		return LogisticsPipes.isDEBUG() || Arrays.asList(DebugCommand.allowedPlayers).contains(sender.getName());
	}

	@Override
	public String[] getDescription() {
		return new String[] { "Debug helper commands" };
	}
}
