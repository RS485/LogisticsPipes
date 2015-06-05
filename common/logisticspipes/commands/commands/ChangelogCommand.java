package logisticspipes.commands.commands;

import logisticspipes.commands.abstracts.ICommandHandler;
import logisticspipes.commands.chathelper.MorePageDisplay;
import logisticspipes.ticks.VersionChecker;

import net.minecraft.command.ICommandSender;

public class ChangelogCommand implements ICommandHandler {

	@Override
	public String[] getNames() {
		return new String[] { "changelog" };
	}

	@Override
	public boolean isCommandUsableBy(ICommandSender sender) {
		return VersionChecker.hasNewVersion;
	}

	@Override
	public String[] getDescription() {
		return new String[] { "Display the changelog between this version", "and the newest one" };
	}

	@Override
	public void executeCommand(ICommandSender sender, String[] args) {
		MorePageDisplay display = new MorePageDisplay(new String[] { "(The newest version is #" + VersionChecker.newVersion + ")", "< Changelog Page %/$ >" }, sender);
		for (String msg : VersionChecker.changeLog) {
			display.append(msg);
		}
		display.display(sender);
	}
}
