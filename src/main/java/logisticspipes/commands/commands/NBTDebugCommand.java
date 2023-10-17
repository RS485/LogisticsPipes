package logisticspipes.commands.commands;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;

import logisticspipes.commands.LogisticsPipesCommand;
import logisticspipes.commands.abstracts.ICommandHandler;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.ActivateNBTDebug;
import logisticspipes.proxy.MainProxy;

public class NBTDebugCommand implements ICommandHandler {

	@Override
	public String[] getNames() {
		return new String[] { "nbtdebug" };
	}

	@Override
	public boolean isCommandUsableBy(ICommandSender sender) {
		return sender instanceof EntityPlayer && LogisticsPipesCommand.isOP(sender);
	}

	@Override
	public String[] getDescription() {
		return new String[] { "Enables the Hotkey to show an debug gui", "for the howered item. (Only if NEI is installed)" };
	}

	@Override
	public void executeCommand(ICommandSender sender, String[] args) {
		sender.sendMessage(new TextComponentString("Trying to Enable NBTDebug"));
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(ActivateNBTDebug.class), (EntityPlayer) sender);
	}
}
