package logisticspipes.renderer.newpipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;

import net.minecraft.block.Block;
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

import com.google.common.collect.Lists;
import org.apache.commons.lang3.tuple.Pair;

import logisticspipes.blocks.LogisticsSolidBlock;
import logisticspipes.blocks.LogisticsSolidBlock.Type;
import logisticspipes.items.LogisticsSolidBlockItem;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.object3d.interfaces.TextureTransformation;
import logisticspipes.renderer.LogisticsRenderPipe;

public class LogisticsBlockModel implements IModel {

	public static class Loader implements ICustomModelLoader {

		@Override
		public boolean accepts(@Nonnull ResourceLocation modelLocation) {
			return getType(modelLocation) != null;
		}

		@Override
		@Nonnull
		public IModel loadModel(@Nonnull ResourceLocation modelLocation) {
			ResourceLocation baseTex = new ResourceLocation(modelLocation.getResourceDomain(), "solid_block/" + modelLocation.getResourcePath());
			return new LogisticsBlockModel(baseTex, Objects.requireNonNull(getType(modelLocation)));
		}

		@Nullable
		private Type getType(ResourceLocation modelLocation) {
			if (!(modelLocation instanceof ModelResourceLocation)) return null;
			ResourceLocation clean = new ResourceLocation(modelLocation.getResourceDomain(), modelLocation.getResourcePath());
			String variant = ((ModelResourceLocation) modelLocation).getVariant();

			if (variant.equals("inventory")) {
				// TODO split placement item into seperate items
				Item item = ForgeRegistries.ITEMS.getValue(clean);
				if (item instanceof LogisticsSolidBlockItem) {
					return ((LogisticsSolidBlockItem) item).getType();
				} else return null;
			} else {
				Block block = ForgeRegistries.BLOCKS.getValue(clean);
				if (block instanceof LogisticsSolidBlock) {
					return ((LogisticsSolidBlock) block).getType();
				} else return null;
			}
		}

		@Override
		public void onResourceManagerReload(@Nonnull IResourceManager resourceManager) {}
	}

	private final ResourceLocation inactive;
	private final ResourceLocation active;

	public LogisticsBlockModel(ResourceLocation texture, Type type) {
		this.inactive = texture;
		if (type.isHasActiveTexture()) {
			this.active = new ResourceLocation(texture.getResourceDomain(), texture.getResourcePath() + "_active");
		} else {
			this.active = texture;
		}
	}

	@Nonnull
	@Override
	public Collection<ResourceLocation> getTextures() {
		return Arrays.asList(inactive, active);
	}

	@Nonnull
	@Override
	public IBakedModel bake(@Nonnull IModelState state, @Nonnull VertexFormat format, @Nonnull Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
		final List<BakedQuad> quads = Lists.newArrayList();

		TextureAtlasSprite inactiveT = bakedTextureGetter.apply(inactive);
		TextureAtlasSprite activeT = bakedTextureGetter.apply(active);

		return new IBakedModel() {

			@Override
			@Nonnull
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
			@Nonnull
			public TextureAtlasSprite getParticleTexture() {
				return inactiveT;
			}

			@Override
			@Nonnull
			public ItemOverrideList getOverrides() {
				return ItemOverrideList.NONE;
			}

			@Override
			@Nonnull
			public Pair<? extends IBakedModel, Matrix4f> handlePerspective(@Nonnull ItemCameraTransforms.TransformType cameraTransformType) {
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

}
