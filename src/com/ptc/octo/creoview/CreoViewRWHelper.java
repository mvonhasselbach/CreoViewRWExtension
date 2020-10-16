package com.ptc.octo.creoview;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.nio.channels.Channels;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3f;
import javax.xml.transform.stream.StreamSource;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ptc.octo.creoview.Structure2.CompInst;

public class CreoViewRWHelper {

	private static final String BOUNDING_BOX = "Bounding Box";
	private static final Logger logger = LoggerFactory.getLogger(CreoViewRWHelper.class);
	//private static int BUFFER_SIZE_4096 = 8192;
	public static final int PVS2JSON = 3;
	public static final int WT_STRUCTURE2 = 2;
	public static final int DEFAULT = 1;
	public static final int WT_SED2_FLAT = 5;
	public static final int WT_SED2_NESTED = 4;
	private static final String ABSOLUTE_PREFIX = "absolute ";
	private static final String INSTANCE_PREFIX = "Instance ";
	private static final String ABSOLUTE_BOUNDING_BOX = "Absolute Bounding Box";
	//private static final String CALCULATED_BOUNDING_BOX = "Calculated Bounding Box";
	
	public static void main(String[] args) {
		System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");
		System.out.println("logger class: "+logger.getClass());
		try {
			if(args.length<1) throw new Exception("You need at least one arg (the CreoView file)!");
			if(args[0].equals("import")) {
				System.out.println("Reading File: "+args[1]);
				byte[] encoded = Files.readAllBytes(Paths.get(args[1]));
				JSONObject json = new JSONObject(new String(encoded, StandardCharsets.UTF_8));
				writePVSFromJSON(json, "WT_SED2_NESTED", new File(args[1]+".pvs"));
				return;
			}
			JSONObject json = getJSONFromPVFile(
					args[0], 
					args.length>1 ? args[1] : null, 
					args.length>2 ? args[2] : null
			);			
			System.out.print( new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(new TreeMap(json.toMap()) ));
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}

	public static JSONObject getJSONFromPVFile(String filename, String jsonFormat, String returnedProperties)
			throws IOException, FileNotFoundException, Exception, JSONException {
		JSONObject json = new JSONObject();
		//InputStream stream = null;
		File pvFile = new File(filename);
		if(!pvFile.exists() || !pvFile.canRead()) throw new Exception("CreoView file: "+filename+" doesn't exist (or is not readable)!");

		Structure2 sed2 = new Structure2();		
		Map<String, InputStream> edpInputStreamMap = null;
		//check if we have a simple pvs file or if we have a pvz that may have proxy comps and nested pvs files
		BufferedInputStream stream;
		if(pvFile.getName().endsWith(".pvs")) {
			stream = new BufferedInputStream(new FileInputStream(pvFile), 8192);
			sed2.readED(stream);
			stream.close();
		}else {
			ZipFile pvzFile=null;
			try {
				pvzFile = new ZipFile(pvFile);
				Enumeration<? extends ZipEntry> pvEntries = pvzFile.entries();
				HashMap<String, ZipEntry> pvsFiles = new HashMap<>();
				while(pvEntries.hasMoreElements()) {
					ZipEntry pvEntry = pvEntries.nextElement();
					if(pvEntry.getName().endsWith(".ol")) continue;
					//have to get the root dir pvs, for proxy pvs resolution!
					else if(pvEntry.getName().endsWith(".pvs")) {
						pvsFiles.put(pvEntry.getName(), pvEntry);
					}
					else {
						if(edpInputStreamMap==null) edpInputStreamMap = new HashMap<String, InputStream>();
						edpInputStreamMap.put(pvEntry.getName(), new BufferedInputStream(pvzFile.getInputStream(pvEntry), 8192));
					}
				}
				logger.trace("pvsFiles: "+pvsFiles);
				ZipEntry rootPvsEntry = pvsFiles.get("/"+pvFile.getName().replace(".pvz", ".pvs"));
				if(rootPvsEntry==null) {
					for (String pvsName : pvsFiles.keySet()) {
						if(pvsName.split("/").length==1) rootPvsEntry = pvsFiles.get(pvsName);
					}
				}
				if(rootPvsEntry==null) throw new Exception("No .pvs file on first level of pvz zip file");
				stream = new BufferedInputStream(pvzFile.getInputStream(rootPvsEntry), 8192);
				sed2.setEDPInputStreamMap(edpInputStreamMap);
				sed2.setCloseInputStreams(true);
				sed2.readED(stream);
				// now resolve and merge proxy comps
				resolveAndMergeProxyComps(sed2.getRootComp(), "", pvzFile, edpInputStreamMap);

			}catch(Exception ex) {
				throw new Exception("The creoView file you specified is neither a .pvs file nor a pvz (ie. zip-based file)", ex);
			}finally {
				if(pvzFile!=null) pvzFile.close();
			}
		}
		int format = jsonFormat==null ? CreoViewRWHelper.DEFAULT : 
			 		jsonFormat.equalsIgnoreCase("WT_SED2_FLAT") ? CreoViewRWHelper.WT_SED2_FLAT : 
			 		jsonFormat.equalsIgnoreCase("WT_SED2_NESTED") ? CreoViewRWHelper.WT_SED2_NESTED : 
					jsonFormat.equalsIgnoreCase("WT_STRUCTURE2") ? CreoViewRWHelper.WT_STRUCTURE2 :
					jsonFormat.equalsIgnoreCase("PVS2JSON") ? CreoViewRWHelper.PVS2JSON : CreoViewRWHelper.DEFAULT;

		json = CreoViewRWHelper.getJSONFromSed2(sed2, format);
		// reduce the json output to the specified list of properties now
		if(returnedProperties !=null) CreoViewRWHelper.reduceJSON2Props(json, returnedProperties.split(","));
		if (stream != null) stream.close();
		
		return json;
	}

	private static void resolveAndMergeProxyComps(Structure2.Comp comp, String parentPath, ZipFile pvzFile, Map<String, InputStream> edpInputStreamMap) throws Exception {
		logger.trace("comp: "+comp.name+" is proxy?: "+comp.proxy );
		String myPath = parentPath;
		if(comp.isProxy()) {
			if(comp.properties==null) comp.properties = new Hashtable<String,String>();
			comp.properties.put("isProxy","true");
//			if(comp.shape!=null)comp.properties.put("shape",comp.shape);
//			if(comp.filename!=null)comp.properties.put("filename",comp.filename);
//			if(comp.getSourceFileName()!=null)comp.properties.put("sourceFilename",comp.getSourceFileName());
//			if(comp.map_filename!=null)comp.properties.put("map_filename",comp.map_filename);
			String proxyFN = comp.filename;
			if(proxyFN!=null) { 
				logger.trace("Found proxy comp: "+comp.name+" with sourcefilename: "+proxyFN);
				logger.trace(""+comp);
				proxyFN = parentPath+proxyFN.replace('\\', '/');
				ZipEntry proxyPVSFile = pvzFile.getEntry(proxyFN);
				myPath = proxyFN.substring(0, proxyFN.lastIndexOf('/')+1);
				logger.trace("myPath: "+myPath);
				if(proxyPVSFile!=null) {
					Structure2 sed2 = new Structure2();
					BufferedInputStream stream = new BufferedInputStream(pvzFile.getInputStream(proxyPVSFile), 8192);
					sed2.setEDPInputStreamMap(edpInputStreamMap);
					sed2.setCloseInputStreams(true);
					sed2.readED(stream);
					logger.trace("now merging "+comp+" with "+sed2.getRootComp());
					Structure2.mergeStructure(comp, sed2.getRootComp(), true, false);
					comp.properties.put("mergedProxy", "true");
				}else {
					logger.warn("Proxy PVS File: "+proxyFN+" does not exist in pvzFile: "+pvzFile.getName()+"! Skipping proxy merge..." );
					comp.properties.put("mergedProxy", "false");
					comp.properties.put("proxyURI", proxyFN);
					comp.properties.put("mergedProxyMsg", "Proxy PVS File: "+proxyFN+" does not exist in pvzFile: "+pvzFile.getName());
				}
			}else {
				logger.warn("Comp is proxy but has no File: "+comp+"! Skipping proxy merge..." );
				comp.properties.put("mergedProxy", "flase");
				comp.properties.put("mergedProxyMsg", "Comp is proxy but has no File: "+comp);
			}
		}//else comp.properties.put("isProxy", "false");
		for (Iterator<CompInst> it = comp.children(); it.hasNext();) {
			CompInst compInst = it.next();
			resolveAndMergeProxyComps(compInst.child, myPath, pvzFile, edpInputStreamMap);
		}
	}

	/**
	 * This is the main method that is used by the TWX Extension
	 * It can output different JSON formats 
	 * @param pvsInputStream
	 * @param edpInputStreamMap
	 * @param format
	 * @return
	 * @throws Exception
	 * @throws JSONException
	 */
	public static JSONObject getJSONFromSed2(Structure2 sed2, int format) throws Exception, JSONException {
		if(format==WT_SED2_FLAT) return outputRecurseSed2(sed2.toTreeStructure(), new JSONObject(),null, false);
		if(format==WT_SED2_NESTED) return outputRecurseSed2(sed2.toTreeStructure(), new JSONObject(),null, true);
		//if(format==PVS2JSON) return xxx; 
		if(format==WT_STRUCTURE2) return getIETreeJSON(sed2.getRootComp());
		JSONObject rootJson = new JSONObject();
		outputRecurseDefault(sed2.getRootComp(), null, rootJson, null);
		return rootJson;
	}

	public static void reduceJSON2Props(JSONObject json, String[] properties) {
		List<String> props = Arrays.asList(properties);
		ArrayList<String> keys2remove = new ArrayList<String>();
		for (Iterator<String> keysIt = json.keys(); keysIt.hasNext();) {
			String key = keysIt.next();
			Object val;
			try {
				val = json.get(key);
				if (val instanceof JSONArray) {
					boolean isJSONObj = false;
					for (int i = 0; i < ((JSONArray) val).length(); i++) {
						Object obj = ((JSONArray) val).get(i);
						if(obj instanceof JSONObject) {
							isJSONObj = true;
							reduceJSON2Props((JSONObject) obj, properties);
						}
					}
					if(!isJSONObj && !props.contains(key)) keys2remove.add(key);
				} else if (val instanceof JSONObject) {
					reduceJSON2Props((JSONObject) val, properties);
				} else if (!props.contains(key)) keys2remove.add(key);
			} catch (JSONException e) {
				// shouldn't happen
				e.printStackTrace();
			}

		}
		for(String key : keys2remove)json.remove(key);
	}

	
	/**
	 * if this should be spit out as a structure like the one in Steve Ghees pvs2json conversion we'd have to:
	 * 1. Separate all nodes with properties in the top-level components[]
	 * 2. add a cid (incremented idx) and vid (=pvs_inst_id)
	 * 3. create nested children[] as we do right now BUT only keep compInt/link params
	 * 4. create idmap with cid, path, idx (either idx of siblings or =vid) and par (=parent vid)
	 * @param comp
	 * @param compInst
	 * @param pnode
	 * @return
	 * @throws JSONException
	 */	private static JSONObject getPVS2JSONFromDefMutTree(DefaultMutableTreeNode node) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * outputs the format that was produced by the first version of this Extension. 
	 * It's a nested JSON with all kind of information, incl rel and abs trafo matrix
	 * It originated from a former InfoEngine webject that was used for pvs processing
	 * 
	 * @param comp
	 * @return JSON rep of pvs & pvt infos
	 * @throws Exception
	 * @throws JSONException
	 */
	public static JSONObject getIETreeJSON(Structure2.Comp comp) throws Exception, JSONException {
		DefaultMutableTreeNode node = structure2ToIeTree(comp, null, null);
		CreoViewTrafoHelper.calculateTransformation(node);
		return outputRecurseIETreeNode(node);
	}
	private static JSONObject outputRecurseIETreeNode(DefaultMutableTreeNode node) throws JSONException {
		JSONObject json = (JSONObject) node.getUserObject();
		JSONArray childs = new JSONArray();
		for (Object child : Collections.list(node.children())) {
			JSONObject cn = outputRecurseIETreeNode((DefaultMutableTreeNode) child);
			childs.put(cn);
		}
		json.put("components", childs);
		return json;
	}
	private static DefaultMutableTreeNode structure2ToIeTree(Structure2.Comp comp, Structure2.CompInst compInst,
			DefaultMutableTreeNode pnode) throws JSONException {
		//TODO: having these as static params is not nice - they should be specific to the method invocation
		boolean isNestedLinkProperties=true;
		boolean isNestedPartProperties=true;

		JSONObject el = new JSONObject();		
		JSONObject propEl = addProperties(el, comp.properties, "properties");

		String val = comp.filename;
		if (val != null)
			propEl.put("pvs_filename", comp.filename);

		propEl.put("pvs_shape", comp.shape);
		propEl.put("pvs_name", comp.name);
		propEl.put("pvs_type", comp.type);
		
		propEl.put("pvs_bbox", comp.bbox);
		propEl.put("pvs_writeIdx", comp.writeIdx);
		propEl.put("pvs_wvs_info", comp.wvs_info);
		propEl.put("pvs_source_part_name", comp.getSourcePartName());
		propEl.put("pvs_source_file_name", comp.getSourceFileName());
		propEl.put("pvs_source_form_name", comp.getSourceFormName());

		if (compInst != null) {
			Hashtable compInstAttrs = compInst.properties;
			JSONObject lpropEl = addProperties(el, comp.properties, "link_properties");
			Matrix4d mat = Structure2.getMatrix4dFromTranslationAndOrientation(compInst.translation,
					compInst.orientation);
			CreoViewTrafoHelper.addTrafoInfos(lpropEl, mat, "pvs_");
			lpropEl.put("pvs_inst_id", compInst.id);
			lpropEl.put("pvs_inst_name", compInst.name);
			lpropEl.put("pvs_inst_type", compInst.type);			
		}
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(el);
		if (pnode != null) pnode.add(node);
		Object[] nodeObjs = node.getUserObjectPath();
		String path = "";
		for (Object obj : nodeObjs) {
			String pId = ((JSONObject)obj).optString("pvs_inst_id");
			if(!pId.equals("")) path+="/"+pId;
		}
		el.put("pvs_path", path);
		for (Object ccompInst : comp.childInsts) {
			structure2ToIeTree(((Structure2.CompInst) ccompInst).child, (Structure2.CompInst) ccompInst, node);
		}
		return node;
	}

	private static int outputRecurseDefault(Structure2.Comp comp, Structure2.CompInst compInst, JSONObject rootJson, JSONObject parentJson) {
		JSONObject thisNode = new JSONObject();
		String pPath = parentJson==null? "" : parentJson.getString("Part ID Path");
		String pNamePath = parentJson==null? "" : parentJson.getString("Part Path");
		String idPath = compInst==null ? "" : pPath+"/"+compInst.id;
		rootJson.put( (idPath.equals("")?"/":idPath), thisNode);
		
		JSONObject propEl = addProperties(thisNode, comp.properties, "");
		JSONObject pvSysP = new JSONObject();
		pvSysP.putOpt(BOUNDING_BOX,comp.bbox);
		if (compInst != null) {
//			pvSysP.put("Instance Translation", compInst.translation);//TODO: comment this and next line
//			pvSysP.put("Instance Orientation", compInst.orientation);
			Matrix4d mat = Structure2.getMatrix4dFromTranslationAndOrientation(compInst.translation, compInst.orientation);
			CreoViewTrafoHelper.addTrafoInfos(pvSysP, mat, INSTANCE_PREFIX);
			//calculate absolute trafo from parent trafo and my trafo
			CreoViewTrafoHelper.addAbsoluteTrafoInfos(parentJson, pvSysP, mat, INSTANCE_PREFIX+ABSOLUTE_PREFIX);
			
			pvSysP.put("Instance ID", compInst.id);
			pvSysP.put("Instance Name", compInst.name);
			pvSysP.put("Instance Type", compInst.type);			
		}
		
		pvSysP.putOpt("Component Name", comp.name);
		pvSysP.putOpt("Display Name", comp.name);
		//pvSysP.putOpt("Model Extents (mm)", comp.modelUnitLength);
		pvSysP.put("OL File Name", (comp.shape!=null && comp.shape.endsWith(".ol") ? comp.shape : ""));
		pvSysP.putOpt("Part Depth", idPath.split("/").length-1);
		pvSysP.putOpt("Part ID Path", idPath);
		pvSysP.putOpt("Part Name", comp.name);
		String partNamePath = compInst==null ? "/"+comp.name : pNamePath+"/"+comp.name;
		pvSysP.putOpt("Part Path", partNamePath);
		thisNode.putOpt("__PV_SystemProperties",pvSysP);
		
		int allChildCount=0, directChildCount=0;
		for (Object ccompInst : comp.childInsts) {
			directChildCount++;
			allChildCount++;
			int grandChildCount = outputRecurseDefault(((Structure2.CompInst) ccompInst).child, (Structure2.CompInst) ccompInst, rootJson, pvSysP);
			allChildCount = allChildCount+grandChildCount;
		}
		pvSysP.putOpt("Direct Child Count", directChildCount);
		pvSysP.putOpt("Child Count", allChildCount);

		if(parentJson!=null) {
			float[] absBBox = parentJson.has(ABSOLUTE_BOUNDING_BOX) ? (float[]) parentJson.get(ABSOLUTE_BOUNDING_BOX) : new float[6];
			if(comp.bbox!=null) { //pvs provided bboxes have precedence, my calculated ones are used when there is no pvs-provided one
				absBBox = CreoViewTrafoHelper.aggregateBBox(comp.bbox, (Matrix4d) pvSysP.get(INSTANCE_PREFIX+ABSOLUTE_PREFIX+CreoViewTrafoHelper.TRAFO_MATRIX4D_MAT), absBBox);
				float[] myAbsBBox = CreoViewTrafoHelper.aggregateBBox(comp.bbox, (Matrix4d) pvSysP.get(INSTANCE_PREFIX+ABSOLUTE_PREFIX+CreoViewTrafoHelper.TRAFO_MATRIX4D_MAT), null);
				pvSysP.put(ABSOLUTE_BOUNDING_BOX, myAbsBBox);
			}else if(pvSysP.has(ABSOLUTE_BOUNDING_BOX)) {
				absBBox = CreoViewTrafoHelper.aggregateBBox((float[])pvSysP.get(ABSOLUTE_BOUNDING_BOX), null, absBBox);			
			}
			parentJson.put(ABSOLUTE_BOUNDING_BOX, absBBox);
		}
		//TODO: we may want to calculate the relative combined bbox as well!?!
		float[] myAbsBBox = pvSysP.has(ABSOLUTE_BOUNDING_BOX) ? (float[]) pvSysP.get(ABSOLUTE_BOUNDING_BOX) : new float[6];
//		pvSysP.put("Model Extents (m)", new Point3f(myAbsBBox[0],myAbsBBox[1],myAbsBBox[2]).distance(new Point3f(myAbsBBox[3],myAbsBBox[4],myAbsBBox[5])) );
		//‘model extents’ is the longest of the 3 bbox axes. It is not the diagonal. (stupid imo)
		pvSysP.put("Model Extents (mm)", 1000*Math.max(Math.abs(myAbsBBox[0]-myAbsBBox[3]), Math.max(Math.abs(myAbsBBox[1]-myAbsBBox[4]), Math.abs(myAbsBBox[2]-myAbsBBox[5]))));
		
		pvSysP.put("Model Bounds",  formatAsString(pvSysP.opt(ABSOLUTE_BOUNDING_BOX)));
		pvSysP.remove(ABSOLUTE_BOUNDING_BOX);
		pvSysP.put("Component Bounds",  formatAsString(pvSysP.opt(BOUNDING_BOX)));
		pvSysP.remove(BOUNDING_BOX);
		// have to calculate Component Bounds from trafo (or do that before already....)
		//pvSysP.put("Component Bounds", Math.max(Math.abs(myAbsBBox[0]-myAbsBBox[3]), Math.max(Math.abs(myAbsBBox[1]-myAbsBBox[4]), Math.abs(myAbsBBox[2]-myAbsBBox[5]))));
		pvSysP.put("Model Location", pvSysP.optString(INSTANCE_PREFIX+ABSOLUTE_PREFIX+"location"));
		pvSysP.remove(INSTANCE_PREFIX+ABSOLUTE_PREFIX+"location");
		pvSysP.put("Instance Location", pvSysP.optString(INSTANCE_PREFIX+"location"));
		pvSysP.remove(INSTANCE_PREFIX+"location");
		//pvSysP.put("Model Orientation", formatAsString((Float[])pvSysP.get(ABSOLUTE_BOUNDING_BOX)));
		//pvSysP.put("Instance Orientation", formatAsString((Float[])pvSysP.get(ABSOLUTE_BOUNDING_BOX)));
		pvSysP.put("Model Transformation", formatAsString(pvSysP.opt(INSTANCE_PREFIX+ABSOLUTE_PREFIX+CreoViewTrafoHelper.TRAFO_MATRIX4D)));
		pvSysP.remove(INSTANCE_PREFIX+ABSOLUTE_PREFIX+CreoViewTrafoHelper.TRAFO_MATRIX4D);
		pvSysP.put("Instance Transformation", formatAsString(pvSysP.opt(INSTANCE_PREFIX+CreoViewTrafoHelper.TRAFO_MATRIX4D)));
		pvSysP.remove(INSTANCE_PREFIX+CreoViewTrafoHelper.TRAFO_MATRIX4D);

		pvSysP.remove(INSTANCE_PREFIX+ABSOLUTE_PREFIX+CreoViewTrafoHelper.TRAFO_MATRIX4D_MAT);
		pvSysP.remove(INSTANCE_PREFIX+CreoViewTrafoHelper.TRAFO_MATRIX4D_MAT);

		return allChildCount;
	}

	private static String formatAsString(Object array) {
		if(array == null) return null;
		if(array instanceof JSONArray) return ((JSONArray)array).join(" ");		
		if(array instanceof List || array.getClass().isArray()) return new JSONArray(array).join(" ");
		return null;
	}

	public static JSONObject addProperties(JSONObject thisNode, Hashtable compAttrs, String defaultPropgroupName) {
		JSONObject propEl=new JSONObject();
		thisNode.put(defaultPropgroupName, propEl);
		if (compAttrs != null) {
			Hashtable<String,String> pgLookup = (Hashtable<String,String>) compAttrs.get(Structure2.PROPERTY_GROUP_LOOKUP);
			Enumeration<?> attKeys = compAttrs.keys();
			while (attKeys.hasMoreElements()) {
				String attrName = (String) attKeys.nextElement();
				JSONObject gr = null;
				if(pgLookup!=null) {
					String group = pgLookup.get(attrName);
					if(group!=null){
						if(thisNode.has(group)) gr = thisNode.optJSONObject(group);
						else { 
							gr = new JSONObject();
							thisNode.put(group, gr);
						}
					} else gr=propEl;
				}else gr=propEl;
				String attValue = Structure2.getPropertyStringValue(compAttrs, attrName);
				if (attValue != null) gr.put(attrName, attValue);
				// log.debug("Comp attrName:"+attrName+" attValue:"+attValue);
			}
		}
		return propEl;
	}
	
	/**
	 * Produces an output format that is just wrapping the Structure2 serialization (with DefMutTrees and Hashtable User objects)
	 * This is a very rich format. It can be nested or flat with ComponentIDPath indenting
	 * @param thisNode
	 * @param parentJ
	 * @param pPath
	 * @return
	 */
	private static JSONObject outputRecurseSed2(DefaultMutableTreeNode thisNode, JSONObject rootJson, String pPath, boolean isNested) {
		Hashtable nodeData = (Hashtable)thisNode.getUserObject();
		JSONObject thisNodeJ = new JSONObject(nodeData);
		String instId = (String) nodeData.get(Structure2.WRITE_SKIP_PREFIX + Structure2.PVCID);
		String idPath=null, levelPath=null;
		if(pPath==null) {
			levelPath="/"; //this is an inconsistency in the format of the Path! The root element should have "" instead of "/" ...
			idPath="";
		}else{ 
			idPath=pPath+"/"+instId;
			levelPath=idPath;
		}
		thisNodeJ.put("id_path", levelPath);
		thisNodeJ.put("child_count", thisNode.getChildCount());
		if(!isNested) rootJson.put(levelPath, thisNodeJ);
		
		Enumeration<DefaultMutableTreeNode> nodesE = thisNode.children();
		JSONArray childs = new JSONArray();
		while(nodesE.hasMoreElements()) {
			DefaultMutableTreeNode childNode = nodesE.nextElement();
			if(isNested) {
				JSONObject childJson = outputRecurseSed2(childNode, thisNodeJ, idPath, true);
				thisNodeJ.append("components", childJson);
			}
			else outputRecurseSed2(childNode, rootJson, idPath, false);
		}
		return isNested ? thisNodeJ : rootJson;
	}

	public static void writePVSFromJSON(JSONObject json, String jsonFormat, File pvFile) throws Exception {
		if(jsonFormat!=null && jsonFormat.equalsIgnoreCase("WT_SED2_NESTED")) json = nest2WTSed2(json);
		//TODO: eventually preprocess other json formats	
//		int format = jsonFormat==null ? CreoViewRWHelper.DEFAULT : 
//	 		jsonFormat.equalsIgnoreCase("WT_SED2_FLAT") ? CreoViewRWHelper.WT_SED2_FLAT : 
//	 		jsonFormat.equalsIgnoreCase("WT_SED2_NESTED") ? CreoViewRWHelper.WT_SED2_NESTED : 
//			jsonFormat.equalsIgnoreCase("WT_STRUCTURE2") ? CreoViewRWHelper.WT_STRUCTURE2 :
//			jsonFormat.equalsIgnoreCase("PVS2JSON") ? CreoViewRWHelper.PVS2JSON : CreoViewRWHelper.DEFAULT;

		DefaultMutableTreeNode rootNode = getStructureTreeFromJson(json);
		
		Structure2.writeEDStructure(rootNode, pvFile);
	}

	private static DefaultMutableTreeNode getStructureTreeFromJson(JSONObject json) {
		Hashtable<String, Object> props = new Hashtable<>();
		DefaultMutableTreeNode myNode = new DefaultMutableTreeNode(props);
		for(String key : JSONObject.getNames(json)) {
			switch (key) {
			case "wrtskp_property_group_lookup":
				JSONObject jobj = json.getJSONObject(key);
				Hashtable<String, String> pht = new Hashtable<>();
				for(String pkey : JSONObject.getNames(jobj)) pht.put(pkey, jobj.getString(pkey));
				props.put(key, pht);
				break;
			case "wrtskp_viewables": //TODO: write out correctly: add in ht by type as key and value is single or list of filenames
				break;
			case "wrtskp_views": //TODO: write out correctly: ht key=views, value= StrArr of Strings 16 numbers + name (17.). 16 Numbers from Mat4d that is generated from  Quat4f fron CView float[]
				break;
			case "wrtskp_appearance_overide": //TODO: write out correctly: ht key=color, value=blank-sep Str of 4 nums, RGB + Opacity
				break;
			case "child_count":
				break;
			case "id_path":
				break;
			case "components":				
				break;
			default:
				props.put(key, json.get(key));
				break;
			}
		}
		JSONArray childAr = json.optJSONArray("components");
		if(childAr!=null)for(Object childObj : childAr) {
			myNode.add( getStructureTreeFromJson((JSONObject)childObj) );
		}
		return myNode;
	}

	private static JSONObject nest2WTSed2(JSONObject json) {
		
		for(String key : JSONObject.getNames(json)) {
			
		}
		return json;
	}
		
	
	
}
