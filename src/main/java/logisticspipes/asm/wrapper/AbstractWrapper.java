package logisticspipes.asm.wrapper;

import java.util.List;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import logisticspipes.LogisticsPipes;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.string.ChatColor;

public abstract class AbstractWrapper {

	@Getter
	@Setter(value = AccessLevel.PACKAGE)
	protected WrapperState state = WrapperState.Enabled;
	@Getter
	@Setter(value = AccessLevel.PACKAGE)
	private Throwable reason;
	@Getter
	@Setter(value = AccessLevel.PACKAGE)
	private String modId;
	@Getter
	@Setter(value = AccessLevel.PACKAGE)
	private List<Class<?>> wrapperInterfaces;

	@SneakyThrows(Throwable.class)
	public void handleException(Throwable e) {
		if (!isEnabled()) {
			if (LogisticsPipes.isDEBUG()) {
				e.printStackTrace();
			}
			return;
		}
		if (LogisticsPipes.isDEBUG()) {
			throw e;
		}
		e.printStackTrace();
		state = WrapperState.Exception;
		reason = e;
		String message = "Disabled " + getName() + getTypeName() + (modId != null ? (" for Mod: " + modId) : "") + ". Cause was an Exception";
		LogisticsPipes.log.fatal(message);
		MainProxy.proxy.sendBroadCast(ChatColor.RED + message);
	}

	public void reEnable() {
		if (state != WrapperState.Exception) {
			return;
		}
		state = WrapperState.Enabled;
		reason = null;
	}

	protected boolean isEnabled() {
		return state == WrapperState.Enabled;
	}

	protected boolean canTryAnyway() {
		return state != WrapperState.ModMissing;
	}

	public abstract String getName();

	public abstract String getTypeName();

}
