package com.ptc.octo.creoview;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Enumeration;

import javax.media.j3d.BoundingBox;
import javax.media.j3d.Transform3D;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreoViewTrafoHelper {

	private static final String LOCATION = "location";
	private static final int ROUND_SCALE = 9;
	private static final Logger logger = LoggerFactory.getLogger(CreoViewTrafoHelper.class);
	private static final String ED_SPACE = " ";
	private static final double DTOR = Math.PI / 180.0;
	public static final String TRAFO_MATRIX4D = "TrafoMatrix4D";
	public static final String TRAFO_MATRIX4D_MAT = "TrafoMatrix4D_Mat";

	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	public static DefaultMutableTreeNode calculateTransformation(DefaultMutableTreeNode parentNode) throws Exception {

		Matrix4d pAbsMx4 = getAbsMatrix4dOfElement((JSONObject) parentNode.getUserObject());

		Enumeration childs = parentNode.children();
		while (childs.hasMoreElements()) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) childs.nextElement();
			JSONObject el = (JSONObject) node.getUserObject();
			el = el.optJSONObject("link_properties")!=null ? el.optJSONObject("link_properties") : el;
			Matrix4d cRelMx4 = getRelMatrix4dOfElement(el);
			Matrix4d cAbsMx4 = new Matrix4d();
			cAbsMx4.mul(pAbsMx4, cRelMx4);
			addTrafoInfos(el, cAbsMx4, "pvs_abs_");
			calculateTransformation(node);
		}
		return parentNode;
	}
	public static void addTrafoInfos(JSONObject el, Matrix4d mat, String prefix) throws JSONException {
		String absLocation = getLocationfromMatrix(mat);
		el.put(prefix+LOCATION, absLocation);
		el.put(prefix+TRAFO_MATRIX4D_MAT, mat);
		el.put(prefix+TRAFO_MATRIX4D, Arrays.asList(
				rnd(mat.m00),rnd(mat.m01),rnd(mat.m02),rnd(mat.m03),
		        rnd(mat.m10),rnd(mat.m11),rnd(mat.m12),rnd(mat.m13),
		        rnd(mat.m20),rnd(mat.m21),rnd(mat.m22),rnd(mat.m23),
		        rnd(mat.m30),rnd(mat.m31),rnd(mat.m32),rnd(mat.m33)));
	}
	private static double rnd(double number) {
		return BigDecimal.valueOf(number).setScale(ROUND_SCALE,RoundingMode.HALF_UP).doubleValue();
	}
	
	public static void addAbsoluteTrafoInfos(JSONObject parentJson, JSONObject myJson, Matrix4d relTrafo, String prefix) throws JSONException {
		if(parentJson==null || !parentJson.has(prefix+TRAFO_MATRIX4D_MAT)) {
			addTrafoInfos(myJson, relTrafo, prefix);
		}else{
			Matrix4d parentAbsMat = (Matrix4d) parentJson.opt(prefix+TRAFO_MATRIX4D_MAT);
			Matrix4d cAbsMx4 = new Matrix4d();
			cAbsMx4.mul(parentAbsMat, relTrafo);
			CreoViewTrafoHelper.addTrafoInfos(myJson, cAbsMx4, prefix);
		}
	}

	private static Matrix4d getAbsMatrix4dOfElement(JSONObject el) throws NumberFormatException, JSONException {
		el = el.optJSONObject("link_properties")!=null ? el.optJSONObject("link_properties") : el;
		Matrix4d absMat4d = (Matrix4d) el.opt("pvs_abs_"+TRAFO_MATRIX4D_MAT);
		if (absMat4d == null) {
			absMat4d = new Matrix4d();
			absMat4d.setIdentity();
			addTrafoInfos(el, absMat4d, "pvs_abs_");
		}
		return absMat4d;
	}

	@SuppressWarnings("unused")
	private static void addMatrix4dToElement(JSONObject el, Matrix4d mx4, String[] matAttNames, String matName)
			throws JSONException {
		el = el.optJSONObject("link_properties")!=null ? el.optJSONObject("link_properties") : el;
		if (matAttNames != null && matAttNames.length == 13) {

			el.put(matAttNames[0], new Double(mx4.m00));
			el.put(matAttNames[1], new Double(mx4.m10));
			el.put(matAttNames[2], new Double(mx4.m20));
			el.put(matAttNames[3], new Double(mx4.m01));
			el.put(matAttNames[4], new Double(mx4.m11));
			el.put(matAttNames[5], new Double(mx4.m21));
			el.put(matAttNames[6], new Double(mx4.m02));
			el.put(matAttNames[7], new Double(mx4.m12));
			el.put(matAttNames[8], new Double(mx4.m22));
			el.put(matAttNames[9], new Double(mx4.m03));
			el.put(matAttNames[10], new Double(mx4.m13));
			el.put(matAttNames[11], new Double(mx4.m23));
			el.put(matAttNames[12], new Double(mx4.m33));
		}
		if (matName != null)
			el.put(matName, mx4);
	}

	private static Matrix4d getRelMatrix4dOfElement(JSONObject el) throws NumberFormatException, JSONException {
		el = el.optJSONObject("link_properties")!=null ? el.optJSONObject("link_properties") : el;
		Matrix4d relMat4d = (Matrix4d) el.opt("pvs_"+TRAFO_MATRIX4D_MAT);
		if (relMat4d == null) {
			relMat4d = new Matrix4d();
			relMat4d.setIdentity();
			el.put("pvs_"+TRAFO_MATRIX4D_MAT, relMat4d);
		}
		return relMat4d;

	}

	public static String getLocationfromMatrix(Matrix4d mat) {
		String location = ""; //$NON-NLS-1$

		Vector3d a = getRotationFromMatrix4d2(mat);
		Vector3d v = getTranslationFromMatrix4d_2(mat);

		// handle possible scaling, generaly this will be 1.0 or -1.0 (CATIA mirroring)
		// if it is 1.0 then keep life simple and leave it off, it is optional for PV
		double scale = getScaleFromMatrix4d(mat);
		if (Math.abs(scale - 1.0) > 0.0001) {
			location = truncate(a.x) + ED_SPACE + truncate(a.y) + ED_SPACE + truncate(a.z) + ED_SPACE + truncate(v.x)
					+ ED_SPACE + truncate(v.y) + ED_SPACE + truncate(v.z) + ED_SPACE + Double.toString(scale);
		} else {
			location = truncate(a.x) + ED_SPACE + truncate(a.y) + ED_SPACE + truncate(a.z) + ED_SPACE + truncate(v.x)
					+ ED_SPACE + truncate(v.y) + ED_SPACE + truncate(v.z) + ED_SPACE;
		}
		return location;
	}

	/**
	 * Extracts the rotation angles (in degrees) from a Matrix4d
	 *
	 * mirroring changes are from Erik Rieger multiply all components of the
	 * rotational matrix with the determinant, so that mirroring along an axis is
	 * covered. (of course, the determinant is then also considered when calculating
	 * the scale) only use the upper 3*3 elements to calculate the determinant, also
	 * make certain passed in Matrix is not changed.
	 *
	 * <BR>
	 * <BR>
	 * <B>Supported API: </B>false
	 *
	 * @return rotation angles
	 **/
	public static Vector3d getRotationFromMatrix4d2(Matrix4d mat) {
		double ax, ay, az;

		if (mat.determinant() > 0.0) {
			ax = Math.atan2(mat.m21, mat.m22) / DTOR;
			ay = -Math.asin(mat.m20) / DTOR;
			az = Math.atan2(mat.m10, mat.m00) / DTOR;
		} else {
			Matrix3d rotMat = new Matrix3d();
			mat.getRotationScale(rotMat);
			rotMat.mul(-1.0D);

			ax = Math.atan2(rotMat.m21, rotMat.m22) / DTOR;
			ay = -Math.asin(rotMat.m20) / DTOR;
			az = Math.atan2(rotMat.m10, rotMat.m00) / DTOR;
		}

		return new Vector3d(ax, ay, az);
	}

	public static Vector3d getTranslationFromMatrix4d_2(Matrix4d matrix4d) {
		Vector3d vector3d = new Vector3d();
		matrix4d.get(vector3d);
		return vector3d;
	}

	public static double getScaleFromMatrix4d(Matrix4d matrix4d) {
		if (matrix4d.determinant() > 0.0D)
			return matrix4d.getScale();
		else
			return -matrix4d.getScale();
	}

	/**
	 * Extracts the uniform scale from a Matrix4d the sign of the scale will
	 * indicate if the matrix is mirrored or not <BR>
	 * <BR>
	 * <B>Supported API: </B>false
	 *
	 * @return scale
	 **/

	private static String truncate(double value) {
		if (Math.abs(value) < 0.0000001) {
			return "0.0";
		}
		return Float.toString((float) value);
	}
	
	public static float[] aggregateBBox(float[] addedBBox, Matrix4d addedAbsTrafo, float[] aggregatedBBox ) {
		Point3d lowerP = new Point3d(addedBBox[0], addedBBox[1], addedBBox[2]);
		Point3d upperP = new Point3d(addedBBox[3], addedBBox[4], addedBBox[5]);
		BoundingBox bbox = new BoundingBox(lowerP, upperP);
		if(addedAbsTrafo!=null) bbox.transform(new Transform3D(addedAbsTrafo));
		
		if(aggregatedBBox!=null) {
			Point3d lowerPAgg = new Point3d(aggregatedBBox[0], aggregatedBBox[1], aggregatedBBox[2]);
			Point3d upperPAgg = new Point3d(aggregatedBBox[3], aggregatedBBox[4], aggregatedBBox[5]);
			BoundingBox aggBBox = new BoundingBox(lowerPAgg, upperPAgg);
			
			bbox.combine(aggBBox);			
		}
		
		bbox.getLower(lowerP);
		bbox.getUpper(upperP);

		return new float[] {
		                 new Double(lowerP.x).floatValue(),
		                 new Double(lowerP.y).floatValue(),
		                 new Double(lowerP.z).floatValue(),
		                 new Double(upperP.x).floatValue(),
		                 new Double(upperP.y).floatValue(),
		                 new Double(upperP.z).floatValue()
		};
	}
	

	public static float[] buildBBox(Point3f addedPointMin, Point3f addedPointMax) {
		float[] absBBox = new float[6];
		absBBox[0] = addedPointMin.x < addedPointMax.x ? addedPointMin.x : addedPointMax.x;
		absBBox[1] = addedPointMin.y < addedPointMax.y ? addedPointMin.y : addedPointMax.y;
		absBBox[2] = addedPointMin.z < addedPointMax.z ? addedPointMin.z : addedPointMax.z;
		absBBox[3] = addedPointMin.x > addedPointMax.x ? addedPointMin.x : addedPointMax.x;
		absBBox[4] = addedPointMin.y > addedPointMax.y ? addedPointMin.y : addedPointMax.y;
		absBBox[5] = addedPointMin.z > addedPointMax.z ? addedPointMin.z : addedPointMax.z;
		return absBBox;
	}
}
