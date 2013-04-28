package logisticspipes.logistics;

import java.util.UUID;

import logisticspipes.routing.SplitMember;
import logisticspipes.utils.SinkReply;

public interface ILogisticsTurnHandler {
	public void subscribeToOrCreateGroup(int group, UUID id, int splitAmount);
	public SinkReply getScaledSinkReply(SinkReply reply, UUID id, int group);
	public void unsubscribe(UUID id);
	public void passTurn(int group, UUID lastTurn);
	public void clearList();
}
