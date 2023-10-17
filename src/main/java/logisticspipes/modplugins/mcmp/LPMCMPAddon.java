package logisticspipes.modplugins.mcmp;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import mcmultipart.api.addon.IMCMPAddon;
import mcmultipart.api.addon.MCMPAddon;
import mcmultipart.api.multipart.IMultipartRegistry;
import mcmultipart.api.multipart.IMultipartTile;
import mcmultipart.api.ref.MCMPCapabilities;

import logisticspipes.LPBlocks;
import logisticspipes.LPConstants;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;

@MCMPAddon
public class LPMCMPAddon implements IMCMPAddon {

	public static final LPPipeMultipart lpPipeMultipart = new LPPipeMultipart();

	@Override
	public void registerParts(IMultipartRegistry registry) {
		MinecraftForge.EVENT_BUS.register(this);
		registry.registerPartWrapper(LPBlocks.pipe, lpPipeMultipart);
	}

	@SubscribeEvent
	public void attachCapability(AttachCapabilitiesEvent<TileEntity> e) {
		final TileEntity tile = e.getObject();
		if (tile instanceof LogisticsTileGenericPipe) {
			e.addCapability(new ResourceLocation(LPConstants.LP_MOD_ID, "mcmpaddon.cap"), new ICapabilityProvider() {

				@Override
				public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
					return capability == MCMPCapabilities.MULTIPART_TILE;
				}

				@Nullable
				@Override
				public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
					if (capability == MCMPCapabilities.MULTIPART_TILE) {
						//noinspection unchecked
						return (T) IMultipartTile.wrap(tile);
					}
					return null;
				}
			});
		}
	}
}
