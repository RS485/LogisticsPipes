package logisticspipes.commands.commands;

import logisticspipes.LogisticsPipes;
import logisticspipes.commands.abstracts.ICommandHandler;
import logisticspipes.ticks.VersionChecker;
import net.minecraft.command.ICommandSender;
import cpw.mods.fml.common.Mod;

public class VersionCommand implements ICommandHandler {
	
	@Override
	public String[] getNames() {
		return new String[]{"version", "v"};
	}
	
	@Override
	public boolean isCommandUsableBy(ICommandSender sender) {
		return true;
	}
	
	@Override
	public String[] getDescription() {
		return new String[]{"Display the used LP version"};
	}
	
	@Override
	public void executeCommand(ICommandSender sender, String[] args) {
		sender.sendChatToPlayer(ChatMessageComponent.createFromText(String.format("LogisticsPipes %s for Minecraft %s.", LogisticsPipes.class.getAnnotation(Mod.class).version(), LogisticsPipes.MCVersion)));
    	if(VersionChecker.hasNewVersion) {
    		sender.sendChatToPlayer(ChatMessageComponent.createFromText("Your LogisticsPipes version is outdated. The newest version is #" + VersionChecker.newVersion + "."));
    		sender.sendChatToPlayer(ChatMessageComponent.createFromText("Use \"/logisticspipes changelog\" to see a changelog."));
        }
	}
}
