package com.ptc.octo.creoview;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.json.JSONObject;
import org.slf4j.Logger;

import com.thingworx.entities.utils.ThingUtilities;
import com.thingworx.logging.LogUtilities;
import com.thingworx.metadata.annotations.ThingworxServiceDefinition;
import com.thingworx.metadata.annotations.ThingworxServiceParameter;
import com.thingworx.metadata.annotations.ThingworxServiceResult;
import com.thingworx.resources.Resource;
import com.thingworx.things.repository.FileRepositoryThing;

public class CreoViewRWResource extends Resource {

	private static Logger _logger = LogUtilities.getInstance().getApplicationLogger(CreoViewRWResource.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = -3244088689448397457L;
	
	public CreoViewRWResource( ) {
		// TODO Auto-generated constructor stub
	}

	@ThingworxServiceDefinition(name = "GetJSONFromCreoViewFile", description = "", category = "CreoView", isAllowOverride = false, aspects = {
			"isAsync:false" })
	@ThingworxServiceResult(name = "result", description = "deeply nested JSON that reflects the structure of the nodes in CreoView with their attributes", baseType = "JSON", aspects = {})
	public JSONObject GetJSONFromCreoViewFile(
			@ThingworxServiceParameter(name = "FileRepository", description = "FileRepository of CreoView file", baseType = "THINGNAME", aspects = {
					"isRequired:true", "thingTemplate:FileRepository" }) String fileRepository,
			@ThingworxServiceParameter(name = "CreoViewFile", description = "File path of the .pvz or .pvs to process ", baseType = "STRING", aspects = {
					"isRequired:true" }) String creoViewFile,
			@ThingworxServiceParameter(name = "JSONFormat", description = "JSON format to return, one of [DEFAULT|WT_SED2_NESTED|WT_SED2_FLAT|WT_STRUCTURE2], \ndefaults to DEFAULT (any value other than the other two will result in DEFAULT), \nwhere DEFAULT is the format that  Vuforia Studio outputs when metadataEnabled is set to true in builder-settings.json, \npvs2json is the format that Steve Ghee generated with his prototype implementation and \nWT_SED2_NESTED is the format that is a 1:1 of the Windchill Structure2.class internal structure", baseType = "STRING") String jsonFormat,
			@ThingworxServiceParameter(name = "ReturnedProperties", description = "comma-separated list of properties that should be returned. If not specified all properties in the CreoView nodes will be returned.", baseType = "STRING") String returnedProperties) throws Exception{
		_logger.trace("Entering Service: GetJSONFromCreoViewFile");
		
		JSONObject json = new JSONObject();
		InputStream stream = null;
		FileRepositoryThing fileRepositoryThing = (FileRepositoryThing)ThingUtilities.findThing(fileRepository);

		//FileInputStream pvFis = fileRepositoryThing.openFileForRead(creoViewFile);
		String rootPath = fileRepositoryThing.getRootPath();
		File pvFile = new File(rootPath, creoViewFile);
		
		
		if(!pvFile.exists()) pvFile = new File(rootPath+creoViewFile); //in case of one file path separator too much
		if(!pvFile.exists() || !pvFile.canRead()) throw new Exception("CreoView file: "+creoViewFile+" doesn't exist (or is not readable) in Repository: "+fileRepository);
		Map<String, InputStream> edpInputStreamMap = null;
		if(pvFile.getName().endsWith(".pvs")) {
			stream = new BufferedInputStream(new FileInputStream(pvFile), 8192);
		}else {
			try {
				ZipFile pvzFile = new ZipFile(pvFile);
				Enumeration<? extends ZipEntry> pvEntries = pvzFile.entries();
				while(pvEntries.hasMoreElements()) {
					ZipEntry pvEntry = pvEntries.nextElement();
					if(pvEntry.getName().endsWith(".ol")) continue;
					else if(pvEntry.getName().endsWith(".pvs")) stream = new BufferedInputStream(pvzFile.getInputStream(pvEntry), 8192);
					else {
						if(edpInputStreamMap==null) edpInputStreamMap = new HashMap<String, InputStream>();
						edpInputStreamMap.put(pvEntry.getName(), new BufferedInputStream(pvzFile.getInputStream(pvEntry), 8192));
					}
				}
			}catch(Exception ex) {
				throw new Exception("The creoView file you specified is neither a .pvs file nor a pvz (ie. zip-based file)", ex);
			}
		}
		int format = jsonFormat==null ? CreoViewRWHelper.DEFAULT : 
			 		jsonFormat.equalsIgnoreCase("WT_SED2_FLAT") ? CreoViewRWHelper.WT_SED2_FLAT : 
			 		jsonFormat.equalsIgnoreCase("WT_SED2_NESTED") ? CreoViewRWHelper.WT_SED2_NESTED : 
					jsonFormat.equalsIgnoreCase("WT_STRUCTURE2") ? CreoViewRWHelper.WT_STRUCTURE2 :
					//jsonFormat.equalsIgnoreCase("PVS2JSON") ? CreoViewRWHelper.PVS2JSON : 
					CreoViewRWHelper.DEFAULT;

		json = CreoViewRWHelper.getJSONFromPVS(stream, edpInputStreamMap, format);
		// reduce the json output to the specified list of properties now
		if(returnedProperties !=null) CreoViewRWHelper.reduceJSON2Props(json, returnedProperties.split(","));
		if (stream != null) stream.close();
		_logger.trace("Exiting Service: GetJSONFromCreoViewFile");
		return json;
	}

	@ThingworxServiceDefinition(name = "GetJSONFromCreoViewFileURL", description = "Get the json representation of the pvs information from a URL that points to the creo view file. I'll use the ContentLoader here and start with 'simple' auth schemas.", category = "", isAllowOverride = false, aspects = {
			"isAsync:false" })
	@ThingworxServiceResult(name = "result", description = "", baseType = "JSON", aspects = {})
	public JSONObject GetJSONFromCreoViewFileURL(
			@ThingworxServiceParameter(name = "CreoViewFileURL", description = "", baseType = "HYPERLINK", aspects = {
					"isRequired:true" }) String CreoViewFileURL) {
		_logger.trace("Entering Service: GetJSONFromCreoViewFileURL");
		_logger.trace("Exiting Service: GetJSONFromCreoViewFileURL");
		return new JSONObject("{\"message\",\"This service is not implemented yet. Please use a combination of ContentLoader services and GetJSONFromCreoViewFile\"}") ;
	}

	@ThingworxServiceDefinition(name = "ReduceJSON2Props", description = "Reduce the content of the JSON to the list of properties specified", category = "", isAllowOverride = false, aspects = {
			"isAsync:false" })
	@ThingworxServiceResult(name = "result", description = "", baseType = "JSON", aspects = {})
	public JSONObject ReduceJSON2Props(
			@ThingworxServiceParameter(name = "pvJSON", description = "", baseType = "JSON", aspects = {
					"isRequired:true" }) JSONObject pvJSON,
			@ThingworxServiceParameter(name = "ReturnedProperties", description = "comma-sep list of properties to return (whitelist of props)", baseType = "STRING", aspects = {
					"isRequired:true" }) String returnedProperties) {
		_logger.trace("Entering Service: ReduceJSON2Props");
		CreoViewRWHelper.reduceJSON2Props(pvJSON, returnedProperties.split(","));
		_logger.trace("Exiting Service: ReduceJSON2Props");
		return pvJSON;
	}

}
