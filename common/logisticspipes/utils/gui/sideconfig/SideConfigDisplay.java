package logisticspipes.utils.gui.sideconfig;

import java.awt.Rectangle;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import logisticspipes.LPBlocks;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsBlockGenericPipe;
import logisticspipes.textures.Textures;
import logisticspipes.utils.LPPositionSet;
import logisticspipes.utils.math.BoundingBox;
import logisticspipes.utils.math.Camera;
import logisticspipes.utils.math.Matrix4d;
import logisticspipes.utils.math.VecmathUtil;
import logisticspipes.utils.math.Vector2d;
import logisticspipes.utils.math.Vector3d;
import logisticspipes.utils.math.Vertex;
import network.rs485.logisticspipes.world.CoordinateUtils;
import network.rs485.logisticspipes.world.DoubleCoordinates;

//Based on: https://github.com/SleepyTrousers/EnderIO/blob/master/src/main/java/crazypants/enderio/machine/gui/GuiOverlayIoConfig.java
@SideOnly(Side.CLIENT)
public abstract class SideConfigDisplay {

	private boolean draggingRotate = false;
	private boolean draggingMove = false;
	private float pitch;
	private float yaw;
	private double distance;
	private long initTime;

	private Minecraft mc = Minecraft.getMinecraft();
	private World world;

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

		pitch = -mc.player.rotationPitch;
		yaw = 180 - mc.player.rotationYaw;

		distance = Math.max(Math.max(size.x, size.y), size.z) + 4;

		for (DoubleCoordinates bc : configurables) {
			for (EnumFacing dir : EnumFacing.VALUES) {
				DoubleCoordinates loc = CoordinateUtils.add(new DoubleCoordinates(bc), dir);
				if (!configurables.contains(loc)) {
					neighbours.add(loc);
				}
			}
		}

		world = mc.player.world;
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

		if (draggingMove) {
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
			boolean inNeigButBounds = false;
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
		List<RayTraceResult> hits = new ArrayList<>();

		LogisticsBlockGenericPipe.ignoreSideRayTrace = true;
		for (DoubleCoordinates bc : configurables) {
			IBlockState bs = bc.getBlockState(world);
			Block block = bs.getBlock();
			if (block != null) {
				LogisticsBlockGenericPipe.InternalRayTraceResult cachedLPBlockTrace;
				if (block instanceof LogisticsBlockGenericPipe) {
					cachedLPBlockTrace = LPBlocks.pipe.doRayTrace(world, bc.getBlockPos(), start.toVec3d(), end.toVec3d());
				} else {
					cachedLPBlockTrace = null;
				}
				RayTraceResult hit = block.collisionRayTrace(bc.getBlockState(world), world, bc.getBlockPos(), start.toVec3d(), end.toVec3d());
				if (hit != null) {
					hits.add(hit);
				}
			}
		}
		LogisticsBlockGenericPipe.ignoreSideRayTrace = false;
		selection = null;
		RayTraceResult hit = getClosestHit(start.toVec3d(), hits);
		if (hit != null) {
			TileEntity te = world.getTileEntity(hit.getBlockPos());
			if (te != null) {
				selection = new SelectedFace(te, hit.sideHit, hit);
			}
		}
	}

	public static RayTraceResult getClosestHit(Vec3d origin, Collection<RayTraceResult> candidates) {
		double minLengthSquared = Double.POSITIVE_INFINITY;
		RayTraceResult closest = null;

		for (RayTraceResult hit : candidates) {
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
		applyCamera(partialTick);
		renderScene();
		renderSelection();

		renderOverlay(par1, par2);
	}

	private void renderSelection() {
		if (selection == null) {
			return;
		}
		BoundingBox bb = new BoundingBox(new DoubleCoordinates(selection.config));

		TextureAtlasSprite icon = (TextureAtlasSprite) Textures.LOGISTICS_SIDE_SELECTION;
		List<Vertex> corners = bb.getCornersWithUvForFace(selection.face, icon.getMinU(), icon.getMaxU(), icon.getMinV(), icon.getMaxV());

		GlStateManager.disableDepth();
		GlStateManager.disableLighting();

		RenderUtil.bindBlockTexture();
		BufferBuilder tes = Tessellator.getInstance().getBuffer();
		GlStateManager.color(1, 1, 1);
		Vector3d trans = new Vector3d((-origin.x) + eye.x, (-origin.y) + eye.y, (-origin.z) + eye.z);
		tes.setTranslation(trans.x, trans.y, trans.z);
		RenderUtil.addVerticesToTessellator(corners, DefaultVertexFormats.POSITION_TEX, true);
		Tessellator.getInstance().draw();
		tes.setTranslation(0, 0, 0);
	}

	private void renderOverlay(int mx, int my) {
		Rectangle vp = camera.getViewport();
		ScaledResolution scaledresolution = new ScaledResolution(mc);

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

		GlStateManager.disableLighting();
	}

	private void renderScene() {
		GlStateManager.enableCull();
		GlStateManager.enableRescaleNormal();

		RenderHelper.disableStandardItemLighting();
		mc.entityRenderer.disableLightmap();
		RenderUtil.bindBlockTexture();

		GlStateManager.disableLighting();
		GlStateManager.enableTexture2D();
		GlStateManager.enableAlpha();

		Vector3d trans = new Vector3d((-origin.x) + eye.x, (-origin.y) + eye.y, (-origin.z) + eye.z);

		BlockRenderLayer renderLayer = MinecraftForgeClient.getRenderLayer();
		try {
			for (BlockRenderLayer layer : BlockRenderLayer.values()) {
				ForgeHooksClient.setRenderLayer(layer);
				setGlStateForPass(layer, false);
				doWorldRenderPass(trans, configurables, layer);
			}

			if (renderNeighbours) {
				for (BlockRenderLayer layer : BlockRenderLayer.values()) {
					ForgeHooksClient.setRenderLayer(layer);
					setGlStateForPass(layer, true);
					doWorldRenderPass(trans, neighbours, layer);
				}
			}
		} finally {
			ForgeHooksClient.setRenderLayer(renderLayer);
		}

		RenderHelper.enableStandardItemLighting();
		GlStateManager.enableLighting();
		TileEntityRendererDispatcher.instance.entityX = origin.x - eye.x;
		TileEntityRendererDispatcher.instance.entityY = origin.y - eye.y;
		TileEntityRendererDispatcher.instance.entityZ = origin.z - eye.z;
		TileEntityRendererDispatcher.staticPlayerX = origin.x - eye.x;
		TileEntityRendererDispatcher.staticPlayerY = origin.y - eye.y;
		TileEntityRendererDispatcher.staticPlayerZ = origin.z - eye.z;

		for (int pass = 0; pass < 2; pass++) {
			ForgeHooksClient.setRenderPass(pass);
			setGlStateForPass(pass, false);
			doTileEntityRenderPass(configurables, pass);
			if (renderNeighbours) {
				setGlStateForPass(pass, true);
				doTileEntityRenderPass(neighbours, pass);
			}
		}
		ForgeHooksClient.setRenderPass(-1);
		setGlStateForPass(0, false);
	}

	private void doTileEntityRenderPass(List<DoubleCoordinates> blocks, int pass) {
		for (DoubleCoordinates bc : blocks) {
			TileEntity tile = world.getTileEntity(bc.getBlockPos());
			if (tile != null) {
				if (tile.shouldRenderInPass(pass)) {
					Vector3d at = new Vector3d(eye.x, eye.y, eye.z);
					at.x += bc.getXCoord() - origin.x;
					at.y += bc.getYCoord() - origin.y;
					at.z += bc.getZCoord() - origin.z;
					if (tile.getClass() == TileEntityChest.class) {
						TileEntityChest chest = (TileEntityChest) tile;
						if (chest.adjacentChestXNeg != null) {
							tile = chest.adjacentChestXNeg;
							at.x--;
						} else if (chest.adjacentChestZNeg != null) {
							tile = chest.adjacentChestZNeg;
							at.z--;
						}
					}
					TileEntityRendererDispatcher.instance.render(tile, at.x, at.y, at.z, 0, -1, 0);
				}
			}
		}
	}

	private void doWorldRenderPass(Vector3d trans, List<DoubleCoordinates> blocks, BlockRenderLayer layer) {

		BufferBuilder wr = Tessellator.getInstance().getBuffer();
		wr.begin(7, DefaultVertexFormats.BLOCK);

		Tessellator.getInstance().getBuffer().setTranslation(trans.x, trans.y, trans.z);

		for (DoubleCoordinates bc : blocks) {

			IBlockState bs = world.getBlockState(bc.getBlockPos());
			Block block = bs.getBlock();
			bs = bs.getActualState(world, bc.getBlockPos());
			if (block.canRenderInLayer(bs, layer)) {
				renderBlock(bs, bc.getBlockPos(), world, Tessellator.getInstance().getBuffer());
			}
		}

		Tessellator.getInstance().draw();
		Tessellator.getInstance().getBuffer().setTranslation(0, 0, 0);
	}

	public void renderBlock(IBlockState state, BlockPos pos, IBlockAccess blockAccess, BufferBuilder worldRendererIn) {

		try {
			BlockRendererDispatcher blockrendererdispatcher = mc.getBlockRendererDispatcher();
			EnumBlockRenderType type = state.getRenderType();
			if (type != EnumBlockRenderType.MODEL) {
				blockrendererdispatcher.renderBlock(state, pos, blockAccess, worldRendererIn);
				return;
			}

			IBakedModel ibakedmodel = blockrendererdispatcher.getModelForState(state);
			state = state.getBlock().getExtendedState(state, world, pos);
			blockrendererdispatcher.getBlockModelRenderer().renderModel(blockAccess, ibakedmodel, state, pos, worldRendererIn, false);

		} catch (Throwable ignored) {
		}
	}

	private void setGlStateForPass(BlockRenderLayer layer, boolean isNeighbour) {
		int pass = layer == BlockRenderLayer.TRANSLUCENT ? 1 : 0;
		setGlStateForPass(pass, isNeighbour);
	}

	private void setGlStateForPass(int layer, boolean isNeighbour) {

		GlStateManager.color(1, 1, 1);
		if (isNeighbour) {

			GlStateManager.enableDepth();
			GlStateManager.enableBlend();
			float alpha = 1f;
			float col = 1f;

			GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_CONSTANT_COLOR);
			GL14.glBlendColor(col, col, col, alpha);
			return;
		}

		if (layer == 0) {
			GlStateManager.enableDepth();
			GlStateManager.disableBlend();
			GlStateManager.depthMask(true);
		} else {
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GlStateManager.depthMask(false);

		}

	}

	private boolean updateCamera(float partialTick, int vpx, int vpy, int vpw, int vph) {
		if (vpw <= 0 || vph <= 0) {
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
		public RayTraceResult hit;

		public SelectedFace(TileEntity config, EnumFacing face, RayTraceResult hit) {
			super();
			this.config = config;
			this.face = face;
			this.hit = hit;
		}
	}

	/*
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
*/
	private static class RenderUtil {

		public static final Vector3d UP_V = new Vector3d(0, 1, 0);
		public static final Vector3d ZERO_V = new Vector3d(0, 0, 0);
		private static final FloatBuffer MATRIX_BUFFER = GLAllocation.createDirectFloatBuffer(16);
		public static final ResourceLocation BLOCK_TEX = TextureMap.LOCATION_BLOCKS_TEXTURE;

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

		public static void addVerticesToTessellator(List<Vertex> vertices, VertexFormat format, boolean doBegin) {
			if (vertices == null || vertices.isEmpty()) {
				return;
			}

			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder tes = tessellator.getBuffer();
			if (doBegin) {
				tes.begin(GL11.GL_QUADS, format);
			}

			for (Vertex v : vertices) {
				for (VertexFormatElement el : format.getElements()) {
					switch (el.getUsage()) {
						case COLOR:
							if (el.getType() == VertexFormatElement.EnumType.FLOAT) {
								tes.color(v.r(), v.g(), v.b(), v.a());
							}
							break;
						case NORMAL:
							tes.normal(v.nx(), v.ny(), v.nz());
							break;
						case POSITION:
							tes.pos(v.x(), v.y(), v.z());
							break;
						case UV:
							if (el.getType() == VertexFormatElement.EnumType.FLOAT && v.uv != null) {
								tes.tex(v.u(), v.v());
							}
							break;
						case GENERIC:
							break;
						case PADDING:
							break;
						default:
							break;

					}
				}
				tes.endVertex();
			}
		}
	}
}
