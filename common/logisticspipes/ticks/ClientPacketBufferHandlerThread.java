package logisticspipes.ticks;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
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

public class ClientPacketBufferHandlerThread {

	private final ClientCompressorThread clientCompressorThread = new ClientCompressorThread();
	private final ClientDecompressorThread clientDecompressorThread = new ClientDecompressorThread();

	public ClientPacketBufferHandlerThread() {}

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

	public void clientTick() {
		clientDecompressorThread.clientTickEnd();
	}

	public void setPause(boolean flag) {
		clientCompressorThread.setPause(flag);
	}

	public void addPacketToCompressor(ModernPacket packet) {
		clientCompressorThread.addPacketToCompressor(packet);
	}

	public void handlePacket(byte[] content) {
		clientDecompressorThread.handlePacket(content);
	}

	public void clear() {
		clientCompressorThread.clear();
		clientDecompressorThread.clear();
	}

	public void queuePacket(ModernPacket packet, EntityPlayer player) {
		clientDecompressorThread.queuePacket(packet, player);
	}

	private static class ClientCompressorThread extends Thread {

		//list of C->S packets to be serialized and compressed
		private final LinkedList<ModernPacket> clientList = new LinkedList<>();
		//serialized but still uncompressed C->S data
		private byte[] clientBuffer = new byte[] {};
		//used to cork the compressor so we can queue up a whole bunch of packets at once
		private boolean pause = false;
		//Clear content on next tick
		private boolean clear = false;

		private Lock clearLock = new ReentrantLock();

		public ClientCompressorThread() {
			super("LogisticsPipes Packet Compressor Client");
			setDaemon(true);
			start();
		}

		@Override
		public void run() {
			while (true) {
				synchronized (clientList) {
					if (!pause && clientList.size() > 0) {
						clientBuffer = LPDataIOWrapper.collectData(output -> {
							output.writeBytes(clientBuffer);
							clearLock.lock();
							try {
								for (ModernPacket packet : clientList) {
									output.writeByteArray(LPDataIOWrapper.collectData(dataOutput -> {
										dataOutput.writeShort(packet.getId());
										dataOutput.writeInt(packet.getDebugId());
										packet.writeData(dataOutput);
									}));
								}
							} finally {
								clientList.clear();
								clearLock.unlock();
							}
						});
					}
				}
				//Send Content
				if (clientBuffer.length > 0) {
					while (clientBuffer.length > 1024 * 32) {
						byte[] sendbuffer = Arrays.copyOf(clientBuffer, 1024 * 32);
						clientBuffer = Arrays.copyOfRange(clientBuffer, 1024 * 32, clientBuffer.length);
						byte[] compressed = ClientPacketBufferHandlerThread.compress(sendbuffer);
						MainProxy.sendPacketToServer(PacketHandler.getPacket(BufferTransfer.class).setContent(compressed));
					}
					byte[] sendbuffer = clientBuffer;
					clientBuffer = new byte[] {};
					byte[] compressed = ClientPacketBufferHandlerThread.compress(sendbuffer);
					MainProxy.sendPacketToServer(PacketHandler.getPacket(BufferTransfer.class).setContent(compressed));
				}
				synchronized (clientList) {
					while (pause || clientList.size() == 0) {
						try {
							clientList.wait();
						} catch (InterruptedException ignored) { }
					}
				}
				if (clear) {
					clear = false;
					clientBuffer = new byte[] {};
				}
			}
		}

		public void addPacketToCompressor(ModernPacket packet) {
			synchronized (clientList) {
				clientList.add(packet);
				if (!pause) {
					clientList.notify();
				}
			}
		}

		public void setPause(boolean flag) {
			synchronized (clientList) {
				pause = flag;
				if (!pause) {
					clientList.notify();
				}
			}
		}

		public void clear() {
			clear = true;
			new Thread() {

				@Override
				public void run() {
					clearLock.lock();
					clientList.clear();
					clearLock.unlock();
				}
			}.start();
		}
	}

	private static class ClientDecompressorThread extends Thread {

		//Received compressed S->C data
		private final LinkedList<byte[]> queue = new LinkedList<>();
		//FIFO for deserialized S->C packets, decompressor adds, tickEnd removes
		private final LinkedList<Pair<EntityPlayer, byte[]>> PacketBuffer = new LinkedList<>();
		private final ReentrantLock packetBufferLock = new ReentrantLock();
		//List of packets that that should be reattempted to apply in the next tick
		private final LinkedList<Pair<EntityPlayer, ModernPacket>> retryPackets = new LinkedList<>();
		private final ReentrantLock retryPacketsLock = new ReentrantLock();
		//decompressed serialized S->C data
		private byte[] ByteBuffer = new byte[] {};
		//Clear content on next tick
		private boolean clear = false;

		public ClientDecompressorThread() {
			super("LogisticsPipes Packet Decompressor Client");
			setDaemon(true);
			start();
		}

		private void handlePacketData(final Pair<EntityPlayer, byte[]> playerDataPair) {
			LPDataIOWrapper.provideData(playerDataPair.getValue2(), input -> PacketHandler.onPacketData(input, playerDataPair.getValue1()));
		}

		public void clientTickEnd() {
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
			Pair<EntityPlayer, ModernPacket> partB;
			while (true) {
				partB = null;
				retryPacketsLock.lock();
				try {
					if (retryPackets.size() > 0) {
						partB = retryPackets.pop();
					}
				} finally {
					retryPacketsLock.unlock();
				}

				if (partB == null) {
					break;
				}

				PacketHandler.onPacketData(partB.getValue2(), partB.getValue1());
			}
		}

		@Override
		public void run() {
			while (true) {
				boolean flag;
				do {
					flag = false;
					byte[] buffer = null;
					synchronized (queue) {
						if (queue.size() > 0) {
							flag = true;
							buffer = queue.getFirst();
							queue.removeFirst();
						}
					}
					if (flag && buffer != null) {
						byte[] packetbytes = ClientPacketBufferHandlerThread.decompress(buffer);
						byte[] newBuffer = new byte[packetbytes.length + ByteBuffer.length];
						System.arraycopy(ByteBuffer, 0, newBuffer, 0, ByteBuffer.length);
						System.arraycopy(packetbytes, 0, newBuffer, ByteBuffer.length, packetbytes.length);
						ByteBuffer = newBuffer;
					}
				} while (flag);

				while (ByteBuffer.length >= 4) {
					int size = ((ByteBuffer[0] & 255) << 24) + ((ByteBuffer[1] & 255) << 16) + ((ByteBuffer[2] & 255) << 8) + ((ByteBuffer[3] & 255) << 0);
					if (size + 4 > ByteBuffer.length) {
						break;
					}
					byte[] packet = Arrays.copyOfRange(ByteBuffer, 4, size + 4);
					ByteBuffer = Arrays.copyOfRange(ByteBuffer, size + 4, ByteBuffer.length);
					packetBufferLock.lock();
					try {
						PacketBuffer.add(new Pair<>(MainProxy.proxy.getClientPlayer(), packet));
					} finally {
						packetBufferLock.unlock();
					}
				}
				synchronized (queue) {
					while (queue.size() == 0) {
						try {
							queue.wait();
						} catch (InterruptedException ignored) { }
					}
				}
				if (clear) {
					clear = false;
					ByteBuffer = new byte[] {};
				}
			}
		}

		public void handlePacket(byte[] content) {
			synchronized (queue) {
				queue.addLast(content);
				queue.notify();
			}
		}

		public void clear() {
			clear = true;
			queue.clear();
			retryPackets.clear();
		}

		public void queuePacket(ModernPacket packet, EntityPlayer player) {
			retryPackets.add(new Pair<>(player, packet));
		}
	}
}
