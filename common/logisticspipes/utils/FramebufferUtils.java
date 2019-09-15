/*
 * Copyright (c) 2015  RS485
 *
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * https://github.com/RS485/LogisticsPipes/blob/mc16/LICENSE.md
 */

package logisticspipes.utils;

import org.lwjgl.opengl.GL30;

public final class FramebufferUtils {

	private FramebufferUtils() {}

	public static void bindRenderBuffer(int renderbuffer) {
		assert OpenGlHelper.isFramebufferEnabled();

		OpenGlHelper.glBindBuffer(GL30.GL_RENDERBUFFER, renderbuffer);
	}

	public static void deleteRenderbuffer(int renderbuffer) {
		assert OpenGlHelper.isFramebufferEnabled();

		OpenGlHelper.glDeleteBuffers(renderbuffer);
	}

	public static int genRenderbuffer() {
		assert OpenGlHelper.isFramebufferEnabled();

		return OpenGlHelper.glGenRenderbuffers();
	}

	public static void renderbufferStorage(int format, int width, int height) {
		assert OpenGlHelper.isFramebufferEnabled();

		OpenGlHelper.glRenderbufferStorage(GL30.GL_RENDERBUFFER, format, width, height);
	}

	public static int genFramebuffer() {
		assert OpenGlHelper.isFramebufferEnabled();

		return OpenGlHelper.glGenFramebuffers();
	}

	public static int checkFramebuffer(int target) {
		assert OpenGlHelper.isFramebufferEnabled();

		return OpenGlHelper.glCheckFramebufferStatus(target);
	}

	public static void bindFramebuffer(int target, int framebuffer) {
		assert OpenGlHelper.isFramebufferEnabled();

		OpenGlHelper.glBindFramebuffer(target, framebuffer);
	}

	public static void deleteFramebuffer(int framebuffer) {
		assert OpenGlHelper.isFramebufferEnabled();

		OpenGlHelper.glDeleteFramebuffers(framebuffer);
	}

	public static void framebufferRenderbuffer(int target, int attachment, int renderbuffer) {
		assert OpenGlHelper.isFramebufferEnabled();

		OpenGlHelper.glFramebufferRenderbuffer(target, attachment, GL30.GL_RENDERBUFFER, renderbuffer);
	}

	public static void framebufferTexture2D(int target, int attachment, int textarget, int texture, int level) {
		assert OpenGlHelper.isFramebufferEnabled();

		OpenGlHelper.glFramebufferTexture2D(target, attachment, textarget, texture, level);
	}
}
