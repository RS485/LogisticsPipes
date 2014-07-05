package logisticspipes.network.exception;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import logisticspipes.LogisticsPipes;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.ModernPacket;


public class TargetNotFoundException extends RuntimeException {

	private static final long	serialVersionUID	= 8830733712757259675L;

	public TargetNotFoundException(String message, ModernPacket packet) {
		super(message);
		if(!LogisticsPipes.DEBUG) return;
		StackTraceElement[] astacktraceelement = PacketHandler.debugMap.get(packet.getDebugId());
		if(astacktraceelement != null) {
			List<StackTraceElement> list = new ArrayList<StackTraceElement>();
			for(StackTraceElement element:this.getStackTrace()) {
				list.add(element);
				if(element.getClassName().equals("logisticspipes.network.PacketHandler")) {
					break;
				}
			}
			list.remove(0);
			this.setStackTrace(list.toArray(new StackTraceElement[list.size()]));
			list = new ArrayList<StackTraceElement>(Arrays.asList(astacktraceelement));
			if(list.size() > 2) {
				list.remove(0);
				list.remove(0);
			}
			RuntimeException runtime = new RuntimeException();
			runtime.setStackTrace(list.toArray(new StackTraceElement[list.size()]));
			this.initCause(runtime);
		}
	}
}
