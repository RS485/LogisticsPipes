package logisticspipes.commands.commands;

import logisticspipes.commands.abstracts.SubCommandHandler;
import logisticspipes.commands.commands.proxy.ListCommand;
import net.minecraft.command.ICommandSender;

public class ProxyCommand extends SubCommandHandler {
	
	@Override
	public String[] getNames() {
		return new String[]{"proxy"};
	}

	@Override
	public boolean isCommandUsableBy(ICommandSender sender) {
		return true;
	}

	@Override
	public String[] getDescription() {
		return new String[]{"proxy controll commands"};
	}

	@Override
	public void registerSubCommands() {
		this.registerSubCommand(new ListCommand());
	}
}
