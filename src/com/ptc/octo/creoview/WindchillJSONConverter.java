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

	// @ThingworxServiceDefinition(name="processJSONRequest", category="PTC
	// Connection", isAllowOverride=true, description="Process a AJAX request and
	// parse its response to JSON")
	// @ThingworxServiceResult(name="Result", baseType="JSON", description="The
	// resulting objects")
	// public JSONObject processJSONRequest(@ThingworxServiceParameter(name="type",
	// baseType="STRING", aspects={"defaultValue:GET"}, description="HTTP method")
	// String type, @ThingworxServiceParameter(name="url", baseType="STRING",
	// description="The url to send request to") String url,
	// @ThingworxServiceParameter(name="queryParams", baseType="JSON",
	// description="The query parameters") JSONObject queryParams,
	// @ThingworxServiceParameter(name="username", baseType="STRING",
	// description="Optional user name credential") String username,
	// @ThingworxServiceParameter(name="password", baseType="STRING", 
	// description="Optional password credential") String password,
	// @ThingworxServiceParameter(name="headers", baseType="JSON", description="HTTP
	// request header") JSONObject headers, @ThingworxServiceParameter(name="data",
	// baseType="JSON", description="HTTP request body") JSONObject data,
	// @ThingworxServiceParameter(name="timeout", baseType="NUMBER",
	// description="HTTP request timeout") Double timeout)
	// throws Exception
	// {
	// try
	// {
	// return processJSONRequest(type, url, queryParams, headers, data, timeout, 3);
	// }
	// catch (JSONServerException e)
	// {
	// throw e;
	// }
	// catch (Exception e)
	// {
	// e.printStackTrace();
	// throw e;
	// }
	// }
	//
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
	//
	// protected void initializeThing()
	// throws Exception
	// {
	// String baseURL = getStringConfigurationSetting("PTC Connector Configuration",
	// "baseURL");
	// String keystorePath =
	// getStringConfigurationSetting("SSLConnectionConfiguration",
	// "SSLKeyStorePath");
	// String keystorePassword =
	// getStringConfigurationSetting("SSLConnectionConfiguration",
	// "SSLKeyStorePassword");
	// String keystoreType =
	// getStringConfigurationSetting("SSLConnectionConfiguration",
	// "SSLKeyStoreType");
	// String truststorePath =
	// getStringConfigurationSetting("SSLConnectionConfiguration",
	// "SSLTrustStorePath");
	// String truststorePassword =
	// getStringConfigurationSetting("SSLConnectionConfiguration",
	// "SSLTrustStorePassword");
	// String truststoreType =
	// getStringConfigurationSetting("SSLConnectionConfiguration",
	// "SSLTrustStoreType");
	//
	// URL url = new URL(baseURL);
	// TwxIEWebServiceImplService service = new TwxIEWebServiceImplService();
	// this.port = service.getTwxIEWebServiceImplPort();
	// if (url.getProtocol().equals("https"))
	// {
	// this.sslContext = createSSLContext(keystorePath, keystorePassword,
	// keystoreType, truststorePath, truststorePassword, truststoreType);
	//
	// Map<String, Object> requestContext =
	// ((BindingProvider)this.port).getRequestContext();
	// requestContext.put("com.sun.xml.ws.transport.https.client.SSLSocketFactory",
	// this.sslContext.getSocketFactory());
	// }
	// }
	//
	// protected SSLContext createSSLContext(String keystorePath, String
	// keystorePassword, String keystoreType, String truststorePath, String
	// truststorePassword, String truststoreType)
	// throws ConnectorConfigurationException
	// {
	// try
	// {
	// if ((keystorePath == null) || (keystorePath.length() == 0)) {
	// throw new ConnectorConfigurationException("SSLKeyStorePath is not
	// specified.");
	// }
	// if ((keystoreType == null) || (keystoreType.length() == 0)) {
	// throw new ConnectorConfigurationException("SSLKeyStoreType is not
	// specified.");
	// }
	// if ((truststorePath == null) || (truststorePath.length() == 0)) {
	// throw new ConnectorConfigurationException("SSLTrustStorePath is not
	// specified.");
	// }
	// if ((truststoreType == null) || (truststoreType.length() == 0)) {
	// throw new ConnectorConfigurationException("SSLTrustStoreType is not
	// specified.");
	// }
	// if (!new File(keystorePath).canRead()) {
	// throw new ConnectorConfigurationException(String.format("SSLKeyStorePath file
	// %s does not exist or cannot be read.", new Object[] { keystorePath }));
	// }
	// if (!new File(truststorePath).canRead()) {
	// throw new ConnectorConfigurationException(String.format("SSLTrustStorePath
	// file %s does not exist or cannot be read.", new Object[] { truststorePath
	// }));
	// }
	// try
	// {
	// keystore = KeyStore.getInstance(keystoreType);
	// }
	// catch (KeyStoreException e)
	// {
	// KeyStore keystore;
	// throw new ConnectorConfigurationException("keystore type " + keystoreType + "
	// not supported.", e);
	// }
	// KeyStore keystore;
	// keystore.load(new FileInputStream(keystorePath),
	// keystorePassword.toCharArray());
	// logger.debug(String.format("KeyStore class: %s", new Object[] {
	// keystore.getClass() }));
	// logger.debug(String.format("KeyStore Type: %s", new Object[] {
	// keystore.getType() }));
	// logger.debug(String.format("KeyStore Provider: %s", new Object[] {
	// keystore.getProvider() }));
	// logger.debug(String.format("KeyStore Size: %s", new Object[] {
	// Integer.valueOf(keystore.size()) }));
	// try
	// {
	// truststore = KeyStore.getInstance(truststoreType);
	// }
	// catch (KeyStoreException e)
	// {
	// KeyStore truststore;
	// throw new ConnectorConfigurationException("keystore type " + truststoreType +
	// " not supported.", e);
	// }
	// KeyStore truststore;
	// truststore.load(new FileInputStream(truststorePath),
	// truststorePassword.toCharArray());
	// logger.debug(String.format("TrustStore class: %s", new Object[] {
	// truststore.getClass() }));
	// logger.debug(String.format("TrustStore Type: %s", new Object[] {
	// truststore.getType() }));
	// logger.debug(String.format("TrustStore Provider: %s", new Object[] {
	// truststore.getProvider() }));
	// logger.debug(String.format("TrustStore Size: %s", new Object[] {
	// Integer.valueOf(truststore.size()) }));
	//
	// KeyManagerFactory kmf =
	// KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
	// logger.debug(String.format("KeyManagerFactory class: %s", new Object[] {
	// kmf.getClass() }));
	// logger.debug(String.format("KeyManagerFactory Algorithm: %s", new Object[] {
	// kmf.getAlgorithm() }));
	// logger.debug(String.format("KeyManagerFactory Provider: %s", new Object[] {
	// kmf.getProvider() }));
	// try
	// {
	// kmf.init(keystore, keystorePassword.toCharArray());
	// }
	// catch (UnrecoverableKeyException e)
	// {
	// throw new ConnectorConfigurationException("The password on keystore " +
	// keystorePath + " must be the same as the password on the private key it
	// contains.", e);
	// }
	// KeyManager[] kmList = kmf.getKeyManagers();
	// logger.debug(String.format("KeyManager class: %s", new Object[] {
	// kmList[0].getClass() }));
	// logger.debug(String.format("KeyManager # of managers: %s", new Object[] {
	// Integer.valueOf(kmList.length) }));
	//
	// TrustManagerFactory tmf =
	// TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
	// logger.debug(String.format("TrustManagerFactory class: %s", new Object[] {
	// tmf.getClass() }));
	// logger.debug(String.format("TrustManagerFactory Algorithm: %s", new Object[]
	// { tmf.getAlgorithm() }));
	// logger.debug(String.format("TrustManagerFactory Provider: %s", new Object[] {
	// tmf.getProvider() }));
	// tmf.init(truststore);
	// TrustManager[] tmList = tmf.getTrustManagers();
	// logger.debug(String.format("TrustManager class: %s", new Object[] {
	// tmList[0].getClass() }));
	// logger.debug(String.format("TrustManager # of managers: %s", new Object[] {
	// Integer.valueOf(tmList.length) }));
	//
	// SSLContext sc = SSLContext.getInstance("TLS");
	// logger.debug(String.format("SSLContext class: %s", new Object[] {
	// sc.getClass() }));
	// logger.debug(String.format("SSLContext Protocol: %s", new Object[] {
	// sc.getProtocol() }));
	// logger.debug(String.format("SSLContext Provider: %s", new Object[] {
	// sc.getProvider() }));
	// sc.init(kmList, tmList, null);
	//
	// return sc;
	// }
	// catch (ConnectorConfigurationException e)
	// {
	// e.printStackTrace();
	// throw e;
	// }
	// catch (Exception e)
	// {
	// e.printStackTrace();
	// throw new ConnectorConfigurationException(e.getMessage(), e);
	// }
	// }
	//
	// protected void addCSRFHeader(HttpURLConnection connection)
	// throws Exception
	// {
	// if ((this.cachedNonceKey == null) || (this.cachedNonce == null))
	// {
	// logger.debug("Fetching CSRF nonce");
	// String baseUrlString = getStringConfigurationSetting("PTC Connector
	// Configuration", "baseURL");
	// String restPathString = getStringConfigurationSetting("PTC Connector
	// Configuration", "restPath");
	// String urlString = baseUrlString + restPathString + "/security/csrf";
	// try
	// {
	// JSONObject response = processJSONRequest("GET", urlString, null, null, null,
	// null, null, Double.valueOf(15000.0D));
	// JSONArray items = response.getJSONArray("items");
	// for (int i = 0; i < items.length(); i++)
	// {
	// JSONObject item = items.getJSONObject(i);
	// if ("csrf".equals(item.getString("id")))
	// {
	// JSONObject attributes = item.getJSONObject("attributes");
	// this.cachedNonceKey = attributes.getString("nonce_key");
	// this.cachedNonce = attributes.getString("nonce");
	// break;
	// }
	// }
	// }
	// catch (JSONServerException e)
	// {
	// if (e.getStatusCode() != 404) {
	// throw e;
	// }
	// logger.info("CSRF nonce is not supported so turn off use of nonce");
	// this.isCSRFProtected = false;
	// }
	// }
	// if ((this.cachedNonceKey != null) && (this.cachedNonce != null))
	// {
	// logger.debug("Add HTTP header nonce");
	// connection.setRequestProperty(this.cachedNonceKey, this.cachedNonce);
	// }
	// }

	// protected JSONObject processJSONRequest(String type, String url, JSONObject
	// queryParams, JSONObject headers, JSONObject data, Double timeout, int
	// retryCount)
	// throws Exception
	// {
	// String effectiveUID = "wt.effectiveUid";
	// if (url.startsWith("/"))
	// {
	// String baseUrl = getStringConfigurationSetting("PTC Connector Configuration",
	// "baseURL");
	// String restPath = getStringConfigurationSetting("PTC Connector
	// Configuration", "restPath");
	// URI uri = new URI(baseUrl + restPath + url);
	// url = uri.toString();
	// }
	// String baseUrlString = getStringConfigurationSetting("PTC Connector
	// Configuration", "baseURL");
	// if (!url.startsWith(baseUrlString)) {
	// throw new IllegalArgumentException("processJSONRequest called with URL to
	// different host than baseURL");
	// }
	// URL baseUrl = new URL(baseUrlString);
	//
	// StringBuilder requestUrl = new StringBuilder(baseUrlString);
	// if (baseUrl.getProtocol().equals("https")) {
	// requestUrl.append("/sslClientAuth");
	// } else {
	// requestUrl.append("/trustedAuth");
	// }
	// requestUrl.append(url.substring(baseUrlString.length()));
	// logger.debug("Request for REST endpoint {}", requestUrl);
	// if (!url.contains("?")) {
	// requestUrl.append("?");
	// } else {
	// requestUrl.append("&");
	// }
	// Iterator<String> it;
	// if (queryParams != null) {
	// for (it = queryParams.keys(); it.hasNext();)
	// {
	// String paramKey = (String)it.next();
	// String paramValue = queryParams.getString(paramKey);
	// logger.debug("Query parameter {} = {}", paramKey, paramValue);
	// requestUrl.append(URLEncoder.encode(paramKey, "UTF-8"));
	// requestUrl.append("=");
	// requestUrl.append(URLEncoder.encode(paramValue, "UTF-8"));
	// requestUrl.append("&");
	// }
	// }
	// if (requestUrl.toString().toLowerCase().contains(effectiveUID.toLowerCase()))
	// {
	// throw new IllegalArgumentException("Processing username cannot be passed in
	// as an argument");
	// }
	// String username = getAssertion(requestUrl.toString());
	// if (username != null)
	// {
	// logger.debug("Effective user {}", username);
	// requestUrl.append(effectiveUID + "=");
	// requestUrl.append(URLEncoder.encode(username, "UTF-8"));
	// }
	// URL serviceURL = new URL(requestUrl.toString());
	//
	// HttpURLConnection con = (HttpURLConnection)serviceURL.openConnection();
	// if (serviceURL.getProtocol().equals("https"))
	// {
	// ((HttpsURLConnection)con).setSSLSocketFactory(this.sslContext.getSocketFactory());
	//
	// ((HttpsURLConnection)con).setHostnameVerifier(new HostnameVerifier()
	// {
	// public boolean verify(String arg0, SSLSession arg1)
	// {
	// return false;
	// }
	// });
	// }
	// int timeoutInt = timeout == null ? getServiceTimeout().intValue() :
	// timeout.intValue();
	// con.setRequestMethod(type);
	// con.setDoOutput(true);
	// con.setDoInput(true);
	// con.setConnectTimeout(timeoutInt);
	// con.setReadTimeout(timeoutInt);
	//
	// logger.debug("Add HTTP header Content-Type: application/json");
	// logger.debug("Add HTTP header Accept: application/json");
	// con.setRequestProperty("Content-Type", "application/json");
	// con.setRequestProperty("Accept", "application/json");
	//
	// String language = getcurrentUserLanguage();
	// if ((language != null) && (!language.equals(""))) {
	// con.setRequestProperty("Accept-Language", language);
	// }
	// Iterator<String> it;
	// if (headers != null) {
	// for (it = headers.keys(); it.hasNext();)
	// {
	// String headerKey = (String)it.next();
	// String headerValue = headers.getString(headerKey);
	// logger.debug("Add HTTP header {}: {}", headerKey, headerValue);
	// con.setRequestProperty(headerKey, headerValue);
	// }
	// }
	// if (!type.equalsIgnoreCase("GET"))
	// {
	// if (this.isCSRFProtected) {
	// addCSRFHeader(con);
	// }
	// logger.debug("Write JSON payload");
	// byte[] content = data.toString().getBytes("UTF-8");
	// OutputStream outStream = con.getOutputStream();
	// outStream.write(content);
	// }
	// int responseCode = con.getResponseCode();
	// logger.debug("Received HTTP response {}", Integer.valueOf(responseCode));
	// int read;
	// if (responseCode != 200)
	// {
	// if (responseCode == 401) {
	// deleteAssertion(url);
	// }
	// if ((responseCode == 412) && (retryCount > 0))
	// {
	// logger.debug("Failed CSRF check, will refresh nonce and try again");
	//
	// this.cachedNonceKey = null;
	// this.cachedNonce = null;
	// this.isCSRFProtected = true;
	// return processJSONRequest(type, url, queryParams, headers, data, timeout,
	// --retryCount);
	// }
	// String contentType = null;
	// if ((con.getHeaderField("Content-Type") != null) &&
	// ((con.getHeaderField("Content-Type").contains("text/plain")) ||
	// (con.getHeaderField("Content-Type").contains("text/html")) ||
	// (con.getHeaderField("Content-Type").contains("application/json"))) &&
	// (con.getErrorStream() != null)) {
	// contentType = con.getHeaderField("Content-Type");
	// }
	// body = null;
	// if (contentType != null)
	// {
	// BufferedReader in = new BufferedReader(new
	// InputStreamReader(con.getErrorStream(), "UTF-8"));Throwable localThrowable6 =
	// null;
	// try
	// {
	// StringBuilder responseBuf = new StringBuilder();
	// int bufferSize = 1024;
	// char[] buffer = new char['?'];
	// while ((read = in.read(buffer)) != -1) {
	// responseBuf.append(buffer, 0, read);
	// }
	// body = responseBuf.toString();
	// }
	// catch (Throwable localThrowable1)
	// {
	// localThrowable6 = localThrowable1;throw localThrowable1;
	// }
	// finally
	// {
	// if (in != null) {
	// if (localThrowable6 != null) {
	// try
	// {
	// in.close();
	// }
	// catch (Throwable localThrowable2)
	// {
	// localThrowable6.addSuppressed(localThrowable2);
	// }
	// } else {
	// in.close();
	// }
	// }
	// }
	// }
	// throw new JSONServerException(serviceURL.toString(), type, responseCode,
	// body, contentType);
	// }
	// logger.debug("Read JSON response");
	// BufferedReader in = new BufferedReader(new
	// InputStreamReader(con.getInputStream(), "UTF-8"));String body = null;
	// try
	// {
	// StringBuilder responseBuf = new StringBuilder();
	// int bufferSize = 1024;
	// char[] buffer = new char['?'];
	// int read;
	// while ((read = in.read(buffer)) != -1) {
	// responseBuf.append(buffer, 0, read);
	// }
	// JSONObject json = JSONUtilities.readJSON(responseBuf.toString());
	// logger.debug("Return JSON response");
	// return json;
	// }
	// catch (Throwable localThrowable4)
	// {
	// body = localThrowable4;throw localThrowable4;
	// }
	// finally
	// {
	// if (in != null) {
	// if (body != null) {
	// try
	// {
	// in.close();
	// }
	// catch (Throwable localThrowable5)
	// {
	// body.addSuppressed(localThrowable5);
	// }
	// } else {
	// in.close();
	// }
	// }
	// }
	// }
	//
	// private String sanitizeType(String type)
	// {
	// if (type == null) {
	// return "";
	// }
	// return type.replaceAll("WCTYPE\\|", "").replaceAll("\\|", ".");
	// }
	//
	// private String escapeAttributeName(String attributeName)
	// {
	// return attributeName.replace(".", "--");
	// }
	//
	// private String unEscapeAttributeName(String attributeName)
	// {
	// return attributeName.replace("--", ".");
	// }
	//
	// private List<Property> convertToPropertyList(InfoTable it)
	// {
	// List<Property> result = new ArrayList();
	// if (it != null) {
	// for (ValueCollection values : it.getRows())
	// {
	// Property p = new Property();
	// p.setName(values.getStringValue("name"));
	// p.setValue(values.getStringValue("value"));
	// result.add(p);
	// }
	// }
	// return result;
	// }
	//
	// private List<GenericBusinessObject> convertToBusinessObjects(InfoTable it)
	// throws Exception
	// {
	// List<GenericBusinessObject> result = new ArrayList();
	// if (it != null) {
	// for (ValueCollection values : it.getRows())
	// {
	// GenericBusinessObject gbo = new GenericBusinessObject();
	// for (String key : values.keySet()) {
	// if (key.equalsIgnoreCase("ufid"))
	// {
	// gbo.setUfid(values.getStringValue("ufid"));
	// }
	// else if (key.equalsIgnoreCase("type"))
	// {
	// gbo.setTypeIdentifier(values.getStringValue("type"));
	// }
	// else
	// {
	// Property p = new Property();
	// p.setName(unEscapeAttributeName(key));
	// p.setValue(values.getStringValue(key));
	// gbo.getProperties().add(p);
	// }
	// }
	// result.add(gbo);
	// }
	// }
	// return result;
	// }
	//
	// private List<ContentHandle> convertToContentHandle(InfoTable it)
	// {
	// List<ContentHandle> result = new ArrayList();
	// if (it != null) {
	// for (ValueCollection values : it.getRows())
	// {
	// ContentHandle ch = new ContentHandle();
	// ch.setMethod(values.getStringValue("method"));
	// ch.setUrl(values.getStringValue("url"));
	// result.add(ch);
	// }
	// }
	// return result;
	// }
	//
	// private InfoTable convertToInfoTable(List<String> list)
	// throws Exception
	// {
	// DataShape ds = (DataShape)EntityUtilities.findEntity("IEListString",
	// RelationshipTypes.ThingworxRelationshipTypes.DataShape);
	// InfoTable it =
	// InfoTableInstanceFactory.createInfoTableFromDataShape(ds.getDataShape());
	// if (list != null) {
	// for (String string : list)
	// {
	// ValueCollection values = new ValueCollection();
	// values.put("value", BaseTypes.ConvertToPrimitive(string, BaseTypes.STRING));
	// it.addRow(values);
	// }
	// }
	// return it;
	// }
	//
	// private InfoTable convertToBooleanInfoTable(List<Boolean> list)
	// throws Exception
	// {
	// DataShape ds = (DataShape)EntityUtilities.findEntity("IEListBoolean",
	// RelationshipTypes.ThingworxRelationshipTypes.DataShape);
	// InfoTable it =
	// InfoTableInstanceFactory.createInfoTableFromDataShape(ds.getDataShape());
	// if (list != null) {
	// for (Boolean bool : list)
	// {
	// ValueCollection values = new ValueCollection();
	// values.put("value", BaseTypes.ConvertToPrimitive(bool, BaseTypes.BOOLEAN));
	// it.addRow(values);
	// }
	// }
	// return it;
	// }
	//
	// private List<String> convertToListString(InfoTable it)
	// throws Exception
	// {
	// List<String> result = new ArrayList();
	// if (it != null) {
	// for (ValueCollection values : it.getRows()) {
	// result.add(values.getStringValue("value"));
	// }
	// }
	// return result;
	// }
	//
	// private InfoTable convertToInfoTable(SchemaNode node)
	// throws Exception
	// {
	// ArrayList<SchemaNode> list = new ArrayList();
	// if (node != null) {
	// list.add(node);
	// }
	// return convertToSchemaNodeInfoTable(list);
	// }
	//
	// private InfoTable convertToSchemaNodeInfoTable(List<SchemaNode> nodes)
	// throws Exception
	// {
	// DataShape ds = (DataShape)EntityUtilities.findEntity("IESchemaNode",
	// RelationshipTypes.ThingworxRelationshipTypes.DataShape);
	// InfoTable it =
	// InfoTableInstanceFactory.createInfoTableFromDataShape(ds.getDataShape());
	// if (nodes != null) {
	// for (SchemaNode node : nodes)
	// {
	// ValueCollection values = new ValueCollection();
	// values.put("name", BaseTypes.ConvertToPrimitive(node.getName(),
	// BaseTypes.STRING));
	// values
	// .put("ancestors",
	// BaseTypes.ConvertToPrimitive(convertToInfoTable(node.getAncestors()),
	// BaseTypes.INFOTABLE));
	// it.addRow(values);
	// }
	// }
	// return it;
	// }
	//
	// private void populateInfoTable(InfoTable it, List<GenericBusinessObject>
	// gbos)
	// throws Exception
	// {
	// if (gbos != null) {
	// for (GenericBusinessObject gbo : gbos)
	// {
	// ValueCollection values = new ValueCollection();
	// values.put("type", BaseTypes.ConvertToPrimitive(gbo.getTypeIdentifier(),
	// BaseTypes.STRING));
	// values.put("ufid", BaseTypes.ConvertToPrimitive(gbo.getUfid(),
	// BaseTypes.STRING));
	// for (Property p : gbo.getProperties())
	// {
	// String fieldName = escapeAttributeName(p.getName());
	// FieldDefinition fieldDef = it.getDataShape().getFieldDefinition(fieldName);
	// if (fieldDef != null)
	// {
	// Object fieldValue;
	// Object fieldValue;
	// Object fieldValue;
	// switch (fieldDef.getBaseType())
	// {
	// case DATETIME:
	// Object fieldValue;
	// try
	// {
	// ZonedDateTime time = ZonedDateTime.parse(p.getValue(),
	// java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"));
	// fieldValue = new DateTime(time.toInstant().toEpochMilli(),
	// DateTimeZone.forTimeZone(TimeZone.getTimeZone(time.getZone())));
	// }
	// catch (DateTimeParseException e)
	// {
	// Object fieldValue;
	// fieldValue = DateTime.parse(p.getValue(),
	// DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.S").withZoneUTC());
	// }
	// case INTEGER:
	// fieldValue = Integer.valueOf(Integer.parseInt(p.getValue()));
	// break;
	// case NUMBER:
	// fieldValue = Double.valueOf(Double.parseDouble(p.getValue()));
	// break;
	// default:
	// fieldValue = p.getValue();
	// }
	// values.put(fieldName, BaseTypes.ConvertToPrimitive(fieldValue,
	// fieldDef.getBaseType()));
	// }
	// }
	// it.addRow(values);
	// }
	// }
	// }
	//
	// private InfoTable
	// convertToPropertyDescriptionInfoTable(List<PropertyDescription> pds)
	// throws Exception
	// {
	// if (pds == null) {
	// return null;
	// }
	// DataShape ds = (DataShape)EntityUtilities.findEntity("IEPropertyDescription",
	// RelationshipTypes.ThingworxRelationshipTypes.DataShape);
	//
	// InfoTable it =
	// InfoTableInstanceFactory.createInfoTableFromDataShape(ds.getDataShape());
	// for (PropertyDescription pd : pds)
	// {
	// ValueCollection values = new ValueCollection();
	// values.put("name", BaseTypes.ConvertToPrimitive(pd.getName(),
	// BaseTypes.STRING));
	// values
	// .put("defaultLocalizedValue",
	// BaseTypes.ConvertToPrimitive(pd.getDefaultLocalizedValue(),
	// BaseTypes.STRING));
	// values.put("label", BaseTypes.ConvertToPrimitive(pd.getLabel(),
	// BaseTypes.STRING));
	// values.put("defaultValue", BaseTypes.ConvertToPrimitive(pd.getDefaultValue(),
	// BaseTypes.STRING));
	// values.put("localizedValues",
	// BaseTypes.ConvertToPrimitive(convertToInfoTable(pd.getLocalizedValues()),
	// BaseTypes.INFOTABLE));
	// values.put("longDescriptions",
	// BaseTypes.ConvertToPrimitive(convertToInfoTable(pd.getLongDescriptions()),
	// BaseTypes.INFOTABLE));
	// values
	// .put("longLabels",
	// BaseTypes.ConvertToPrimitive(convertToInfoTable(pd.getLongLabels()),
	// BaseTypes.INFOTABLE));
	// values.put("lowerLimit", BaseTypes.ConvertToPrimitive(pd.getLowerLimit(),
	// BaseTypes.STRING));
	// values.put("required",
	// BaseTypes.ConvertToPrimitive(Boolean.valueOf(pd.isRequired()),
	// BaseTypes.BOOLEAN));
	// values.put("selectable",
	// BaseTypes.ConvertToPrimitive(convertToBooleanInfoTable(pd.getSelectable()),
	// BaseTypes.INFOTABLE));
	// values.put("shortDescriptions",
	// BaseTypes.ConvertToPrimitive(convertToInfoTable(pd.getShortDescriptions()),
	// BaseTypes.INFOTABLE));
	// values.put("shortLabels",
	// BaseTypes.ConvertToPrimitive(convertToInfoTable(pd.getShortLabels()),
	// BaseTypes.INFOTABLE));
	// values.put("stringValues",
	// BaseTypes.ConvertToPrimitive(convertToInfoTable(pd.getStringValues()),
	// BaseTypes.INFOTABLE));
	// values.put("upperLimit", BaseTypes.ConvertToPrimitive(pd.getUpperLimit(),
	// BaseTypes.STRING));
	// values.put("syntax", BaseTypes.ConvertToPrimitive(pd.getSyntax(),
	// BaseTypes.STRING));
	// values.put("values",
	// BaseTypes.ConvertToPrimitive(convertToInfoTable(pd.getValues()),
	// BaseTypes.INFOTABLE));
	// it.addRow(values);
	// }
	// return it;
	// }
	//
	// private List<String> convertToPropertyNameArray(DataShape ds)
	// {
	// List<String> result = new ArrayList();
	// if (ds == null) {
	// return result;
	// }
	// DataShapeDefinition def = ds.getDataShape();
	// for (FieldDefinition fd : def.getFields().getOrderedFields()) {
	// result.add(unEscapeAttributeName(fd.getName()));
	// }
	// return result;
	// }

	// private void setup()
	// throws Exception
	// {
	// URL url = new URL(getStringConfigurationSetting("PTC Connector
	// Configuration", "baseURL"));
	//
	// Credentials.setUsername("notused");
	// Credentials.setPassword("notused");
	//
	// String filterURL = url.getProtocol().equals("https") ? "sslClientAuth" :
	// "trustedAuth";
	//
	// StringBuilder requestURL = new StringBuilder(url.toString());
	// requestURL.append("/");
	// requestURL.append(filterURL);
	// requestURL.append("/servlet/TwxIEWebService?wt.effectiveUid=");
	// requestURL.append(ThreadLocalContext.getSecurityContext().getName());
	// WebServiceURLHandler.setWebServiceURL(requestURL.toString());
	// }
	//
	// public Double getServiceTimeout()
	// throws Exception
	// {
	// return (Double)getConfigurationSetting("PTC Connector Configuration",
	// "ServiceTimeout");
	// }
	//
	// @ThingworxServiceDefinition(name="convertItemListToDataShape", category="PTC
	// Connection", description="Convert a JSON string of an ItemList to an
	// infotable based on a given datashape name")
	// @ThingworxServiceResult(name="Result", description="The resulting objects",
	// baseType="INFOTABLE")
	// public InfoTable
	// convertItemListToDataShape(@ThingworxServiceParameter(name="itemList",
	// description="A JSON string of an ItemList", baseType="STRING") String
	// itemListStr, @ThingworxServiceParameter(name="shapeName", description="Data
	// shape that will returned, it also defines what attributes will be returned",
	// baseType="DATASHAPENAME") String dataShape)
	// throws Exception
	// {
	// DataShape ds = (DataShape)EntityUtilities.findEntity(dataShape,
	// RelationshipTypes.ThingworxRelationshipTypes.DataShape);
	// if (ds == null) {
	// throw new Exception("Unable convert to an invalid datashape [" + dataShape +
	// "]");
	// }
	// InfoTable it =
	// InfoTableInstanceFactory.createInfoTableFromDataShape(ds.getDataShape());
	//
	// JSONObject itemList = new JSONObject(itemListStr);
	// JSONArray items = itemList.getJSONArray("items");
	// for (int i = 0; i < items.length(); i++)
	// {
	// JSONObject item = items.getJSONObject(i);
	// JSONObject attributes = item.getJSONObject("attributes");
	// it.AddRow(attributes);
	// }
	// return it;
	// }
	//
	// @ThingworxServiceDefinition(name="getRestUrl", category="PTC Connection",
	// description="Get the prefix of all RESTful web service URLs")
	// @ThingworxServiceResult(name="Result", description="The resulting objects",
	// baseType="STRING")
	// public String getRestUrl()
	// throws Exception
	// {
	// String baseURL = (String)getConfigurationSetting("PTC Connector
	// Configuration", "baseURL");
	// String restPath = (String)getConfigurationSetting("PTC Connector
	// Configuration", "restPath");
	// return baseURL + restPath;
	// }
	//
	// @ThingworxServiceDefinition(name="getBaseUrl", category="PTC Connection",
	// description="Get the base Windchill URL")
	// @ThingworxServiceResult(name="Result", description="The resulting objects",
	// baseType="STRING")
	// public String getBaseUrl()
	// throws Exception
	// {
	// return (String)getConfigurationSetting("PTC Connector Configuration",
	// "baseURL");
	// }
	//
	// private void deleteAssertion(String url)
	// throws Exception
	// {
	// SessionInfo sessionInfo =
	// (SessionInfo)EntityUtilities.findEntity("CurrentSessionInfo",
	// RelationshipTypes.ThingworxRelationshipTypes.Resource);
	//
	// InfoTable globalSession = sessionInfo.GetGlobalSessionValues();
	// InfoTable assertions =
	// (InfoTable)globalSession.getFirstRow().getValue("AssertionPairs");
	// if (assertions != null)
	// {
	// int toDelete = -1;
	// for (int i = 0; i < assertions.getLength().intValue(); i++)
	// {
	// String server = assertions.getRow(i).getStringValue("Server");
	// if (url.startsWith(server)) {
	// toDelete = i;
	// }
	// }
	// if (toDelete > -1) {
	// assertions.removeRow(toDelete);
	// }
	// }
	// sessionInfo.SetGlobalSessionInfoTableValue("AssertionPairs", assertions);
	// sessionInfo.initializeEntity();
	// }
	//
	// private String getAssertion(String url)
	// throws Exception
	// {
	// String sessionUser = ThreadLocalContext.getSecurityContext().getName();
	//
	// String effectiveUser = getParamValueFromUrl(url, "effectiveUser");
	// if (effectiveUser != null)
	// {
	// if (isImpersonatableUser(sessionUser)) {
	// return effectiveUser;
	// }
	// throw new Exception("Session user is not super/impersonatable user so that
	// effectiveUser can be specified.");
	// }
	// String assertion = null;
	//
	// SessionInfo sessionInfo = getCurrentSessionInfo();
	// InfoTable globalSession = sessionInfo.GetGlobalSessionValues();
	// InfoTable assertions =
	// (InfoTable)globalSession.getFirstRow().getValue("AssertionPairs");
	// if ((assertions != null) && (assertions.getRowCount().intValue() > 0))
	// {
	// for (ValueCollection row : assertions.getRows())
	// {
	// String server = row.getStringValue("Server");
	// if (url.startsWith(server))
	// {
	// assertion = row.getStringValue("Assertion");
	// break;
	// }
	// }
	// return assertion;
	// }
	// if (assertion == null) {
	// assertion = sessionUser;
	// }
	// return assertion;
	// }
	//
	// private boolean isImpersonatableUser(String sessionUser)
	// throws Exception
	// {
	// ConfigurationTable userConfigTable = getConfigurationTable("Impersonsated
	// Users Configuration");
	// if (userConfigTable != null)
	// {
	// ValueCollectionList configTableRows = userConfigTable.getRows();
	// for (ValueCollection row : configTableRows)
	// {
	// String userName = row.getStringValue("ValidImpersonatedUser");
	// if ((userName != null) && (userName.equals(sessionUser))) {
	// return true;
	// }
	// }
	// }
	// return false;
	// }
	//
	// public String getParamValueFromUrl(String url, String paramName)
	// throws Exception
	// {
	// List<NameValuePair> pairs = URLEncodedUtils.parse(new URI(url), "UTF-8");
	// for (NameValuePair p : pairs) {
	// if (p.getName().equals(paramName)) {
	// return p.getValue();
	// }
	// }
	// return null;
	// }
	//
	// private String getcurrentUserLanguage()
	// throws Exception
	// {
	// SessionInfo sessionInfo = getCurrentSessionInfo();
	// return sessionInfo.GetCurrentUserLanguage();
	// }
	//
	// private SessionInfo getCurrentSessionInfo()
	// {
	// SessionInfo sessionInfo =
	// (SessionInfo)EntityUtilities.findEntity("CurrentSessionInfo",
	// RelationshipTypes.ThingworxRelationshipTypes.Resource);
	//
	// return sessionInfo;
	// }
	//
	// public void migrateConfigurationSettings(Map.Entry<String, RootEntity>
	// ConnectorEntry)
	// throws Exception
	// {
	// Thing incomingConnector = (Thing)ConnectorEntry.getValue();
	//
	// Thing existingConnector =
	// (Thing)EntityUtilities.findEntity(incomingConnector.getName(),
	// RelationshipTypes.ThingworxRelationshipTypes.Thing);
	//
	// migrateConnectorConfiguration(incomingConnector, existingConnector);
	//
	// ConnectorEntry.setValue(existingConnector);
	// }
	//
	// private void migrateConnectorConfiguration(Thing incomingConnector, Thing
	// existingConnector)
	// throws Exception
	// {
	// existingConnector.SetConfigurationTable("PTC Connector Configuration",
	// incomingConnector
	// .getConfigurationTable("PTC Connector Configuration"),
	// Boolean.valueOf(false));
	//
	// existingConnector.SetConfigurationTable("SSLConnectionConfiguration",
	// incomingConnector.getConfigurationTable("SSLConnectionConfiguration"),
	// Boolean.valueOf(false));
	// }
}
