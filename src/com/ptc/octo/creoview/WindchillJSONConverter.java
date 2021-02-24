package com.ptc.octo.creoview;


import java.time.ZonedDateTime;
import java.util.Iterator;
import java.util.TimeZone;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONArray;
import org.json.JSONObject;

import com.thingworx.metadata.FieldDefinition;
import com.thingworx.metadata.annotations.ThingworxServiceDefinition;
import com.thingworx.metadata.annotations.ThingworxServiceParameter;
import com.thingworx.metadata.annotations.ThingworxServiceResult;
import com.thingworx.things.Thing;
import com.thingworx.types.BaseTypes;
import com.thingworx.types.primitives.DatetimePrimitive;

public class WindchillJSONConverter extends Thing {
	private static final long serialVersionUID = -3399098263557626382L;


	@ThingworxServiceDefinition(name = "ConvertJSONToObject", category = "Connected PLM", description = "Takes existing JSON object and creates result compatible with InfoTable. Process ignores attributes that are unknown and not specified in data shape.")
	@ThingworxServiceResult(name = "result", baseType = "JSON", description = "Will have some attributes flattened and some ids converted.")
	public JSONObject ConvertJSONToObject(
			@ThingworxServiceParameter(name = "json", baseType = "JSON", description = "Object to convert.") JSONObject json,
			@ThingworxServiceParameter(name = "fields", baseType = "JSON", description = "List of keys identifying array or objects to iterate: ['name', 'number'] or {'name': null, 'number': null}.") JSONObject fields)
			throws Exception {
		JSONObject result;
		if (json.opt("paths") != null) {
			result = processDescendant(json, fields);
		} else {
			result = processEntity(json, fields);
		}
		return result;
	}

	private JSONObject processDescendant(JSONObject json, JSONObject fields) throws Exception {
		JSONArray array = new JSONArray();

		JSONObject linkMap = new JSONObject();
		if (json.opt("links") != null) {
			JSONArray links = json.getJSONArray("links");
			int i = 0;
			for (int l = links.length(); i < l; i++) {
				linkMap.put(json.getJSONArray("links").getJSONObject(i).getString("id"),
						json.getJSONArray("links").getJSONObject(i));
			}
		}
		JSONObject occMap = new JSONObject();
		if (json.opt("occurrences") != null) {
			JSONArray occurrences = json.getJSONArray("occurrences");
			int i = 0;
			for (int l = occurrences.length(); i < l; i++) {
				occMap.put(json.getJSONArray("occurrences").getJSONObject(i).getString("id"),
						json.getJSONArray("occurrences").getJSONObject(i));
			}
		}
		JSONArray paths = json.getJSONArray("paths");
		int i = 0;
		for (int l = paths.length(); i < l; i++) {
			JSONObject object = json.getJSONObject("object");
			JSONObject path = paths.getJSONObject(i);
			JSONObject link;
			if (path.opt("link") == null) {
				link = null;
			} else {
				if ((path.opt("link") instanceof String)) {
					link = linkMap.getJSONObject(path.getString("link"));
				} else {
					link = path.getJSONObject("link");
				}
			}
			JSONObject occurrence;
			if (path.opt("occurrence") == null) {
				occurrence = null;
			} else {
				if ((path.opt("occurrence") instanceof String)) {
					occurrence = occMap.getJSONObject(path.getString("occurrence"));
				} else {
					occurrence = path.getJSONObject("occurrence");
				}
			}
			JSONObject obj = new JSONObject();
			for (Iterator<String> it = fields.keys(); it.hasNext();) {
				String fieldName = (String) it.next();

				String wcFieldName = convertToWCFieldName(fieldName);
				if (object.getJSONObject("attributes").opt(wcFieldName) != null) {
					obj.put(fieldName, object.getJSONObject("attributes").opt(wcFieldName));
				} else if ((link != null) && (link.getJSONObject("attributes").opt(wcFieldName) != null)) {
					obj.put(fieldName, link.getJSONObject("attributes").opt(wcFieldName));
				} else if ((occurrence != null) && (occurrence.getJSONObject("attributes").opt(wcFieldName) != null)) {
					obj.put(fieldName, occurrence.getJSONObject("attributes").opt(wcFieldName));
				} else if (path.opt(wcFieldName) != null) {
					obj.put(fieldName, path.opt(wcFieldName));
				} else if (json.opt(wcFieldName) != null) {
					obj.put(fieldName, json.opt(wcFieldName));
				} else if (object.opt(wcFieldName) != null) {
					obj.put(fieldName, object.opt(wcFieldName));
				} else if ((link != null) && (link.opt(wcFieldName) != null)) {
					obj.put(fieldName, link.opt(wcFieldName));
				} else if ((occurrence != null) && (occurrence.opt(wcFieldName) != null)) {
					obj.put(fieldName, occurrence.opt(wcFieldName));
				}
				convertDateField(obj, fieldName, fields);
			}
			obj.put("objectId", object.opt("id"));
			obj.put("objectTypeId", object.opt("typeId"));
			if (link != null) {
				obj.put("linkId", link.opt("id"));
				obj.put("linkTypeId", link.opt("typeId"));
			}
			array.put(obj);
		}
		JSONObject ret = new JSONObject();
		ret.put("array", array);

		return ret;
	}

	private JSONObject processEntity(JSONObject json, JSONObject fields) throws Exception {
		JSONObject obj = new JSONObject();
		for (Iterator<String> it = fields.keys(); it.hasNext();) {
			String fieldName = (String) it.next();

			String wcFieldName = convertToWCFieldName(fieldName);
			if (json.opt(wcFieldName) != null) {
				obj.put(fieldName, json.opt(wcFieldName));
			} else if ((json.opt("attributes") != null)
					&& (json.getJSONObject("attributes").opt(wcFieldName) != null)) {
				obj.put(fieldName, json.getJSONObject("attributes").opt(wcFieldName));
			}
			convertDateField(obj, fieldName, fields);
		}
		if (json.opt("id") != null) {
			obj.put("objectId", json.opt("id"));
		}
		if (json.opt("typeId") != null) {
			obj.put("objectTypeId", json.opt("typeId"));
		}
		return obj;
	}

	private String convertToWCFieldName(String fieldName) {
		String wcFieldName = fieldName;
		if (fieldName.contains("--")) {
			wcFieldName = wcFieldName.replace("--", ".");
		}
		return wcFieldName;
	}

	private void convertDateField(JSONObject obj, String fieldName, JSONObject fields) throws Exception {
		FieldDefinition fd = (FieldDefinition) fields.get(fieldName);
		if ((obj.opt(fieldName) != null) && (fd.getBaseType().equals(BaseTypes.DATETIME))
				&& (!obj.getString(fieldName).isEmpty())) {
			DateTime converted;
			try {
				converted = DateTime.parse(obj.getString(fieldName), ISODateTimeFormat.dateTimeNoMillis());
			} catch (IllegalArgumentException e) {
				ZonedDateTime time = ZonedDateTime.parse(obj.getString(fieldName),
						java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm z"));
				converted = new DateTime(time.toInstant().toEpochMilli(),
						DateTimeZone.forTimeZone(TimeZone.getTimeZone(time.getZone())));
			}
			obj.put(fieldName, new DatetimePrimitive(converted).getStringValue());
		}
	}

}
