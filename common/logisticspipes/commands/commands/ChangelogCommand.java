package logisticspipes.commands.commands;

import logisticspipes.LogisticsPipes;
import logisticspipes.commands.abstracts.ICommandHandler;
import logisticspipes.commands.chathelper.MorePageDisplay;
import logisticspipes.ticks.VersionChecker;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

public class ChangelogCommand implements ICommandHandler {

	@Override
	public String[] getNames() {
		return new String[] { "changelog" };
	}

	@Override
	public boolean isCommandUsableBy(ICommandSender sender) {
		return true;
	}

	@Override
	public String[] getDescription() {
		return new String[] { "Display the changelog between this version", "and the newest one" };
	}

	@Override
	public void executeCommand(ICommandSender sender, String[] args) {
		VersionChecker versionChecker = LogisticsPipes.versionChecker;
		String statusMessage = versionChecker.getVersionCheckerStatus();

		if (versionChecker.isVersionCheckDone() && versionChecker.getVersionInfo().isNewVersionAvailable()) {
			VersionChecker.VersionInfo versionInfo = versionChecker.getVersionInfo();

			MorePageDisplay display = new MorePageDisplay(new String[] { "(The newest version is #" + versionInfo.getNewestBuild() + ")", "< Changelog Page %/$ >" }, sender);
			if (versionInfo.getChangelog().isEmpty()) {
				display.append("No commits since your version.");
			} else {
				for (String commit : versionInfo.getChangelog()) {
					display.append(commit);
				}
			}
			display.display(sender);
		} else {
			sender.addChatMessage(new ChatComponentText(statusMessage));
		}
	}
}
