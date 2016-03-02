package logisticspipes.renderer.newpipe;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.renderer.GLAllocation;

public class GLRenderListHandler {

	private List<GLRenderList> collection = new ArrayList<>();

	public GLRenderList getNewRenderList() {
		GLRenderList list = new GLRenderList();
		collection.add(list);
		return list;
	}

	public void tick() {
		List<GLRenderList> newCollection = new ArrayList<>(collection);
		collection.stream().filter(ref -> !ref.check()).forEach(ref -> {
			GLAllocation.deleteDisplayLists(ref.getID());
			newCollection.remove(ref);
		});
		if (newCollection.size() != collection.size()) {
			collection = newCollection;
		}
	}
}
