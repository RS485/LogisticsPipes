package logisticspipes.logisticspipes;

import java.util.LinkedList;

import logisticspipes.utils.AdjacentTile;

/**
 * This interface gives access to the surrounding world
 * 
 * @author Krapht
 */
public interface IAdjacentWorldAccess {

	public LinkedList<AdjacentTile> getConnectedEntities();

	public int getRandomInt(int maxSize);
}
