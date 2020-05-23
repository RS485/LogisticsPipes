package logisticspipes.ticks;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import net.minecraft.entity.player.EntityPlayer;

import net.minecraftforge.fml.common.gameevent.TickEvent;

import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.packets.BufferTransfer;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.tuples.Pair;
import network.rs485.logisticspipes.util.LPDataIOWrapper;

public class ServerPacketBufferHandlerThread {

	private final ServerCompressorThread serverCompressorThread = new ServerCompressorThread();
	private final ServerDecompressorThread serverDecompressorThread = new ServerDecompressorThread();

	public ServerPacketBufferHandlerThread() {}

	private static byte[] compress(byte[] content) {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		try {
			GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream);
			gzipOutputStream.write(content);
			gzipOutputStream.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return byteArrayOutputStream.toByteArray();
	}

	private static byte[] decompress(byte[] contentBytes) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(contentBytes));
			int buffer;
			while ((buffer = gzip.read()) != -1) {
				out.write(buffer);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return out.toByteArray();
	}

	public void serverTick() {
		serverDecompressorThread.serverTickEnd();
	}

	public void setPause(boolean flag) {
		serverCompressorThread.setPause(flag);
	}

	public void addPacketToCompressor(ModernPacket packet, EntityPlayer player) {
		serverCompressorThread.addPacketToCompressor(packet, player);
	}

	public void handlePacket(byte[] content, EntityPlayer player) {
		serverDecompressorThread.handlePacket(content, player);
	}

	public void clear(final EntityPlayer player) {
		new Thread() {

			@Override
			public void run() {
				serverCompressorThread.clear(player);
				serverDecompressorThread.clear(player);
			}
		}.start();
	}

	private static class ServerCompressorThread extends Thread {

		//Map of Players to lists of S->C packets to be serialized and compressed
		private final HashMap<EntityPlayer, LinkedList<ModernPacket>> serverList = new HashMap<>();
		//Map of Players to serialized but still uncompressed S->C data
		private final HashMap<EntityPlayer, byte[]> serverBuffer = new HashMap<>();
		//used to cork the compressor so we can queue up a whole bunch of packets at once
		private boolean pause = false;
		//Clear content on next tick
		private Queue<EntityPlayer> playersToClear = new LinkedList<>();

		public ServerCompressorThread() {
			super("LogisticsPipes Packet Compressor Server");
			setDaemon(true);
			start();
		}

		@Override
		public void run() {
			while (true) {
				synchronized (serverList) {
					if (!pause) {
						for (Entry<EntityPlayer, LinkedList<ModernPacket>> playerPacketEntry : serverList.entrySet()) {
							EntityPlayer player = playerPacketEntry.getKey();
							serverBuffer.put(player, LPDataIOWrapper.collectData(output -> {
								if (serverBuffer.containsKey(player)) {
									output.writeBytes(serverBuffer.get(player));
								}

								LinkedList<ModernPacket> packets = playerPacketEntry.getValue();
								try {
									for (ModernPacket packet : packets) {
										output.writeByteArray(LPDataIOWrapper.collectData(dataOutput -> {
											dataOutput.writeShort(packet.getId());
											dataOutput.writeInt(packet.getDebugId());
											packet.writeData(dataOutput);
										}));
									}
								} finally {
									packets.clear();
								}
							}));
						}
						serverList.clear();
					}
				}
				//Send Content
				for (Entry<EntityPlayer, byte[]> player : serverBuffer.entrySet()) {
					while (player.getValue().length > 32 * 1024) {
						byte[] sendbuffer = Arrays.copyOf(player.getValue(), 1024 * 32);
						byte[] newbuffer = Arrays.copyOfRange(player.getValue(), 1024 * 32, player.getValue().length);
						player.setValue(newbuffer);
						byte[] compressed = ServerPacketBufferHandlerThread.compress(sendbuffer);
						MainProxy.sendPacketToPlayer(PacketHandler.getPacket(BufferTransfer.class).setContent(compressed), player.getKey());
					}
					byte[] sendbuffer = player.getValue();
					byte[] compressed = ServerPacketBufferHandlerThread.compress(sendbuffer);
					MainProxy.sendPacketToPlayer(PacketHandler.getPacket(BufferTransfer.class).setContent(compressed), player.getKey());
				}
				serverBuffer.clear();
				synchronized (serverList) {
					while (pause || serverList.size() == 0) {
						try {
							serverList.wait();
						} catch (InterruptedException ignored) { }
					}
				}
				synchronized (playersToClear) {
					EntityPlayer player;
					do {
						player = playersToClear.poll();
						if (player != null) {
							serverBuffer.remove(player);
						}
					} while (player != null);
				}
			}
		}

		public void addPacketToCompressor(ModernPacket packet, EntityPlayer player) {
			synchronized (serverList) {
				LinkedList<ModernPacket> packetList = serverList.get(player);
				if (packetList == null) {
					packetList = new LinkedList<>();
					serverList.put(player, packetList);
				}
				packetList.add(packet);
				if (!pause) {
					serverList.notify();
				}
			}
		}

		public void setPause(boolean flag) {
			synchronized (serverList) {
				pause = flag;
				if (!pause) {
					serverList.notify();
				}
			}
		}

		public void clear(EntityPlayer player) {
			synchronized (serverList) {
				serverList.remove(player);
			}
			synchronized (playersToClear) {
				playersToClear.add(player);
			}
		}
	}

	private static class ServerDecompressorThread extends Thread {

		//Map of Player to received compressed C->S data
		private final HashMap<EntityPlayer, LinkedList<byte[]>> queue = new HashMap<>();
		//Map of Player to decompressed serialized C->S data
		private final HashMap<EntityPlayer, byte[]> ByteBuffer = new HashMap<>();
		//FIFO for deserialized C->S packets, decompressor adds, tickEnd removes
		private final LinkedList<Pair<EntityPlayer, byte[]>> PacketBuffer = new LinkedList<>();
		private final ReentrantLock packetBufferLock = new ReentrantLock();
		//Clear content on next tick
		private Queue<EntityPlayer> playersToClear = new LinkedList<>();

		public ServerDecompressorThread() {
			super("LogisticsPipes Packet Decompressor Server");
			setDaemon(true);
			start();
		}

		private void handlePacketData(final Pair<EntityPlayer, byte[]> playerDataPair) {
			LPDataIOWrapper.provideData(playerDataPair.getValue2(), input -> {
				PacketHandler.onPacketData(input, playerDataPair.getValue1());
			});
		}

		public void serverTickEnd() {
			Pair<EntityPlayer, byte[]> part;
			while (true) {
				part = null;
				packetBufferLock.lock();
				try {
					if (PacketBuffer.size() > 0) {
						part = PacketBuffer.pop();
					}
				} finally {
					packetBufferLock.unlock();
				}

				if (part == null) {
					break;
				}

				handlePacketData(part);
			}
		}

		@Override
		public void run() {
			while (true) {
				boolean flag;
				do {
					flag = false;
					byte[] buffer = null;
					EntityPlayer player = null;
					synchronized (queue) {
						if (queue.size() > 0) {
							for (Iterator<Entry<EntityPlayer, LinkedList<byte[]>>> it = queue.entrySet().iterator(); it.hasNext(); ) {
								Entry<EntityPlayer, LinkedList<byte[]>> lPlayer = it.next();
								if (lPlayer.getValue().size() > 0) {
									flag = true;
									buffer = lPlayer.getValue().getFirst();
									player = lPlayer.getKey();
									if (lPlayer.getValue().size() > 1) {
										lPlayer.getValue().removeFirst();
									} else {
										it.remove();
									}
									break;
								} else {
									it.remove();
								}
							}
						}
					}
					if (flag && buffer != null && player != null) {
						byte[] ByteBufferForPlayer = ByteBuffer.get(player);
						if (ByteBufferForPlayer == null) {
							ByteBufferForPlayer = new byte[] {};
							ByteBuffer.put(player, ByteBufferForPlayer);
						}
						byte[] packetbytes = ServerPacketBufferHandlerThread.decompress(buffer);
						byte[] newBuffer = new byte[packetbytes.length + ByteBufferForPlayer.length];
						System.arraycopy(ByteBufferForPlayer, 0, newBuffer, 0, ByteBufferForPlayer.length);
						System.arraycopy(packetbytes, 0, newBuffer, ByteBufferForPlayer.length, packetbytes.length);
						ByteBuffer.put(player, newBuffer);
					}
				} while (flag);

				for (Entry<EntityPlayer, byte[]> player : ByteBuffer.entrySet()) {
					while (player.getValue().length >= 4) {
						byte[] ByteBufferForPlayer = player.getValue();
						int size = ((ByteBufferForPlayer[0] & 255) << 24) + ((ByteBufferForPlayer[1] & 255) << 16) + ((ByteBufferForPlayer[2] & 255) << 8) + ((ByteBufferForPlayer[3] & 255) << 0);
						if (size + 4 > ByteBufferForPlayer.length) {
							break;
						}
						byte[] packet = Arrays.copyOfRange(ByteBufferForPlayer, 4, size + 4);
						ByteBufferForPlayer = Arrays.copyOfRange(ByteBufferForPlayer, size + 4, ByteBufferForPlayer.length);
						player.setValue(ByteBufferForPlayer);
						packetBufferLock.lock();
						try {
							PacketBuffer.add(new Pair<>(player.getKey(), packet));
						} finally {
							packetBufferLock.unlock();
						}
					}
				}
				for (Iterator<byte[]> it = ByteBuffer.values().iterator(); it.hasNext(); ) {
					byte[] ByteBufferForPlayer = it.next();
					if (ByteBufferForPlayer.length == 0) {
						it.remove();
					}
				}

				synchronized (queue) {
					while (queue.size() == 0) {
						try {
							queue.wait();
						} catch (InterruptedException ignored) { }
					}
				}
				synchronized (playersToClear) {
					EntityPlayer player;
					do {
						player = playersToClear.poll();
						if (player != null) {
							ByteBuffer.remove(player);
						}
					} while (player != null);
				}
			}
		}

		public void handlePacket(byte[] content, EntityPlayer player) {
			synchronized (queue) {
				LinkedList<byte[]> list = queue.get(player);
				if (list == null) {
					list = new LinkedList<>();
					queue.put(player, list);
				}
				list.addLast(content);
				queue.notify();
			}
		}

		public void clear(EntityPlayer player) {
			synchronized (queue) {
				queue.remove(player);
			}
			synchronized (playersToClear) {
				playersToClear.add(player);
			}
		}
	}
}
