package logisticspipes.commands.commands.debug;

import logisticspipes.commands.abstracts.ICommandHandler;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.debuggui.DebugAskForTarget;
import logisticspipes.proxy.MainProxy;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;

public class TargetCommand implements ICommandHandler {

	@Override
	public String[] getNames() {
		return new String[] { "target", "look", "watch" };
	}

	@Override
	public boolean isCommandUsableBy(ICommandSender sender) {
		return sender instanceof EntityPlayer;
	}

	@Override
	public String[] getDescription() {
		return new String[] { "Starts debugging the TileEntity", "or Entitiy you are currently looking at." };
	}

	@Override
	public void executeCommand(ICommandSender sender, String[] args) {
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(DebugAskForTarget.class), (EntityPlayer) sender);
		sender.addChatMessage(new ChatComponentText("Asking for Target."));
	}
}
