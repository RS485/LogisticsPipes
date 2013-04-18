package appeng.api.me.util;

public interface ICraftWatcher {
	
	void markChainCrafted();
	void markComplete( ICraftRequest cr );
	
}
