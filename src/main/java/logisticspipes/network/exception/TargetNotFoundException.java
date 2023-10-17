package logisticspipes.network.exception;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import logisticspipes.LogisticsPipes;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.ModernPacket;

public class TargetNotFoundException extends DelayPacketException {

	private static final long serialVersionUID = 8830733712757259675L;

	public TargetNotFoundException(String message, Object causedIn) {
		super(message);
		if (!LogisticsPipes.isDEBUG()) {
			return;
		}
		if (causedIn instanceof ModernPacket) {
			StackTraceElement[] astacktraceelement = PacketHandler.debugMap.get(((ModernPacket) causedIn).getDebugId());
			if (astacktraceelement != null) {
				List<StackTraceElement> list = new ArrayList<>();
				for (StackTraceElement element : getStackTrace()) {
					list.add(element);
					if (element.getClassName().equals("logisticspipes.network.PacketHandler")) {
						break;
					}
				}
				list.remove(0);
				setStackTrace(list.toArray(new StackTraceElement[0]));
				list = new ArrayList<>(Arrays.asList(astacktraceelement));
				if (list.size() > 2) {
					list.remove(0);
					list.remove(0);
				}
				RuntimeException runtime = new RuntimeException("Packet was send from: ");
				runtime.setStackTrace(list.toArray(new StackTraceElement[0]));
				initCause(runtime);
			}
		}
	}
}
