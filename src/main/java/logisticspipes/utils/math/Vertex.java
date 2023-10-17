package logisticspipes.utils.math;

public class Vertex {

	public Vector3d xyz = new Vector3d();
	public Vector2f uv;
	public Vector3f normal;

	public Vertex(Vector3d xyz, Vector3f normal, Vector2f uv) {
		this.xyz.set(xyz);
		this.normal = new Vector3f(normal);
		this.uv = new Vector2f(uv);
	}

	public double x() {
		return xyz.x;
	}

	public double y() {
		return xyz.y;
	}

	public double z() {
		return xyz.z;
	}

	public float nx() {
		return normal.x;
	}

	public float ny() {
		return normal.y;
	}

	public float nz() {
		return normal.z;
	}

	public float u() {
		return uv.x;
	}

	public float v() {
		return uv.y;
	}

	public float r() {
		return 1;
	}

	public float g() {
		return 1;
	}

	public float b() {
		return 1;
	}

	public float a() {
		return 0;
	}

}
