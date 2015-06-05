package logisticspipes.renderer.newpipe;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.renderer.GLAllocation;

public class GLRenderListHandler {

	private List<GLRenderList> collection = new ArrayList<GLRenderList>();

	public GLRenderList getNewRenderList() {
		GLRenderList list = new GLRenderList();
		collection.add(list);
		return list;
	}

	public void tick() {
		List<GLRenderList> newCollection = new ArrayList<GLRenderList>(collection);
		for (GLRenderList ref : collection) {
			if (!ref.check()) {
				GLAllocation.deleteDisplayLists(ref.getID());
				newCollection.remove(ref);
			}
		}
		if (newCollection.size() != collection.size()) {
			collection = newCollection;
		}
	}
}
