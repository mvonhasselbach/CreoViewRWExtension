package com.ptc.octo.creoview;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.json.JSONObject;
import org.slf4j.Logger;

import com.thingworx.entities.utils.ThingUtilities;
import com.thingworx.logging.LogUtilities;
import com.thingworx.metadata.annotations.ThingworxServiceDefinition;
import com.thingworx.metadata.annotations.ThingworxServiceParameter;
import com.thingworx.metadata.annotations.ThingworxServiceResult;
import com.thingworx.resources.Resource;
import com.thingworx.things.repository.FileRepositoryThing;

public class CreoViewRWHelper extends Resource {

	private static Logger _logger = LogUtilities.getInstance().getApplicationLogger(CreoViewRWHelper.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = -3244088689448397457L;
	
	public CreoViewRWHelper( ) {
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
			@ThingworxServiceParameter(name = "ReturnedProperties", description = "comma-separated list of properties that should be returned. If not specified all properties in the CreoView nodes will be returned.", baseType = "STRING") String returnedProperties) throws Exception{
		_logger.trace("Entering Service: GetJSONFromCreoViewFile");
		
		JSONObject json = new JSONObject();
		InputStream stream = null;
		FileRepositoryThing fileRepositoryThing = (FileRepositoryThing)ThingUtilities.findThing(fileRepository);
		FileInputStream pvFis = fileRepositoryThing.openFileForRead(creoViewFile);
		
//		ValueCollection params = new ValueCollection();
//		params.SetBooleanValue("isCool", true);
//		fileRepositoryThing.processAPIServiceRequest("vv", params);
		
		if (creoViewFile.endsWith(".pvz")) {
			ZipInputStream pvzIs = new ZipInputStream(pvFis);
			ZipEntry entry = null;
			//find the pvs zip entry
			do { entry = pvzIs.getNextEntry(); } while (entry !=null && !entry.getName().endsWith(".pvs") );
			if(entry==null) return json;
			stream = new BufferedInputStream(pvzIs, 8192);
		} else { // assuming a .pvs file			
			stream = new BufferedInputStream(pvFis, 8192);
		}
		json = Structure2.getJSONFromPVS(stream);
		// reduce the json output to the specified list of properties now
		if(returnedProperties !=null) Structure2.reduceJSON2Props(json, returnedProperties.split(","));
		if (stream != null) stream.close();
		if (pvFis != null) pvFis.close();
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
		return null;
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
		Structure2.reduceJSON2Props(pvJSON, returnedProperties.split(","));
		_logger.trace("Exiting Service: ReduceJSON2Props");
		return pvJSON;
	}

}
