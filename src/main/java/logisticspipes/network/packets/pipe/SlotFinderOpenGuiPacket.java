package logisticspipes.network.packets.pipe;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.ISpecialInsertion;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.abstractpackets.ModuleCoordinatesPacket;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.interfaces.ICraftingRecipeProvider;
import logisticspipes.utils.StaticResolve;
import network.rs485.logisticspipes.connection.LPNeighborTileEntityKt;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

@StaticResolve
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
			if (player.inventory.getStackInSlot(i).isEmpty()) {
				foundSlot = true;
				player.inventory.currentItem = i;
				break;
			}
		}
		//okay, anything that's a block?
		if (!foundSlot) {
			for (int i = 0; i < 9; i++) {
				ItemStack is = player.inventory.getStackInSlot(i);
				if (!is.isEmpty() && is.getItem() instanceof ItemBlock) {
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

		boolean openedGui = false;
		final LogisticsTileGenericPipe genericPipe = getPipe(player.world, LTGPCompletionCheck.PIPE);
		if (genericPipe.isRoutingPipe()) {
			openedGui = genericPipe.getRoutingPipe().getAvailableAdjacent().inventories().stream()
					.filter(neighbor -> LPNeighborTileEntityKt.getInventoryUtil(neighbor) instanceof ISpecialInsertion)
					.anyMatch(neighbor -> {
						for (ICraftingRecipeProvider provider : SimpleServiceLocator.craftingRecipeProviders) {
							if (provider.canOpenGui(neighbor.getTileEntity())) {
								return true;
							}
						}

						Block block = neighbor.getTileEntity().getBlockType();
						final BlockPos blockPos = neighbor.getTileEntity().getPos();
						final IBlockState blockState = player.world.getBlockState(blockPos);
						if (!block.isAir(blockState, player.world, blockPos)) {
							int xCoord = blockPos.getX();
							int yCoord = blockPos.getY();
							int zCoord = blockPos.getZ();

							if (SimpleServiceLocator.enderStorageProxy.isEnderChestBlock(block)) {
								SimpleServiceLocator.enderStorageProxy.openEnderChest(player.world, xCoord, yCoord, zCoord, player);
								MainProxy.sendPacketToPlayer(PacketHandler.getPacket(SlotFinderActivatePacket.class)
										.setTargetPosX(xCoord)
										.setTargetPosY(yCoord)
										.setTargetPosZ(zCoord)
										.setSlot(getSlot())
										.setPacketPos(this), player);
								return true;
							}

							if (block.onBlockActivated(player.world, blockPos, blockState, player, EnumHand.MAIN_HAND, EnumFacing.UP, 0, 0, 0)) {
								MainProxy.sendPacketToPlayer(PacketHandler.getPacket(SlotFinderActivatePacket.class)
										.setTargetPosX(xCoord)
										.setTargetPosY(yCoord)
										.setTargetPosZ(zCoord)
										.setSlot(getSlot())
										.setPacketPos(this), player);
								return true;
							}
						}

						return false;
					});
		}

		if (!openedGui) {
			LogisticsPipes.log.warn("Ignored SlotFinderOpenGuiPacket from " + player.toString() + ", because of failing preconditions");
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
