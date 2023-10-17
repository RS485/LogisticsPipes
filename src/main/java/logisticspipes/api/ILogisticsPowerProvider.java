package logisticspipes.api;

/**
 * things which directly provide power to the logsitics network implement this.
 * lists of these objects available to a network will be cached, and the closest
 * one with power preferentially pulled from.
 *
 * @author Andrew
 */
public interface ILogisticsPowerProvider extends IRoutedPowerProvider {

	int getPowerLevel();
}
