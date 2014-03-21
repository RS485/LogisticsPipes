package logisticspipes.commands.commands;

import java.util.Arrays;

import logisticspipes.LogisticsPipes;
import logisticspipes.commands.abstracts.SubCommandHandler;
import logisticspipes.commands.commands.debug.CloseCommand;
import logisticspipes.commands.commands.debug.MeCommand;
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
		this.registerSubCommands(new MeCommand());
		this.registerSubCommands(new TargetCommand());
		this.registerSubCommands(new CloseCommand());
	}
}
