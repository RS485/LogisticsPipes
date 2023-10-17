package logisticspipes.interfaces;

public interface ISecurityProvider {

	boolean getAllowCC(int id);

	boolean canAutomatedDestroy();
}
