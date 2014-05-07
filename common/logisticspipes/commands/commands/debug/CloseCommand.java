package logisticspipes.commands.commands.debug;

import logisticspipes.commands.abstracts.ICommandHandler;
import logisticspipes.ticks.DebugGuiTickHandler;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

public class CloseCommand implements ICommandHandler {
	
	@Override
	public String[] getNames() {
		return new String[]{"close"};
	}
	
	@Override
	public boolean isCommandUsableBy(ICommandSender sender) {
		return sender instanceof EntityPlayer;
	}
	
	@Override
	public String[] getDescription() {
		return new String[]{"Stops checking the currently opened", "Object for changes"};
	}
	
	@Override
	public void executeCommand(ICommandSender sender, String[] args) {
		DebugGuiTickHandler.instance().closeWatchingFrom((EntityPlayer) sender);
	}
}
