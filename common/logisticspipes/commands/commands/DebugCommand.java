package logisticspipes.commands.commands;

import java.util.Arrays;

import logisticspipes.LogisticsPipes;
import logisticspipes.commands.abstracts.SubCommandHandler;
import logisticspipes.commands.commands.debug.CloseCommand;
import logisticspipes.commands.commands.debug.HandCommand;
import logisticspipes.commands.commands.debug.MeCommand;
import logisticspipes.commands.commands.debug.PipeCommand;
import logisticspipes.commands.commands.debug.RoutingTableCommand;
import logisticspipes.commands.commands.debug.TargetCommand;
import net.minecraft.command.ICommandSender;

public class DebugCommand extends SubCommandHandler {

	private static final String[] allowedPlayers = new String[]{"davboecki", "theZorro266"};
	
	@Override
	public String[] getNames() {
		return new String[]{"debug"};
	}

	@Override
	public boolean isCommandUsableBy(ICommandSender sender) {
		return LogisticsPipes.DEBUG || Arrays.asList(allowedPlayers).contains(sender.getCommandSenderName());
	}

	@Override
	public String[] getDescription() {
		return new String[]{"Debug helper commands"};
	}

	@Override
	public void registerSubCommands() {
		this.registerSubCommand(new MeCommand());
		this.registerSubCommand(new TargetCommand());
		this.registerSubCommand(new CloseCommand());
		this.registerSubCommand(new RoutingTableCommand());
		this.registerSubCommand(new PipeCommand());
		this.registerSubCommand(new HandCommand());
	}
}
