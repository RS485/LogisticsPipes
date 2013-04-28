package logisticspipes.logistics;

import logisticspipes.utils.SinkReply;

public interface ILogisticsTurnHandler {
	public void subscribeToOrCreateGroup(int group, int id, int splitAmount);
	public SinkReply getScaledSinkReply(SinkReply reply, int id, int group);
	public void unsubscribe(int id);
	public void passTurn(int group, int idLastTurn);
	public void clearList();
}
