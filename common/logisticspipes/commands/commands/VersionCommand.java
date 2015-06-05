package logisticspipes.commands.commands;

import logisticspipes.LPConstants;
import logisticspipes.LogisticsPipes;
import logisticspipes.commands.abstracts.ICommandHandler;
import logisticspipes.ticks.VersionChecker;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

import cpw.mods.fml.common.Mod;

public class VersionCommand implements ICommandHandler {

	@Override
	public String[] getNames() {
		return new String[] { "version", "v" };
	}

	@Override
	public boolean isCommandUsableBy(ICommandSender sender) {
		return true;
	}

	@Override
	public String[] getDescription() {
		return new String[] { "Display the used LP version" };
	}

	@Override
	public void executeCommand(ICommandSender sender, String[] args) {
		sender.addChatMessage(new ChatComponentText(String.format("LogisticsPipes %s for Minecraft %s.", LogisticsPipes.class.getAnnotation(Mod.class).version(), LPConstants.MCVersion)));
		if (VersionChecker.hasNewVersion) {
			sender.addChatMessage(new ChatComponentText("Your LogisticsPipes version is outdated. The newest version is #" + VersionChecker.newVersion + "."));
			sender.addChatMessage(new ChatComponentText("Use \"/logisticspipes changelog\" to see a changelog."));
		}
	}
}
