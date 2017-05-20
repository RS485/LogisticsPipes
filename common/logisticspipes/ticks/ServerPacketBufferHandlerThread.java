package logisticspipes.ticks;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.packets.BufferTransfer;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.tuples.Pair;

import net.minecraft.entity.player.EntityPlayer;

import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.ServerTickEvent;

public class ServerPacketBufferHandlerThread {

	private class ServerCompressorThread extends Thread {

		//Map of Players to lists of S->C packets to be serialized and compressed
		private final HashMap<EntityPlayer, LinkedList<ModernPacket>> serverList = new HashMap<EntityPlayer, LinkedList<ModernPacket>>();
		//Map of Players to serialized but still uncompressed S->C data
		private final HashMap<EntityPlayer, byte[]> serverBuffer = new HashMap<EntityPlayer, byte[]>();
		//used to cork the compressor so we can queue up a whole bunch of packets at once
		private boolean pause = false;
		//Clear content on next tick
		private Queue<EntityPlayer> playersToClear = new LinkedList<EntityPlayer>();

		public ServerCompressorThread() {
			super("LogisticsPipes Packet Compressor Server");
			setDaemon(true);
			start();
		}

		@Override
		public void run() {
			while (true) {
				try {
					synchronized (serverList) {
						if (!pause) {
							for (Entry<EntityPlayer, LinkedList<ModernPacket>> player : serverList.entrySet()) {
								ByteArrayOutputStream out = new ByteArrayOutputStream();
								DataOutputStream data = new DataOutputStream(out);
								byte[] towrite = serverBuffer.get(player.getKey());
								if (towrite != null) {
									data.write(towrite);
								}
								LinkedList<ModernPacket> packets = player.getValue();
								for (ModernPacket packet : packets) {
									LPDataOutputStream t = new LPDataOutputStream();
									t.writeShort(packet.getId());
									t.writeInt(packet.getDebugId());
									try {
										packet.writeData(t);
									} catch (ConcurrentModificationException e) {
										throw new RuntimeException("LogisticsPipes error (please report): Method writeData is not thread-safe in packet " + packet.getClass().getSimpleName(), e);
									}
									data.writeInt(t.size());
									data.write(t.toByteArray());
								}
								serverBuffer.put(player.getKey(), out.toByteArray());
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
				} catch (IOException e) {
					e.printStackTrace();
				}
				serverBuffer.clear();
				synchronized (serverList) {
					while (pause || serverList.size() == 0) {
						try {
							serverList.wait();
						} catch (InterruptedException e) {}
					}
				}
				synchronized (playersToClear) {
					EntityPlayer player = null;
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
					packetList = new LinkedList<ModernPacket>();
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

	private final ServerCompressorThread serverCompressorThread = new ServerCompressorThread();

	private class ServerDecompressorThread extends Thread {

		//Map of Player to received compressed C->S data
		private final HashMap<EntityPlayer, LinkedList<byte[]>> queue = new HashMap<EntityPlayer, LinkedList<byte[]>>();
		//Map of Player to decompressed serialized C->S data
		private final HashMap<EntityPlayer, byte[]> ByteBuffer = new HashMap<EntityPlayer, byte[]>();
		//FIFO for deserialized C->S packets, decompressor adds, tickEnd removes
		private final LinkedList<Pair<EntityPlayer, byte[]>> PacketBuffer = new LinkedList<Pair<EntityPlayer, byte[]>>();
		//Clear content on next tick
		private Queue<EntityPlayer> playersToClear = new LinkedList<EntityPlayer>();

		public ServerDecompressorThread() {
			super("LogisticsPipes Packet Decompressor Server");
			setDaemon(true);
			start();
		}

		public void serverTickEnd() {
			boolean flag = false;
			do {
				flag = false;
				Pair<EntityPlayer, byte[]> part = null;
				synchronized (PacketBuffer) {
					if (PacketBuffer.size() > 0) {
						flag = true;
						part = PacketBuffer.pop();
					}
				}
				if (flag) {
					try {
						PacketHandler.onPacketData(new LPDataInputStream(part.getValue2()), part.getValue1());
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			} while (flag);
		}

		@Override
		public void run() {
			while (true) {
				boolean flag = false;
				do {
					flag = false;
					byte[] buffer = null;
					EntityPlayer player = null;
					synchronized (queue) {
						if (queue.size() > 0) {
							for (Iterator<Entry<EntityPlayer, LinkedList<byte[]>>> it = queue.entrySet().iterator(); it.hasNext();) {
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
						synchronized (PacketBuffer) {
							PacketBuffer.add(new Pair<EntityPlayer, byte[]>(player.getKey(), packet));
						}
					}
				}
				for (Iterator<byte[]> it = ByteBuffer.values().iterator(); it.hasNext();) {
					byte[] ByteBufferForPlayer = it.next();
					if (ByteBufferForPlayer.length == 0) {
						it.remove();
					}
				}

				synchronized (queue) {
					while (queue.size() == 0) {
						try {
							queue.wait();
						} catch (InterruptedException e) {}
					}
				}
				synchronized (playersToClear) {
					EntityPlayer player = null;
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
					list = new LinkedList<byte[]>();
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

	private final ServerDecompressorThread serverDecompressorThread = new ServerDecompressorThread();

	public ServerPacketBufferHandlerThread() {}

	public void serverTick(ServerTickEvent event) {
		if (event.phase != Phase.END) {
			return;
		}
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
			int buffer = 0;
			while ((buffer = gzip.read()) != -1) {
				out.write(buffer);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return out.toByteArray();
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
}
