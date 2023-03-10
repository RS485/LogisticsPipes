package logisticspipes.pipes;

import java.util.BitSet;

import javax.annotation.Nullable;

import logisticspipes.modules.ModuleFirewall;

import lombok.Getter;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.modules.LogisticsModule;
import logisticspipes.network.GuiIDs;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.pipe.FireWallFlag;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.request.resources.IResource;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;
import network.rs485.logisticspipes.world.DoubleCoordinates;

public class PipeItemsFirewall extends CoreRoutedPipe {

	@Getter
	private final ModuleFirewall moduleFirewall;
	private IFilter filter = null;

	public PipeItemsFirewall(Item item) {
		super(item);
		moduleFirewall = new ModuleFirewall();
		moduleFirewall.registerHandler(this, this);
		moduleFirewall.registerPosition(LogisticsModule.ModulePositionType.IN_PIPE, 0);
	}

	@Nullable
	@Override
	public LogisticsModule getLogisticsModule() {
		return this.moduleFirewall;
	}

	@Override
	public ItemSendMode getItemSendMode() {
		return ItemSendMode.Normal;
	}

	@Override
	public void onWrenchClicked(EntityPlayer entityplayer) {
		entityplayer.openGui(LogisticsPipes.instance, GuiIDs.GUI_FIREWALL, getWorld(), getX(), getY(), getZ());
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(FireWallFlag.class).setFlags(getFlags()).setPosX(getX()).setPosY(getY()).setPosZ(getZ()), entityplayer);
	}

	@Override
	public TextureType getCenterTexture() {
		return Textures.LOGISTICSPIPE_FIREWALL_TEXTURE;
	}

	public IFilter getFilter() {
		if (filter == null) {
			filter = new IFilter() {

				@Override
				public boolean isBlocked() {
					return moduleFirewall.isBlocking.getValue();
				}

				@Override
				public boolean isFilteredItem(ItemIdentifier item) {
					return moduleFirewall.inv.containsUndamagedExcludeNBTItem(item.getIgnoringNBT().getUndamaged());
				}

				@Override
				public boolean blockProvider() {
					return moduleFirewall.blockProvider.getValue();
				}

				@Override
				public boolean blockCrafting() {
					return moduleFirewall.blockCrafter.getValue();
				}

				@Override
				public boolean blockRouting() {
					return moduleFirewall.blockSorting.getValue();
				}

				@Override
				public boolean blockPower() {
					return moduleFirewall.blockPower.getValue();
				}

				@Override
				public int hashCode() {
					return PipeItemsFirewall.this.hashCode();
				}

				@Override
				public String toString() {
					return super.toString() + " (" + PipeItemsFirewall.this.getX() + ", " + PipeItemsFirewall.this.getY() + ", " + PipeItemsFirewall.this.getZ() + ")";
				}

				@Override
				public DoubleCoordinates getLPPosition() {
					return PipeItemsFirewall.this.getLPPosition();
				}

				@Override
				public boolean isFilteredItem(IResource resultItem) {
					for (ItemIdentifierStack stack : moduleFirewall.inv) {
						if (stack != null && resultItem.matches(stack.getItem(), IResource.MatchSettings.NORMAL)) {
							return true;
						}
					}
					return false;
				}
			};
		}
		return filter;
	}

	public boolean isBlockProvider() {
		return moduleFirewall.blockProvider.getValue();
	}

	public void setBlockProvider(boolean blockProvider) {
		moduleFirewall.blockProvider.setValue(blockProvider);
		MainProxy.sendPacketToServer(PacketHandler.getPacket(FireWallFlag.class).setFlags(getFlags()).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
	}

	public boolean isBlockCrafter() {
		return moduleFirewall.blockCrafter.getValue();
	}

	public void setBlockCrafer(boolean blockCrafter) {
		moduleFirewall.blockCrafter.setValue(blockCrafter);
		MainProxy.sendPacketToServer(PacketHandler.getPacket(FireWallFlag.class).setFlags(getFlags()).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
	}

	public boolean isBlockSorting() {
		return moduleFirewall.blockSorting.getValue();
	}

	public void setBlockSorting(boolean blockSorting) {
		moduleFirewall.blockSorting.setValue(blockSorting);
		MainProxy.sendPacketToServer(PacketHandler.getPacket(FireWallFlag.class).setFlags(getFlags()).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
	}

	public boolean isBlockPower() {
		return moduleFirewall.blockPower.getValue();
	}

	public void setBlockPower(boolean blockPower) {
		moduleFirewall.blockPower.setValue(blockPower);
		MainProxy.sendPacketToServer(PacketHandler.getPacket(FireWallFlag.class).setFlags(getFlags()).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
	}

	public boolean isBlocking() {
		return moduleFirewall.isBlocking.getValue();
	}

	public void setBlocking(boolean isBlocking) {
		moduleFirewall.isBlocking.setValue(isBlocking);
		MainProxy.sendPacketToServer(PacketHandler.getPacket(FireWallFlag.class).setFlags(getFlags()).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
	}

	private BitSet getFlags() {
		BitSet flags = new BitSet();
		flags.set(0, moduleFirewall.blockProvider.getValue());
		flags.set(1, moduleFirewall.blockCrafter.getValue());
		flags.set(2, moduleFirewall.blockSorting.getValue());
		flags.set(3, moduleFirewall.blockPower.getValue());
		flags.set(4, moduleFirewall.isBlocking.getValue());
		return flags;
	}

	public void setFlags(BitSet flags) {
		moduleFirewall.blockProvider.setValue(flags.get(0));
		moduleFirewall.blockCrafter.setValue(flags.get(1));
		moduleFirewall.blockSorting.setValue(flags.get(2));
		moduleFirewall.blockPower.setValue(flags.get(3));
		moduleFirewall.isBlocking.setValue(flags.get(4));
	}

	@Override
	public boolean hasGenericInterests() {
		return true;
	}
}
