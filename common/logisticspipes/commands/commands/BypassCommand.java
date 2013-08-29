package logisticspipes.commands.commands;

import logisticspipes.blocks.LogisticsSecurityTileEntity;
import logisticspipes.commands.LogisticsPipesCommand;
import logisticspipes.commands.abstracts.ICommandHandler;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatMessageComponent;

public class BypassCommand implements ICommandHandler {
	
	@Override
	public String[] getNames() {
		return new String[]{"bypass", "bp"};
	}
	
	@Override
	public boolean isCommandUsableBy(ICommandSender sender) {
		return sender instanceof EntityPlayer && LogisticsPipesCommand.isOP(sender);
	}
	
	@Override
	public String[] getDescription() {
		return new String[]{"Allows to enable/disable the", "security station bypass token"};
	}
	
	@Override
	public void executeCommand(ICommandSender sender, String[] args) {
		if(!LogisticsSecurityTileEntity.byPassed.contains((EntityPlayer) sender)) {
    		LogisticsSecurityTileEntity.byPassed.add((EntityPlayer) sender);
    		sender.sendChatToPlayer(ChatMessageComponent.func_111066_d("Enabled"));
    	} else {
    		LogisticsSecurityTileEntity.byPassed.remove((EntityPlayer) sender);
    		sender.sendChatToPlayer(ChatMessageComponent.func_111066_d("Disabled"));
    	}
	}
}
