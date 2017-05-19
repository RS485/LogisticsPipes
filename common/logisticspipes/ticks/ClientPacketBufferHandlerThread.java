package logisticspipes.ticks;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
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

import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;

public class ClientPacketBufferHandlerThread {

	private class ClientCompressorThread extends Thread {

		//list of C->S packets to be serialized and compressed
		private final LinkedList<ModernPacket> clientList = new LinkedList<ModernPacket>();
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
				try {
					synchronized (clientList) {
						if (!pause && clientList.size() > 0) {
							ByteArrayOutputStream out = new ByteArrayOutputStream();
							DataOutputStream data = new DataOutputStream(out);
							data.write(clientBuffer);
							LinkedList<ModernPacket> packets = clientList;
							clearLock.lock();
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
							packets.clear();
							clearLock.unlock();
							clientBuffer = out.toByteArray();
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
				} catch (IOException e) {
					e.printStackTrace();
				}
				synchronized (clientList) {
					while (pause || clientList.size() == 0) {
						try {
							clientList.wait();
						} catch (InterruptedException e) {}
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

	private final ClientCompressorThread clientCompressorThread = new ClientCompressorThread();

	private class ClientDecompressorThread extends Thread {

		//Received compressed S->C data
		private final LinkedList<byte[]> queue = new LinkedList<byte[]>();
		//decompressed serialized S->C data
		private byte[] ByteBuffer = new byte[] {};
		//FIFO for deserialized S->C packets, decompressor adds, tickEnd removes
		private final LinkedList<Pair<EntityPlayer, byte[]>> PacketBuffer = new LinkedList<Pair<EntityPlayer, byte[]>>();
		//List of packets that that should be reattempted to apply in the next tick
		private final LinkedList<Pair<EntityPlayer, ModernPacket>> retryPackets = new LinkedList<Pair<EntityPlayer, ModernPacket>>();
		//Clear content on next tick
		private boolean clear = false;

		public ClientDecompressorThread() {
			super("LogisticsPipes Packet Decompressor Client");
			setDaemon(true);
			start();
		}

		public void clientTickEnd() {
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
					synchronized (PacketBuffer) {
						PacketBuffer.add(new Pair<EntityPlayer, byte[]>(MainProxy.proxy.getClientPlayer(), packet));
					}
				}
				synchronized (queue) {
					while (queue.size() == 0) {
						try {
							queue.wait();
						} catch (InterruptedException e) {}
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

		public void queueFailedPacket(ModernPacket packet, EntityPlayer player) {
			retryPackets.add(new Pair<EntityPlayer, ModernPacket>(player, packet));
		}
	}

	private final ClientDecompressorThread clientDecompressorThread = new ClientDecompressorThread();

	public ClientPacketBufferHandlerThread() {}

	public void clientTick(ClientTickEvent event) {
		if (event.phase != Phase.END) {
			return;
		}
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

	public void clear() {
		clientCompressorThread.clear();
		clientDecompressorThread.clear();
	}

	public void queueFailedPacket(ModernPacket packet, EntityPlayer player) {
		clientDecompressorThread.queueFailedPacket(packet, player);
	}
}
