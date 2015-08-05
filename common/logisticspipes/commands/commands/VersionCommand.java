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
		return new String[] { "Display the used LP version", "and shows, if an update is available" };
	}

	@Override
	public void executeCommand(ICommandSender sender, String[] args) {
		sender.addChatMessage(new ChatComponentText(String.format("LogisticsPipes %s for Minecraft %s.", LogisticsPipes.class.getAnnotation(Mod.class).version(), LPConstants.MCVersion)));

		VersionChecker versionChecker = LogisticsPipes.versionChecker;
		sender.addChatMessage(new ChatComponentText(versionChecker.getVersionCheckerStatus()));

		if (versionChecker.isVersionCheckDone() && versionChecker.getVersionInfo().isNewVersionAvailable()) {
			sender.addChatMessage(new ChatComponentText("Use \"/logisticspipes changelog\" to see a changelog."));
		}
	}
}
