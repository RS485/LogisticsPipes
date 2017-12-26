package logisticspipes.renderer.newpipe;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

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

/**
 * Created by davboecki on 21.09.2017.
 * All rights reserved.
 */
public class LogisticsNewPipeModel implements IModel {

	@Override
	public Collection<ResourceLocation> getDependencies() {
		return null;
	}

	@Override
	public Collection<ResourceLocation> getTextures() {
		return null;
	}

	@Override
	public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
		return new IBakedModel() {

			@Override
			public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
				return null;
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
				return null;
			}

			@Override
			public ItemCameraTransforms getItemCameraTransforms() {
				return null;
			}

			@Override
			public ItemOverrideList getOverrides() {
				return null;
			}
		};
	}

	@Override
	public IModelState getDefaultState() {
		return null;
	}
}
