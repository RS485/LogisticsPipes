package logisticspipes.pipes;

import java.util.BitSet;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.modules.LogisticsModule;
import logisticspipes.network.GuiIDs;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.pipe.FireWallFlag;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.security.SecuritySettings;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatMessageComponent;
import cpw.mods.fml.common.network.Player;

public class PipeItemsFirewall extends CoreRoutedPipe {

	public ItemIdentifierInventory inv = new ItemIdentifierInventory(6 * 6, "Filter Inv", 1);
	private boolean blockProvider = false;
	private boolean blockCrafer = false;
	private boolean blockSorting = false;
	private boolean blockPower = true;
	private boolean isBlocking = true;
	
	public PipeItemsFirewall(int itemID) {
		super(itemID);
	}

	@Override
	public ItemSendMode getItemSendMode() {
		return ItemSendMode.Normal;
	}
	
	@Override
	public boolean wrenchClicked(EntityPlayer entityplayer, SecuritySettings settings) {
		if(MainProxy.isServer(getWorld())) {
			if (settings == null || settings.openGui) {
				entityplayer.openGui(LogisticsPipes.instance, GuiIDs.GUI_FIREWALL, getWorld(), getX(), getY(), getZ());
				MainProxy.sendPacketToPlayer(PacketHandler.getPacket(FireWallFlag.class).setFlags(getFlags()).setPosX(getX()).setPosY(getY()).setPosZ(getZ()), (Player) entityplayer);
			} else {
				entityplayer.sendChatToPlayer(ChatMessageComponent.createFromText("Permission denied"));
			}
		}
		return true;
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);
		inv.writeToNBT(nbttagcompound);
		nbttagcompound.setBoolean("blockProvider", blockProvider);
		nbttagcompound.setBoolean("blockCrafer", blockCrafer);
		nbttagcompound.setBoolean("blockSorting", blockSorting);
		nbttagcompound.setBoolean("blockPower", blockPower);
		nbttagcompound.setBoolean("isBlocking", isBlocking);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);
		inv.readFromNBT(nbttagcompound);
		blockProvider = nbttagcompound.getBoolean("blockProvider");
		blockCrafer = nbttagcompound.getBoolean("blockCrafer");
		blockSorting = nbttagcompound.getBoolean("blockSorting");
		if(nbttagcompound.hasKey("blockPower")) {
			blockPower = nbttagcompound.getBoolean("blockPower");
		}
		isBlocking = nbttagcompound.getBoolean("isBlocking");
	}

	@Override
	public TextureType getCenterTexture() {
		return Textures.LOGISTICSPIPE_FIREWALL_TEXTURE;
	}

	@Override
	public LogisticsModule getLogisticsModule() {
		return null;
	}
	
	public IFilter getFilter() {
		return new IFilter() {
			@Override
			public boolean isBlocked() {
				return isBlocking;
			}

			@Override
			public boolean isFilteredItem(ItemIdentifier item) {
				return inv.containsUndamagedItem(item);
			}

			@Override
			public boolean blockProvider() {
				return blockProvider;
			}

			@Override
			public boolean blockCrafting() {
				return blockCrafer;
			}

			@Override
			public boolean blockRouting() {
				return blockSorting;
			}

			@Override
			public boolean blockPower() {
				return blockPower;
			}
		};
	}

	public boolean isBlockProvider() {
		return blockProvider;
	}

	public void setBlockProvider(boolean blockProvider) {
		this.blockProvider = blockProvider;
		MainProxy.sendPacketToServer(PacketHandler.getPacket(FireWallFlag.class).setFlags(getFlags()).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
	}

	public boolean isBlockCrafer() {
		return blockCrafer;
	}

	public void setBlockCrafer(boolean blockCrafer) {
		this.blockCrafer = blockCrafer;
		MainProxy.sendPacketToServer(PacketHandler.getPacket(FireWallFlag.class).setFlags(getFlags()).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
	}

	public boolean isBlockSorting() {
		return blockSorting;
	}

	public void setBlockSorting(boolean blockSorting) {
		this.blockSorting = blockSorting;
		MainProxy.sendPacketToServer(PacketHandler.getPacket(FireWallFlag.class).setFlags(getFlags()).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
	}

	public boolean isBlockPower() {
		return blockPower;
	}

	public void setBlockPower(boolean blockPower) {
		this.blockPower = blockPower;
		MainProxy.sendPacketToServer(PacketHandler.getPacket(FireWallFlag.class).setFlags(getFlags()).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
	}

	public boolean isBlocking() {
		return isBlocking;
	}

	public void setBlocking(boolean isBlocking) {
		this.isBlocking = isBlocking;
		MainProxy.sendPacketToServer(PacketHandler.getPacket(FireWallFlag.class).setFlags(getFlags()).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
	}
	
	private BitSet getFlags() {
		BitSet flags = new BitSet();
		flags.set(0, blockProvider);
		flags.set(1, blockCrafer);
		flags.set(2, blockSorting);
		flags.set(3, blockPower);
		flags.set(4, isBlocking);
		return flags;
	}
	
	public void setFlags(BitSet flags) {
		blockProvider = flags.get(0);
		blockCrafer = flags.get(1);
		blockSorting = flags.get(2);
		blockPower = flags.get(3);
		isBlocking = flags.get(4);
	}

	@Override
	public boolean hasGenericInterests() {
		return true;
	}
}
