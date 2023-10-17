package logisticspipes.utils.math;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class Vector2f {

	public float x;
	public float y;

	public Vector2f(Vector2f vector3d) {
		this.x = vector3d.x;
		this.y = vector3d.y;
	}

	public Vector2f multiply(double i) {
		x *= i;
		y *= i;
		return this;
	}

	public double abs() {
		return Math.hypot(x, y);
	}

	public Vector2f add(Vector2f vector, double factor) {
		Vector2f lVector = vector.clone().makeVectorLength(factor);
		x += lVector.x;
		y += lVector.y;
		return this;
	}

	public Vector2f add(Vector2f vector) {
		x += vector.x;
		y += vector.y;
		return this;
	}

	public Vector2f sub(Vector2f vector) {
		x -= vector.x;
		y -= vector.y;
		return this;
	}

	public Vector2f reverse() {
		x = -x;
		y = -y;
		return this;
	}

	public Vector2f makeVectorLength(double length) {
		double divide = Math.sqrt(Math.pow(abs(), 2) / Math.pow(length, 2));
		double multiply = 1 / divide;
		x *= multiply;
		y *= multiply;
		return this;
	}

	@Override
	public String toString() {
		return String.format("[%s,%s](%s)", x, y, abs());
	}

	@Override
	public Vector2f clone() {
		Vector2f vector = new Vector2f();
		vector.x = x;
		vector.y = y;
		return vector;
	}

	public void set(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public void set(Vector2f vector3d) {
		this.set(vector3d.x, vector3d.y);
	}
}
