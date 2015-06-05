package logisticspipes.commands.commands.wrapper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import logisticspipes.asm.wrapper.AbstractWrapper;
import logisticspipes.asm.wrapper.LogisticsWrapperHandler;
import logisticspipes.asm.wrapper.WrapperState;
import logisticspipes.commands.LogisticsPipesCommand;
import logisticspipes.commands.abstracts.ICommandHandler;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

public class ShowCommand implements ICommandHandler {

	@Override
	public String[] getNames() {
		return new String[] { "show" };
	}

	@Override
	public boolean isCommandUsableBy(ICommandSender sender) {
		return LogisticsPipesCommand.isOP(sender);
	}

	@Override
	public String[] getDescription() {
		return new String[] { "Reenables the givven proxy", "if it was disabled by an exception." };
	}

	@Override
	public void executeCommand(ICommandSender sender, String[] args) {
		if (args.length != 1) {
			sender.addChatMessage(new ChatComponentText("Wrong amount of arguments"));
			return;
		}
		String name = args[0];
		List<AbstractWrapper> list = new ArrayList<AbstractWrapper>(LogisticsWrapperHandler.wrapperController);
		Iterator<AbstractWrapper> iter = list.iterator();
		while (iter.hasNext()) {
			AbstractWrapper item = iter.next();
			if (item.getState() != WrapperState.Exception) {
				iter.remove();
			}
		}
		iter = list.iterator();
		while (iter.hasNext()) {
			AbstractWrapper item = iter.next();
			if (!(item.getName() + item.getTypeName()).startsWith(name)) {
				iter.remove();
			}
		}
		if (list.size() > 1) {
			sender.addChatMessage(new ChatComponentText("Possible: "));
			for (AbstractWrapper can : list) {
				sender.addChatMessage(new ChatComponentText(can.getName() + can.getTypeName()));
			}
		} else if (list.isEmpty()) {
			sender.addChatMessage(new ChatComponentText("No match found"));
		} else {
			AbstractWrapper wrapper = list.get(0);
			Throwable t = wrapper.getReason();
			if (t == null) {
				sender.addChatMessage(new ChatComponentText("null"));
				return;
			} else {
				sender.addChatMessage(new ChatComponentText("-----------------------------------------------------"));
				sender.addChatMessage(new ChatComponentText(t.toString()));
				for (StackTraceElement s : t.getStackTrace()) {
					sender.addChatMessage(new ChatComponentText(" " + s.toString()));
				}
				t.printStackTrace();
			}
		}
	}
}
