package logisticspipes.renderer.state;

import java.io.IOException;

import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraftforge.common.util.ForgeDirection;

public class FacadeMatrix {

	private final Block[] blocks = new Block[ForgeDirection.VALID_DIRECTIONS.length];
	private final int[] blockMetas = new int[ForgeDirection.VALID_DIRECTIONS.length];
	private boolean dirty = false;

	public FacadeMatrix() {
	}

	public void setFacade(ForgeDirection direction, Block block, int blockMeta) {
		if (blocks[direction.ordinal()] != block || blockMetas[direction.ordinal()] != blockMeta) {
			blocks[direction.ordinal()] = block;
			blockMetas[direction.ordinal()] = blockMeta;
			dirty = true;
		}
	}

	public Block getFacadeBlock(ForgeDirection direction) {
		return blocks[direction.ordinal()];
	}

	public int getFacadeMetaId(ForgeDirection direction) {
		return blockMetas[direction.ordinal()];
	}

	public boolean isDirty() {
		return dirty;
	}

	public void clean() {
		dirty = false;
	}

	public void writeData(LPDataOutputStream data) throws IOException {
		for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
			if (blocks [i] == null) {
				data.writeShort(0);
			} else {
				data.writeShort(Block.blockRegistry.getIDForObject(blocks[i]));
			}
			
			data.writeByte(blockMetas[i]);
		}
	}

	public void readData(LPDataInputStream data) throws IOException {
		for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
			short id = data.readShort();
			
			Block block;
			
			if (id == 0) {
				block = null;
			} else {
				block = (Block) Block.blockRegistry.getObjectById(id);
			}
			
			if (blocks[i] != block) {
				blocks[i] = block;
				dirty = true;
			}
			byte meta = data.readByte();
			if (blockMetas[i] != meta) {
				blockMetas[i] = meta;
				dirty = true;
			}
		}
	}
}