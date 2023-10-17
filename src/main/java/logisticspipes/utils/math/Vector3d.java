package logisticspipes.utils.math;

import net.minecraft.util.math.Vec3d;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class Vector3d {

	public double x;
	public double y;
	public double z;

	public Vector3d(Vector3d vector3d) {
		this.x = vector3d.x;
		this.y = vector3d.y;
		this.z = vector3d.z;
	}

	public static Vector3d getFromAngles(double Yaw, double Pitch) {
		Vector3d vector = new Vector3d();
		vector.x = Math.cos(Yaw) * Math.cos(Pitch);
		vector.y = Math.sin(Pitch);
		vector.z = Math.sin(Yaw) * Math.cos(Pitch);
		return vector;
	}

	public Vector3d multiply(double i) {
		x *= i;
		y *= i;
		z *= i;
		return this;
	}

	public double abs() {
		return Math.hypot(x, Math.hypot(y, z));
	}

	public Vector3d add(Vector3d vector, double factor) {
		Vector3d lVector = vector.clone().makeVectorLength(factor);
		x += lVector.x;
		y += lVector.y;
		z += lVector.z;
		return this;
	}

	public Vector3d add(Vector3d vector) {
		x += vector.x;
		y += vector.y;
		z += vector.z;
		return this;
	}

	public Vector3d sub(Vector3d vector) {
		x -= vector.x;
		y -= vector.y;
		z -= vector.z;
		return this;
	}

	public Vector3d negate() {
		x = -x;
		y = -y;
		z = -z;
		return this;
	}

	public Vector3d getOrtogonal(Double pX, Double pY, Double pZ) {
		if ((pX != null && pY != null && pZ != null) || (pX == null && pY == null) || (pY == null && pZ == null) || (pX == null && pZ == null)) {
			throw new UnsupportedOperationException("One, only one parameter needs to be null");
		}
		Vector3d answer = new Vector3d();
		if (pX == null) {
			answer.x = (((-pY) * y) - (pZ * z)) / x;
			answer.y = pY;
			answer.z = pZ;
		} else if (pY == null) {
			answer.x = pX;
			answer.y = (((-pX) * x) - (pZ * z)) / y;
			answer.z = pZ;
		} else if (pZ == null) {
			answer.x = pX;
			answer.y = pY;
			answer.z = (((-pX) * x) - (pY * y)) / z;
		}
		return answer;
	}

	public Vector3d makeVectorLength(double length) {
		double divide = Math.sqrt(Math.pow(abs(), 2) / Math.pow(length, 2));
		double multiply = 1 / divide;
		x *= multiply;
		y *= multiply;
		z *= multiply;
		return this;
	}

	@Override
	public String toString() {
		return String.format("[%s,%s,%s](%s)", x, y, z, abs());
	}

	@Override
	public Vector3d clone() {
		Vector3d vector = new Vector3d();
		vector.x = x;
		vector.y = y;
		vector.z = z;
		return vector;
	}

	public void set(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public void set(Vector3d vector3d) {
		this.set(vector3d.x, vector3d.y, vector3d.z);
	}

	public void cross(Vector3d v1, Vector3d v2) {
		x = v1.y * v2.z - v1.z * v2.y;
		y = v2.x * v1.z - v2.z * v1.x;
		z = v1.x * v2.y - v1.y * v2.x;
	}

	public Vec3d toVec3d() {
		return new Vec3d(x, y, z);
	}
}
