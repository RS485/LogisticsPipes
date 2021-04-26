package logisticspipes.commands.commands;

import java.lang.reflect.Method;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.TextComponentString;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlinx.coroutines.Job;

import logisticspipes.commands.abstracts.ICommandHandler;
import logisticspipes.utils.string.ChatColor;

public class TestCommand implements ICommandHandler {

	@Override
	public String[] getNames() {
		return new String[] { "retest" };
	}

	@Override
	public boolean isCommandUsableBy(ICommandSender sender) {
		return true;
	}

	@Override
	public String[] getDescription() {
		return new String[] { "Reruns all tests" };
	}

	@Override
	public void executeCommand(ICommandSender sender, String[] args) {
		final Class<?> testClass;
		try {
			testClass = Class.forName("network.rs485.logisticspipes.integration.MinecraftTest");
		} catch (ReflectiveOperationException e) {
			sender.sendMessage(new TextComponentString(ChatColor.RED + "Error loading minecraft test class " + e));
			return;
		}
		final Object minecraftTestInstance;
		try {
			minecraftTestInstance = testClass.getDeclaredField("INSTANCE").get(null);
			final Method startTestsMethod = testClass.getDeclaredMethod("startTests", Function1.class);
			final Job job = (Job) startTestsMethod.invoke(minecraftTestInstance, (Function1<Object, Unit>) msg -> {
				sender.sendMessage(new TextComponentString(String.valueOf(msg)));
				return Unit.INSTANCE;
			});
			job.invokeOnCompletion(throwable -> {
				if (throwable == null) {
					sender.sendMessage(new TextComponentString(ChatColor.GREEN + "SUCCESS"));
				} else {
					sender.sendMessage(new TextComponentString(ChatColor.RED + "Tests failed with: " + throwable));
				}
				return Unit.INSTANCE;
			});
		} catch (ReflectiveOperationException | ClassCastException e) {
			sender.sendMessage(new TextComponentString(ChatColor.RED + "Error accessing minecraft test instance " + e));
		}
	}
}
