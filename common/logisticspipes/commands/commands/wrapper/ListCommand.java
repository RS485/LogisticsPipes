package logisticspipes.commands.commands.wrapper;

import net.minecraft.command.ICommandSender;

import logisticspipes.asm.wrapper.AbstractWrapper;
import logisticspipes.asm.wrapper.LogisticsWrapperHandler;
import logisticspipes.asm.wrapper.WrapperState;
import logisticspipes.commands.abstracts.ICommandHandler;
import logisticspipes.commands.chathelper.MorePageDisplay;
import logisticspipes.utils.string.ChatColor;

public class ListCommand implements ICommandHandler {

	@Override
	public String[] getNames() {
		return new String[] { "list" };
	}

	@Override
	public boolean isCommandUsableBy(ICommandSender sender) {
		return true;
	}

	@Override
	public String[] getDescription() {
		return new String[] { "Lists all wrapper and their status." };
	}

	@Override
	public void executeCommand(ICommandSender sender, String[] args) {
		MorePageDisplay display = new MorePageDisplay(new String[] { "|< Wrapper status - Page: %/$ >|" }, sender);
		for (AbstractWrapper controller : LogisticsWrapperHandler.wrapperController) {
			StringBuilder builder = new StringBuilder();
			builder.append(ChatColor.AQUA);
			builder.append(controller.getName());
			builder.append(controller.getTypeName());
			builder.append(": ");
			if (controller.getState() == WrapperState.Enabled) {
				builder.append(ChatColor.GREEN);
				builder.append("enabled");
			} else if (controller.getState() == WrapperState.ModMissing) {
				builder.append(ChatColor.GRAY);
				builder.append("disabled (mod not found)");
			} else if (controller.getState() == WrapperState.Exception) {
				builder.append(ChatColor.RED);
				builder.append("disabled (exception)");
			} else {
				builder.append(ChatColor.BLUE);
				builder.append("disabled (").append(controller.getReason()).append(")");
			}
			display.append(builder.toString());
		}
		display.display(sender);
	}
}
