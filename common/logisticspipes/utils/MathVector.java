package logisticspipes.utils;

public class MathVector {

	public double X;
	public double Y;
	public double Z;

	public static MathVector getFromAngles(double Yaw, double Pitch) {
		MathVector vector = new MathVector();
		vector.X = Math.cos(Yaw) * Math.cos(Pitch);
		vector.Y = Math.sin(Pitch);
		vector.Z = Math.sin(Yaw) * Math.cos(Pitch);
		return vector;
	}

	public MathVector multiply(double i) {
		X *= i;
		Y *= i;
		Z *= i;
		return this;
	}

	public double abs() {
		return Math.hypot(X, Math.hypot(Y, Z));
	}

	public MathVector add(MathVector vector, double factor) {
		MathVector lVector = vector.clone().makeVectorLength(factor);
		X += lVector.X;
		Y += lVector.Y;
		Z += lVector.Z;
		return this;
	}

	public MathVector add(MathVector vector) {
		X += vector.X;
		Y += vector.Y;
		Z += vector.Z;
		return this;
	}

	public MathVector reverse() {
		X = -X;
		Y = -Y;
		Z = -Z;
		return this;
	}

	public MathVector getOrtogonal(Double pX, Double pY, Double pZ) {
		if ((pX != null && pY != null && pZ != null) || (pX == null && pY == null) || (pY == null && pZ == null) || (pX == null && pZ == null)) {
			throw new UnsupportedOperationException("One, only one parameter needs to be null");
		}
		MathVector answer = new MathVector();
		if (pX == null) {
			answer.X = (((-pY) * Y) - (pZ * Z)) / X;
			answer.Y = pY;
			answer.Z = pZ;
		} else if (pY == null) {
			answer.X = pX;
			answer.Y = (((-pX) * X) - (pZ * Z)) / Y;
			answer.Z = pZ;
		} else if (pZ == null) {
			answer.X = pX;
			answer.Y = pY;
			answer.Z = (((-pX) * X) - (pY * Y)) / Z;
		}
		return answer;
	}

	public MathVector makeVectorLength(double length) {
		double divide = Math.sqrt(Math.pow(abs(), 2) / Math.pow(length, 2));
		double multiply = 1 / divide;
		X *= multiply;
		Y *= multiply;
		Z *= multiply;
		return this;
	}

	@Override
	public String toString() {
		return new StringBuilder().append("[").append(X).append(",").append(Y).append(",").append(Z).append("]").append("(").append(abs()).append(")").toString();
	}

	@Override
	public MathVector clone() {
		MathVector vector = new MathVector();
		vector.X = X;
		vector.Y = Y;
		vector.Z = Z;
		return vector;
	}
}
