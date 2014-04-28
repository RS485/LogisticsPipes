package logisticspipes.commands.commands;

import logisticspipes.Configs;
import logisticspipes.LogisticsPipes;
import logisticspipes.commands.abstracts.ICommandHandler;
import logisticspipes.proxy.MainProxy;
import logisticspipes.ticks.Watchdog;
import net.minecraft.command.ICommandSender;

public class WatchCommand implements ICommandHandler {
	
	@Override
	public String[] getNames() {
		return new String[]{"watch"};
	}
	
	@Override
	public boolean isCommandUsableBy(ICommandSender sender) {
		return LogisticsPipes.DEBUG;
	}
	
	@Override
	public String[] getDescription() {
		return new String[]{"Enables the Watchdog", "even if it was disabled inside the config"};
	}
	
	@Override
	public void executeCommand(ICommandSender sender, String[] args) {
		new Watchdog(MainProxy.proxy.getSide().equals("Client"));
		LogisticsPipes.WATCHDOG = true;
		Configs.WATCHDOG_CLIENT = true;
		Configs.WATCHDOG_SERVER = true;
    	sender.sendChatToPlayer(ChatMessageComponent.createFromText("Starting Watchdog"));
	}
}
