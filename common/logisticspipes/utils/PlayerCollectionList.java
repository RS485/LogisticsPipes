package logisticspipes.utils;

import java.util.ArrayList;
import java.util.Iterator;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

public class PlayerCollectionList extends ArrayList<EntityPlayer> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5678512898054117833L;
	private boolean checkingPlayers =false;

	public void checkPlayers() {
		checkingPlayers = true;
		Iterator<EntityPlayer> players = super.iterator();
		while(players.hasNext()) {
			EntityPlayer player = players.next();
			boolean remove = false;
			if(player.isDead) {
				remove = true;
			} else if(player instanceof EntityPlayerMP) {
				if(((EntityPlayerMP)player).playerNetServerHandler.connectionClosed) {
					remove = true;
				}
			}
			if(remove) {
				players.remove();
			}
		}
		checkingPlayers = false;
	}
	
	@Override
	public Iterator<EntityPlayer> iterator() {
		checkPlayers();
		return super.iterator();
	}
	
	@Override
	public int size() {
		if(!checkingPlayers)
			checkPlayers();
		return super.size();
	}

	@Override
	public boolean isEmpty() {
		return size() == 0;
	}
}
