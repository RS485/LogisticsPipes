package logisticspipes.renderer;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nonnull;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import net.minecraftforge.client.model.BakedItemModel;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ItemLayerModel;
import net.minecraftforge.client.model.ItemTextureQuadConverter;
import net.minecraftforge.client.model.PerspectiveMapWrapper;
import net.minecraftforge.client.model.SimpleModelState;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import logisticspipes.utils.FluidIdentifier;

@SideOnly(Side.CLIENT)
public class FluidContainerRenderer implements IModel {

	public static class FluidContainerRendererModelLoader implements ICustomModelLoader {

		@Override
		public boolean accepts(@Nonnull ResourceLocation modelLocation) {
			return modelLocation.getResourceDomain().equals("logisticspipes") && modelLocation.getResourcePath().equals("models/item/fluid_container");
		}

		@Nonnull
		@Override
		public IModel loadModel(@Nonnull ResourceLocation modelLocation) {
			return new FluidContainerRenderer();
		}

		@Override
		public void onResourceManagerReload(@Nonnull IResourceManager resourceManager) {

		}
	}

	private static final ResourceLocation STENCIL = new ResourceLocation("logisticspipes:items/liquids/stencil");
	private static final ResourceLocation EMPTY = new ResourceLocation("logisticspipes:items/liquids/empty");

	@Nonnull
	@Override
	public Collection<ResourceLocation> getTextures() {
		return ImmutableList.of(EMPTY, STENCIL);
	}

	@Nonnull
	@Override
	public IBakedModel bake(@Nonnull IModelState state, @Nonnull VertexFormat format, @Nonnull Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
		ImmutableMap<ItemCameraTransforms.TransformType, TRSRTransformation> transformMap = PerspectiveMapWrapper.getTransforms(state);

		ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();

		IBakedModel model = (new ItemLayerModel(ImmutableList.of(EMPTY))).bake(state, format, bakedTextureGetter);
		builder.addAll(model.getQuads(null, null, 0));
		TextureAtlasSprite particleSprite = model.getParticleTexture();

		return new BakedItemModel(builder.build(), particleSprite, Maps.immutableEnumMap(transformMap), new FluidContainerItemOverrideList(state, format, transformMap, bakedTextureGetter));
	}

	private static class FluidContainerItemOverrideList extends ItemOverrideList {

		private static final float NORTH_Z = 7.502f / 16f;
		private static final float SOUTH_Z = 8.498f / 16f;

		private Map<FluidIdentifier, IBakedModel> cache = new HashMap<>();

		private VertexFormat format;
		private ImmutableMap<ItemCameraTransforms.TransformType, TRSRTransformation> transformMap;
		private Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter;

		public FluidContainerItemOverrideList(IModelState state, VertexFormat format,
				ImmutableMap<ItemCameraTransforms.TransformType, TRSRTransformation> transformMap,
				Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
			super(ImmutableList.of());
			this.format = format;
			this.transformMap = transformMap;
			this.bakedTextureGetter = bakedTextureGetter;
		}

		@Override
		@Nonnull
		public IBakedModel handleItemState(@Nonnull IBakedModel originalModel, @Nonnull ItemStack stack, World world, EntityLivingBase entity) {
			FluidIdentifier fluidIdent = FluidIdentifier.get(stack);
			if (fluidIdent != null) {
				if (cache.containsKey(fluidIdent)) {
					return cache.get(fluidIdent);
				}

				Fluid fluid = fluidIdent.getFluid();

				ResourceLocation fluidSprite = fluid.getStill(fluidIdent.makeFluidStack(1000));

				TRSRTransformation transform = new SimpleModelState(transformMap).apply(Optional.empty())
						.orElse(TRSRTransformation.identity());
				ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();

				builder.addAll(
						ItemTextureQuadConverter
								.convertTexture(format, transform, this.bakedTextureGetter.apply(STENCIL), this.bakedTextureGetter.apply(fluidSprite),
										NORTH_Z, EnumFacing.NORTH, fluid.getColor()));
				builder.addAll(
						ItemTextureQuadConverter
								.convertTexture(format, transform, this.bakedTextureGetter.apply(STENCIL), this.bakedTextureGetter.apply(fluidSprite),
										SOUTH_Z, EnumFacing.SOUTH, fluid.getColor()));

				builder.addAll(originalModel.getQuads(null, null, 0));

				BakedItemModel override = new BakedItemModel(builder.build(), bakedTextureGetter.apply(fluidSprite), Maps.immutableEnumMap(transformMap),
						ItemOverrideList.NONE);
				cache.put(fluidIdent, override);
				return override;
			}
			return originalModel;
		}
	}
}
