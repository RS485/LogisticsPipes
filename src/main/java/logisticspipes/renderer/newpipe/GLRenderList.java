package logisticspipes.renderer.newpipe;

import net.minecraft.client.renderer.GLAllocation;

import org.lwjgl.opengl.GL11;

public class GLRenderList {

	private final int listID = GLAllocation.generateDisplayLists(1);
	public boolean isValid = true;
	private long lastUsed = System.currentTimeMillis();
	private boolean isFilled = false;

	public int getID() {
		return listID;
	}

	public void startListCompile() {
		if (!isValid) {
			throw new UnsupportedOperationException("Can't use a removed list");
		}
		GL11.glNewList(listID, GL11.GL_COMPILE);
	}

	public void stopCompile() {
		if (!isValid) {
			throw new UnsupportedOperationException("Can't use a removed list");
		}
		GL11.glEndList();
		isFilled = true;
	}

	public void render() {
		if (!isValid) {
			throw new UnsupportedOperationException("Can't use a removed list");
		}
		GL11.glCallList(listID);
		lastUsed = System.currentTimeMillis();
	}

	public boolean check() {
		if (!isValid) {
			return true;
		}
		if (lastUsed + 1000 * 60 * 1 < System.currentTimeMillis()) {
			isValid = false;
			return false;
		}
		return true;
	}

	public boolean isInvalid() {
		return !isValid;
	}

	public boolean isFilled() {
		return isFilled;
	}
}
