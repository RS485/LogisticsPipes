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

import logisticspipes.LogisticsPipes;
import logisticspipes.network.ClientPacketHandler;
import logisticspipes.network.packets.PacketBufferTransfer;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.Pair;
import net.minecraft.network.packet.Packet250CustomPayload;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

public class ClientPacketBufferHandlerThread extends Thread {
	
	//Shared
	private LinkedList<Packet250CustomPayload> clientList = new LinkedList<Packet250CustomPayload>();
	private byte[] clientBuffer = new byte[0];
	private byte[] ByteBuffer = new byte[]{};
	private Object queueLock = new Object();
	private LinkedList<byte[]> queue = new LinkedList<byte[]>();
	public boolean pause = false;
	
	private LinkedList<Pair<Player,byte[]>> PacketBuffer = new LinkedList<Pair<Player,byte[]>>();
	
	public ClientPacketBufferHandlerThread() {
		super("LogisticsPipes Packet Compressor Client");
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
						ClientPacketHandler.onPacketData(new DataInputStream(new ByteArrayInputStream(part.getValue2())), part.getValue1());
					}
				} while(flag);
			}
			
			@Override
			public String getLabel() {
				return "LogisticsPipes Packet Compressor Tick Client";
			}
		}, Side.CLIENT);
	}
	
	public void addPacketToCompressor(Packet250CustomPayload packet) {
		if(packet.channel.equals("BCLP")) {
			synchronized(clientList) {
				clientList.add(packet);
			}
		}
	}
	
	public void handlePacket(PacketBufferTransfer packet) {
		synchronized(queueLock) {
			queue.addLast(packet.content);
		}
	}
	
	@Override
	public void run() {
		while(true) {
			if(!pause) {
				try {
					boolean flag = false;
					do {
						flag = false;
						byte[] buffer = null;
						synchronized(queueLock) {
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
					if(ByteBuffer.length > 0) {
						int size = ((ByteBuffer[0] & 255) << 24) + ((ByteBuffer[1] & 255) << 16) + ((ByteBuffer[2] & 255) << 8) + ((ByteBuffer[3] & 255) << 0);
						while(size + 4 <= ByteBuffer.length) {
							byte[] packet = Arrays.copyOfRange(ByteBuffer, 4, size + 4);
							ByteBuffer = Arrays.copyOfRange(ByteBuffer, size + 4, ByteBuffer.length);
							synchronized (PacketBuffer) {
								PacketBuffer.add(new Pair<Player,byte[]>((Player) MainProxy.proxy.getClientPlayer(),packet));
							}
							if(ByteBuffer.length > 4) {
								size = ((ByteBuffer[0] & 255) << 24) + ((ByteBuffer[1] & 255) << 16) + ((ByteBuffer[2] & 255) << 8) + ((ByteBuffer[3] & 255) << 0);
							} else {
								size = 0;
							}
						}
					}
					synchronized(clientList) {
						if(clientList.size() > 0) {
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
						//Send Content
						if(clientBuffer.length > 0) {
							byte[] sendbuffer = Arrays.copyOf(clientBuffer, Math.min(1024 * 32, clientBuffer.length));
							byte[] newbuffer = Arrays.copyOfRange(clientBuffer, Math.min(1024 * 32, clientBuffer.length), clientBuffer.length);
							clientBuffer = newbuffer;
							byte[] compressed = compress(sendbuffer);
							MainProxy.sendPacketToServer(new PacketBufferTransfer(compressed).getPacket());
						}
					}
				} catch (IOException e) {
						e.printStackTrace();
				}
			}
			try {
				boolean toDo = queue.size() > 0;
				if(ByteBuffer.length > 0) {
					toDo = true;
				}
				synchronized(clientList) {
					if(clientList.size() > 0) {
						toDo = true;
					}
				}
				if(!toDo) {
					Thread.sleep(100);
				}
			} catch(Exception e) {}
		}
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
