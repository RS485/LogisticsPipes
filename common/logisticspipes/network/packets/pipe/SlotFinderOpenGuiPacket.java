package logisticspipes.network.packets.pipe;

import java.util.Iterator;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.interfaces.ISpecialInsertion;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.abstractpackets.ModuleCoordinatesPacket;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.interfaces.ICraftingRecipeProvider;
import logisticspipes.routing.pathfinder.IPipeInformationProvider.ConnectionPipeType;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;
import network.rs485.logisticspipes.world.DoubleCoordinates;
import network.rs485.logisticspipes.world.IntegerCoordinates;
import network.rs485.logisticspipes.world.WorldCoordinatesWrapper;
import network.rs485.logisticspipes.world.WorldCoordinatesWrapper.AdjacentTileEntity;

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

		WorldCoordinatesWrapper worldCoordinates = new WorldCoordinatesWrapper(player.worldObj, getPosX(), getPosY(), getPosZ());
		Iterator<AdjacentTileEntity> adjacentIt = worldCoordinates.getConnectedAdjacentTileEntities(ConnectionPipeType.ITEM).iterator();

		boolean found = false;
		while (adjacentIt.hasNext()) {
			AdjacentTileEntity adjacent = adjacentIt.next();

			if (adjacent.tileEntity instanceof IInventory) {
				if (!(SimpleServiceLocator.inventoryUtilFactory.getInventoryUtil((IInventory) adjacent.tileEntity) instanceof ISpecialInsertion)) {
					continue;
				}
			}
			for (ICraftingRecipeProvider provider : SimpleServiceLocator.craftingRecipeProviders) {
				if (provider.canOpenGui(adjacent.tileEntity)) {
					found = true;
					break;
				}
			}

			if (!found) {
				found = (adjacent.tileEntity instanceof IInventory);
			}

			if (found) {
				Block block = adjacent.tileEntity.getBlockType();
				DoubleCoordinates pos = new DoubleCoordinates(adjacent.tileEntity.getPos());
				int xCoord = adjacent.tileEntity.getPos().getX();
				int yCoord = adjacent.tileEntity.getPos().getY();
				int zCoord = adjacent.tileEntity.getPos().getZ();

				if (SimpleServiceLocator.enderStorageProxy.isEnderChestBlock(block)) {
					SimpleServiceLocator.enderStorageProxy.openEnderChest(player.worldObj, xCoord, yCoord, zCoord, player);
					//@formatter:off
					MainProxy.sendPacketToPlayer(PacketHandler.getPacket(SlotFinderActivatePacket.class)
							.setTagetPosX(xCoord).setTagetPosY(yCoord).setTagetPosZ(zCoord).setSlot(getSlot()).setPacketPos(this), player);
					//@formatter:on
				}

				if (block != null) {
					if (block.onBlockActivated(player.worldObj, pos.getBlockPos(), pos.getBlockState(player.worldObj), player, EnumHand.MAIN_HAND, null,
							EnumFacing.UP, 0, 0, 0)) {
						//@formatter:off
						MainProxy.sendPacketToPlayer(PacketHandler.getPacket(SlotFinderActivatePacket.class)
								.setTagetPosX(xCoord).setTagetPosY(yCoord).setTagetPosZ(zCoord).setSlot(getSlot()).setPacketPos(this), player);
						//@formatter:on
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
	public void writeData(LPDataOutput output) {
		super.writeData(output);
		output.writeInt(slot);
	}

	@Override
	public void readData(LPDataInput input) {
		super.readData(input);
		slot = input.readInt();
	}
}
