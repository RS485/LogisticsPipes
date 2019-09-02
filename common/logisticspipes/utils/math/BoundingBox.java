package logisticspipes.utils.math;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

import net.minecraft.util.EnumFacing;

import network.rs485.logisticspipes.world.DoubleCoordinates;

public class BoundingBox {

	public static final BoundingBox UNIT_CUBE = new BoundingBox(0, 0, 0, 1, 1, 1);

	public final float minX;
	public final float minY;
	public final float minZ;
	public final float maxX;
	public final float maxY;
	public final float maxZ;

	public BoundingBox(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
		this.minX = minX;
		this.minY = minY;
		this.minZ = minZ;
		this.maxX = maxX;
		this.maxY = maxY;
		this.maxZ = maxZ;
	}

	public BoundingBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		this.minX = (float) minX;
		this.minY = (float) minY;
		this.minZ = (float) minZ;
		this.maxX = (float) maxX;
		this.maxY = (float) maxY;
		this.maxZ = (float) maxZ;
	}

	public BoundingBox(DoubleCoordinates bc) {
		this(bc.getXDouble(), bc.getYDouble(), bc.getZDouble(), bc.getXDouble() + 1, bc.getYDouble() + 1, bc.getZDouble() + 1);
	}

	@Nonnull
	public List<Vertex> getCornersWithUvForFace(@Nonnull EnumFacing face, float minU, float maxU, float minV, float maxV) {
		List<Vertex> result = new ArrayList<>(4);
		switch (face) {
			case NORTH:
				result.add(new Vertex(new Vector3d(maxX, minY, minZ), new Vector3f(0, 0, -1), new Vector2f(minU, minV)));
				result.add(new Vertex(new Vector3d(minX, minY, minZ), new Vector3f(0, 0, -1), new Vector2f(maxU, minV)));
				result.add(new Vertex(new Vector3d(minX, maxY, minZ), new Vector3f(0, 0, -1), new Vector2f(maxU, maxV)));
				result.add(new Vertex(new Vector3d(maxX, maxY, minZ), new Vector3f(0, 0, -1), new Vector2f(minU, maxV)));
				break;
			case SOUTH:
				result.add(new Vertex(new Vector3d(minX, minY, maxZ), new Vector3f(0, 0, 1), new Vector2f(maxU, minV)));
				result.add(new Vertex(new Vector3d(maxX, minY, maxZ), new Vector3f(0, 0, 1), new Vector2f(minU, minV)));
				result.add(new Vertex(new Vector3d(maxX, maxY, maxZ), new Vector3f(0, 0, 1), new Vector2f(minU, maxV)));
				result.add(new Vertex(new Vector3d(minX, maxY, maxZ), new Vector3f(0, 0, 1), new Vector2f(maxU, maxV)));
				break;
			case EAST:
				result.add(new Vertex(new Vector3d(maxX, maxY, minZ), new Vector3f(1, 0, 0), new Vector2f(maxU, maxV)));
				result.add(new Vertex(new Vector3d(maxX, maxY, maxZ), new Vector3f(1, 0, 0), new Vector2f(minU, maxV)));
				result.add(new Vertex(new Vector3d(maxX, minY, maxZ), new Vector3f(1, 0, 0), new Vector2f(minU, minV)));
				result.add(new Vertex(new Vector3d(maxX, minY, minZ), new Vector3f(1, 0, 0), new Vector2f(maxU, minV)));
				break;
			case WEST:
				result.add(new Vertex(new Vector3d(minX, minY, minZ), new Vector3f(-1, 0, 0), new Vector2f(maxU, minV)));
				result.add(new Vertex(new Vector3d(minX, minY, maxZ), new Vector3f(-1, 0, 0), new Vector2f(minU, minV)));
				result.add(new Vertex(new Vector3d(minX, maxY, maxZ), new Vector3f(-1, 0, 0), new Vector2f(minU, maxV)));
				result.add(new Vertex(new Vector3d(minX, maxY, minZ), new Vector3f(-1, 0, 0), new Vector2f(maxU, maxV)));
				break;
			case UP:
				result.add(new Vertex(new Vector3d(maxX, maxY, maxZ), new Vector3f(0, 1, 0), new Vector2f(minU, minV)));
				result.add(new Vertex(new Vector3d(maxX, maxY, minZ), new Vector3f(0, 1, 0), new Vector2f(minU, maxV)));
				result.add(new Vertex(new Vector3d(minX, maxY, minZ), new Vector3f(0, 1, 0), new Vector2f(maxU, maxV)));
				result.add(new Vertex(new Vector3d(minX, maxY, maxZ), new Vector3f(0, 1, 0), new Vector2f(maxU, minV)));
				break;
			case DOWN:
				result.add(new Vertex(new Vector3d(minX, minY, minZ), new Vector3f(0, -1, 0), new Vector2f(maxU, maxV)));
				result.add(new Vertex(new Vector3d(maxX, minY, minZ), new Vector3f(0, -1, 0), new Vector2f(minU, maxV)));
				result.add(new Vertex(new Vector3d(maxX, minY, maxZ), new Vector3f(0, -1, 0), new Vector2f(minU, minV)));
				result.add(new Vertex(new Vector3d(minX, minY, maxZ), new Vector3f(0, -1, 0), new Vector2f(maxU, minV)));
				break;
			default:
				throw new IllegalArgumentException("Unknown side");
		}
		return result;
	}

}
