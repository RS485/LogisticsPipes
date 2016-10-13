package logisticspipes.utils.gui.sideconfig;

import java.awt.Rectangle;
import java.lang.reflect.Field;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL14;

import logisticspipes.LogisticsPipes;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsBlockGenericPipe;
import logisticspipes.textures.Textures;
import logisticspipes.utils.LPPositionSet;
import logisticspipes.utils.math.BoundingBox;
import logisticspipes.utils.math.Camera;
import logisticspipes.utils.math.Matrix4d;
import logisticspipes.utils.math.VecmathUtil;
import logisticspipes.utils.math.Vector2d;
import logisticspipes.utils.math.Vertex;
import network.rs485.logisticspipes.world.CoordinateUtils;
import network.rs485.logisticspipes.world.DoubleCoordinates;

//Based on: https://github.com/SleepyTrousers/EnderIO/blob/master/src/main/java/crazypants/enderio/machine/gui/GuiOverlayIoConfig.java
public abstract class SideConfigDisplay {

	protected static final RenderBlocks RB = new RenderBlocks();

	private boolean draggingRotate = false;
	private boolean draggingMove = false;
	private float pitch = 0;
	private float yaw = 0;
	private double distance;
	private long initTime;

	private Minecraft mc = Minecraft.getMinecraft();
	private World world = mc.thePlayer.worldObj;

	private final Vector3d origin = new Vector3d();
	private final Vector3d eye = new Vector3d();
	private final Camera camera = new Camera();
	private final Matrix4d pitchRot = new Matrix4d();
	private final Matrix4d yawRot = new Matrix4d();

	public DoubleCoordinates originBC;

	private List<DoubleCoordinates> configurables = new ArrayList<>();
	private List<DoubleCoordinates> neighbours = new ArrayList<>();

	private SelectedFace selection;

	public boolean renderNeighbours = true;
	private boolean inNeigButBounds = false;
	private LogisticsBlockGenericPipe.RaytraceResult cachedLPBlockTrace;

	public SideConfigDisplay(CoreRoutedPipe configurables) {
		this(Collections.singletonList(configurables.getLPPosition()));
	}

	public SideConfigDisplay(LPPositionSet<DoubleCoordinates> configurables) {
		this(Arrays.asList(configurables.toArray(new DoubleCoordinates[0])));
	}

	public SideConfigDisplay(List<DoubleCoordinates> configurables) {
		this.configurables.addAll(configurables);

		Vector3d c;
		Vector3d size;
		if (configurables.size() == 1) {
			DoubleCoordinates bc = this.configurables.get(0);
			c = new Vector3d(bc.getXDouble() + 0.5, bc.getYDouble() + 0.5, bc.getZDouble() + 0.5);
			size = new Vector3d(1, 1, 1);
		} else {
			Vector3d min = new Vector3d(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
			Vector3d max = new Vector3d(-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE);
			for (DoubleCoordinates bc : configurables) {
				min.set(Math.min(bc.getXDouble(), min.x), Math.min(bc.getYDouble(), min.y), Math
						.min(bc.getZDouble(), min.z));
				max.set(Math.max(bc.getXDouble(), max.x), Math.max(bc.getYDouble(), max.y), Math
						.max(bc.getZDouble(), max.z));
			}
			size = new Vector3d(max);
			size.sub(min);
			size.multiply(0.5);
			c = new Vector3d(min.x + size.x, min.y + size.y, min.z + size.z);
			size.multiply(2);
		}

		originBC = new DoubleCoordinates((int) c.x, (int) c.y, (int) c.z);
		origin.set(c);
		pitchRot.setIdentity();
		yawRot.setIdentity();

		pitch = -mc.thePlayer.rotationPitch;
		yaw = 180 - mc.thePlayer.rotationYaw;

		distance = Math.max(Math.max(size.x, size.y), size.z) + 4;

		for (DoubleCoordinates bc : configurables) {
			for (EnumFacing dir : EnumFacing.VALUES) {
				DoubleCoordinates loc = CoordinateUtils.add(new DoubleCoordinates(bc), dir);
				if (!configurables.contains(loc)) {
					neighbours.add(loc);
				}
			}
		}

		world = mc.thePlayer.worldObj;
		RB.blockAccess = new InnerBA();
	}

	public abstract void handleSelection(SelectedFace selection);

	public void init() {
		initTime = System.currentTimeMillis();
	}

	public SelectedFace getSelection() {
		return selection;
	}

	public void handleMouseInput() {

		if (Mouse.getEventButton() == 0) {
			draggingRotate = Mouse.getEventButtonState();
		}
		if (Mouse.getEventButton() == 2) {
			draggingMove = Mouse.getEventButtonState();
		}

		if (draggingRotate) {
			double dx = (Mouse.getEventDX() / (double) mc.displayWidth);
			double dy = (Mouse.getEventDY() / (double) mc.displayHeight);
			if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
				distance -= dy * 15;
			} else {
				yaw -= 4 * dx * 180;
				pitch += 2 * dy * 180;
				pitch = (float) VecmathUtil.clamp(pitch, -80, 80);
			}
		}

		if(draggingMove) {
			double dx = Mouse.getEventDX();
			double dy = -Mouse.getEventDY();
			Vector3d orivec = camera.getWorldPoint(new Vector2d(0, 0));
			Vector3d newvec = camera.getWorldPoint(new Vector2d(dx * distance, dy * distance)).negate();
			origin.add(orivec).add(newvec);
		}

		distance -= Mouse.getEventDWheel() * 0.01;
		distance = VecmathUtil.clamp(distance, 0.01, 200);

		long elapsed = System.currentTimeMillis() - initTime;

		int x = Mouse.getEventX();
		int y = Mouse.getEventY();
		Vector3d start = new Vector3d();
		Vector3d end = new Vector3d();
		if (camera.getRayForPixel(x, y, start, end)) {
			end.multiply(distance * 2);
			end.add(start);
			updateSelection(start, end);
		}

		if (!Mouse.getEventButtonState() && camera.isValid() && elapsed > 500) {
			if (Mouse.getEventButton() == 1) {
				if (selection != null) {
					handleSelection(selection);
				}
			} else if (Mouse.getEventButton() == 0 && inNeigButBounds) {
				renderNeighbours = !renderNeighbours;
			}
		}
	}

	private void updateSelection(Vector3d start, Vector3d end) {
		start.add(origin);
		end.add(origin);
		List<MovingObjectPosition> hits = new ArrayList<>();

		LogisticsBlockGenericPipe.ignoreSideRayTrace = true;
		for (DoubleCoordinates bc : configurables) {
			Block block = world.getBlock(bc.getXInt(), bc.getYInt(), bc.getZInt());
			if (block != null) {
				if(block instanceof LogisticsBlockGenericPipe) {
					cachedLPBlockTrace = LogisticsPipes.LogisticsPipeBlock.doRayTrace(world, bc.getXInt(), bc.getYInt(), bc.getZInt(), Vec3.createVectorHelper(start.x, start.y, start.z), Vec3.createVectorHelper(end.x, end.y, end.z));
				} else {
					cachedLPBlockTrace = null;
				}
				MovingObjectPosition hit = block.collisionRayTrace(world, bc.getXInt(), bc.getYInt(), bc.getZInt(), Vec3.createVectorHelper(start.x, start.y, start.z), Vec3.createVectorHelper(end.x, end.y, end.z));
				if (hit != null) {
					hits.add(hit);
				}
			}
		}
		LogisticsBlockGenericPipe.ignoreSideRayTrace = false;
		selection = null;
		MovingObjectPosition hit = getClosestHit(Vec3.createVectorHelper(start.x, start.y, start.z), hits);
		if (hit != null) {
			TileEntity te = world.getTileEntity(hit.blockX, hit.blockY, hit.blockZ);
			if(te != null) {
				EnumFacing face = EnumFacing.getFront(hit.sideHit);
				selection = new SelectedFace(te, face, hit);
			}
		}
	}

	public static MovingObjectPosition getClosestHit(Vec3 origin, Collection<MovingObjectPosition> candidates) {
		double minLengthSquared = Double.POSITIVE_INFINITY;
		MovingObjectPosition closest = null;

		for (MovingObjectPosition hit : candidates) {
			if (hit != null) {
				double lengthSquared = hit.hitVec.squareDistanceTo(origin);
				if (lengthSquared < minLengthSquared) {
					minLengthSquared = lengthSquared;
					closest = hit;
				}
			}
		}
		return closest;
	}

	public void drawScreen(int par1, int par2, float partialTick, Rectangle vp, Rectangle parentBounds) {

		if (!updateCamera(partialTick, vp.x, vp.y, vp.width, vp.height)) {
			return;
		}
		GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
		applyCamera(partialTick);
		renderScene(false);
		renderScene(true);
		renderSelection();

		renderOverlay(par1, par2);
		GL11.glPopAttrib();
	}

	private void renderSelection() {
		if (selection == null) {
			return;
		}
		GL11.glPushMatrix();
		GL11.glShadeModel(GL11.GL_FLAT);
		GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);

		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glPopMatrix();

		GL11.glDisable(GL11.GL_ALPHA_TEST);
		if (selection.hit.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK)
		{
			GL11.glEnable(GL11.GL_BLEND);
			OpenGlHelper.glBlendFunc(770, 771, 1, 0);
			GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.4F);
			GL11.glLineWidth(2.0F);
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glDepthMask(false);
			float f1 = 0.002F;
			Block block = mc.theWorld.getBlock(selection.hit.blockX, selection.hit.blockY, selection.hit.blockZ);

			if (block.getMaterial() != Material.air)
			{
				if(block instanceof LogisticsBlockGenericPipe) {
					LogisticsBlockGenericPipe.bypassPlayerTrace = cachedLPBlockTrace;
				}
				block.setBlockBoundsBasedOnState(mc.theWorld, selection.hit.blockX, selection.hit.blockY, selection.hit.blockZ);
				double d0 = origin.x - eye.x;
				double d1 = origin.y - eye.y;
				double d2 = origin.z - eye.z;
				RenderGlobal.drawOutlinedBoundingBox(block.getSelectedBoundingBoxFromPool(mc.theWorld, selection.hit.blockX, selection.hit.blockY, selection.hit.blockZ).expand((double)f1, (double)f1, (double)f1).getOffsetBoundingBox(-d0, -d1, -d2), -1);
				if(block instanceof LogisticsBlockGenericPipe) {
					LogisticsBlockGenericPipe.bypassPlayerTrace = null;
				}
			}

			GL11.glDepthMask(true);
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL11.glDisable(GL11.GL_BLEND);
		}
		GL11.glEnable(GL11.GL_ALPHA_TEST);

		BoundingBox bb = new BoundingBox(new DoubleCoordinates(selection.config));

		IIcon icon = Textures.LOGISTICS_SIDE_SELECTION;
		List<Vertex> corners = bb.getCornersWithUvForFace(selection.face, icon.getMinU(), icon.getMaxU(), icon.getMinV(), icon.getMaxV());

		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_LIGHTING);
		RenderUtil.bindBlockTexture();
		GL11.glColor3f(1, 1, 1);
		Tessellator.instance.startDrawingQuads();
		Tessellator.instance.setColorOpaque_F(1, 1, 1);
		Vector3d trans = new Vector3d((-origin.x) + eye.x, (-origin.y) + eye.y, (-origin.z) + eye.z);
		Tessellator.instance.setTranslation(trans.x, trans.y, trans.z);
		RenderUtil.addVerticesToTesselator(corners);
		Tessellator.instance.draw();
		Tessellator.instance.setTranslation(0, 0, 0);
	}

	private void renderOverlay(int mx, int my) {
		Rectangle vp = camera.getViewport();
		ScaledResolution scaledresolution = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);

		int vpx = vp.x / scaledresolution.getScaleFactor();
		int vph = vp.height / scaledresolution.getScaleFactor();
		int vpw = vp.width / scaledresolution.getScaleFactor();
		int vpy = (int) ((float) (vp.y + vp.height - 4) / (float) scaledresolution.getScaleFactor());

		GL11.glViewport(0, 0, mc.displayWidth, mc.displayHeight);
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0.0D, scaledresolution.getScaledWidth_double(), scaledresolution
				.getScaledHeight_double(), 0.0D, 1000.0D, 3000.0D);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		GL11.glTranslatef(vpx, vpy, -2000.0F);

		GL11.glDisable(GL11.GL_LIGHTING);
	}

	private void renderScene(boolean tryNeighbours) {
		if(tryNeighbours && !renderNeighbours) return;
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);

		RenderHelper.disableStandardItemLighting();
		mc.entityRenderer.disableLightmap(0);
		RenderUtil.bindBlockTexture();
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_ALPHA_TEST);

		Vector3d trans = new Vector3d((-origin.x) + eye.x, (-origin.y) + eye.y, (-origin.z) + eye.z);
		for (int pass = 0; pass < 1; pass++) {
			if(!tryNeighbours) {
				setGlStateForPass(pass, false);
				doWorldRenderPass(trans, configurables, pass);
			} else if (renderNeighbours) {
				setGlStateForPass(pass, true);
				doWorldRenderPass(trans, neighbours, pass);
			}
		}

		RenderHelper.enableStandardItemLighting();
		GL11.glEnable(GL11.GL_LIGHTING);
		TileEntityRendererDispatcher.instance.field_147558_l = origin.x - eye.x;
		TileEntityRendererDispatcher.instance.field_147560_j = origin.y - eye.y;
		TileEntityRendererDispatcher.instance.field_147561_k = origin.z - eye.z;
		TileEntityRendererDispatcher.staticPlayerX = origin.x - eye.x;
		TileEntityRendererDispatcher.staticPlayerY = origin.y - eye.y;
		TileEntityRendererDispatcher.staticPlayerZ = origin.z - eye.z;

		for (int pass = 0; pass < 2; pass++) {
			if(!tryNeighbours) {
				setGlStateForPass(pass, false);
				doTileEntityRenderPass(configurables, pass);
			} else if (renderNeighbours) {
				setGlStateForPass(pass, true);
				doTileEntityRenderPass(neighbours, pass);
			}
		}
		setGlStateForPass(0, false);
	}

	private void doTileEntityRenderPass(List<DoubleCoordinates> blocks, int pass) {
		RenderPassHelper.setEntityRenderPass(pass);
		for (DoubleCoordinates bc : blocks) {
			TileEntity tile = world.getTileEntity(bc.getXInt(), bc.getYInt(), bc.getZInt());
			if (tile != null) {
				Vector3d at = new Vector3d(eye.x, eye.y, eye.z);
				at.x += bc.getXDouble() - origin.x;
				at.y += bc.getYDouble() - origin.y;
				at.z += bc.getZDouble() - origin.z;
				GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
				TileEntityRendererDispatcher.instance.renderTileEntityAt(tile, at.x, at.y, at.z, 0);
				GL11.glPopAttrib();
			}
		}
		RenderPassHelper.clearEntityRenderPass();
	}

	private void doWorldRenderPass(Vector3d trans, List<DoubleCoordinates> blocks, int pass) {
		RenderPassHelper.setBlockRenderPass(pass);

		Tessellator.instance.startDrawingQuads();
		Tessellator.instance.setTranslation(trans.x, trans.y, trans.z);
		Tessellator.instance.setBrightness(15 << 20 | 15 << 4);

		for (DoubleCoordinates bc : blocks) {
			Block block = world.getBlock(bc.getXInt(), bc.getYInt(), bc.getZInt());
			if (block != null) {
				if (block.canRenderInPass(pass)) {
					RB.renderAllFaces = true;
					RB.setRenderAllFaces(true);
					RB.setRenderBounds(0, 0, 0, 1, 1, 1);
					try {
						RB.renderBlockByRenderType(block, bc.getXInt(), bc.getYInt(), bc.getZInt());
					} catch (Exception e) {
						//Ignore, things might blow up in rendering due to the modified block access
						//but this is about as good as we can do
					}
				}
			}
		}

		Tessellator.instance.draw();
		Tessellator.instance.setTranslation(0, 0, 0);
		RenderPassHelper.clearBlockRenderPass();
	}

	private void setGlStateForPass(int pass, boolean isNeighbour) {

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		if(isNeighbour) {

			float alpha = 0.8f;
			if(pass == 0) {
				GL11.glEnable(GL11.GL_DEPTH_TEST);
				GL11.glEnable(GL11.GL_BLEND);
				GL11.glEnable(GL11.GL_CULL_FACE);
				GL11.glBlendFunc(GL11.GL_CONSTANT_ALPHA, GL11.GL_CONSTANT_COLOR);
				GL14.glBlendColor(1.0f, 1.0f, 1.0f, alpha);
				GL11.glDepthMask(true);
			} else {
				GL11.glEnable(GL11.GL_BLEND);
				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_CONSTANT_COLOR);
				GL14.glBlendColor(1.0f, 1.0f, 1.0f, 0.8f);
				GL14.glBlendColor(1.0f, 1.0f, 1.0f, alpha);
				GL11.glDepthMask(false);
			}
			return;
		}

		if(pass == 0) {
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glDepthMask(true);
		} else {
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GL11.glDepthMask(false);
		}

	}

	private boolean updateCamera(float partialTick, int vpx, int vpy, int vpw, int vph) {
		if(vpw <= 0 || vph <= 0) {
			return false;
		}
		camera.setViewport(vpx, vpy, vpw, vph);
		camera.setProjectionMatrixAsPerspective(30, 0.05, 50, vpw, vph);
		eye.set(0, 0, distance);
		pitchRot.makeRotationX(Math.toRadians(pitch));
		yawRot.makeRotationY(Math.toRadians(yaw));
		pitchRot.transform(eye);
		yawRot.transform(eye);
		camera.setViewMatrixAsLookAt(eye, RenderUtil.ZERO_V, RenderUtil.UP_V);
		return camera.isValid();
	}

	private void applyCamera(float partialTick) {
		Rectangle vp = camera.getViewport();
		GL11.glViewport(vp.x, vp.y, vp.width, vp.height);
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		RenderUtil.loadMatrix(camera.getTransposeProjectionMatrix());
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		RenderUtil.loadMatrix(camera.getTransposeViewMatrix());
		GL11.glTranslatef(-(float) eye.x, -(float) eye.y, -(float) eye.z);

	}

	public static class SelectedFace {

		public TileEntity config;
		public EnumFacing face;
		public MovingObjectPosition hit;

		public SelectedFace(TileEntity config, EnumFacing face, MovingObjectPosition hit) {
			super();
			this.config = config;
			this.face = face;
			this.hit = hit;
		}
	}

	private class InnerBA implements IBlockAccess {

		protected IBlockAccess wrapped;

		InnerBA() {
			wrapped = world;
		}

		@Override
		public boolean isSideSolid(int x, int y, int z, EnumFacing side, boolean _default) {
			return false;
		}

		@Override
		@SideOnly(Side.CLIENT)
		public int getLightBrightnessForSkyBlocks(int var1, int var2, int var3, int var4) {
			return 15 << 20 | 15 << 4;
		}

		@Override
		public int isBlockProvidingPowerTo(int var1, int var2, int var3, int var4) {
			return wrapped.isBlockProvidingPowerTo(var1, var2, var3, var4);
		}

		@Override
		public boolean isAirBlock(int var1, int var2, int var3) {
			if(!configurables.contains(new DoubleCoordinates(var1,var2,var3))) {
				return false;
			}
			return wrapped.isAirBlock(var1, var2, var3);
		}

		@Override
		public TileEntity getTileEntity(int var1, int var2, int var3) {
			if (var2 >= 0 && var2 < 256) {
				return wrapped.getTileEntity(var1, var2, var3);
			} else {
				return null;
			}
		}

		@Override
		@SideOnly(Side.CLIENT)
		public int getHeight() {
			return wrapped.getHeight();
		}

		@Override
		public int getBlockMetadata(int var1, int var2, int var3) {
			return wrapped.getBlockMetadata(var1, var2, var3);
		}

		@Override
		public Block getBlock(int var1, int var2, int var3) {
			if(!configurables.contains(new DoubleCoordinates(var1,var2,var3))) {
				return Blocks.air;
			}
			return wrapped.getBlock(var1, var2, var3);
		}

		@Override
		@SideOnly(Side.CLIENT)
		public BiomeGenBase getBiomeGenForCoords(int var1, int var2) {

			return wrapped.getBiomeGenForCoords(var1, var2);
		}

		@Override
		@SideOnly(Side.CLIENT)
		public boolean extendedLevelsInChunkCache() {
			return wrapped.extendedLevelsInChunkCache();
		}
	}

	private static class RenderPassHelper {
		private static Field worldRenderPass = null;
		private static int savedWorldRenderPass = -1;
		private static int savedEntityRenderPass = -1;

		static {
			try {
				worldRenderPass = ForgeHooksClient.class.getDeclaredField("worldRenderPass");
				worldRenderPass.setAccessible(true);
			} catch (Exception e) {
				LogisticsPipes.log.warn("Failed to access ForgeHooksClient.worldRenderPass because of: " + e);
				e.printStackTrace();
			}
		}

		public static void setBlockRenderPass(int pass) {
			savedWorldRenderPass = ForgeHooksClient.getWorldRenderPass();
			savedEntityRenderPass = MinecraftForgeClient.getRenderPass();
			setBlockRenderPassImpl(pass);
			setEntityRenderPass(pass);
		}

		private static void setBlockRenderPassImpl(int pass) {
			if (worldRenderPass != null) {
				try {
					worldRenderPass.setInt(null, pass);
				} catch (Exception e) {
					LogisticsPipes.log.warn("Failed to access ForgeHooksClient.worldRenderPass because of: " + e);
					e.printStackTrace();
					worldRenderPass = null;
				}
			}
		}

		private static void clearBlockRenderPass() {
			setBlockRenderPassImpl(savedWorldRenderPass);
			setEntityRenderPass(savedEntityRenderPass);
		}

		private static void clearEntityRenderPass() {
			ForgeHooksClient.setRenderPass(-1);
		}

		private static void setEntityRenderPass(int pass) {
			ForgeHooksClient.setRenderPass(pass);
		}
	}

	private static class RenderUtil {
		public static final Vector3d UP_V = new Vector3d(0, 1, 0);
		public static final Vector3d ZERO_V = new Vector3d(0, 0, 0);
		private static final FloatBuffer MATRIX_BUFFER = GLAllocation.createDirectFloatBuffer(16);
		public static final ResourceLocation BLOCK_TEX = TextureMap.locationBlocksTexture;

		public static void loadMatrix(Matrix4d mat) {
			MATRIX_BUFFER.rewind();
			MATRIX_BUFFER.put((float) mat.m00);
			MATRIX_BUFFER.put((float) mat.m01);
			MATRIX_BUFFER.put((float) mat.m02);
			MATRIX_BUFFER.put((float) mat.m03);
			MATRIX_BUFFER.put((float) mat.m10);
			MATRIX_BUFFER.put((float) mat.m11);
			MATRIX_BUFFER.put((float) mat.m12);
			MATRIX_BUFFER.put((float) mat.m13);
			MATRIX_BUFFER.put((float) mat.m20);
			MATRIX_BUFFER.put((float) mat.m21);
			MATRIX_BUFFER.put((float) mat.m22);
			MATRIX_BUFFER.put((float) mat.m23);
			MATRIX_BUFFER.put((float) mat.m30);
			MATRIX_BUFFER.put((float) mat.m31);
			MATRIX_BUFFER.put((float) mat.m32);
			MATRIX_BUFFER.put((float) mat.m33);
			MATRIX_BUFFER.rewind();
			GL11.glLoadMatrix(MATRIX_BUFFER);
		}

		public static void bindBlockTexture() {
			Minecraft.getMinecraft().renderEngine.bindTexture(BLOCK_TEX);
		}

		public static void addVerticesToTesselator(List<Vertex> vertices) {
			addVerticesToTessellator(vertices, Tessellator.instance);
		}

		public static void addVerticesToTessellator(List<Vertex> vertices, Tessellator tes) {
			for (Vertex v : vertices) {
				if (v.uv != null) {
					tes.setTextureUV(v.u(), v.v());
				}
				if (v.normal != null) {
					tes.setNormal(v.nx(), v.ny(), v.nz());
				}
				tes.addVertex(v.x(), v.y(), v.z());
			}
		}
	}
}
