package logisticspipes.commands.commands;

import logisticspipes.commands.abstracts.SubCommandHandler;
import logisticspipes.commands.commands.wrapper.ListCommand;
import net.minecraft.command.ICommandSender;

public class WrapperCommand extends SubCommandHandler {
	
	@Override
	public String[] getNames() {
		return new String[]{"wrapper"};
	}

	@Override
	public boolean isCommandUsableBy(ICommandSender sender) {
		return true;
	}

	@Override
	public String[] getDescription() {
		return new String[]{"wrapper controll commands"};
	}

	@Override
	public void registerSubCommands() {
		this.registerSubCommand(new ListCommand());
	}
}
