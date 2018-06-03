package logisticspipes.renderer.newpipe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.PerspectiveMapWrapper;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import logisticspipes.blocks.LogisticsSolidBlock;
import logisticspipes.pipes.PipeBlockRequestTable;
import logisticspipes.pipes.basic.CoreUnroutedPipe;
import logisticspipes.pipes.basic.LogisticsBlockGenericPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.object3d.interfaces.IModel3D;
import logisticspipes.proxy.object3d.interfaces.TextureTransformation;
import logisticspipes.proxy.object3d.operation.LPUVScale;
import logisticspipes.proxy.object3d.operation.LPUVTransformationList;
import logisticspipes.renderer.LogisticsRenderPipe;
import logisticspipes.textures.Textures;
import logisticspipes.textures.provider.LPPipeIconTransformerProvider;

public class LogisticsNewPipeModel implements IModel {

	public static class LogisticsNewPipeModelLoader implements ICustomModelLoader {

		@Override
		public boolean accepts(ResourceLocation modelLocation) {
			if (modelLocation.getResourceDomain().equals("logisticspipes")) {
				if(modelLocation instanceof ModelResourceLocation) {
					if(((ModelResourceLocation)modelLocation).getVariant().equals("inventory")) {
						return LogisticsNewPipeModel.nameTextureIdMap.containsKey(modelLocation);
					}
					if(modelLocation.getResourcePath().equals("tile.logisticsblockgenericpipe")) {
						return true;
					}
				}
			}
			return false;
		}

		@Override
		public IModel loadModel(ResourceLocation modelLocation) {
			return new LogisticsNewPipeModel((ModelResourceLocation) modelLocation);
		}

		@Override
		public void onResourceManagerReload(IResourceManager resourceManager) {

		}
	}

	public static Map<ModelResourceLocation, CoreUnroutedPipe> nameTextureIdMap = Maps.newLinkedHashMap();
	private ModelResourceLocation key;

	public LogisticsNewPipeModel(ModelResourceLocation resource) {
		key = resource;
	}

	@Override
	public Collection<ResourceLocation> getDependencies() {
		return Lists.newArrayList();
	}

	@Override
	public Collection<ResourceLocation> getTextures() {
		return Lists.newArrayList();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
		final List<BakedQuad> quads = Lists.newArrayList();
		return new IBakedModel() {

			@Override
			@SideOnly(Side.CLIENT)
			public List<BakedQuad> getQuads(@Nullable IBlockState blockstate, @Nullable EnumFacing side, long rand) {
				if(blockstate != null) {
					if (side == null) {
						if (quads.isEmpty()) {
							quads.addAll(LogisticsRenderPipe.secondRenderer.getQuadsFromRenderList(generatePipeRenderList(blockstate), format));
						}
						return quads;
					}
				} else {
					if (quads.isEmpty()) {
						quads.addAll(LogisticsRenderPipe.secondRenderer.getQuadsFromRenderList(generatePipeRenderList(), format));
					}
					return quads;
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
			public TextureAtlasSprite getParticleTexture() {
				if(getPipe() == null) {
					return null;
				}
				return Textures.LPnewPipeIconProvider.getIcon(getPipe().getTextureIndex()).getTexture();
			}

			@Override
			public ItemOverrideList getOverrides() {
				return ItemOverrideList.NONE;
			}

			@Override
			public org.apache.commons.lang3.tuple.Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) {
				return PerspectiveMapWrapper.handlePerspective(this, SimpleServiceLocator.cclProxy.getDefaultBlockState(), cameraTransformType);
			}
		};
	}

	private List<RenderEntry> generatePipeRenderList(IBlockState blockstate) {
		List<RenderEntry> objectsToRender = new ArrayList<>();

		if(blockstate.getValue(LogisticsBlockGenericPipe.modelTypeProperty) == LogisticsBlockGenericPipe.PipeRenderModel.REQUEST_TABLE) {
			TextureTransformation icon = SimpleServiceLocator.cclProxy.createIconTransformer(Textures.LOGISTICS_REQUEST_TABLE_NEW);

			LogisticsNewSolidBlockWorldRenderer.BlockRotation rotation = LogisticsNewSolidBlockWorldRenderer.BlockRotation.getRotation(blockstate.getValue(LogisticsBlockGenericPipe.rotationProperty));

			//Draw
			objectsToRender.add(new RenderEntry(LogisticsNewSolidBlockWorldRenderer.block.get(rotation), icon));
			for (LogisticsNewSolidBlockWorldRenderer.CoverSides side : LogisticsNewSolidBlockWorldRenderer.CoverSides.values()) {
				if(blockstate.getValue(LogisticsBlockGenericPipe.connectionPropertys.get(side.getDir(rotation)))) {

				}
				objectsToRender.add(new RenderEntry(LogisticsNewSolidBlockWorldRenderer.texturePlate_Outer.get(side).get(rotation), icon));
			}
		}

		return objectsToRender;
	}

	private CoreUnroutedPipe getPipe() {
		return nameTextureIdMap.get(key);
	}
	private List<RenderEntry> generatePipeRenderList() {
		List<RenderEntry> objectsToRender = new ArrayList<>();

		if(getPipe() == null) {

			System.out.println("'" + key + "' does not result in pipe");
		} else if(getPipe() instanceof PipeBlockRequestTable) {
			TextureTransformation icon = SimpleServiceLocator.cclProxy.createIconTransformer(Textures.LOGISTICS_REQUEST_TABLE_NEW);

			LogisticsNewSolidBlockWorldRenderer.BlockRotation rotation = LogisticsNewSolidBlockWorldRenderer.BlockRotation.ZERO;

			//Draw
			objectsToRender.add(new RenderEntry(LogisticsNewSolidBlockWorldRenderer.block.get(rotation), icon));
			for (LogisticsNewSolidBlockWorldRenderer.CoverSides side : LogisticsNewSolidBlockWorldRenderer.CoverSides.values()) {
				objectsToRender.add(new RenderEntry(LogisticsNewSolidBlockWorldRenderer.texturePlate_Outer.get(side).get(rotation), icon));
			}
		} else {
			int red = 0;
			boolean toggle = Math.random() < 0.5;
			for (LogisticsNewRenderPipe.Corner corner : LogisticsNewRenderPipe.Corner.values()) {
				final int fred = red;
				final boolean ftoggle = toggle;
				objectsToRender.addAll(LogisticsNewRenderPipe.corners_M.get(corner).stream()
						.map(model -> new RenderEntry(model, ftoggle && (fred % 4 == 0 || fred % 4 == 3) || !ftoggle && (fred % 4 == 1 || fred % 4 == 2) ? LogisticsNewRenderPipe.inactiveTexture : LogisticsNewRenderPipe.basicPipeTexture))
						.collect(Collectors.toList()));
				red++;
				if(red > 3) {
					red -= 4;
					toggle = !toggle;
				}
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
						objectsToRender.add(new RenderEntry(model, new LPUVTransformationList(new LPUVScale(12f/16, 12f/16), icon)));
					}
				}
			}
		}
		return objectsToRender;
	}
}
