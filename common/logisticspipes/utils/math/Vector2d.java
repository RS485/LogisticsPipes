package logisticspipes.utils.math;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class Vector2d {

	public double x;
	public double y;

	public Vector2d(Vector2d vector3d) {
		this.x = vector3d.x;
		this.y = vector3d.y;
	}

	public Vector2d multiply(double i) {
		x *= i;
		y *= i;
		return this;
	}

	public double abs() {
		return Math.hypot(x, y);
	}

	public Vector2d add(Vector2d vector, double factor) {
		Vector2d lVector = vector.clone().makeVectorLength(factor);
		x += lVector.x;
		y += lVector.y;
		return this;
	}

	public Vector2d add(Vector2d vector) {
		x += vector.x;
		y += vector.y;
		return this;
	}

	public Vector2d sub(Vector2d vector) {
		x -= vector.x;
		y -= vector.y;
		return this;
	}

	public Vector2d reverse() {
		x = -x;
		y = -y;
		return this;
	}

	public Vector2d makeVectorLength(double length) {
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
	public Vector2d clone() {
		Vector2d vector = new Vector2d();
		vector.x = x;
		vector.y = y;
		return vector;
	}

	public void set(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public void set(Vector2d vector3d) {
		this.set(vector3d.x, vector3d.y);
	}
}
