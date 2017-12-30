package logisticspipes.renderer.newpipe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.client.model.IModel;
import net.minecraftforge.common.model.IModelState;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import logisticspipes.pipes.basic.CoreUnroutedPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.object3d.interfaces.IModel3D;
import logisticspipes.proxy.object3d.interfaces.TextureTransformation;
import logisticspipes.renderer.LogisticsRenderPipe;
import logisticspipes.textures.Textures;
import logisticspipes.utils.tuples.Pair;

public class LogisticsNewPipeModel implements IModel {
	public static class REF {
		public CoreUnroutedPipe object;
		public REF(CoreUnroutedPipe object) {
			this.object = object;
		}
	}

	public static Map<String, REF> nameTextureIdMap = Maps.newLinkedHashMap();
	private ResourceLocation key;

	public LogisticsNewPipeModel(ResourceLocation resource) {
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
	public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
		return new IBakedModel() {

			@Override
			public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
				return LogisticsRenderPipe.secondRenderer.getQuadsFromRenderList(generatePipeRenderList(nameTextureIdMap.get(key).object.getTextureIndex()), format);
			}

			@Override
			public boolean isAmbientOcclusion() {
				return false;
			}

			@Override
			public boolean isGui3d() {
				return false;
			}

			@Override
			public boolean isBuiltInRenderer() {
				return false;
			}

			@Override
			public TextureAtlasSprite getParticleTexture() {
				return Textures.LPnewPipeIconProvider.getIcon(nameTextureIdMap.get(key).object.getTextureIndex()).getTexture();
			}

			@Override
			public ItemCameraTransforms getItemCameraTransforms() {
				return ItemCameraTransforms.DEFAULT;
			}

			@Override
			public ItemOverrideList getOverrides() {
				return ItemOverrideList.NONE;
			}
		};
	}

	@Override
	public IModelState getDefaultState() {
		return null;
	}


	private List<RenderEntry> generatePipeRenderList(int texture) {
		List<RenderEntry> objectsToRender = new ArrayList<>();

		for (LogisticsNewRenderPipe.Corner corner : LogisticsNewRenderPipe.Corner.values()) {
			objectsToRender.addAll(LogisticsNewRenderPipe.corners_M.get(corner).stream()
					.map(model -> new RenderEntry(model, LogisticsNewRenderPipe.basicPipeTexture))
					.collect(Collectors.toList()));
		}

		for (LogisticsNewRenderPipe.Edge edge : LogisticsNewRenderPipe.Edge.values()) {
			objectsToRender.add(new RenderEntry(LogisticsNewRenderPipe.edges
					.get(edge), LogisticsNewRenderPipe.basicPipeTexture));
		}

		//ArrayList<Pair<CCModel, IconTransformation>> objectsToRender2 = new ArrayList<Pair<CCModel, IconTransformation>>();
		for (EnumFacing dir : EnumFacing.VALUES) {
			for (IModel3D model : LogisticsNewRenderPipe.texturePlate_Outer.get(dir)) {
				TextureTransformation icon = Textures.LPnewPipeIconProvider.getIcon(texture);
				if (icon != null) {
					objectsToRender.add(new RenderEntry(model, icon));
				}
			}
		}
		return objectsToRender;
	}
}
