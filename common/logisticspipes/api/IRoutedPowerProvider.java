package logisticspipes.api;

import java.util.List;

/** things implementing this interface are capable of providing power, but they draw from another sources
 * Implement ILogisticsPowerProvider if you wish to provide power to the LP network.
 * Losses of energy based on distance may be involved.
 * @author Andrew
 *
 */
public interface IRoutedPowerProvider {
	// typically calls useEnergy(amount,null);
	boolean useEnergy(int amount);	
	// typically calls canUseEnergy(amount,null);
	boolean canUseEnergy(int amount);
	
	// for the case where 1 IRoutedPowerProvider has to check another
	// each IRoutedPowerProvider that can recurse should:
	//   a) check that it is not already on the list
	//   b) add itself to the list (creating it if the list is null), 
	boolean useEnergy(int amount, List<Object> providersToIgnore);	
	boolean canUseEnergy(int amount, List<Object> providersToIgnore);
	
	int getX(); // the coordinates of the associated tile (typically "this.xCoords"). needed for sending packets.
	int getY();
	int getZ(); 
}
