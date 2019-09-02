package logisticspipes.utils.math;

import java.util.Objects;
import javax.annotation.ParametersAreNonnullByDefault;

import mcp.MethodsReturnNonnullByDefault;

/**
 * 2-dimensional vector.
 * Imported from qcommon-croco (https://github.com/therealfarfetchd/qcommon)
 *
 * @author therealfarfetchd
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class Vec2 {

	public static final Vec2 ORIGIN = new Vec2(0, 0);

	public final float x;
	public final float y;

	private float length = Float.NaN;
	private Vec2 normalized;

	public Vec2(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public Vec2 add(Vec2 other) {
		return new Vec2(this.x + other.x, this.y + other.y);
	}

	public Vec2 sub(Vec2 other) {
		return new Vec2(this.x - other.x, this.y - other.y);
	}

	public Vec2 mul(Vec2 other) {
		return new Vec2(this.x * other.x, this.y * other.y);
	}

	public Vec2 mul(float other) {
		return new Vec2(this.x * other, this.y * other);
	}

	public Vec2 div(Vec2 other) {
		return new Vec2(x / other.x, this.y / other.y);
	}

	public Vec2 div(float other) {
		return new Vec2(this.x / other, this.y / other);
	}

	public float dot(Vec2 other) {
		return this.x * other.x + this.y * other.y;
	}

	public float getLength() {
		if (Float.isNaN(length)) {
			length = (float) Math.sqrt(x * x + y * y);
		}

		return length;
	}

	public Vec2 getNormalized() {
		if (normalized == null) {
			normalized = new Vec2(x / getLength(), y / getLength());
			normalized.length = 1;
		}

		return normalized;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Vec2 vec2 = (Vec2) o;
		return Float.compare(vec2.x, x) == 0 &&
				Float.compare(vec2.y, y) == 0;
	}

	@Override
	public int hashCode() {
		return Objects.hash(x, y);
	}

	@Override
	public String toString() {
		return String.format("(%f, %f)", x, y);
	}

}
