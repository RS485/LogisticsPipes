package logisticspipes.renderer.newpipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.item.Item;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;

import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.PerspectiveMapWrapper;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.common.cache.Cache;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import logisticspipes.LPBlocks;
import logisticspipes.items.ItemLogisticsPipe;
import logisticspipes.pipes.PipeBlockRequestTable;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.CoreUnroutedPipe;
import logisticspipes.pipes.basic.LogisticsBlockGenericPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.object3d.interfaces.I3DOperation;
import logisticspipes.proxy.object3d.interfaces.IModel3D;
import logisticspipes.proxy.object3d.interfaces.TextureTransformation;
import logisticspipes.proxy.object3d.operation.LPScale;
import logisticspipes.proxy.object3d.operation.LPTranslation;
import logisticspipes.proxy.object3d.operation.LPUVScale;
import logisticspipes.proxy.object3d.operation.LPUVTransformationList;
import logisticspipes.renderer.LogisticsRenderPipe;
import logisticspipes.renderer.state.PipeRenderState;
import logisticspipes.textures.Textures;

public class LogisticsNewPipeModel implements IModel {

	private static final ResourceLocation BASE_TEXTURE = new ResourceLocation("logisticspipes", "blocks/blank_pipe");
	public static TextureAtlasSprite BASE_TEXTURE_SPRITE;
	public static TextureTransformation BASE_TEXTURE_TRANSFORM;

	public static void registerTextures(TextureMap iconRegister) {
		BASE_TEXTURE_SPRITE = iconRegister.registerSprite(BASE_TEXTURE);
		if (BASE_TEXTURE_TRANSFORM == null) {
			BASE_TEXTURE_TRANSFORM = SimpleServiceLocator.cclProxy.createIconTransformer(BASE_TEXTURE_SPRITE);
		} else {
			BASE_TEXTURE_TRANSFORM.update(BASE_TEXTURE_SPRITE);
		}
	}

	public static class LogisticsNewPipeModelLoader implements ICustomModelLoader {

		@Override
		public boolean accepts(ResourceLocation modelLocation) {
			if (modelLocation.getResourceDomain().equals("logisticspipes")) {
				if (modelLocation instanceof ModelResourceLocation) {
					ResourceLocation rl = new ResourceLocation(modelLocation.getResourceDomain(), modelLocation.getResourcePath());
					if (((ModelResourceLocation) modelLocation).getVariant().equals("inventory")) {
						Item item = ForgeRegistries.ITEMS.getValue(rl);
						if (item instanceof ItemLogisticsPipe) {
							CoreUnroutedPipe pipe = ((ItemLogisticsPipe) item).getDummyPipe();
							nameTextureIdMap.put((ModelResourceLocation) modelLocation, pipe);
							return true;
						}
					}
					if (rl.equals(LPBlocks.pipe.getRegistryName())) {
						return true;
					}
				}
			}
			return false;
		}

		@Nonnull
		@Override
		public IModel loadModel(@Nonnull ResourceLocation modelLocation) {
			return new LogisticsNewPipeModel((ModelResourceLocation) modelLocation);
		}

		@Override
		public void onResourceManagerReload(@Nonnull IResourceManager resourceManager) {

		}
	}

	public static Map<ModelResourceLocation, CoreUnroutedPipe> nameTextureIdMap = Maps.newLinkedHashMap();
	private ModelResourceLocation key;

	public LogisticsNewPipeModel(ModelResourceLocation resource) {
		key = resource;
	}

	@Override
	@Nonnull
	public Collection<ResourceLocation> getDependencies() {
		return Collections.emptyList();
	}

	@Override
	@Nonnull
	public Collection<ResourceLocation> getTextures() {
		return Collections.emptyList();
	}

	@Override
	@SideOnly(Side.CLIENT)
	@Nonnull
	public IBakedModel bake(@Nonnull IModelState state, @Nonnull VertexFormat format, @Nonnull Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
		return new IBakedModel() {
			private ArrayList<BakedQuad> quads = null;

			@Override
			@SideOnly(Side.CLIENT)
			@Nonnull
			public List<BakedQuad> getQuads(@Nullable IBlockState blockstate, @Nullable EnumFacing side, long rand) {
				BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();
				ArrayList<BakedQuad> result;
				if (layer == BlockRenderLayer.CUTOUT || layer == null || blockstate == null) {
					result = getLPQuads(blockstate, side);
				} else {
					result = Lists.newArrayList();
				}
				addOtherQuads(result, blockstate, side, rand);
				return result;
			}

			private void addOtherQuads(@Nonnull List<BakedQuad> list, IBlockState blockstate, EnumFacing side, long rand) {
				if (blockstate != null) {
					SimpleServiceLocator.mcmpProxy.addQuads(list, blockstate, side, rand);
				}
			}

			private ArrayList<BakedQuad> getLPQuads(@Nullable IBlockState blockstate, @Nullable EnumFacing side) {
				if (blockstate != null) {
					if (side == null) {
						IExtendedBlockState eState = (IExtendedBlockState) blockstate;
						Cache<PipeRenderState.LocalCacheType, Object> objectCache = eState.getValue(LogisticsBlockGenericPipe.propertyCache);
						if (objectCache != null) {
							Object pipeQuads = objectCache.getIfPresent(PipeRenderState.LocalCacheType.QUADS);
							if (pipeQuads instanceof Collection) {
								//noinspection unchecked
								return new ArrayList<>((Collection<? extends BakedQuad>) pipeQuads);
							}
						}
						final ArrayList<BakedQuad> pipeQuads = LogisticsRenderPipe.secondRenderer.getQuadsFromRenderList(generatePipeRenderList(blockstate), format, true);

						if (objectCache != null) {
							objectCache.put(PipeRenderState.LocalCacheType.QUADS, new ArrayList<>(pipeQuads));
						}

						return pipeQuads;
					}
				} else {
					if (quads == null) {
						quads = LogisticsRenderPipe.secondRenderer.getQuadsFromRenderList(generatePipeRenderList(), format, true);
					}
					return new ArrayList<>(quads);
				}
				return Lists.newArrayList();
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
				return BASE_TEXTURE_SPRITE;
			}

			@Override
			@Nonnull
			public ItemOverrideList getOverrides() {
				return ItemOverrideList.NONE;
			}

			@Override
			@Nonnull
			public org.apache.commons.lang3.tuple.Pair<? extends IBakedModel, Matrix4f> handlePerspective(@Nonnull ItemCameraTransforms.TransformType cameraTransformType) {
				return PerspectiveMapWrapper.handlePerspective(this, SimpleServiceLocator.cclProxy.getDefaultBlockState(), cameraTransformType);
			}
		};
	}

	private List<RenderEntry> generatePipeRenderList(IBlockState blockstate) {
		List<RenderEntry> objectsToRender = new ArrayList<>();

		if (blockstate.getValue(LogisticsBlockGenericPipe.modelTypeProperty) == LogisticsBlockGenericPipe.PipeRenderModel.REQUEST_TABLE) {
			TextureTransformation icon = SimpleServiceLocator.cclProxy.createIconTransformer((TextureAtlasSprite) Textures.LOGISTICS_REQUEST_TABLE_NEW);

			LogisticsNewSolidBlockWorldRenderer.BlockRotation rotation = LogisticsNewSolidBlockWorldRenderer.BlockRotation.getRotation(blockstate.getValue(LogisticsBlockGenericPipe.rotationProperty));

			//Draw
			objectsToRender.add(new RenderEntry(LogisticsNewSolidBlockWorldRenderer.block.get(rotation), icon));
			for (LogisticsNewSolidBlockWorldRenderer.CoverSides side : LogisticsNewSolidBlockWorldRenderer.CoverSides.values()) {
				if (!blockstate.getValue(LogisticsBlockGenericPipe.connectionPropertys.get(side.getDir(rotation)))) {
					objectsToRender.add(new RenderEntry(LogisticsNewSolidBlockWorldRenderer.texturePlate_Outer.get(side).get(rotation), icon));
				}
			}
		} else if (blockstate instanceof IExtendedBlockState) {
			IExtendedBlockState lpState = (IExtendedBlockState) blockstate;
			objectsToRender = lpState.getValue(LogisticsBlockGenericPipe.propertyRenderList);
		}

		if (objectsToRender != null) {
			return objectsToRender;
		} else {
			return Collections.emptyList();
		}
	}

	private CoreUnroutedPipe getPipe() {
		return nameTextureIdMap.get(key);
	}

	private List<RenderEntry> generatePipeRenderList() {
		ArrayList<RenderEntry> objectsToRender = new ArrayList<>();

		if (getPipe() == null) {

			System.out.println("'" + key + "' does not result in pipe");
		} else if (getPipe() instanceof PipeBlockRequestTable) {
			TextureTransformation icon = SimpleServiceLocator.cclProxy.createIconTransformer((TextureAtlasSprite) Textures.LOGISTICS_REQUEST_TABLE_NEW);

			LogisticsNewSolidBlockWorldRenderer.BlockRotation rotation = LogisticsNewSolidBlockWorldRenderer.BlockRotation.ZERO;

			//Draw
			objectsToRender.add(new RenderEntry(LogisticsNewSolidBlockWorldRenderer.block.get(rotation), icon));
			for (LogisticsNewSolidBlockWorldRenderer.CoverSides side : LogisticsNewSolidBlockWorldRenderer.CoverSides.values()) {
				objectsToRender.add(new RenderEntry(LogisticsNewSolidBlockWorldRenderer.texturePlate_Outer.get(side).get(rotation), icon));
			}
		} else if (getPipe().getSpecialRenderer() != null) {
			getPipe().getSpecialRenderer().renderToList(null, objectsToRender);
			AxisAlignedBB[] bb = new AxisAlignedBB[1];
			bb[0] = new AxisAlignedBB(0.5, 0.5, 0.5, 0.5, 0.5, 0.5);
			objectsToRender.forEach(it -> bb[0] = bb[0].union(it.getModel().bounds().toAABB()));

			double size = Math.max(Math.max(bb[0].maxX - bb[0].minX, bb[0].maxY - bb[0].minY), bb[0].maxZ - bb[0].minZ);
			objectsToRender.replaceAll(it -> {
				RenderEntry content = it.clone(new I3DOperation[] { new LPUVTransformationList(BASE_TEXTURE_TRANSFORM) });
				content.getModel().apply(new LPScale(0.95 / size));
				return content;
			});

			bb[0] = new AxisAlignedBB(0.5, 0.5, 0.5, 0.5, 0.5, 0.5);
			objectsToRender.forEach(it -> bb[0] = bb[0].union(it.getModel().bounds().toAABB()));
			objectsToRender.forEach(it -> it.getModel().apply(new LPTranslation(0.5 - (bb[0].maxX + bb[0].minX) / 2, 0.5 - (bb[0].maxY + bb[0].minY) / 2, 0.5 - (bb[0].maxZ + bb[0].minZ) / 2)));

		} else {
			if (getPipe() instanceof CoreRoutedPipe) {
				int red = 0;
				boolean toggle = Math.random() < 0.5;
				for (LogisticsNewRenderPipe.Corner corner : LogisticsNewRenderPipe.Corner.values()) {
					final int fred = red;
					final boolean ftoggle = toggle;
					LogisticsNewRenderPipe.corners_M.get(corner).stream()
							.map(model -> new RenderEntry(model, ftoggle && (fred % 4 == 0 || fred % 4 == 3) || !ftoggle && (fred % 4 == 1 || fred % 4 == 2) ?
									LogisticsNewRenderPipe.inactiveTexture :
									LogisticsNewRenderPipe.basicPipeTexture))
							.forEach(objectsToRender::add);
					red++;
					if (red > 3) {
						red -= 4;
						toggle = !toggle;
					}
				}
			} else {
				Arrays.stream(LogisticsNewRenderPipe.Corner.values())
						.flatMap(corner -> LogisticsNewRenderPipe.corners_M.get(corner).stream().map(model -> new RenderEntry(model, LogisticsNewRenderPipe.basicPipeTexture)))
						.forEach(objectsToRender::add);
			}

			for (LogisticsNewRenderPipe.Edge edge : LogisticsNewRenderPipe.Edge.values()) {
				objectsToRender.add(new RenderEntry(LogisticsNewRenderPipe.edges
						.get(edge), LogisticsNewRenderPipe.basicPipeTexture));
			}

			//ArrayList<Pair<CCModel, IconTransformation>> objectsToRender2 = new ArrayList<Pair<CCModel, IconTransformation>>();
			for (EnumFacing dir : EnumFacing.VALUES) {
				for (IModel3D model : LogisticsNewRenderPipe.texturePlate_Outer.get(dir)) {
					TextureTransformation icon = Textures.LPnewPipeIconProvider.getIcon(getPipe().getTextureIndex());
					if (icon != null) {
						objectsToRender.add(new RenderEntry(model, new LPUVTransformationList(new LPUVScale(12f / 16, 12f / 16), icon)));
					}
				}
			}
		}
		return objectsToRender;
	}
}
