package logisticspipes.ticks;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.BufferTransfer;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.Pair;
import net.minecraft.network.packet.Packet250CustomPayload;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

public class ClientPacketBufferHandlerThread {

	private class ClientCompressorThread extends Thread {
		//list of C->S packets to be serialized and compressed
		private final LinkedList<Packet250CustomPayload> clientList = new LinkedList<Packet250CustomPayload>();
		//serialized but still uncompressed C->S data
		private byte[] clientBuffer = new byte[]{};
		//used to cork the compressor so we can queue up a whole bunch of packets at once
		private boolean pause = false;

		public ClientCompressorThread() {
			super("LogisticsPipes Packet Compressor Client");
			this.setDaemon(true);
			this.start();
		}

		@Override
		public void run() {
			while(true) {
				try {
					synchronized(clientList) {
						if(!pause && clientList.size() > 0) {
							ByteArrayOutputStream out = new ByteArrayOutputStream();
							DataOutputStream data = new DataOutputStream(out);
							data.write(clientBuffer);
							LinkedList<Packet250CustomPayload> packets = clientList;
							for(Packet250CustomPayload packet:packets) {
								data.writeInt(packet.data.length);
								data.write(packet.data);
							}
							packets.clear();
							clientBuffer = out.toByteArray();
						}
					}
					//Send Content
					if(clientBuffer.length > 0) {
						while(clientBuffer.length > 1024 * 32) {
							byte[] sendbuffer = Arrays.copyOf(clientBuffer, 1024 * 32);
							clientBuffer = Arrays.copyOfRange(clientBuffer, 1024 * 32, clientBuffer.length);
							byte[] compressed = compress(sendbuffer);
							MainProxy.sendPacketToServer(PacketHandler.getPacket(BufferTransfer.class).setContent(compressed));
						}
						byte[] sendbuffer = clientBuffer;
						clientBuffer = new byte[]{};
						byte[] compressed = compress(sendbuffer);
						MainProxy.sendPacketToServer(PacketHandler.getPacket(BufferTransfer.class).setContent(compressed));
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				synchronized(clientList) {
					while(pause || clientList.size() == 0) {
						try {
							clientList.wait();
						} catch (InterruptedException e) {}
					}
				}
			}
		}

		public void addPacketToCompressor(Packet250CustomPayload packet) {
			if(packet.channel.equals("BCLP")) {
				synchronized(clientList) {
					clientList.add(packet);
					if(!pause) {
						clientList.notify();
					}
				}
			}
		}

		public void setPause(boolean flag) {
			synchronized(clientList) {
				pause = flag;
				if(!pause) {
					clientList.notify();
				}
			}
		}
	}
	private final ClientCompressorThread clientCompressorThread = new ClientCompressorThread();

	private class ClientDecompressorThread extends Thread {
		//Received compressed S->C data
		private final LinkedList<byte[]> queue = new LinkedList<byte[]>();
		//decompressed serialized S->C data
		private byte[] ByteBuffer = new byte[]{};
		//FIFO for deserialized S->C packets, decompressor adds, tickEnd removes
		private final LinkedList<Pair<Player,byte[]>> PacketBuffer = new LinkedList<Pair<Player,byte[]>>();

		public ClientDecompressorThread() {
			super("LogisticsPipes Packet Decompressor Client");
			this.setDaemon(true);
			this.start();
			TickRegistry.registerTickHandler(new ITickHandler() {
				@Override
				public EnumSet<TickType> ticks() {
					return EnumSet.of(TickType.CLIENT);
				}

				@Override
				public void tickStart(EnumSet<TickType> type, Object... tickData) {}

				@Override
				public void tickEnd(EnumSet<TickType> type, Object... tickData) {
					boolean flag = false;
					do {
						flag = false;
						Pair<Player,byte[]> part = null;
						synchronized (PacketBuffer) {
							if(PacketBuffer.size() > 0) {
								flag = true;
								part = PacketBuffer.pop();
							}
						}
						if(flag) {
							try {
								PacketHandler.onPacketData(new DataInputStream(new ByteArrayInputStream(part.getValue2())), part.getValue1());
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					} while(flag);
				}

				@Override
				public String getLabel() {
					return "LogisticsPipes Packet Compressor Tick Client";
				}
			}, Side.CLIENT);
		}

		@Override
		public void run() {
			while(true) {
				boolean flag = false;
				do {
					flag = false;
					byte[] buffer = null;
					synchronized(queue) {
						if(queue.size() > 0) {
							flag = true;
							buffer = queue.getFirst();
							queue.removeFirst();
						}
					}
					if(flag && buffer != null) {
						byte[] packetbytes = decompress(buffer);
						byte[] newBuffer = new byte[packetbytes.length + ByteBuffer.length];
						System.arraycopy(ByteBuffer, 0, newBuffer, 0, ByteBuffer.length);
						System.arraycopy(packetbytes, 0, newBuffer, ByteBuffer.length, packetbytes.length);
						ByteBuffer = newBuffer;
					}
				}
				while(flag);

				while(ByteBuffer.length >= 4) {
					int size = ((ByteBuffer[0] & 255) << 24) + ((ByteBuffer[1] & 255) << 16) + ((ByteBuffer[2] & 255) << 8) + ((ByteBuffer[3] & 255) << 0);
					if(size + 4 > ByteBuffer.length) {
						break;
					}
					byte[] packet = Arrays.copyOfRange(ByteBuffer, 4, size + 4);
					ByteBuffer = Arrays.copyOfRange(ByteBuffer, size + 4, ByteBuffer.length);
					synchronized (PacketBuffer) {
						PacketBuffer.add(new Pair<Player,byte[]>((Player) MainProxy.proxy.getClientPlayer(),packet));
					}
				}
				synchronized(queue) {
					while(queue.size() == 0) {
						try {
							queue.wait();
						} catch (InterruptedException e) {}
					}
				}
			}
		}

		public void handlePacket(byte[] content) {
			synchronized(queue) {
				queue.addLast(content);
				queue.notify();
			}
		}
	}
	private final ClientDecompressorThread clientDecompressorThread = new ClientDecompressorThread();

	public ClientPacketBufferHandlerThread() {
	}

	public void setPause(boolean flag) {
		clientCompressorThread.setPause(flag);
	}

	public void addPacketToCompressor(Packet250CustomPayload packet) {
		clientCompressorThread.addPacketToCompressor(packet);
	}

	public void handlePacket(byte[] content) {
		clientDecompressorThread.handlePacket(content);
	}

	private static byte[] compress(byte[] content){
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try{
            GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream);
            gzipOutputStream.write(content);
            gzipOutputStream.close();
        } catch(IOException e){
            throw new RuntimeException(e);
        }
        return byteArrayOutputStream.toByteArray();
    }

	private static byte[] decompress(byte[] contentBytes){
    	ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
        	GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(contentBytes));
        	int buffer = 0;
        	while((buffer = gzip.read()) != -1) {
        		out.write(buffer);
        	}
        } catch(IOException e){
            throw new RuntimeException(e);
        }
        return out.toByteArray();
    }
}
