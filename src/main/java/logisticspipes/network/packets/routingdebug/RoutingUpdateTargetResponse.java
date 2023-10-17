package logisticspipes.network.packets.routingdebug;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.commands.chathelper.LPChatListener;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.packets.gui.OpenChatGui;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.routing.ServerRouter;
import logisticspipes.routing.debug.DebugController;
import logisticspipes.utils.StaticResolve;
import logisticspipes.utils.string.ChatColor;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

@StaticResolve
public class RoutingUpdateTargetResponse extends ModernPacket {

	@Getter
	@Setter
	private TargetMode mode;
	@Getter
	@Setter
	private int[] additions = new int[0];

	public RoutingUpdateTargetResponse(int id) {
		super(id);
	}

	@Override
	public void readData(LPDataInput input) {
		mode = TargetMode.values()[input.readByte()];
		additions = input.readIntArray();
	}

	@Override
	public void processPacket(final EntityPlayer player) {
		if (mode == TargetMode.None) {
			player.sendMessage(new TextComponentString(ChatColor.RED + "No Target Found"));
		} else if (mode == TargetMode.Block) {
			int x = additions[0];
			int y = additions[1];
			int z = additions[2];
			player.sendMessage(new TextComponentString("Checking Block at: x:" + x + " y:" + y + " z:" + z));
			Block id = player.world.getBlockState(new BlockPos(x, y, z)).getBlock();
			player.sendMessage(new TextComponentString("Found Block with Id: " + Block.getIdFromBlock(id)));
			final TileEntity tile = player.world.getTileEntity(new BlockPos(x, y, z));
			if (tile == null) {
				player.sendMessage(new TextComponentString(ChatColor.RED + "No TileEntity found"));
			} else if (!(tile instanceof LogisticsTileGenericPipe)) {
				player.sendMessage(new TextComponentString(ChatColor.RED + "No LogisticsTileGenericPipe found"));
			} else if (!(((LogisticsTileGenericPipe) tile).pipe instanceof CoreRoutedPipe)) {
				player.sendMessage(new TextComponentString(ChatColor.RED + "No CoreRoutedPipe found"));
			} else {
				LPChatListener.addTask(() -> {
					player.sendMessage(new TextComponentString(ChatColor.GREEN + "Starting RoutingTable debug update."));
					DebugController.instance(player).debug(((ServerRouter) ((CoreRoutedPipe) ((LogisticsTileGenericPipe) tile).pipe).getRouter()));
					MainProxy.sendPacketToPlayer(PacketHandler.getPacket(OpenChatGui.class), player);
					return true;
				}, player);
				player.sendMessage(new TextComponentString(
						ChatColor.AQUA + "Start RoutingTable debug update ? " + ChatColor.RESET + "<" + ChatColor.GREEN + "yes" + ChatColor.RESET + "/"
								+ ChatColor.RED + "no" + ChatColor.RESET + ">"));
				MainProxy.sendPacketToPlayer(PacketHandler.getPacket(OpenChatGui.class), player);
			}
		} else if (mode == TargetMode.Entity) {
			player.sendMessage(new TextComponentString(ChatColor.RED + "Entity not allowed"));
		}
	}

	@Override
	public void writeData(LPDataOutput output) {
		output.writeByte(mode.ordinal());
		output.writeIntArray(additions);
	}

	@Override
	public ModernPacket template() {
		return new RoutingUpdateTargetResponse(getId());
	}

	@Override
	public boolean isCompressable() {
		return true;
	}

	public enum TargetMode {
		Block,
		Entity,
		None
	}
}
