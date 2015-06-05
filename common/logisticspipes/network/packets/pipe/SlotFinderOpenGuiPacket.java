package logisticspipes.network.packets.pipe;

import java.io.IOException;

import logisticspipes.interfaces.ISpecialInsertion;
import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.abstractpackets.ModuleCoordinatesPacket;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.interfaces.ICraftingRecipeProvider;
import logisticspipes.utils.AdjacentTile;
import logisticspipes.utils.WorldUtil;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
public class SlotFinderOpenGuiPacket extends ModuleCoordinatesPacket {

	@Getter
	@Setter
	private int slot;

	public SlotFinderOpenGuiPacket(int id) {
		super(id);
	}

	@Override
	public void processPacket(EntityPlayer player) {
		//hack to avoid wrenching blocks
		int savedEquipped = player.inventory.currentItem;
		boolean foundSlot = false;
		//try to find a empty slot
		for (int i = 0; i < 9; i++) {
			if (player.inventory.getStackInSlot(i) == null) {
				foundSlot = true;
				player.inventory.currentItem = i;
				break;
			}
		}
		//okay, anything that's a block?
		if (!foundSlot) {
			for (int i = 0; i < 9; i++) {
				ItemStack is = player.inventory.getStackInSlot(i);
				if (is.getItem() instanceof ItemBlock) {
					foundSlot = true;
					player.inventory.currentItem = i;
					break;
				}
			}
		}
		//give up and select whatever is right of the current slot
		if (!foundSlot) {
			player.inventory.currentItem = (player.inventory.currentItem + 1) % 9;
		}

		final WorldUtil worldUtil = new WorldUtil(player.worldObj, getPosX(), getPosY(), getPosZ());
		boolean found = false;
		for (final AdjacentTile tile : worldUtil.getAdjacentTileEntities(true)) {
			if (tile instanceof IInventory && !(SimpleServiceLocator.inventoryUtilFactory.getInventoryUtil((IInventory) tile) instanceof ISpecialInsertion)) {
				continue;
			}
			for (ICraftingRecipeProvider provider : SimpleServiceLocator.craftingRecipeProviders) {
				if (provider.canOpenGui(tile.tile)) {
					found = true;
					break;
				}
			}

			if (!found) {
				found = (tile.tile instanceof IInventory);
			}

			if (found) {
				Block block = player.worldObj.getBlock(tile.tile.xCoord, tile.tile.yCoord, tile.tile.zCoord);
				if (SimpleServiceLocator.enderStorageProxy.isEnderChestBlock(block)) {
					SimpleServiceLocator.enderStorageProxy.openEnderChest(player.worldObj, tile.tile.xCoord, tile.tile.yCoord, tile.tile.zCoord, player);
					MainProxy.sendPacketToPlayer(PacketHandler.getPacket(SlotFinderActivatePacket.class).setTagetPosX(tile.tile.xCoord).setTagetPosY(tile.tile.yCoord).setTagetPosZ(tile.tile.zCoord).setSlot(getSlot()).setPacketPos(this), player);
				}
				if (block != null) {
					if (block.onBlockActivated(player.worldObj, tile.tile.xCoord, tile.tile.yCoord, tile.tile.zCoord, player, 0, 0, 0, 0)) {
						MainProxy.sendPacketToPlayer(PacketHandler.getPacket(SlotFinderActivatePacket.class).setTagetPosX(tile.tile.xCoord).setTagetPosY(tile.tile.yCoord).setTagetPosZ(tile.tile.zCoord).setSlot(getSlot()).setPacketPos(this), player);
						break;
					}
				}
			}
		}

		player.inventory.currentItem = savedEquipped;
	}

	@Override
	public ModernPacket template() {
		return new SlotFinderOpenGuiPacket(getId());
	}

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeInt(slot);
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		super.readData(data);
		slot = data.readInt();
	}
}
