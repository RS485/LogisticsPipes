package logisticspipes.utils.math;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class Vector3f {

	public float x;
	public float y;
	public float z;

	public Vector3f(Vector3f vector3d) {
		this.x = vector3d.x;
		this.y = vector3d.y;
		this.z = vector3d.z;
	}

	public static Vector3f getFromAngles(double Yaw, double Pitch) {
		Vector3f vector = new Vector3f();
		vector.x = (float) (Math.cos(Yaw) * Math.cos(Pitch));
		vector.y = (float) Math.sin(Pitch);
		vector.z = (float) (Math.sin(Yaw) * Math.cos(Pitch));
		return vector;
	}

	public Vector3f multiply(double i) {
		x *= i;
		y *= i;
		z *= i;
		return this;
	}

	public double abs() {
		return Math.hypot(x, Math.hypot(y, z));
	}

	public Vector3f add(Vector3f vector, double factor) {
		Vector3f lVector = vector.clone().makeVectorLength(factor);
		x += lVector.x;
		y += lVector.y;
		z += lVector.z;
		return this;
	}

	public Vector3f add(Vector3f vector) {
		x += vector.x;
		y += vector.y;
		z += vector.z;
		return this;
	}

	public Vector3f sub(Vector3f vector) {
		x -= vector.x;
		y -= vector.y;
		z -= vector.z;
		return this;
	}

	public Vector3f negate() {
		x = -x;
		y = -y;
		z = -z;
		return this;
	}

	public Vector3f getOrtogonal(Float pX, Float pY, Float pZ) {
		if ((pX != null && pY != null && pZ != null) || (pX == null && pY == null) || (pY == null && pZ == null) || (pX == null && pZ == null)) {
			throw new UnsupportedOperationException("One, only one parameter needs to be null");
		}
		Vector3f answer = new Vector3f();
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

	public Vector3f makeVectorLength(double length) {
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
	public Vector3f clone() {
		Vector3f vector = new Vector3f();
		vector.x = x;
		vector.y = y;
		vector.z = z;
		return vector;
	}

	public void set(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public void set(Vector3f vector3d) {
		this.set(vector3d.x, vector3d.y, vector3d.z);
	}

	public void cross(Vector3f v1, Vector3f v2) {
		x = v1.y * v2.z - v1.z * v2.y;
		y = v2.x * v1.z - v2.z * v1.x;
		z = v1.x * v2.y - v1.y * v2.x;
	}

	public void normalize() {
		makeVectorLength(1);
	}
}
