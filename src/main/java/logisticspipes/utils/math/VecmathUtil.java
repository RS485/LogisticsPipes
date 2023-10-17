package logisticspipes.utils.math;

import java.awt.Rectangle;

public class VecmathUtil {

	public static Vector3d clamp(Vector3d v, double min, double max) {
		v.x = clamp(v.x, min, max);
		v.y = clamp(v.y, min, max);
		v.z = clamp(v.z, min, max);
		return v;
	}

	public static double clamp(double val, double min, double max) {
		return val < min ? min : (Math.min(val, max));
	}

	public static int clamp(int val, int min, int max) {
		return val < min ? min : (Math.min(val, max));
	}

	/**
	 * Creates a perspective projection matrix.
	 *
	 * @param fovDegrees     The field of view angle in degrees.
	 * @param near           near plane.
	 * @param far            far plane.
	 * @param viewportWidth  viewport width.
	 * @param viewportHeight viewport height.
	 * @return the matrix.
	 */
	public static Matrix4d createProjectionMatrixAsPerspective(double fovDegrees, double near, double far, int viewportWidth, int viewportHeight) {
		// for impl details see gluPerspective doco in OpenGL reference manual
		double aspect = (double) viewportWidth / (double) viewportHeight;

		double theta = (Math.toRadians(fovDegrees) / 2d);
		double f = Math.cos(theta) / Math.sin(theta);

		double a = (far + near) / (near - far);
		double b = (2d * far * near) / (near - far);

		return new Matrix4d(f / aspect, 0, 0, 0, 0, f, 0, 0, 0, 0, a, b, 0, 0, -1, 0);
	}

	/**
	 * Creates a look at matrix.
	 *
	 * @param eyePos    the position of the eye.
	 * @param lookAtPos the point to look at.
	 * @param upVec     the up vector.
	 * @return the look at matrix.
	 */
	public static Matrix4d createMatrixAsLookAt(Vector3d eyePos, Vector3d lookAtPos, Vector3d upVec) {

		Vector3d eye = new Vector3d(eyePos);
		Vector3d lookAt = new Vector3d(lookAtPos);
		Vector3d up = new Vector3d(upVec);

		Vector3d forwardVec = new Vector3d(lookAt);
		forwardVec.sub(eye);
		forwardVec.makeVectorLength(1);

		Vector3d sideVec = new Vector3d();
		sideVec.cross(forwardVec, up);
		sideVec.makeVectorLength(1);

		Vector3d upVed = new Vector3d();
		upVed.cross(sideVec, forwardVec);
		upVed.makeVectorLength(1);

		Matrix4d mat = new Matrix4d(sideVec.x, sideVec.y, sideVec.z, 0, upVed.x, upVed.y, upVed.z, 0, -forwardVec.x, -forwardVec.y, -forwardVec.z, 0, 0, 0, 0, 1);

		eye.negate();
		// mat.transform(eye);
		mat.transformNormal(eye);
		mat.setTranslation(eye);

		return mat;
	}

	/**
	 * This function computes the ray that goes from the eye, through the
	 * specified pixel.
	 *
	 * @param x         the x pixel location (x = 0 is the left most pixel)
	 * @param y         the y pixel location (y = 0 is the bottom most pixel)
	 * @param eyeOut    the eyes position.
	 * @param normalOut the normal description the directional component of the ray.
	 */
	public static void computeRayForPixel(Rectangle vp, Matrix4d ipm, Matrix4d ivm, int x, int y, Vector3d eyeOut, Vector3d normalOut) {

		// grab the eye's position
		ivm.getTranslation(eyeOut);

		Matrix4d vpm = new Matrix4d();
		vpm.mul(ivm, ipm);

		// Calculate the pixel location in screen clip space (width and height from
		// -1 to 1)
		double screenX = (x - vp.getX()) / vp.getWidth();
		double screenY = (y - vp.getY()) / vp.getHeight();
		screenX = (screenX * 2.0) - 1.0;
		screenY = (screenY * 2.0) - 1.0;

		// Now calculate the XYZ location of this point on the near plane
		Vector4d tmp = new Vector4d();
		tmp.x = screenX;
		tmp.y = screenY;
		tmp.z = -1;
		tmp.w = 1.0;
		vpm.transform(tmp);

		double w = tmp.w;
		Vector3d nearXYZ = new Vector3d(tmp.x / w, tmp.y / w, tmp.z / w);

		// and then on the far plane
		tmp.x = screenX;
		tmp.y = screenY;
		tmp.z = 1;
		tmp.w = 1.0;
		vpm.transform(tmp);

		w = tmp.w;
		Vector3d farXYZ = new Vector3d(tmp.x / w, tmp.y / w, tmp.z / w);

		normalOut.set(farXYZ);
		normalOut.sub(nearXYZ);
		normalOut.makeVectorLength(1);
	}
}
