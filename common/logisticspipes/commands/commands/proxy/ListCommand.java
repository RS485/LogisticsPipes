package logisticspipes.commands.commands.proxy;

import logisticspipes.asm.LogisticsProxyHandler;
import logisticspipes.commands.abstracts.ICommandHandler;
import logisticspipes.commands.chathelper.MorePageDisplay;
import logisticspipes.proxy.interfaces.IProxyController;
import logisticspipes.utils.string.ChatColor;
import net.minecraft.command.ICommandSender;

public class ListCommand implements ICommandHandler {
	
	@Override
	public String[] getNames() {
		return new String[]{"list"};
	}
	
	@Override
	public boolean isCommandUsableBy(ICommandSender sender) {
		return true;
	}
	
	@Override
	public String[] getDescription() {
		return new String[]{"Lists all proxies and their status."};
	}
	
	@Override
	public void executeCommand(ICommandSender sender, String[] args) {
		MorePageDisplay display = new MorePageDisplay(new String[]{"|< Proxy status - Page: %/$ >|"}, sender);
		for(IProxyController controller:LogisticsProxyHandler.proxyController) {
			StringBuilder builder = new StringBuilder();
			builder.append(ChatColor.AQUA);
			builder.append(controller.getProxyName());
			builder.append("Proxy: ");
			if(controller.isEnabled()) {
				builder.append(ChatColor.GREEN);
				builder.append("enabled");
			} else {
				if(controller.getReason() == null) {
					builder.append(ChatColor.RED);
					builder.append("disabled (exception)");
				} else if(controller.getReason().equals("dummy")) {
					builder.append(ChatColor.GRAY);
					builder.append("disabled (mod not found)");
				} else {
					builder.append(ChatColor.BLUE);
					builder.append("disabled (" + controller.getReason() + ")");
				}
			}
			display.append(builder.toString());
		}
		display.display(sender);
	}
}
