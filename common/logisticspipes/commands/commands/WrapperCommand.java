package logisticspipes.commands.commands;

import net.minecraft.command.ICommandSender;

import logisticspipes.commands.abstracts.SubCommandHandler;
import logisticspipes.commands.commands.wrapper.EnableCommand;
import logisticspipes.commands.commands.wrapper.ListCommand;
import logisticspipes.commands.commands.wrapper.ShowCommand;

public class WrapperCommand extends SubCommandHandler {

	@Override
	public String[] getNames() {
		return new String[] { "wrapper" };
	}

	@Override
	public boolean isCommandUsableBy(ICommandSender sender) {
		return true;
	}

	@Override
	public String[] getDescription() {
		return new String[] { "wrapper control commands" };
	}

	@Override
	public void registerSubCommands() {
		registerSubCommand(new ListCommand());
		registerSubCommand(new EnableCommand());
		registerSubCommand(new ShowCommand());
	}
}
