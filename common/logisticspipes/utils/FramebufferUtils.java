/*
 * Copyright (c) 2015  RS485
 *
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * https://github.com/RS485/LogisticsPipes/blob/mc16/LICENSE.md
 */

package logisticspipes.utils;

import net.minecraft.client.renderer.OpenGlHelper;

public final class FramebufferUtils {

	/** renderbuffer target */
	public static final int GL_RENDERBUFFER = 0x8D41;

	/** framebuffer targets */
	public static final int GL_FRAMEBUFFER = 0x8D40, GL_READ_FRAMEBUFFER = 0x8CA8, GL_DRAW_FRAMEBUFFER = 0x8CA9;

	private FramebufferUtils() {}

	public static void bindRenderBuffer(int renderbuffer) {
		assert OpenGlHelper.isFramebufferEnabled();

		OpenGlHelper.func_153176_h(FramebufferUtils.GL_RENDERBUFFER, renderbuffer);
	}

	public static void deleteRenderbuffer(int renderbuffer) {
		assert OpenGlHelper.isFramebufferEnabled();

		OpenGlHelper.func_153184_g(renderbuffer);
	}

	public static int genRenderbuffer() {
		assert OpenGlHelper.isFramebufferEnabled();

		return OpenGlHelper.func_153185_f();
	}

	public static void renderbufferStorage(int format, int width, int height) {
		assert OpenGlHelper.isFramebufferEnabled();

		OpenGlHelper.func_153186_a(FramebufferUtils.GL_RENDERBUFFER, format, width, height);
	}

	public static int genFramebuffer() {
		assert OpenGlHelper.isFramebufferEnabled();

		return OpenGlHelper.func_153165_e();
	}

	public static int checkFramebuffer(int target) {
		assert OpenGlHelper.isFramebufferEnabled();

		return OpenGlHelper.func_153167_i(target);
	}

	public static void bindFramebuffer(int target, int framebuffer) {
		assert OpenGlHelper.isFramebufferEnabled();

		OpenGlHelper.func_153171_g(target, framebuffer);
	}

	public static void deleteFramebuffer(int framebuffer) {
		assert OpenGlHelper.isFramebufferEnabled();

		OpenGlHelper.func_153174_h(framebuffer);
	}

	public static void framebufferRenderbuffer(int target, int attachment, int renderbuffer) {
		assert OpenGlHelper.isFramebufferEnabled();

		OpenGlHelper.func_153190_b(target, attachment, FramebufferUtils.GL_RENDERBUFFER, renderbuffer);
	}

	public static void framebufferTexture2D(int target, int attachment, int textarget, int texture, int level) {
		assert OpenGlHelper.isFramebufferEnabled();

		OpenGlHelper.func_153188_a(target, attachment, textarget, texture, level);
	}
}
