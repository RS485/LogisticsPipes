package logisticspipes.network.packets.debuggui;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.concurrent.Callable;

import logisticspipes.commands.chathelper.LPChatListener;
import logisticspipes.commands.commands.debug.DebugGuiController;
import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.packets.gui.OpenChatGui;
import logisticspipes.proxy.MainProxy;

import logisticspipes.utils.string.ChatColor;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;

@Accessors(chain = true)
public class DebugTargetResponse extends ModernPacket {

	public DebugTargetResponse(int id) {
		super(id);
	}

	public enum TargetMode {
		Block,
		Entity,
		None;
	}

	@Getter
	@Setter
	private TargetMode mode;

	@Getter
	@Setter
	private Object[] additions = new Object[0];

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		mode = TargetMode.values()[data.readByte()];
		int size = data.readInt();
		additions = new Object[size];
		for (int i = 0; i < size; i++) {
			int arraySize = data.readInt();
			byte[] bytes = new byte[arraySize];
			data.read(bytes);
			ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
			ObjectInput in = null;
			in = new ObjectInputStream(bis);
			try {
				Object o = in.readObject();
				additions[i] = o;
			} catch (ClassNotFoundException e) {
				throw new UnsupportedOperationException(e);
			}
		}
	}

	@Override
	public void processPacket(final EntityPlayer player) {
		if (mode == TargetMode.None) {
			player.addChatComponentMessage(new ChatComponentText(ChatColor.RED + "No Target Found"));
		} else if (mode == TargetMode.Block) {
			int x = (Integer) additions[0];
			int y = (Integer) additions[1];
			int z = (Integer) additions[2];
			player.addChatComponentMessage(new ChatComponentText("Checking Block at: x:" + x + " y:" + y + " z:" + z));
			Block id = player.worldObj.getBlock(x, y, z);
			player.addChatComponentMessage(new ChatComponentText("Found Block with Id: " + id.getClass()));
			final TileEntity tile = player.worldObj.getTileEntity(x, y, z);
			if (tile == null) {
				player.addChatComponentMessage(new ChatComponentText(ChatColor.RED + "No TileEntity found"));
			} else {
				LPChatListener.addTask(new Callable<Boolean>() {

					@Override
					public Boolean call() throws Exception {
						player.addChatComponentMessage(new ChatComponentText(ChatColor.GREEN + "Starting debuging of TileEntity: " + ChatColor.BLUE + ChatColor.UNDERLINE + tile.getClass().getSimpleName()));
						DebugGuiController.instance().startWatchingOf(tile, player);
						MainProxy.sendPacketToPlayer(PacketHandler.getPacket(OpenChatGui.class), player);
						return true;
					}
				}, player);
				player.addChatComponentMessage(new ChatComponentText(ChatColor.AQUA + "Start debuging of TileEntity: " + ChatColor.BLUE + ChatColor.UNDERLINE + tile.getClass().getSimpleName() + ChatColor.AQUA + "? " + ChatColor.RESET + "<" + ChatColor.GREEN + "yes" + ChatColor.RESET + "/" + ChatColor.RED + "no"
						+ ChatColor.RESET + ">"));
				MainProxy.sendPacketToPlayer(PacketHandler.getPacket(OpenChatGui.class), player);
			}
		} else if (mode == TargetMode.Entity) {
			int entityId = (Integer) additions[0];
			final Entity entity = player.worldObj.getEntityByID(entityId);
			if (entity == null) {
				player.addChatComponentMessage(new ChatComponentText(ChatColor.RED + "No Entity found"));
			} else {
				LPChatListener.addTask(new Callable<Boolean>() {

					@Override
					public Boolean call() throws Exception {
						player.addChatComponentMessage(new ChatComponentText(ChatColor.GREEN + "Starting debuging of Entity: " + ChatColor.BLUE + ChatColor.UNDERLINE + entity.getClass().getSimpleName()));
						DebugGuiController.instance().startWatchingOf(entity, player);
						MainProxy.sendPacketToPlayer(PacketHandler.getPacket(OpenChatGui.class), player);
						return true;
					}
				}, player);
				player.addChatComponentMessage(new ChatComponentText(ChatColor.AQUA + "Start debuging of Entity: " + ChatColor.BLUE + ChatColor.UNDERLINE + entity.getClass().getSimpleName() + ChatColor.AQUA + "? " + ChatColor.RESET + "<" + ChatColor.GREEN + "yes" + ChatColor.RESET + "/" + ChatColor.RED + "no"
						+ ChatColor.RESET + ">"));
				MainProxy.sendPacketToPlayer(PacketHandler.getPacket(OpenChatGui.class), player);
			}
		}
	}

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		data.writeByte(mode.ordinal());
		data.writeInt(additions.length);
		for (Object addition : additions) {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutput out = null;
			out = new ObjectOutputStream(bos);
			out.writeObject(addition);
			byte[] bytes = bos.toByteArray();
			data.writeInt(bytes.length);
			data.write(bytes);
		}
	}

	@Override
	public ModernPacket template() {
		return new DebugTargetResponse(getId());
	}

	@Override
	public boolean isCompressable() {
		return true;
	}
}
