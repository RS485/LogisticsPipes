package logisticspipes.renderer.newpipe;

import com.google.common.collect.Lists;
import logisticspipes.LPBlocks;
import logisticspipes.blocks.LogisticsSolidBlock;
import logisticspipes.blocks.LogisticsSolidBlock.BlockType;
import logisticspipes.items.LogisticsSolidBlockItem;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.object3d.interfaces.TextureTransformation;
import logisticspipes.renderer.LogisticsRenderPipe;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.PerspectiveMapWrapper;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogisticsBlockModel implements IModel {

	public static class Loader implements ICustomModelLoader {

		private static final Pattern TYPE_REGEX = Pattern.compile(".*block_sub_type=(\\w+).*");

		@Override
		public boolean accepts(ResourceLocation modelLocation) {
			return getType(modelLocation) != null;
		}

		@Override
		public IModel loadModel(ResourceLocation modelLocation) {
			return new LogisticsBlockModel(getType(modelLocation));
		}

		@Nullable
		private BlockType getType(ResourceLocation modelLocation) {
			if (!(modelLocation instanceof ModelResourceLocation)) return null;
			ResourceLocation clean = new ResourceLocation(modelLocation.getResourceDomain(), modelLocation.getResourcePath());
			String variant = ((ModelResourceLocation) modelLocation).getVariant();

			if (variant.equals("inventory")) {
				// TODO split placement item into seperate items
				int index = 0;
				String newPath = clean.getResourcePath();
				if (newPath.contains(".")) {
					int i = newPath.lastIndexOf(".");
					String a = newPath.substring(0, i);
					String b = newPath.substring(i + 1);
					try {
						index = Integer.parseInt(b);
					} catch (NumberFormatException ignored) {
						return null;
					}
					newPath = a;
				}
				Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(clean.getResourceDomain(), newPath));
				if (item instanceof LogisticsSolidBlockItem) {
					return BlockType.getForMeta(index);
				} else return null;
			} else {
				// TODO move each block type to seperate block registry entry
				if (!clean.equals(LPBlocks.solidBlock.getRegistryName())) return null;
				Matcher matcher = TYPE_REGEX.matcher(variant);
				if (!matcher.matches()) return null;

				String result = matcher.group(1);
				return BlockType.getForName(result);
			}
		}

		@Override
		public void onResourceManagerReload(IResourceManager resourceManager) {}
	}

	private static final ResourceLocation[] TEXTURES = new ResourceLocation[32];

	private final BlockType type;
	private final ResourceLocation inactive;
	private final ResourceLocation active;

	public LogisticsBlockModel(BlockType type) {
		this.type = type;
		this.inactive = TEXTURES[type.getMeta()];
		this.active = TEXTURES[type.getMeta() + 16];
	}

	@Override
	public Collection<ResourceLocation> getTextures() {
		return Arrays.asList(inactive, active);
	}

	@Override
	public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
		final List<BakedQuad> quads = Lists.newArrayList();

		TextureAtlasSprite inactiveT = bakedTextureGetter.apply(inactive);
		TextureAtlasSprite activeT = bakedTextureGetter.apply(active);

		return new IBakedModel() {

			@Override
			public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
				if (side == null) {
					if (quads.isEmpty()) {
						quads.addAll(LogisticsRenderPipe.secondRenderer.getQuadsFromRenderList(generateBlockRenderList(state, inactiveT, activeT), format, true));
					}
					return quads;
				} else {
					return Collections.emptyList();//LogisticsRenderPipe.secondRenderer.getQuadsFromRenderList(generateBlockRenderList(state), format);
				}
			}

			@Override
			public boolean isAmbientOcclusion() {
				return false;
			}

			@Override
			public boolean isGui3d() {
				return true;
			}

			@Override
			public boolean isBuiltInRenderer() {
				return false;
			}

			@Override
			public TextureAtlasSprite getParticleTexture() {
				return inactiveT;
			}

			@Override
			public ItemOverrideList getOverrides() {
				return ItemOverrideList.NONE;
			}

			@Override
			public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) {
				return PerspectiveMapWrapper.handlePerspective(this, SimpleServiceLocator.cclProxy.getDefaultBlockState(), cameraTransformType);
			}
		};
	}

	private List<RenderEntry> generateBlockRenderList(@Nullable IBlockState state, @Nonnull TextureAtlasSprite inactive, @Nonnull TextureAtlasSprite active) {
		List<RenderEntry> objectsToRender = new ArrayList<>();

		LogisticsNewSolidBlockWorldRenderer.BlockRotation rotation = LogisticsNewSolidBlockWorldRenderer.BlockRotation.ZERO;
		TextureTransformation icon;
		if (state != null) {
			rotation = LogisticsNewSolidBlockWorldRenderer.BlockRotation.getRotation(state.getValue(LogisticsSolidBlock.rotationProperty));
			icon = SimpleServiceLocator.cclProxy.createIconTransformer(state.getValue(LogisticsSolidBlock.active) ? active : inactive);
		} else {
			icon = SimpleServiceLocator.cclProxy.createIconTransformer(inactive);
		}

		// Draw
		objectsToRender.add(new RenderEntry(LogisticsNewSolidBlockWorldRenderer.block.get(rotation), icon));
		for (LogisticsNewSolidBlockWorldRenderer.CoverSides side : LogisticsNewSolidBlockWorldRenderer.CoverSides.values()) {
			boolean render = true;
			if (state != null) {
				if (!state.getValue(LogisticsSolidBlock.connectionPropertys.get(side.getDir(rotation)))) {
					render = false;
				}
			}
			if (render) {
				objectsToRender.add(new RenderEntry(LogisticsNewSolidBlockWorldRenderer.texturePlate_Outer.get(side).get(rotation), icon));
				objectsToRender.add(new RenderEntry(LogisticsNewSolidBlockWorldRenderer.texturePlate_Inner.get(side).get(rotation), icon));
			}
		}

		return objectsToRender;
	}

	static {
		for (BlockType type : BlockType.values()) {
			String s1 = String.format("logisticspipes:solid_block/%s", type.getName());
			String s2 = s1;

			if (type.isHasActiveTexture()) s2 += "_active";

			TEXTURES[type.getMeta()] = new ResourceLocation(s1);
			TEXTURES[type.getMeta() + 16] = new ResourceLocation(s2);
		}
	}

}
