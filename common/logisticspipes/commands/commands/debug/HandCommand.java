package logisticspipes.commands.commands.debug;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentString;

import logisticspipes.commands.abstracts.ICommandHandler;

public class HandCommand implements ICommandHandler {

	@Override
	public String[] getNames() {
		return new String[] { "hand" };
	}

	@Override
	public boolean isCommandUsableBy(ICommandSender sender) {
		return sender instanceof EntityPlayer;
	}

	@Override
	public String[] getDescription() {
		return new String[] { "Start debugging the selected ItemStack" };
	}

	@Override
	public void executeCommand(ICommandSender sender, String[] args) {
		EntityPlayer player = (EntityPlayer) sender;
		ItemStack item = player.inventory.mainInventory.get(player.inventory.currentItem);
		if (!item.isEmpty()) {
			DebugGuiController.instance().startWatchingOf(item, player);
			sender.sendMessage(new TextComponentString("Starting HandDebuging"));
		}
	}
}
