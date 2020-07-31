/* bcwti
 *
 * Copyright (c) 2010 Parametric Technology Corporation (PTC). All Rights Reserved.
 *
 * This software is the confidential and proprietary information of PTC
 * and is subject to the terms of a software license agreement. You shall
 * not disclose such confidential information and shall use it only in accordance
 * with the terms of the license agreement.
 *
 * ecwti
 */
package com.ptc.octo.creoview;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
//import java.util.logging.Level;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Structure2 // extends Structure
{
	private static final Logger logger = LoggerFactory.getLogger(Structure2.class);

	public static final int PV_MARKUP_ICON_B = 2;
	public static final int PV_BAD_COMP_ICON_B = 30;
	public static final int PV_DOC_ICON_B = 31;
	public static final int PV_PART_ICON_B = 32;
	public static final int PV_EPMDOC_ICON_B = 33;
	public static final int PV_PART_MASTER_ICON_B = 34;
	public static final int PV_EPMDOC_MASTER_ICON_B = 35;
	public static final int PV_PRODUCT_ICON_B = 36;
	public static final int PV_PRODUCT_MASTER_ICON_B = 37;
	public static final int PV_SNPART_ICON_B = 38;
	public static final int PV_SNPART_MASTER_ICON_B = 39;
	public static final int PV_PROCESS_PLAN_ICON_B = 40;
	public static final int PV_SEQUENCE_ICON_B = 41;
	public static final int PV_OPERATION_ICON_B = 42;
	public static final int PV_RESOURCE_ICON_B = 43;
	public static final int PV_PAR_ICON_B = 44;
	public static final int PV_CAR_ICON_B = 45;
	public static final int PV_PARTLIST_ICON_B = 46;

	public static final int NORMAL_TYPE_B = 0;
	public static final int DOCUMENT_REFERENCE_B = 1;

	public static final int MAX_VERSION_NUMBER = 99;
	public static final int MIN_VERSION_NUMBER = 0;
	private static final int MAJOR_VERSION = 3; // 03
	private static final int MINOR_VERSION = 0; // 00
	private static final int MAX_FLOAT_ORIENTATION_MAJOR_VERSION = 2; // 02
	private static final int MAX_FLOAT_ORIENTATION_MINOR_VERSION = 2; // 02

	private static final String DELIM = " ";
	private static final String UTF8 = "UTF-8";
	private static final int VERSION_NO_FIELD_LEN = 2; // length of major or minor number in file header
	private static final int MAJOR_VERSION_OFFSET = 6; // position of major version in file header
	private static final int MINOR_VERSION_OFFSET = MAJOR_VERSION_OFFSET + VERSION_NO_FIELD_LEN;
	// length of the full id string eg. PV-PVS0200
	private static final int ED2_FILE_ID_LEN = MINOR_VERSION_OFFSET + VERSION_NO_FIELD_LEN;
	private static final String ED2_FILE_ID_STR = "PV-PVS";
	private static final String EDP_FILE_ID_STR = "PV-PVP";
	private static final String EDM_FILE_ID_STR = "PV-PVM";

	private static final int SECTION_TYPE_SYMBOLS = 1;
	private static final int SECTION_TYPE_STRUCTURE = 2;
	private static final int SECTION_TYPE_PROPERTIES = 3;
	private static final int SECTION_TYPE_FILEMAP = 4;
	private static final int SECTION_TYPE_VIEWSTATE = 5;

	private static final int GEN_TAG_END_FLAG = 0x8000;
	private static final int GEN_MAX_TAG = 0x01ff;

	private static final int GEN_TAG_MASK = 0x01ff;
	private static final int GEN_TAG_SIZE_SIZE_SHIFT = 9;
	private static final int GEN_TAG_BITS_SIZE_SHIFT = 12;

	private static final int GEN_END_TAG = 0;

	private static final int ED_COMPONENT_TAG = 32;
	private static final int ED_COMPONENT_INSTANCE_TAG = 33;
	private static final int ED_COMPONENT_PROXY_TAG = 34;
	private static final int ED_SHAPE_TAG = 35;
	public static final int ED_DRAWING_TAG = 36;
	public static final int ED_DOCUMENT_TAG = 37;
	public static final int ED_IMAGE_TAG = 38;
	public static final int ED_FILE_TAG = 39;
	public static final int ED_OLEDOC_TAG = 40;
	private static final int ED_FILEMAP_ENTRY_TAG = 41;
	private static final int ED_PROPERTY_COMPONENT_REF_TAG = 42;
	private static final int ED_PROPERTY_TAG = 43;
	private static final int ED_STRUCTURE_INSTANCE_REF_TAG = 44;
	private static final int ED_PROPERTY_INSTANCE_REF_TAG = 45;
	private static final int ED_APPEARANCE_TABLE_TAG = 46;
	private static final int ED_APPEARANCE_TAG = 47;
	private static final int ED_APPEARANCE_OVERIDE_TAG = 48;
	private static final int ED_VIEW_TAG = 49;
	public static final int ED_ILLUSTRATION_TAG = 50;
	private static final int ED_THUMBNAIL_3D_TAG = 51;
	private static final int ED_VIEWSTATE_TAG = 52;
	private static final int ED_ALTERNATEREP_TAG = 53;
	private static final int ED_EXPLODESTATE_TAG = 54;
	private static final int ED_SECTIONCUT_TAG = 55;
	private static final int ED_PROCESS_TAG = 56;
	public static final int ED_ECAD_TAG = 57;
	private static final int ED_DRAWING_SHEET_TAG = 58;
	public static final int ED_ILLUSTRATION3D_TAG = 59;
	public static final int ED_INSTANCE_APPEARANCE_OVERIDE_TAG = 60;

	private static final int SHAPE_BBOX = 0x01;
	private static final int SHAPE_INDEX = 0x02;

	private static final int THUMBNAIL_INDEX = 0x01;

	private static final int VIEWABLE_DISPLAY_NAME = 0x01;
	private static final int VIEWABLE_TYPE = 0x02;
	private static final int VIEWABLE_ECAD_TYPE = 0x04; // bit 2 on ecad only
	private static final int VIEWABLE_DRAWING_SHEET_OF_PREVIOUS_TYPE = 0x04; // bit 2 on drawing only
	private static final int VIEWABLE_DRAWING_NEXT_DRAWING_IS_CONTINUATION = 0x08; // bit 3 on drawing only

	private static final int DRAWING_SHEET_SIZE = 0x01;
	private static final int DRAWING_SHEET_ORIENTATION = 0x02;
	private static final int DRAWING_SHEET_NAME = 0x04;

	private static final int VIEW_ORIENTATION = 0x01;

	private static final int VIEWSTATE_FILENAME = 0x01;
	private static final int VIEWSTATE_INDEX = 0x02;

	public static final int ECAD_TYPE_UNKNOWN = 0;
	public static final int ECAD_TYPE_SCHEMATIC = 1;
	public static final int ECAD_TYPE_PCB = 2;

	private static final int FILEMAP_OID = 0x01;
	private static final int FILEMAP_OID1_PREFIX = 0x02;
	private static final int FILEMAP_OID1_SUFFIX = 0x04;
	private static final int FILEMAP_OID2_PREFIX = 0x08;
	private static final int FILEMAP_OID2_SUFFIX = 0x10;
	private static final int FILEMAP_OID_COMPLETE = 0x20;

	private static final int FILEMAP_ENTRY_OID2 = 0x01;
	private static final int FILEMAP_ENTRY_OID1_COMPLETE = 0x02;
	private static final int FILEMAP_ENTRY_OID2_COMPLETE = 0x04;

	private static final int COMPONENT_TYPE = 0x01;
	private static final int COMPONENT_MODEL_UNIT_LENGTH = 0x02;
	private static final int COMPONENT_DISPLAY_UNIT_LENGTH = 0x04;
	private static final int COMPONENT_MODEL_UNIT_MASS = 0x08;
	private static final int COMPONENT_DISPLAY_UNIT_MASS = 0x10;
	private static final int COMPONENT_DEFAULT_APPEARANCE = 0x20;

	private static final int COMPONENT_PROXY_MAP_FILENAME = 0x02;
	private static final int COMPONENT_PROXY_WVS_INFO = 0x04;

	private static final int COMPONENT_INSTANCE_RELATIONSHIP = 0x01;
	private static final int COMPONENT_INSTANCE_TRANSLATION = 0x02;
	private static final int COMPONENT_INSTANCE_ORIENTATION = 0x04;
	private static final int COMPONENT_INSTANCE_ID = 0x08;
	private static final int COMPONENT_INSTANCE_NAME = 0x10;
	private static final int COMPONENT_INSTANCE_HIDE_SELF = 0x20;
	private static final int COMPONENT_INSTANCE_HIDE_DESCENDANTS = 0x40;
	private static final int COMPONENT_INSTANCE_COMBINE = 0x80;

	private static final int STRUCTURE_INST_REF_OFFSET = 0x01;

	private static final int APPEARANCE_BASE_COLOR = 0x01;
	private static final int APPEARANCE_AMBIENT_COLOR = 0x02;
	private static final int APPEARANCE_DIFFUSE_COLOR = 0x04;
	private static final int APPEARANCE_EMISSIVE_COLOR = 0x08;
	private static final int APPEARANCE_SPECULAR = 0x10;
	private static final int APPEARANCE_OPACITY = 0x20;

	private static final int APPEARANCE_OVERIDE_SELF = 0x01;
	private static final int APPEARANCE_OVERIDE_DESCENDANTS = 0x02;

	private static final int INSTANCE_APPEARANCE_OVERIDE_RECURSE = 0x01;
	private static final int INSTANCE_APPEARANCE_OVERIDE_PATH = 0x02;
	private static final int INSTANCE_APPEARANCE_OVERIDE_FACEIDS = 0x04;

	private static final int PROPERTY_COMP_REF_OFFSET = 0x01;
	private static final int PROPERTY_INST_REF_OFFSET = 0x01;

	private static final int PROPERTY_TYPE_STRING = 0x00;
	private static final int PROPERTY_TYPE_FLOAT = 0x01;
	private static final int PROPERTY_TYPE_DOUBLE = 0x02;
	private static final int PROPERTY_TYPE_BYTE = 0x03;
	private static final int PROPERTY_TYPE_SHORT = 0x04;
	private static final int PROPERTY_TYPE_INT = 0x05;
	private static final int PROPERTY_TYPE_BOOLEAN = 0x06;
	private static final int PROPERTY_TYPE_DATE = 0x07;
	private static final int PROPERTY_TYPE_VOID = 0x08;

	public static final int PAPER_SIZE_UNKNOWN = 0;
	public static final int PAPER_SIZE_CUSTOM = 1;
	public static final int PAPER_SIZE_LETTER = 2;
	public static final int PAPER_SIZE_LEGAL = 3;
	public static final int PAPER_SIZE_TABLOID = 4;
	public static final int PAPER_SIZE_ANSI_C = 5;
	public static final int PAPER_SIZE_ANSI_D = 6;
	public static final int PAPER_SIZE_ANSI_E = 7;
	public static final int PAPER_SIZE_ANSI_F = 8;
	public static final int PAPER_SIZE_A0 = 9;
	public static final int PAPER_SIZE_A1 = 10;
	public static final int PAPER_SIZE_A2 = 11;
	public static final int PAPER_SIZE_A3 = 12;
	public static final int PAPER_SIZE_A4 = 13;
	public static final int PAPER_SIZE_A5 = 14;
	public static final int PAPER_SIZE_A6 = 15;
	public static final int PAPER_SIZE_A7 = 16;
	public static final int PAPER_SIZE_A8 = 17;
	public static final int PAPER_SIZE_A9 = 18;
	public static final int PAPER_SIZE_A10 = 19;

	public static final int PAPER_ORIENTATION_UNKNOWN = 0;
	public static final int PAPER_ORIENTATION_LANDSCAPE = 1;
	public static final int PAPER_ORIENTATION_PORTRAIT = 2;

	// constants from Structure class
	public static final String WRITE_SKIP_PREFIX = "wrtskp_";
	public static final String DISPLAY_ICON_ID = WRITE_SKIP_PREFIX + "display_icon_id";
	public static final String DISPLAY_INSTANCE_NAME = WRITE_SKIP_PREFIX + "display_instance_name";
	public static final String COMPONENT_BRANCH_LINK = WRITE_SKIP_PREFIX + "component_branch_link";
	public static final String PROPERTY_GROUP_LOOKUP = WRITE_SKIP_PREFIX + "property_group_lookup";
	public static final String PROPERTY_COUNT = "x_property_count_x";
	public static final String IGNORE_ON_MERGE = "ignoreonmerge";
	public static final String REFERENCE_STRUCTURE = "reference_structure";
	public static final String DOCUMENT_REFERENCE = "document_reference";
	public static final String REFERENCE_CONTENT_HOLDER = "reference_content_holder";
	public static final String OUTOFDATE_REPRESENTATION = "outofdate_representation";
	public static final String POSITIONING_ASSEMBLY = "positioning_assembly";
	public static final String POSITIONING_ASSEMBLY_BRANCH = "positioning_assembly_branch";
	public static final String NO_BRANCH_REP = "positioning_assembly_no_rep";
	public static final String CONVERTED_FROM_ED = "converted_from_ed";
	public static final String ED_LOCATION = "ed_location";
	public static final String ADD_IBA_DATA = WRITE_SKIP_PREFIX + "add_iba_data";
	public static final String EDFILE_COMPONENT_ID = WRITE_SKIP_PREFIX + "edfile_component_id";
	public static final String EDFILE_CHARSET = WRITE_SKIP_PREFIX + "edfile_charset";
	public static final String TRANSLATION = "translation";
	public static final String ORIENTATION = "orientation";
	public static final String FILENAME_IS_ENCODED = "filename_is_encoded";
	public static final String X_COMP_PARENT_ID = "x_comp_parent_id";
	public static final String X_COMP_COPY_ID = "x_comp_copy_id";

	public static final String DOC_PREFIX = "doc_";
	public static final String PART_PREFIX = "part_";
	public static final String PAR_PREFIX = "par_";
	public static final String CAR_PREFIX = "car_";
	public static final String PARTLINK_PREFIX = "partlink_";
	public static final String EPMDOC_PREFIX = "epmdoc_";
	public static final String EPMDOCLINK_PREFIX = "epmdoclink_";
	public static final String MPMLINK_PREFIX = "mpmlink_";
	public static final String PARTLIST_PREFIX = "partslist_";
	public static final String PARTLISTITEM_PREFIX = "partslistlink_";

	public static final String STANDARDATTRIBUTE_PREFIX = "sa_";
	public static final String SECURITYLABELPROPERTIES_PREFIX = "sl";

	public static final String OBJECT_ID = "objectid";
	public static final String PVCID = "pvcid";

	public static final String DOC_OBJECT_ID = DOC_PREFIX + OBJECT_ID;
	public static final String PART_OBJECT_ID = PART_PREFIX + OBJECT_ID;
	public static final String PAR_OBJECT_ID = PAR_PREFIX + OBJECT_ID;
	public static final String CAR_OBJECT_ID = CAR_PREFIX + OBJECT_ID;
	public static final String PARTLINK_OBJECT_ID = PARTLINK_PREFIX + OBJECT_ID;
	public static final String EPMDOC_OBJECT_ID = EPMDOC_PREFIX + OBJECT_ID;
	public static final String EPMDOCLINK_OBJECT_ID = EPMDOCLINK_PREFIX + OBJECT_ID;
	public static final String MPMLINK_OBJECT_ID = MPMLINK_PREFIX + OBJECT_ID;
	public static final String PARTLIST_OBJECT_ID = PARTLIST_PREFIX + OBJECT_ID;

	public static final String ITERATION = "qualifiedidentifier";

	public static final String DOC_ITERATION = DOC_PREFIX + ITERATION;
	public static final String PART_ITERATION = PART_PREFIX + ITERATION;
	public static final String EPMDOC_ITERATION = EPMDOC_PREFIX + ITERATION;
	public static final String MPMLINK_ITERATION = MPMLINK_PREFIX + ITERATION;
	public static final String PARTLIST_ITERATION = PARTLIST_PREFIX + ITERATION;

	public static final String NAME = "name";
	public static final String NUMBER = "number";
	public static final String CONTAINER_NAME = "containerName";
	public static final String ORGANIZATION_NAME = "organizationName";
	public static final String VIEW_NAME = "viewName";
	public static final String CAD_NAME = "CADName";
	public static final String FORMAT_NAME = "formatName";
	public static final String DESCRIPTION = "description";
	public static final String APPLICE3C = "ec3diapplic";
	public static final String APPLICWNC = "wncapplic";
	public static final String TITLE = "title";

	public static final String PV_MARKUP_ICON = "2";
	public static final String PV_BAD_COMP_ICON = "30";
	public static final String PV_DOC_ICON = "31";
	public static final String PV_PART_ICON = "32";
	public static final String PV_EPMDOC_ICON = "33";
	public static final String PV_PART_MASTER_ICON = "34";
	public static final String PV_EPMDOC_MASTER_ICON = "35";
	public static final String PV_PRODUCT_ICON = "36";
	public static final String PV_PRODUCT_MASTER_ICON = "37";
	public static final String PV_SNPART_ICON = "38";
	public static final String PV_SNPART_MASTER_ICON = "39";

	public static final String LENGTH_UNIT = "length_unit";
	public static final String UNIT_M = "M";
	public static final String LOCATION = "location";
	public static final String DEFAULT_LOCATION = "0.0 0.0 0.0 0.0 0.0 0.0";
	public static final String HTMLDOC = "htmldoc";
	public static final String EPM_HTMLDOC = "EPMDocument";
	public static final String PART_HTMLDOC = "Part";
	public static final String PARTMASTER_HTMLDOC = "PartMaster";
	public static final String DOC_HTMLDOC = "Document";
	public static final String OBJECT_HTMLDOC = "Object";
	public static final String PRODUCTINSTANCE_HTMLDOC = "ProductInstance";
	public static final String PRODUCTCONFIGURATION_HTMLDOC = "ProductConfiguration";
	public static final String TRUE = "true";
	public static final String FALSE = "false";
	public static final String IDSHAPESOURCE = "idshapesource";
	public static final String BBOX = "bbox";
	public static final String SHAPESOURCE = "shapesource";
	public static final String DRAWING = "drawing";
	public static final String DOCUMENT = "document";
	public static final String IMAGE = "image";
	public static final String FILE = "file";
	public static final String ECAD = "ecad";
	public static final String SIMULATION = "simulation";
	public static final String OLEDOC = "oledoc";
	public static final String ILLUSTRATION = "illustration";
	public static final String ILLUSTRATION3D = "illustration3d";
	public static final String TEXTURE_LOOKUP = "texture_lookup";
	public static final String XREF_LOOKUP = "xref_lookup";
	public static final String SHAPETYPE = "shapetype";
	public static final String COLOR = "color";

	public static final String PV_SOURCE_FILENAME = "Source_file_name";
	public static final String PV_SOURCE_PARTNAME = "Source_part_name";
	public static final String PV_SOURCE_FORMNAME = "Source_form_name";
	public static final String PV_SOURCE_FILEPATH = "pvsourcefilepath";
	public static final String PV_SOURCE_WORKSPACE = "pvsourceworkspace";
	public static final String PV_SOURCE_NOTSTANDARD = "Source_not_standard";
	public static final String PV_SOURCE_FORMAT = "Source_format";

	// private static final String ED_ZERO = "0";
	// private static final String ED_ZERO_ZERO = " 0 0 ";
	// private static final String ED_ONE = "1";
	private static final String ED_SPACE = " ";
	// private static final String ED_NULL = "";
	// private static final String ED_T = "T ";
	// private static final String ED_C = "C ";
	// private static final String ED_I = "I ";
	// private static final String ED_L = "L ";
	// private static final String ED_P = "P ";
	// private static final String ED_N = "N ";
	// private static final String ED_V = "V ";
	// private static final String ED_CHARSET = "J ";
	// private static final char ED_CHARSET_CHAR = 'J';
	// Structure const end
	public static final String END_URLHIDE = "&dummyarg=!>";
	public static final int LEN_END_URLHIDE = END_URLHIDE.length();

	private static Matrix3d identityMatrix = new Matrix3d();
	private static final int BYTE_ARRAY_SIZE_32 = 32;
	private static final int BYTE_ARRAY_SIZE_64 = 64;
	private static final int BYTE_ARRAY_SIZE_128 = 128;
	private static int BUFFER_SIZE_4096 = 8192;
	private static double TOL = 0.000001;

	private static final String PART_PROP_GROUP;
	private static final String EPM_PROP_GROUP;
	private static final String DOC_PROP_GROUP;
	private static final String MPM_PROP_GROUP;

	public static final String ED = "pvs";
	public static final String EDP = "pvp";
	public static final String EDM = "pvm";
	public static final String EDZ = "pvz";

	public static final String WCEDP = "wvs" + EDP + "." + EDP;
	public static final String EDP_IN_ED = "EDP_IN_ED";

	public static final String PART_INFO = ":Part:";
	public static final String FORM_INFO = ":Form:";
	public static final String COMP_NO_NAME = "noName";

	// used only in ed files to allow creation of pvs with schematic or pcb
	// ecad_type set
	public static final String ECAD_SCHEMATIC = "ecad_schematic";
	public static final String ECAD_PCB = "ecad_pcb";

	public static final String SOURCE_NOT_STANDARD = "Source_not_standard";
	public static final String STRUCTURE_HAS_ASSEMBLY_FEATURES = "Structure_has_assembly_features";

	private static final boolean matchCompsOnConversion;

	// flag to indicate writing single precision orientation to PVS files.
	private static final boolean WRITES_FLOAT_ORIENTATION;	

	static {
		identityMatrix.setIdentity();
		String v = "false";// WVSProperties.getPropertyValue("wvs.edstructure.verbose");
		if (v != null && v.equalsIgnoreCase("true")) {
			// logger.setLevel(Level.DEBUG);
		}

		// v = WVSProperties.getPropertyValue("wvs.edstructure.bytearraysize32");
		// if( v != null ) BYTE_ARRAY_SIZE_32 = Integer.parseInt(v);
		// v = WVSProperties.getPropertyValue("wvs.edstructure.bytearraysize64");
		// if( v != null ) BYTE_ARRAY_SIZE_64 = Integer.parseInt(v);
		// v = WVSProperties.getPropertyValue("wvs.edstructure.bytearraysize128");
		// if( v != null ) BYTE_ARRAY_SIZE_128 = Integer.parseInt(v);
		// v = WVSProperties.getPropertyValue("wvs.edstructure.buffersize4096");
		// if( v != null ) BUFFER_SIZE_4096 = Integer.parseInt(v);
		// v = WVSProperties.getPropertyValue("wvs.edstructure.tol");
		// if( v != null ) TOL = Double.parseDouble(v);

		// v = WVSProperties.getPropertyValue("wvs.edstructure.matchcompsonconversion");
		matchCompsOnConversion = false; // v != null && v.equalsIgnoreCase("true");

		// This is a temp version override in X-20_M-10 when Windchill is ready to
		// write V03.00 PVS files but the viewer can only read the old V02.02 files.
		// v =
		// WVSProperties.getPropertyValue("wvs.edstructure.writesingleprecisionorientation");
		// if (v != null && Boolean.valueOf(v = v.trim()).booleanValue()) {
		// WRITES_FLOAT_ORIENTATION = true;
		// logger.debug("Single Precision Orientation will be written to PVS files");
		// }
		// else {
		WRITES_FLOAT_ORIENTATION = false;
		// }

		PART_PROP_GROUP = "WindchillPart";// WVSProperties.getPartPropertyGroup(); //edrload.partpropertygroup
		EPM_PROP_GROUP = "WindchillPart";// WVSProperties.getEPMPropertyGroup();//edrload.epmpropertygroup
		DOC_PROP_GROUP = "WindchillPart";// WVSProperties.getDocPropertyGroup();//edrload.docpropertygroup
		MPM_PROP_GROUP = "WindchillMPMLink";// WVSProperties.getMPMLinkPropertyGroup();//edrload.mpmlinkpropertygroup
	}

	private int tagTag;
	private int tagSize;
	private int tagBits;
	private boolean tagHasEndFlag;
	private int tagReadCount;
	private boolean writeEndTag = false;

	private InputStream bi = null;
	private InputStream zi = null;
	private InputStream input = null;

	private OutputStream bo = null;
	private OutputStream zo = null;
	private OutputStream output = null;

	private int majorVersion;
	private int minorVersion;
	private final Map<String, String> edpMap = new HashMap<String, String>();
	private Map<String, InputStream> edpInputStreamMap = null;
	private Map edpOutputStreamMap = null;
	private Map writeFileNameEDPMap = new HashMap();
	private Map readFileNameEDPMap = new HashMap();
	private File edpWriteDir = null;
	private boolean closeInputStreams = false;
	private boolean closeOutputStreams = false;
	private Comp rootComp = null;
	private ArrayList<Comp> compList = null;
	private ArrayList<Appearance> appearanceList = null; // used when reading and writing for order
	private Map<Object, Object> appearanceMap = null; // used when writing for quick lookup
	private Map<String, Set> groupCompAndInstMap = null; // used when writing for performance
	private String[] symbols = null; // used when reading
	private ArrayList<String> symbolList = null; // used when writing for order
	private Map<String, Integer> symbolMap = null; // used when writing for quick lookup
	private boolean usingStringsAsSymbols = false; // state for writing strings
	private boolean writeStructureAsSymbols = false; // write component strings as symbols
	private boolean writePropertiesAsSymbols = true; // write property strings as symbols
	private boolean writeFileMapAsSymbols = false; // write file map strings as symbols
	private boolean writeIncludeFileMap = false; // if there is a file map defined write it into ed file
	private boolean writeIncludeProperties = true;
	private boolean writeExternalEDPFiles = true;
	private CompInstComparator compInstComparator = null;

	private boolean readExternalEDPFiles = true;
	private boolean readIncludeProperties = true;

	private boolean convertBranchLink = false;
	private boolean convertURL = false;

	private int compressionType = 1; // 0=no compression, 1= gzip, 2=bzip (to be implemented)

	private Map<String, String> fileMap = null;
	private String fileMapOid = null;
	private String fileMapOid1Prefix = null;
	private String fileMapOid1Suffix = null;
	private String fileMapOid2Prefix = null;
	private String fileMapOid2Suffix = null;
	private boolean fileMapFullyPopulateOnRead = false;

	// mvh:start
	public static void main(String[] args) {
		try {
			JSONObject json = getJSONFromPVFile(args[0], null);
			System.out.print(json.toString(3));

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static JSONObject getJSONFromPVFile(String filename, String[] properties)
			throws IOException, FileNotFoundException, Exception, JSONException {
		InputStream stream = null;
		ZipFile zipFile = null;
		if (filename.endsWith(".pvz")) {
			zipFile = new ZipFile(filename);
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				if (entry.getName().endsWith(".pvs"))
					stream = new BufferedInputStream(zipFile.getInputStream(entry), BUFFER_SIZE_4096);
			}
		} else { // assuming a .pvs file
			stream = new BufferedInputStream(new FileInputStream(filename), BUFFER_SIZE_4096);
		}
		JSONObject json = getJSONFromPVS(stream);
		// reduce the json output to the specified list of properties now
		if(properties !=null) reduceJSON2Props(json, properties);
		if (stream != null)
			stream.close();
		if (zipFile != null)
			zipFile.close();
		return json;
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
					for (int i = 0; i < ((JSONArray) val).length(); i++) {
						JSONObject child = (JSONObject) ((JSONArray) val).get(i);
						reduceJSON2Props(child, properties);
					}
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

	public static JSONObject getJSONFromPVS(InputStream pvsInputStream) throws Exception, JSONException {
		Structure2.Comp comp = Structure2.readEDStructureToComp(pvsInputStream);
		DefaultMutableTreeNode node = structure2ToIeTree(comp, null, null);
		calculateTransformation(node);
		JSONObject json = getJSONFromDefMutTree(node);
		return json;
	}

	private static JSONObject getJSONFromDefMutTree(DefaultMutableTreeNode node) throws JSONException {
		JSONObject json = (JSONObject) node.getUserObject();
		JSONArray childs = new JSONArray();
		for (Object child : Collections.list(node.children())) {
			JSONObject cn = getJSONFromDefMutTree((DefaultMutableTreeNode) child);
			//childs.push(cn);
			childs.put(cn);
		}
		json.put("components", childs);
		return json;
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
	 */
	private static DefaultMutableTreeNode structure2ToIeTree(Structure2.Comp comp, Structure2.CompInst compInst,
			DefaultMutableTreeNode pnode) throws JSONException {
		//TODO: having these as static params is not nice - they should be specific to the method invocation
		boolean isNestedLinkProperties=true;
		boolean isNestedPartProperties=true;

		JSONObject el = new JSONObject();		
		// Element el = new Element();
		Hashtable compAttrs = comp.properties;
		if (compAttrs != null) {
			JSONObject propEl = el;
			if(isNestedPartProperties) {
				propEl = new JSONObject();
				el.put("properties", propEl);
			}
			Enumeration<?> attKeys = compAttrs.keys();
			while (attKeys.hasMoreElements()) {
				String attrName = (String) attKeys.nextElement();
				String attValue = Structure2.getPropertyStringValue(comp, attrName);
				if (attValue != null)
					propEl.put(attrName, attValue);
				// log.debug("Comp attrName:"+attrName+" attValue:"+attValue);
			}
		}

		String val = comp.filename;
		if (val != null)
			el.put("pvs_filename", comp.filename);

		el.put("pvs_shape", comp.shape);
		el.put("pvs_name", comp.name);
		el.put("pvs_type", comp.type);
		
		el.put("pvs_bbox", comp.bbox);
		el.put("pvs_writeIdx", comp.writeIdx);
		el.put("pvs_wvs_info", comp.wvs_info);
		el.put("pvs_source_part_name", comp.getSourcePartName());
		el.put("pvs_source_file_name", comp.getSourceFileName());
		el.put("pvs_source_form_name", comp.getSourceFormName());

		if (compInst != null) {
			Hashtable compInstAttrs = compInst.properties;
			if (compInstAttrs != null) {
				JSONObject propEl = el;
				if(isNestedPartProperties) {
					propEl = new JSONObject();
					String propName = isNestedLinkProperties ? "link_properties" : "properties";
					el.put( propName, propEl);
				}
				Enumeration<?> instAttrKeys = compInstAttrs.keys();
				while (instAttrKeys.hasMoreElements()) {
					String attrName = (String) instAttrKeys.nextElement();
					String attValue = Structure2.getPropertyStringValue(compInst, attrName);
					if (attValue != null)
						el.put(attrName, attValue);
					// log.debug("CompInst attrName:"+attrName+" attValue:"+attValue);
				}
			}
			Matrix4d mat = Structure2.getMatrix4dFromTranslationAndOrientation(compInst.translation,
					compInst.orientation);
			el.put("pvs_mat4d", mat);
			String location = getLocationfromMatrix(mat);
			el.put("pvs_location", location);
			el.put("pvs_inst_id", compInst.id);
			el.put("pvs_inst_name", compInst.name);
			el.put("pvs_inst_type", compInst.type);			
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

	public static DefaultMutableTreeNode calculateTransformation(DefaultMutableTreeNode parentNode) throws Exception {

		Matrix4d pAbsMx4 = getAbsMatrix4dOfElement((JSONObject) parentNode.getUserObject());

		Enumeration childs = parentNode.children();
		while (childs.hasMoreElements()) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) childs.nextElement();
			JSONObject el = (JSONObject) node.getUserObject();
			Matrix4d cRelMx4 = getRelMatrix4dOfElement(el);
			Matrix4d cAbsMx4 = new Matrix4d();
			cAbsMx4.mul(pAbsMx4, cRelMx4);

			//addAbsMatrix4dToElement(el, cAbsMx4);
			String absLocation = getLocationfromMatrix(cAbsMx4);
			el.put("pvs_abs_location", absLocation);

			calculateTransformation(node);

		}
		return parentNode;
	}

	private static Matrix4d getAbsMatrix4dOfElement(JSONObject el) throws NumberFormatException, JSONException {
		Matrix4d absMat4d = (Matrix4d) el.opt("pvs_abs_mat4d");
		if (absMat4d == null) {
			absMat4d = new Matrix4d(1d, 0d, 0d, 0d, 0d, 1d, 0d, 0d, 0d, 0d, 1d, 0d, 0d, 0d, 0d, 1d);
			el.put("pvs_abs_mat4d", absMat4d);
		}
		return absMat4d;
		// return getMatrix4dOfElement(el,new
		// String[]{"abs_x_x","abs_x_y","abs_x_z","abs_y_x","abs_y_y","abs_y_z","abs_z_x","abs_z_y","abs_z_z","abs_p_x","abs_p_y","abs_p_z","abs_scale"});
	}

	// private static Matrix4d getMatrix4dOfElement(JSONObject el, String[]
	// matAttNames) {
	// double p_x = el.optDouble(matAttNames[9], 0.0d);
	// double p_y = el.optDouble(matAttNames[10], 0.0d);
	// double p_z = el.optDouble(matAttNames[11], 0.0d);
	// double z_x = el.optDouble(matAttNames[6], 0.0d);
	// double z_y = el.optDouble(matAttNames[7], 0.0d);
	// double z_z = el.optDouble(matAttNames[8], 1.0d);
	// double y_x = el.optDouble(matAttNames[3], 0.0d);
	// double y_y = el.optDouble(matAttNames[4], 1.0d);
	// double y_z = el.optDouble(matAttNames[5], 0.0d);
	// double x_x = el.optDouble(matAttNames[0], 1.0d);
	// double x_y = el.optDouble(matAttNames[1], 0.0d);
	// double x_z = el.optDouble(matAttNames[2], 0.0d);
	// double scale = el.optDouble(matAttNames[12], 1.0d);
	//
	// Matrix4d mx4 = new Matrix4d(x_x, y_x, z_x, p_x, x_y, y_y, z_y, p_y, x_z, y_z,
	// z_z, p_z, 0.0d, 0.0d, 0.0d, scale);
	// return mx4;
	// }

	@SuppressWarnings("unused")
	private static void addMatrix4dToElement(JSONObject el, Matrix4d mx4, String[] matAttNames, String matName)
			throws JSONException {
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
		Matrix4d relMat4d = (Matrix4d) el.opt("pvs_mat4d");
		if (relMat4d == null) {
			relMat4d = new Matrix4d(1d, 0d, 0d, 0d, 0d, 1d, 0d, 0d, 0d, 0d, 1d, 0d, 0d, 0d, 0d, 1d);
			el.put("pvs_mat4d", relMat4d);
		}
		return relMat4d;
		// return getMatrix4dOfElement(el,new
		// String[]{"x_x","x_y","x_z","y_x","y_y","y_z","z_x","z_y","z_z","p_x","p_y","p_z","scale"});

	}

//	private static void addAbsMatrix4dToElement(JSONObject el, Matrix4d mx4_abs) throws JSONException {
//		addMatrix4dToElement(el, mx4_abs, new String[] { "abs_x_x", "abs_x_y", "abs_x_z", "abs_y_x", "abs_y_y",
//				"abs_y_z", "abs_z_x", "abs_z_y", "abs_z_z", "abs_p_x", "abs_p_y", "abs_p_z", "abs_scale" },
//				"pvs_abs_mat4d");
//	}

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

	//////////////////// static read methods ///////////////////////////

	/**
	 * Creates a new {@link NumberFormat} instance for reading and writing PV
	 * versions.
	 */
	private static NumberFormat getVersionNoFormat() {
		NumberFormat nf = NumberFormat.getIntegerInstance(Locale.US);
		nf.setMinimumIntegerDigits(VERSION_NO_FIELD_LEN);
		nf.setMaximumIntegerDigits(VERSION_NO_FIELD_LEN);
		return nf;
	}

	/**
	 * Determines whether to write float or double precision orientations when
	 * writing PVS files.
	 *
	 * DON'T CALL THIS METHOD outside of this class. Exposed for automated test
	 * only.
	 *
	 * @return <tt>true</tt> to write float orientations. Returns <tt>false</tt> to
	 *         write double precision orientations.
	 */
	protected static boolean writesFloatOrientation() {
		return WRITES_FLOAT_ORIENTATION;
	}

	/**
	 * Determines if the specified PV file version supports double precision
	 * orientation.
	 *
	 * @param majorVersion
	 *            - the major version
	 * @param minorVersion
	 *            - the minor version
	 * @return <tt>true</tt> if the specified PV file version supports double
	 *         precision orientation. Returns <tt>false</tt> if single precision
	 *         orientation is supported.
	 */
	public static boolean isDoublePrecisionOrientation(int majorVersion, int minorVersion) {
		boolean doublePrecision = majorVersion > MAX_FLOAT_ORIENTATION_MAJOR_VERSION
				|| majorVersion == MAX_FLOAT_ORIENTATION_MAJOR_VERSION
						&& minorVersion > MAX_FLOAT_ORIENTATION_MINOR_VERSION;
		return doublePrecision;
	}

	public static DefaultMutableTreeNode readEDStructure(File edFile) throws Exception {
		Structure2 sed2 = new Structure2();
		sed2.readED(edFile);
		return sed2.toTreeStructure();
	}

	public static DefaultMutableTreeNode readEDStructure(InputStream is) throws Exception {
		Structure2 sed2 = new Structure2();
		sed2.readED(is);
		return sed2.toTreeStructure();
	}

	public static Comp readEDStructureToComp(File edFile) throws Exception {
		Structure2 sed2 = new Structure2();
		sed2.readED(edFile);
		return sed2.getRootComp();
	}

	public static Comp readEDStructureToComp(InputStream is) throws Exception {
		Structure2 sed2 = new Structure2();
		sed2.readED(is);
		return sed2.getRootComp();
	}

	public static Comp readEDStructureToComp(InputStream is, Map<String, InputStream> edpInputStreamMap,
			boolean closeInputStreams) throws Exception {
		Structure2 sed2 = new Structure2();
		sed2.setEDPInputStreamMap(edpInputStreamMap);
		sed2.setCloseInputStreams(closeInputStreams);
		sed2.readED(is);
		return sed2.getRootComp();
	}

	public static Comp readEDStructureToComp(URL u, Map<String, InputStream> edpInputStreamMap,
			boolean closeInputStreams) throws Exception {
		Structure2 sed2 = new Structure2();
		sed2.setEDPInputStreamMap(edpInputStreamMap);
		sed2.setCloseInputStreams(closeInputStreams);
		sed2.readED(u);
		return sed2.getRootComp();
	}

	public static void readEDPFile(File edpFile, ArrayList<Structure2.Comp> cl, ArrayList onlySections)
			throws Exception {
		Structure2 sed2 = new Structure2();
		sed2.setCompList(cl, true);
		sed2.readEDP(edpFile, onlySections);
	}

	public static void readEDPFile(InputStream is, ArrayList<Structure2.Comp> cl, ArrayList onlySections)
			throws Exception {
		Structure2 sed2 = new Structure2();
		sed2.setCompList(cl, true);
		sed2.readEDP(is, onlySections);
	}

	public static Map<String, String> readEDMFile(File edmFile) throws Exception {
		Structure2 sed2 = new Structure2();
		sed2.setFileMapFullyPopulateOnRead(true);
		sed2.readEDM(edmFile);
		return sed2.getFileMap();
	}

	public static Map<String, String> readEDMFile(InputStream is) throws Exception {
		Structure2 sed2 = new Structure2();
		sed2.setFileMapFullyPopulateOnRead(true);
		sed2.readEDM(is);
		return sed2.getFileMap();
	}

	//////////////////// static write methods ///////////////////////////

	public static void writeEDStructure(DefaultMutableTreeNode r, File edFile) throws Exception {
		// writeEDStructure(r, edFile, new File(edFile.getParent(),
		// Util.setExtension(edFile.getName(), EDP)));
		writeEDStructure(r, edFile, (File) null); // any props go in ed file
	}

	public static void writeEDStructure(DefaultMutableTreeNode r, File edFile, File edpFile) throws Exception {
		Structure2 sed2 = new Structure2();
		sed2.fromTreeStructure(r);
		sed2.writeED(edFile, edpFile);
	}

	public static void writeEDStructure(DefaultMutableTreeNode r, OutputStream os) throws Exception {
		Structure2 sed2 = new Structure2();
		sed2.fromTreeStructure(r);
		sed2.writeED(os);
	}

	public static void writeEDStructure(Comp r, File edFile) throws Exception {
		// writeEDStructure(r, edFile, new File(edFile.getParent(),
		// Util.setExtension(edFile.getName(), EDP)));
		writeEDStructure(r, edFile, (File) null); // any props go in ed file
	}

	public static void writeEDStructure(Comp r, File edFile, File edpFile) throws Exception {
		Structure2 sed2 = new Structure2();
		sed2.setRootComp(r);
		sed2.writeED(edFile, edpFile);
	}

	public static void writeEDStructure(Comp r, OutputStream os) throws Exception {
		Structure2 sed2 = new Structure2();
		sed2.setRootComp(r);
		sed2.writeED(os);
	}

	public static void writeEDStructure(Comp r, OutputStream os, Map edpOutputStreamMap, boolean closeOutputStreams)
			throws Exception {
		Structure2 sed2 = new Structure2();
		sed2.setRootComp(r);
		sed2.setEDPOutputStreamMap(edpOutputStreamMap);
		sed2.setCloseOutputStreams(closeOutputStreams);
		sed2.writeEDUsingEDPMap(os);
	}

	public static void writeEDPFile(File edpFile, ArrayList<Structure2.Comp> cl, ArrayList onlySections)
			throws Exception {
		Structure2 sed2 = new Structure2();
		sed2.setCompList(cl, true);
		sed2.writeEDP(edpFile, onlySections);
	}

	public static void writeEDPFile(OutputStream os, ArrayList<Structure2.Comp> cl, ArrayList onlySections)
			throws Exception {
		Structure2 sed2 = new Structure2();
		sed2.setCompList(cl, true);
		sed2.writeEDP(os, onlySections);
	}

	public static void writeEDMFile(File edmFile, Map<String, String> fm, String fmo, String fmo1p) throws Exception {
		writeEDMFile(edmFile, fm, fmo, fmo1p, null, null, null);
	}

	public static void writeEDMFile(File edmFile, Map<String, String> fm, String fmo, String fmo1p, String fmo1s,
			String fmo2p, String fmo2s) throws Exception {
		Structure2 sed2 = new Structure2();
		sed2.setFileMap(fm);
		sed2.setFileMapOid(fmo);
		sed2.setFileMapOid1Prefix(fmo1p);
		sed2.setFileMapOid1Suffix(fmo1s);
		sed2.setFileMapOid2Prefix(fmo2p);
		sed2.setFileMapOid2Suffix(fmo2s);
		sed2.writeEDM(edmFile);
	}

	public static void writeEDMFile(OutputStream os, Map<String, String> fm, String fmo, String fmo1p)
			throws Exception {
		writeEDMFile(os, fm, fmo, fmo1p, null, null, null);
	}

	public static void writeEDMFile(OutputStream os, Map<String, String> fm, String fmo, String fmo1p, String fmo1s,
			String fmo2p, String fmo2s) throws Exception {
		Structure2 sed2 = new Structure2();
		sed2.setFileMap(fm);
		sed2.setFileMapOid(fmo);
		sed2.setFileMapOid1Prefix(fmo1p);
		sed2.setFileMapOid1Suffix(fmo1s);
		sed2.setFileMapOid2Prefix(fmo2p);
		sed2.setFileMapOid2Suffix(fmo2s);
		sed2.writeEDM(os);
	}

	///////////////////////////////// test if ED2 file
	///////////////////////////////// ////////////////////////////////

	public static boolean isED2File(File edfile) {
		boolean ret = false;
		try {
			InputStream is = new FileInputStream(edfile);
			ret = isED2File(is);
			is.close();
		} catch (Exception e) {
			logger.error("Unexpected exception", e);
		}
		return ret;
	}

	public static boolean isED2File(InputStream is) {
		boolean ret = false;
		try {
			String fileIdHeader = readEDFileIdHeader(is);
			if (fileIdHeader != null) {
				ret = fileIdHeader.startsWith(ED2_FILE_ID_STR);
			}
		} catch (Exception e) {
			logger.error("Unexpected exception", e);
		}
		return ret;
	}

	private static String readEDFileIdHeader(InputStream is) throws IOException {
		String fileIdHeader = null;
		if (is != null) {
			int c, rc = 0;
			byte buff[] = new byte[ED2_FILE_ID_LEN];
			while ((c = is.read(buff, rc, ED2_FILE_ID_LEN - rc)) != -1 && (rc += c) < ED2_FILE_ID_LEN)
				;
			fileIdHeader = new String(buff, UTF8);
		}
		return fileIdHeader;
	}

	/**
	 * Reads and returns the ED file id header. Returns <tt>null</tt> if it not an
	 * ED (PVS, PVP and PVM) file.
	 * <p>
	 * Note that this method is invoked via Java reflection by some classes (e.g.
	 * wt.wvs.WVSLoggerHelper). So don't forget to change the callers when the
	 * signature of this method is changed.
	 */
	public static String getEDFileIdHeader(InputStream is) {
		String edFileIdHeader = null;

		try {
			String fid = readEDFileIdHeader(is);
			if (fid != null && (fid.startsWith(ED2_FILE_ID_STR) || fid.startsWith(EDP_FILE_ID_STR)
					|| fid.startsWith(EDM_FILE_ID_STR))) {
				edFileIdHeader = fid;
			}
		} catch (IOException ioe) {
			logger.error(ioe.toString());
		}

		return edFileIdHeader;
	}

	///////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////

	public Structure2() {
		this((Comp) null);
	}

	public Structure2(Comp r) {
		rootComp = r;
		compList = null;
		if (writesFloatOrientation()) {
			majorVersion = MAX_FLOAT_ORIENTATION_MAJOR_VERSION;
			minorVersion = MAX_FLOAT_ORIENTATION_MINOR_VERSION;
		} else {
			majorVersion = MAJOR_VERSION;
			minorVersion = MINOR_VERSION;
		}
	}

	public Structure2(File edF) throws Exception {
		this((Comp) null);
		if (edF != null)
			readED(edF);
	}

	/**
	 * Returns the major version of the PV file read or to be written.
	 */
	public int getMajorVersion() {
		return majorVersion;
	}

	/**
	 * Sets the major version of the PV file (for testing purposes).
	 */
	public void setMajorVersion(int majorVersion) {
		if (majorVersion > MAX_VERSION_NUMBER || majorVersion < MIN_VERSION_NUMBER) {
			throw new IllegalArgumentException("majorVersion - " + majorVersion);
		}
		this.majorVersion = majorVersion;
	}

	/**
	 * Returns the minor version of the PV file read or to be written.
	 */
	public int getMinorVersion() {
		return minorVersion;
	}

	/**
	 * Sets the minor version of the PV file (for testing purposes).
	 */
	public void setMinorVersion(int minorVersion) {
		if (minorVersion > MAX_VERSION_NUMBER || minorVersion < MIN_VERSION_NUMBER) {
			throw new IllegalArgumentException("minorVersion - " + minorVersion);
		}
		this.minorVersion = minorVersion;
	}

	/**
	 * Returns a concatenation of major and minor versions of the PV file read or to
	 * be written. Each version has 2 digits so the return is a String of 4
	 * characters.
	 */
	public String getFullVersionString() {
		NumberFormat nf = getVersionNoFormat();
		return nf.format(getMajorVersion()) + nf.format(getMinorVersion());
	}

	/**
	 * Determines if double precision orientation is supported.
	 *
	 * @return <tt>true</tt> if double precision orientation is supported. Returns
	 *         </tt>false</tt> if single precision is supported.
	 */
	public boolean isDoublePrecisionOrientation() {
		return isDoublePrecisionOrientation(getMajorVersion(), getMinorVersion());
	}

	public Map<String, String> getEDPMap() {
		return edpMap;
	}

	public void addWCEDPMap(String wcedp) {
		if (edpMap.get(null) == null)
			edpMap.put(null, EDP_IN_ED);
		if (wcedp == null || wcedp.length() == 0)
			wcedp = WCEDP;
		if (edpMap.get(PART_PROP_GROUP) == null)
			edpMap.put(PART_PROP_GROUP, wcedp);
		if (edpMap.get(EPM_PROP_GROUP) == null)
			edpMap.put(EPM_PROP_GROUP, wcedp);
		if (edpMap.get(DOC_PROP_GROUP) == null)
			edpMap.put(DOC_PROP_GROUP, wcedp);
		if (edpMap.get(MPM_PROP_GROUP) == null)
			edpMap.put(MPM_PROP_GROUP, wcedp);
	}

	public Map getEDPInputStreamMap() {
		return edpInputStreamMap;
	}

	public void setEDPInputStreamMap(Map<String, InputStream> m) {
		edpInputStreamMap = m;
	}

	public Map getEDPOutputStreamMap() {
		return edpOutputStreamMap;
	}

	public void setEDPOutputStreamMap(Map m) {
		edpOutputStreamMap = m;
	}

	public Map getWriteFileNameEDPMap() {
		return writeFileNameEDPMap;
	}

	public void setWriteFileNameEDPMap(Map m) {
		writeFileNameEDPMap = m;
	}

	public File getEDPWriteDir() {
		return edpWriteDir;
	}

	public void setEDPWriteDir(File d) {
		edpWriteDir = d;
	}

	public Map getReadFileNameEDPMap() {
		return readFileNameEDPMap;
	}

	public void setReadFileNameEDPMap(Map m) {
		readFileNameEDPMap = m;
	}

	public boolean isCloseInputStreams() {
		return closeInputStreams;
	}

	public void setCloseInputStreams(boolean b) {
		closeInputStreams = b;
	}

	public boolean isCloseOutputStreams() {
		return closeOutputStreams;
	}

	public void setCloseOutputStreams(boolean b) {
		closeOutputStreams = b;
	}

	public void setCompList(ArrayList<Structure2.Comp> cl) {
		setCompList(cl, false);
	}

	public void setCompList(ArrayList<Structure2.Comp> cl, boolean setRoot) {
		compList = cl;
		if (setRoot && cl != null && !cl.isEmpty())
			rootComp = cl.get(cl.size() - 1);
	}

	public ArrayList getCompList() {
		if (compList == null || compList.size() == 0) {
			if (rootComp == null)
				return new ArrayList(1);
			populateCompList();
		}
		return compList;
	}

	public void setAppearanceList(ArrayList<Appearance> c) {
		appearanceList = c;
	}

	public ArrayList getAppearanceList() {
		return appearanceList;
	}

	public Iterator components() {
		return getCompList().iterator();
	}

	public int getCompCount() {
		return compList == null ? 0 : compList.size();
	}

	public void setRootComp(Comp r) {
		rootComp = r;
		compList = null;
	}

	public Comp getRootComp() {
		if (rootComp == null && compList != null && compList.size() > 0)
			rootComp = compList.get(compList.size() - 1);
		return rootComp;
	}

	public CompInst getRootCompInst() {
		return new CompInst(null, getRootComp());
	}

	public void setFileMap(Map<String, String> c) {
		fileMap = c;
	}

	public Map<String, String> getFileMap() {
		return fileMap;
	}

	public void setFileMapOid(String o) {
		fileMapOid = o;
	}

	public String getFileMapOid() {
		return fileMapOid;
	}

	public void setFileMapOid1Prefix(String o) {
		fileMapOid1Prefix = o;
	}

	public String getFileMapOid1Prefix() {
		return fileMapOid1Prefix;
	}

	public void setFileMapOid1Suffix(String o) {
		fileMapOid1Suffix = o;
	}

	public String getFileMapOid1Suffix() {
		return fileMapOid1Suffix;
	}

	public void setFileMapOid2Prefix(String o) {
		fileMapOid2Prefix = o;
	}

	public String getFileMapOid2Prefix() {
		return fileMapOid2Prefix;
	}

	public void setFileMapOid2Suffix(String o) {
		fileMapOid2Suffix = o;
	}

	public String getFileMapOid2Suffix() {
		return fileMapOid2Suffix;
	}

	public void setFileMapFullyPopulateOnRead(boolean b) {
		fileMapFullyPopulateOnRead = b;
	}

	public boolean isFileMapFullyPopulateOnRead() {
		return fileMapFullyPopulateOnRead;
	}

	public void addFileMapEntry(String f, String o1, String o2) throws Exception {
		if (f == null || f.length() == 0 || o1 == null || o1.length() == 0 || (o2 != null && o2.length() == 0))
			throw new IOException("Invalid data for file map entry");
		if (fileMap == null)
			fileMap = new HashMap<String, String>(10);
		fileMap.put(f, o2 == null ? o1 : o1 + DELIM + o2);
	}

	public void clearFileMap() {
		fileMap = null;
		fileMapOid = null;
	}

	public boolean isWriteIncludeProperties() {
		return writeIncludeProperties;
	}

	public void setWriteIncludeProperties(boolean b) {
		writeIncludeProperties = b;
	}

	public boolean isWriteExternalEDPFiles() {
		return writeExternalEDPFiles;
	}

	public void setWriteExternalEDPFiles(boolean b) {
		writeExternalEDPFiles = b;
	}

	public boolean isReadIncludeProperties() {
		return readIncludeProperties;
	}

	public void setReadIncludeProperties(boolean b) {
		readIncludeProperties = b;
	}

	public boolean isReadExternalEDPFiles() {
		return readExternalEDPFiles;
	}

	public void setReadExternalEDPFiles(boolean b) {
		readExternalEDPFiles = b;
	}

	public boolean isWriteIncludeFileMap() {
		return writeIncludeFileMap;
	}

	public void setWriteIncludeFileMap(boolean b) {
		writeIncludeFileMap = b;
	}

	public boolean isWriteFileMapAsSymbols() {
		return writeFileMapAsSymbols;
	}

	public void setWriteFileMapAsSymbols(boolean b) {
		writeFileMapAsSymbols = b;
	}

	public int getWriteSortChildren() {
		if (compInstComparator == null) {
			return 0;
		} else {
			return compInstComparator.getSortChildren();
		}
	}

	public void setWriteSortChildren(int b) {
		if (b == 1 || b == -1) {
			compInstComparator = new CompInstComparator(b);
		} else {
			compInstComparator = null;
		}
	}

	public boolean isWriteStructureAsSymbols() {
		return writeStructureAsSymbols;
	}

	public void setWriteStructureAsSymbols(boolean b) {
		writeStructureAsSymbols = b;
	}

	public boolean isWritePropertiesAsSymbols() {
		return writePropertiesAsSymbols;
	}

	public void setWritePropertiesAsSymbols(boolean b) {
		writePropertiesAsSymbols = b;
	}

	public int getCompressionType() {
		return compressionType;
	}

	public void setCompressionType(int c) {
		compressionType = c;
	}

	public boolean isConvertBranchLink() {
		return convertBranchLink;
	}

	public void setConvertBranchLink(boolean b) {
		convertBranchLink = b;
	}

	public boolean isConvertURL() {
		return convertURL;
	}

	public void setConvertURL(boolean b) {
		convertURL = b;
	}

	public void populateCompList() {
		compList = new ArrayList<Comp>(100);
		appearanceList = new ArrayList<Appearance>(10);
		appearanceMap = new HashMap<Object, Object>(10);
		if (rootComp != null)
			addToCompList(rootComp, new HashSet<Comp>());
	}

	private void addToCompList(Comp comp, HashSet<Comp> parentsOfComp) {
		if (compList.contains(comp))
			return;

		@SuppressWarnings("unchecked")
		HashSet<Comp> parentsOfChildren = (HashSet<Comp>) parentsOfComp.clone();
		parentsOfChildren.add(comp);

		for (Iterator<CompInst> it = comp.children(); it.hasNext();) {
			CompInst childInst = it.next();

			if (parentsOfChildren.contains(childInst.child)) {
				logger.debug("RECURSIVE STRUCTURE - " + childInst.child.name);
				Structure2.Comp dummy = new Structure2.Comp(childInst.child.name);
				dummy.type = childInst.child.type;
				dummy.addPropertiesFrom(childInst.child);
				Structure2.addPropertyValue(dummy, "RECURSIVE", Structure2.TRUE, null);
				childInst.child = dummy;
			}

			if (childInst.child != null) {
				addToCompList(childInst.child, parentsOfChildren);
			}
			addToAppearanceList(childInst.appearanceOveride);
			addInstAppOverideToAppearanceList(childInst.instanceAppearanceOverideList);
		}

		compList.add(comp);
		addToAppearanceList(comp.defaultAppearance);
		addInstAppOverideToAppearanceList(comp.instanceAppearanceOverideList);
	}

	private void addInstAppOverideToAppearanceList(ArrayList instAppOverideList) {
		if (instAppOverideList != null) {
			for (Iterator it = instAppOverideList.iterator(); it.hasNext();) {
				addToAppearanceList(((InstAppearance) it.next()).appearance);
			}
		}
	}

	private void addToAppearanceList(Appearance app) {
		if (app != null && appearanceMap.get(app) == null) {
			appearanceMap.put(app, Integer.valueOf(appearanceList.size()));
			appearanceList.add(app);
		}
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////// reading of ed2 files
	/////////////////////////////////////////////////////////////////////////////////////////////////// //////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////

	public void readED(URL u) throws Exception {
		URLConnection urcl = u.openConnection();
		if (urcl instanceof HttpURLConnection) {
			if (((HttpURLConnection) urcl).getResponseCode() != HttpURLConnection.HTTP_OK) {
				logger.debug(">> Error retrieving URL    : " + urcl.getURL());
				logger.debug(">> HTTP response code is   : " + ((HttpURLConnection) urcl).getResponseCode());
				logger.debug(">> HTTP response message is: " + ((HttpURLConnection) urcl).getResponseMessage());
				throw new IOException("failed to read url");
			}
		}
		InputStream is = urcl.getInputStream();
		readED(null, is, null);
		try {
			is.close();
		} catch (Exception e) {
		}
	}

	public void readED(InputStream is) throws Exception {
		readED(null, is, null);
	}

	public void readED(File edF) throws Exception {
		readED(edF, null, null);
	}

	public void readED(InputStream is, InputStream edpStream) throws Exception {
		readED(null, is, edpStream);
	}

	public void readED(File edF, InputStream edpStream) throws Exception {
		readED(edF, null, edpStream);
	}

	@SuppressWarnings("unchecked") // unchecked calls to add and put
	private void readED(File edFilename, InputStream is, InputStream edpStream) throws Exception {
		rootComp = null;
		compList = new ArrayList<Comp>(100);
		appearanceList = new ArrayList<Appearance>(10);

		if (edFilename != null)
			is = new FileInputStream(edFilename);
		bi = new BufferedInputStream(is, BUFFER_SIZE_4096);
		input = bi;
		symbols = null;

		readFileIdHeader(edFilename, ED2_FILE_ID_STR);
		int firstSectionOffset = read4();
		final boolean trace = logger.isTraceEnabled();
		if (trace)
			logger.trace("firstSectionOffset = " + firstSectionOffset);

		int numIntSections = read2();
		int numExtSections = read2();
		if (trace)
			logger.trace("numIntSections = " + numIntSections + " numExtSections = " + numExtSections);

		IndexEntry[] intIndex = new IndexEntry[numIntSections];
		IndexEntry[] extIndex = numExtSections == 0 ? null : new IndexEntry[numExtSections];

		for (int i = 0; i < numIntSections; i++) {
			intIndex[i] = new IndexEntry(true);
			intIndex[i].readIndex();
		}

		for (int i = 0; i < numExtSections; i++) {
			extIndex[i] = new IndexEntry(false);
			extIndex[i].readIndex();
		}

		// read all the internal sections
		for (int i = 0; i < numIntSections; i++) {
			if (readIncludeProperties && intIndex[i].type == SECTION_TYPE_PROPERTIES)
				edpMap.put(intIndex[i].description, EDP_IN_ED);
			readSection(intIndex[i], readIncludeProperties, null);

		}

		// free off the symbols array
		symbols = null;
		appearanceList = null;
		if (edFilename != null || isCloseInputStreams())
			is.close();

		// read an external file map, not expected though
		for (int i = 0; i < numExtSections; i++) {
			if (extIndex[i].type == SECTION_TYPE_FILEMAP) {
				readEDM(edFilename == null ? new File(extIndex[i].filename)
						: new File(edFilename.getParentFile(), extIndex[i].filename));
			}
		}

		// read external properties sections
		readFileNameEDPMap.clear();
		for (int i = 0; i < numExtSections; i++) {
			if (extIndex[i].type == SECTION_TYPE_PROPERTIES) {
				edpMap.put(extIndex[i].description, extIndex[i].filename);
				ArrayList d = (ArrayList) readFileNameEDPMap.get(extIndex[i].filename);
				if (d == null) {
					d = new ArrayList(2);
					readFileNameEDPMap.put(extIndex[i].filename, d);
				}
				d.add(extIndex[i].description);
			}
		}

		if (readIncludeProperties && readExternalEDPFiles) {
			for (Iterator it = readFileNameEDPMap.entrySet().iterator(); it.hasNext();) {
				Map.Entry entry = (Map.Entry) it.next();
				String file = (String) entry.getKey();
				ArrayList onlySections = (ArrayList) entry.getValue();
				if (trace)
					logger.trace("Read External properties from " + file + " number of sections to read "
							+ onlySections.size());

				if (edpStream == null) {
					InputStream edps = getEDPInputStreamFromMap(file);
					if (edps == null) {
						readEDP(edFilename == null ? new File(file) : new File(edFilename.getParentFile(), file),
								onlySections);
					} else {
						readEDP(edps, onlySections);
					}
				} else {
					readEDP(edpStream, onlySections);
				}
			}

			// list out any external property setions that remain unresolved
			if (trace) {
				for (Iterator it = readFileNameEDPMap.entrySet().iterator(); it.hasNext();) {
					Map.Entry entry = (Map.Entry) it.next();
					ArrayList onlySections = (ArrayList) entry.getValue();
					for (Iterator it1 = onlySections.iterator(); it1.hasNext();) {
						logger.trace("Warning: properties group >>" + (String) it1.next() + "<< from file >>"
								+ (String) entry.getKey() + "<< not found");
					}
				}
			}
		}

		if (isCloseInputStreams() && edpStream != null)
			edpStream.close();

		if (isCloseInputStreams() && edpInputStreamMap != null) {
			for (Iterator it = edpInputStreamMap.values().iterator(); it.hasNext();) {
				Object o = it.next();
				if (o instanceof InputStream)
					((InputStream) o).close();
			}
		}
	}

	/**
	 * Read the PV file ID header, validate the file ID prefix, and parse the major
	 * and minor versions of the file.
	 *
	 * @param edFile
	 *            - File to read. May be <tt>null</tt>.
	 * @param fileIdPrefix
	 *            - expected file ID prefix. If <tt>null</tt>, no validation of file
	 *            type.
	 * @return - the full header line.
	 * @throws IOException
	 *             if IO error, unexpected header prefix, or missing or invalid
	 *             version info.
	 */
	private String readFileIdHeader(File edFile, String fileIdPrefix) throws IOException {
		String fileIdHeader = reads(ED2_FILE_ID_LEN);
		if (logger.isTraceEnabled()) {
			String fileName = edFile == null ? "-" : edFile.toString();
			logger.trace("Read ED2: " + fileName + " File ID: " + fileIdHeader);
		}

		// validate file header prefix
		if (fileIdPrefix != null && !fileIdHeader.startsWith(fileIdPrefix)) {
			throw new IOException("Unexpected file id - read '" + fileIdHeader + "' instead of '" + fileIdPrefix + "'"
					+ (edFile == null ? "" : (" in file " + edFile.toString())));
		}

		// parse file version info
		for (int i = 0; i < VERSION_NO_FIELD_LEN * 2; i++) {
			char ch = fileIdHeader.charAt(MAJOR_VERSION_OFFSET + i);
			if (!Character.isDigit(ch)) {
				throw new IOException("Invalid version info '" + fileIdHeader + "'"
						+ (edFile == null ? "" : (" in file " + edFile.toString())));
			}
		}
		String sMajor = fileIdHeader.substring(MAJOR_VERSION_OFFSET, MAJOR_VERSION_OFFSET + VERSION_NO_FIELD_LEN);
		String sMinor = fileIdHeader.substring(MINOR_VERSION_OFFSET, MINOR_VERSION_OFFSET + VERSION_NO_FIELD_LEN);
		NumberFormat nf = getVersionNoFormat();
		try {
			Number oMajor = nf.parse(sMajor);
			Number oMinor = nf.parse(sMinor);
			majorVersion = oMajor.intValue();
			minorVersion = oMinor.intValue();
		} catch (ParseException pe) {
			throw new IOException("Invalid version info '" + fileIdHeader + "'"
					+ (edFile == null ? "" : (" in file " + edFile.toString())), pe);
		}

		return fileIdHeader;
	}

	private void readSection(IndexEntry index, boolean incProps, ArrayList onlySections) throws Exception {
		startReadSection(index);

		switch (index.type) {
		case SECTION_TYPE_STRUCTURE:
			readStructureSection();
			break;
		case SECTION_TYPE_PROPERTIES:
			if (incProps && (onlySections == null || onlySections.contains(index.description))) {
				readPropertiesSection(index.description);
				if (onlySections != null)
					onlySections.remove(index.description);
			}
			break;
		case SECTION_TYPE_SYMBOLS:
			readSymbolTableSection();
			break;
		case SECTION_TYPE_FILEMAP:
			readFileMapSection();
			break;
		case SECTION_TYPE_VIEWSTATE:
			logger.debug("VIEWSTATE sections are not supported in PVS/PVP/PVM files");
			break;
		default:
			logger.debug("Unknown PVS/PVP/PVM internal file section type " + index.type);
		}

		endReadSection(index);
	}

	private void startReadSection(IndexEntry index) throws Exception {
		int count = index.length;
		if (logger.isTraceEnabled())
			logger.trace("readSection: size " + count);

		try {
			if (zi != null)
				zi.close();
		} catch (Exception e) {
		}
		zi = null;

		if ((index.compression & 0xff) == 0) { // no compression
			input = bi;
			return;
		}

		int c, rc = 0;
		byte[] buf = new byte[count];
		while ((c = bi.read(buf, rc, count - rc)) != -1 && (rc += c) < count)
			;

		if (rc != count)
			throw new IOException("startReadSection: failed to read all section data block");

		if ((index.compression & 0xff) == 1) {
			zi = new BufferedInputStream(new GZIPInputStream(new ByteArrayInputStream(buf), count), BUFFER_SIZE_4096);
		} else if ((index.compression & 0xff) == 2) {
			logger.debug("BZIP Compression not supported");
			throw new IOException("BZIP Compression not supported");
		} else {
			throw new IOException("invalid compresson type ");
		}

		input = zi;
	}

	private void endReadSection(IndexEntry index) throws Exception {
		try {
			if (zi != null)
				zi.close();
		} catch (Exception e) {
		}
		zi = null;
		input = bi;
	}

	private boolean readStructureSection() throws Exception {
		readTag();
		if (tagTag != SECTION_TYPE_STRUCTURE || !tagHasEndFlag)
			return false;

		skipTagBitData();

		boolean loop = true;
		while (loop) {
			readTag();
			switch (tagTag) {
			case ED_COMPONENT_TAG:
				readComponent(false);
				break;
			case ED_COMPONENT_PROXY_TAG:
				readComponent(true);
				break;
			case ED_APPEARANCE_TABLE_TAG:
				readAppearanceTable();
				break;
			case GEN_END_TAG:
				loop = false;
				break;
			default:
				logger.debug("readStructureSection: Unexpected tag type " + tagTag);
				skipObject();
				break;
			}
		}

		// last component in ed file is the root node
		if (rootComp == null && compList != null && compList.size() > 0)
			rootComp = compList.get(compList.size() - 1);

		return true;
	}

	private boolean readComponent(boolean isProxy) throws Exception {
		Comp comp = new Comp(reads(), isProxy); // read name
		compList.add(comp);

		if (isProxy) {
			comp.filename = reads();
			if ((tagBits & COMPONENT_TYPE) != 0) {
				comp.type = readc();
			}
			if ((tagBits & COMPONENT_PROXY_MAP_FILENAME) != 0) {
				comp.map_filename = reads();
			}
			if ((tagBits & COMPONENT_PROXY_WVS_INFO) != 0) {
				comp.wvs_info = reads();
			}
		} else {
			if ((tagBits & COMPONENT_TYPE) != 0) {
				comp.type = readc();
			}
			if ((tagBits & COMPONENT_MODEL_UNIT_LENGTH) != 0) {
				comp.modelUnitLength = readc();
			}
			if ((tagBits & COMPONENT_DISPLAY_UNIT_LENGTH) != 0) {
				comp.displayUnitLength = readc();
			}
			if ((tagBits & COMPONENT_MODEL_UNIT_MASS) != 0) {
				comp.modelUnitMass = readc();
			}
			if ((tagBits & COMPONENT_DISPLAY_UNIT_MASS) != 0) {
				comp.displayUnitMass = readc();
			}
			if ((tagBits & COMPONENT_DEFAULT_APPEARANCE) != 0) {
				int idx = readc();
				Appearance app = appearanceList.get(idx);
				if (app == null)
					throw new IOException("readComponent: Appearance index " + idx + " not found");
				comp.defaultAppearance = app;
			}
		}

		skipTagBitData();

		if (!tagHasEndFlag) {
			return true;
		}

		boolean loop = true;
		while (loop) {
			readTag();
			switch (tagTag) {
			case ED_SHAPE_TAG:
				readShape(comp);
				break;
			case ED_THUMBNAIL_3D_TAG:
				readThumbnail3D(comp);
				break;
			case ED_COMPONENT_INSTANCE_TAG:
				readComponentInstance(comp);
				break;
			case ED_DRAWING_TAG:
			case ED_DOCUMENT_TAG:
			case ED_IMAGE_TAG:
			case ED_FILE_TAG:
			case ED_OLEDOC_TAG:
			case ED_ILLUSTRATION_TAG:
			case ED_ILLUSTRATION3D_TAG:
			case ED_ECAD_TAG:
				readViewable(comp);
				break;
			case ED_VIEW_TAG:
				readView(comp);
				break;
			case ED_VIEWSTATE_TAG:
			case ED_ALTERNATEREP_TAG:
			case ED_EXPLODESTATE_TAG:
			case ED_SECTIONCUT_TAG:
			case ED_PROCESS_TAG:
				readViewState(comp);
				break;
			case ED_INSTANCE_APPEARANCE_OVERIDE_TAG:
				readInstanceAppearanceOveride(null, comp);
				break;
			case GEN_END_TAG:
				loop = false;
				break;
			default:
				logger.debug("readComponent: Unexpected tag type " + tagTag);
				skipObject();
				break;
			}
		}

		return true;
	}

	private boolean readShape(Comp comp) throws Exception {
		comp.shape = reads();

		if ((tagBits & SHAPE_BBOX) != 0) {
			comp.bbox = new float[6];
			for (int i = 0; i < 6; i++)
				comp.bbox[i] = readf();
		}

		if ((tagBits & SHAPE_INDEX) != 0) {
			comp.shape_index = readc();
		}

		skipTagBitData();

		// optionally read the shape end flag
		if (tagHasEndFlag)
			readTag();

		return true;
	}

	private boolean readThumbnail3D(Comp comp) throws Exception {
		comp.thumbnail3d = reads();

		if ((tagBits & THUMBNAIL_INDEX) != 0) {
			comp.thumbnail3d_index = readc();
		}

		skipTagBitData();

		// optionally read the shape end flag
		if (tagHasEndFlag)
			readTag();

		return true;
	}

	private boolean readViewable(Comp comp) throws Exception {
		String filename = reads();
		String displayname = null;
		int type = 0;
		int ecad_type = 0;
		boolean drawingSheetOfPrevious = false;
		boolean drawingNextDrawingIsContinuation = false;
		if ((tagBits & VIEWABLE_DISPLAY_NAME) != 0) {
			displayname = reads();
		}
		if ((tagBits & VIEWABLE_TYPE) != 0) {
			type = readc();
		}
		if (tagTag == ED_ECAD_TAG) {
			if ((tagBits & VIEWABLE_ECAD_TYPE) != 0) {
				ecad_type = readc();
			}
		} else if (tagTag == ED_DRAWING_TAG) {
			if ((tagBits & VIEWABLE_DRAWING_SHEET_OF_PREVIOUS_TYPE) != 0) {
				drawingSheetOfPrevious = true;
			}
			if ((tagBits & VIEWABLE_DRAWING_NEXT_DRAWING_IS_CONTINUATION) != 0) {
				drawingNextDrawingIsContinuation = true;
			}
		}

		CViewable cviewable = new CViewable(comp, tagTag, filename, displayname, type, ecad_type,
				drawingSheetOfPrevious, drawingNextDrawingIsContinuation);

		skipTagBitData();

		if (!tagHasEndFlag)
			return true;

		boolean loop = true;
		while (loop) {
			readTag();

			switch (tagTag) {
			case ED_DRAWING_SHEET_TAG:
				readDrawingSheet(cviewable);
				break;
			case GEN_END_TAG:
				loop = false;
				break;
			default:
				logger.debug("readViewable: Unexpected tag type " + tagTag);
				skipObject();
				break;
			}
		}

		return true;
	}

	private boolean readDrawingSheet(CViewable cviewable) throws Exception {
		int size = 0;
		int units = 0;
		float width = 0.0f, height = 0.0f;
		int orientation = 0;
		String name = null;
		if ((tagBits & DRAWING_SHEET_SIZE) != 0) {
			size = readc();
		}
		if (size == 1) {
			units = readc();
			width = readf();
			height = readf();
		}
		if ((tagBits & DRAWING_SHEET_ORIENTATION) != 0) {
			orientation = readc();
		}
		if ((tagBits & DRAWING_SHEET_NAME) != 0) {
			name = reads();
		}

		new CDrawingSheet(cviewable, size, units, width, height, orientation, name);

		skipTagBitData();

		// optionally read the view end flag
		if (tagHasEndFlag)
			readTag();

		return true;
	}

	private boolean readView(Comp comp) throws Exception {
		String name = reads();
		float[] orientation = null;
		if ((tagBits & VIEW_ORIENTATION) != 0) {
			orientation = new float[4];
			for (int i = 0; i < 4; i++)
				orientation[i] = readf();
		}

		new CView(comp, name, orientation);

		skipTagBitData();

		// optionally read the view end flag
		if (tagHasEndFlag)
			readTag();

		return true;
	}

	private boolean readViewState(Comp comp) throws Exception {
		String name = reads();

		CViewState viewState = new CViewState(comp, tagTag, name);

		if ((tagBits & VIEWSTATE_FILENAME) != 0) {
			viewState.filename = reads();
		}
		if ((tagBits & VIEWSTATE_INDEX) != 0) {
			viewState.index = readc();
		}

		skipTagBitData();

		// optionally read the view state end flag
		if (tagHasEndFlag)
			readTag();

		return true;
	}

	private boolean readComponentInstance(Comp parent) throws Exception {
		int idx = readc(); // compressed int
		Comp child = compList.get(idx);
		if (child == null)
			throw new IOException("child component index " + idx + " not found");

		CompInst compInst = new CompInst(parent, child);

		if ((tagBits & COMPONENT_INSTANCE_RELATIONSHIP) != 0) {
			compInst.type = readc();
		}

		if ((tagBits & COMPONENT_INSTANCE_TRANSLATION) != 0) {
			compInst.translation = new double[3];
			for (int i = 0; i < 3; i++)
				compInst.translation[i] = readd();
		}

		if ((tagBits & COMPONENT_INSTANCE_ORIENTATION) != 0) {
			double[] orientation = new double[9];
			if (isDoublePrecisionOrientation()) {
				for (int i = 0; i < 9; i++)
					orientation[i] = readd();
			} else {
				for (int i = 0; i < 9; i++)
					orientation[i] = readf();
			}
			compInst.orientation = orientation;
		}

		if ((tagBits & COMPONENT_INSTANCE_ID) != 0) {
			compInst.id = reads();
		}
		if ((tagBits & COMPONENT_INSTANCE_NAME) != 0) {
			compInst.name = reads();
		}
		if ((tagBits & COMPONENT_INSTANCE_HIDE_SELF) != 0) {
			compInst.hideSelf = true;
		}
		if ((tagBits & COMPONENT_INSTANCE_HIDE_DESCENDANTS) != 0) {
			compInst.hideDescendants = true;
		}
		if ((tagBits & COMPONENT_INSTANCE_COMBINE) != 0) {
			compInst.combine = true;
		}

		skipTagBitData();

		if (!tagHasEndFlag)
			return true;

		boolean loop = true;
		while (loop) {
			readTag();

			switch (tagTag) {
			case ED_STRUCTURE_INSTANCE_REF_TAG:
				readStructureInstanceRef(compInst);
				break;
			case ED_APPEARANCE_OVERIDE_TAG:
				readAppearanceOveride(compInst);
				break;
			case ED_INSTANCE_APPEARANCE_OVERIDE_TAG:
				readInstanceAppearanceOveride(compInst, null);
				break;
			case GEN_END_TAG:
				loop = false;
				break;
			default:
				logger.debug("readComponentInstance: Unexpected tag type " + tagTag);
				skipObject();
				break;
			}
		}

		return true;
	}

	private boolean readStructureInstanceRef(CompInst compInst) throws Exception {
		logger.trace("** skipping strcture instance ref");

		if ((tagBits & STRUCTURE_INST_REF_OFFSET) != 0) {
			readc();
		}

		skipTagBitData();

		if (!tagHasEndFlag)
			return true;

		boolean loop = true;
		while (loop) {
			readTag();
			switch (tagTag) {
			case ED_STRUCTURE_INSTANCE_REF_TAG:
				readStructureInstanceRef(compInst);
				break;
			case GEN_END_TAG:
				loop = false;
				break;
			default:
				logger.debug("readStructureInstanceRef: Unexpected tag type " + tagTag);
				skipObject();
				break;
			}
		}

		return true;
	}

	private boolean readAppearanceOveride(CompInst compInst) throws Exception {
		int idx = readc();
		Appearance app = appearanceList.get(idx);
		if (app == null)
			throw new IOException("readAppearanceOveride: Appearance index " + idx + " not found");
		compInst.appearanceOveride = app;

		compInst.appearanceOverideSelf = true;
		compInst.appearanceOverideDescendants = true;
		if ((tagBits & APPEARANCE_OVERIDE_SELF) != 0) {
			compInst.appearanceOverideSelf = false;
		}
		if ((tagBits & APPEARANCE_OVERIDE_DESCENDANTS) != 0) {
			compInst.appearanceOverideDescendants = false;
		}

		skipTagBitData();

		// optionally read the appearance overide end flag
		if (tagHasEndFlag)
			readTag();

		return true;
	}

	private boolean readInstanceAppearanceOveride(CompInst compInst, Comp comp) throws Exception {
		int idx = readc();
		Appearance app = appearanceList.get(idx);
		if (app == null)
			throw new IOException("readInstanceAppearanceOveride: Appearance index " + idx + " not found");

		InstAppearance instApp = (compInst != null) ? new InstAppearance(compInst, app) : new InstAppearance(comp, app);

		if ((tagBits & INSTANCE_APPEARANCE_OVERIDE_RECURSE) != 0) {
			instApp.recurse = false;
		}
		if ((tagBits & INSTANCE_APPEARANCE_OVERIDE_PATH) != 0) {
			instApp.instancePath = reads();
		}

		if ((tagBits & INSTANCE_APPEARANCE_OVERIDE_FACEIDS) != 0) {
			int count = readc();
			if (count > 0) {
				instApp.faceids = new String[count];
				for (int i = 0; i < count; i++)
					instApp.faceids[i] = reads();
			}
		}

		skipTagBitData();

		// optionally read the appearance overide end flag
		if (tagHasEndFlag)
			readTag();

		return true;
	}

	private boolean readPropertiesSection(String groupName) throws Exception {
		readTag();
		if (tagTag != SECTION_TYPE_PROPERTIES)
			return false;

		skipTagBitData();

		if (!tagHasEndFlag)
			return true; // no properties in section

		Iterator it = compList.iterator();
		boolean loop = true;
		while (loop) {
			readTag();
			switch (tagTag) {
			case ED_PROPERTY_COMPONENT_REF_TAG:
				readPropertyComponentRef(it, groupName);
				break;
			case GEN_END_TAG:
				loop = false;
				break;
			default:
				logger.debug("readPropertiesSection: Unexpected tag type " + tagTag);
				skipObject();
				break;
			}
		}

		return true;
	}

	@SuppressWarnings("unchecked") // unchecked call to readProperty
	private boolean readPropertyComponentRef(Iterator it, String groupName) throws Exception {
		int offset = ((tagBits & PROPERTY_COMP_REF_OFFSET) != 0) ? readc() : 1;
		Comp comp = null;
		for (int i = 0; i < offset; i++)
			comp = (Comp) it.next();

		skipTagBitData();

		if (!tagHasEndFlag)
			return true; // no properties for this comp

		Iterator<CompInst> inst_it = comp.children();
		boolean loop = true;
		while (loop) {
			readTag();
			switch (tagTag) {
			case ED_PROPERTY_TAG:
				if (comp.properties == null)
					comp.properties = new Hashtable(5);
				readProperty(comp.properties, groupName);
				break;
			case ED_PROPERTY_INSTANCE_REF_TAG:
				readPropertyInstanceRef(inst_it, groupName);
				break;
			case GEN_END_TAG:
				loop = false;
				break;
			default:
				logger.debug("readPropertyComponentRef: Unexpected tag type " + tagTag);
				skipObject();
				break;
			}
		}

		return true;
	}

	@SuppressWarnings("unchecked") // unchecked call to readProperty
	private boolean readPropertyInstanceRef(Iterator<CompInst> it, String groupName) throws Exception {
		int offset = ((tagBits & PROPERTY_INST_REF_OFFSET) != 0) ? readc() : 1;
		CompInst compInst = null;
		if (it != null) {
			for (int i = 0; i < offset; i++)
				compInst = it.next();
		}

		skipTagBitData();

		if (!tagHasEndFlag)
			return true; // no properties for this instance

		final boolean trace = logger.isTraceEnabled();
		boolean loop = true;
		while (loop) {
			readTag();
			switch (tagTag) {
			case ED_PROPERTY_TAG:
				if (compInst == null) {
					Hashtable dummy = new Hashtable(5);
					readProperty(dummy, groupName);
					if (trace)
						logger.trace("**Discarded property data " + dummy);
				} else {
					if (compInst.properties == null)
						compInst.properties = new Hashtable(5);
					readProperty(compInst.properties, groupName);
				}
				break;
			case ED_PROPERTY_INSTANCE_REF_TAG:
				if (trace)
					logger.trace("**Nested property_instance_ref");
				readPropertyInstanceRef(null, groupName);
				break;
			case GEN_END_TAG:
				loop = false;
				break;
			default:
				logger.debug("readPropertyInstanceRef: Unexpected tag type " + tagTag);
				skipObject();
				break;
			}
		}

		return true;
	}

	private boolean readProperty(Hashtable<String, Object> h, String groupName) throws Exception {
		String name = reads(); // property name

		Object value;

		tagBits = tagBits & 0x1f; // mask out any higher bit values

		if (tagBits == PROPERTY_TYPE_STRING) {
			value = reads();
		} else if (tagBits == PROPERTY_TYPE_FLOAT) {
			value = Float.valueOf(readf());
		} else if (tagBits == PROPERTY_TYPE_DOUBLE) {
			value = Double.valueOf(readd());
		} else if (tagBits == PROPERTY_TYPE_BYTE) {
			value = Byte.valueOf((byte) read1());
		} else if (tagBits == PROPERTY_TYPE_SHORT) {
			value = Short.valueOf((short) read2());
		} else if (tagBits == PROPERTY_TYPE_INT) {
			value = Integer.valueOf(read4());
		} else if (tagBits == PROPERTY_TYPE_BOOLEAN) {
			value = Boolean.valueOf(read1() == 0 ? false : true);
		} else if (tagBits == PROPERTY_TYPE_DATE) {
			value = reads();
		} else if (tagBits == PROPERTY_TYPE_VOID) {
			value = "";
		} else {
			value = "";
			logger.debug("readProperty: Unknown property type for property " + name);
		}

		addPropertyValue(h, name, value, groupName);

		skipTagBitData();

		return true;
	}

	/**
	 * add the property name/value on the hashtable, allows milti-value properties,
	 * with group This differs from the version in Structure.java in that the value
	 * is an Object <BR>
	 * <BR>
	 * <B>Supported API: </B>false
	 *
	 **/
	@SuppressWarnings("unchecked") // unchedked calls to add() and put()
	public static void addPropertyValue(Hashtable<String, Object> h, String name, Object value, String group) {
		if (h == null || name == null || value == null)
			return;

		if (value instanceof List) {
			for (Iterator it = ((List) value).iterator(); it.hasNext();) {
				addPropertyValue(h, name, it.next(), group);
			}
			return;
		}

		Object o = h.get(name);

		if (o == null) {
			h.put(name, value);

			if (group != null && group.length() > 0) {
				Hashtable g = (Hashtable) h.get(PROPERTY_GROUP_LOOKUP);
				if (g == null) {
					g = new Hashtable(5);
					h.put(PROPERTY_GROUP_LOOKUP, g);
				}
				g.put(name, group);
			}
		} else if (o instanceof List) {
			List l = (List) o;
			if (l.contains(value))
				return; // value already there, don't duplicate
			l.add(value);
		} else { // other type of object
			if (value.equals(o))
				return; // value already there, don't duplicate
			List l = new ArrayList(2);
			l.add(o);
			l.add(value);
			h.put(name, l);
		}
	}

	@SuppressWarnings("unchecked") // unchecked call to addPropertyValue
	public static void addPropertyValue(Comp comp, String name, Object value, String group) {
		if (comp == null || name == null || value == null)
			return;
		if (comp.properties == null)
			comp.properties = new Hashtable(5);
		addPropertyValue(comp.properties, name, value, group);
	}

	@SuppressWarnings("unchecked") // unchecked call to addPropertyValue
	public static void addPropertyValue(CompInst inst, String name, Object value, String group) {
		if (inst == null || name == null || value == null)
			return;
		if (inst.properties == null)
			inst.properties = new Hashtable(5);
		addPropertyValue(inst.properties, name, value, group);
	}

	public static void putPropertyValue(Comp comp, String name, Object value, String group) {
		if (comp == null || name == null || value == null)
			return;
		if (comp.properties == null)
			comp.properties = new Hashtable(5);
		putPropertyValue(comp.properties, name, value, group);
	}

	public static void putPropertyValue(CompInst inst, String name, Object value, String group) {
		if (inst == null || name == null || value == null)
			return;
		if (inst.properties == null)
			inst.properties = new Hashtable(5);
		putPropertyValue(inst.properties, name, value, group);
	}

	@SuppressWarnings("unchecked") // unchecked calls to put()
	public static void putPropertyValue(Hashtable h, String name, Object value, String group) {
		if (h == null || name == null || value == null)
			return;

		h.put(name, value);

		if (group != null && group.length() > 0) {
			Hashtable g = (Hashtable) h.get(PROPERTY_GROUP_LOOKUP);
			if (g == null) {
				g = new Hashtable(5);
				h.put(PROPERTY_GROUP_LOOKUP, g);
			}
			g.put(name, group);
		}
	}

	public static Object[] getPropertyObjectValues(Comp comp, String name) {
		if (comp == null)
			return null;
		return getPropertyValues(comp.properties, name);
	}

	public static Object[] getPropertyObjectValues(CompInst inst, String name) {
		if (inst == null)
			return null;
		return getPropertyValues(inst.properties, name);
	}

	@SuppressWarnings("unchecked") // use of raw type List without paramaters
	public static Object[] getPropertyObjectValues(Hashtable h, String name) {
		if (h == null || name == null)
			return null;

		Object o = h.get(name);

		if (o == null) {
			return null;
		} else if (o instanceof List) {
			Object[] s = new Object[((List) o).size()];
			((List) o).toArray(s);
			return s;
		} else {
			Object[] s = new Object[1];
			s[0] = o;
			return s;
		}
	}

	public static String getPropertyStringValue(Comp comp, String name) {
		if (comp == null)
			return null;
		return getPropertyStringValue(comp.properties, name);
	}

	public static String getPropertyStringValue(CompInst inst, String name) {
		if (inst == null)
			return null;
		return getPropertyStringValue(inst.properties, name);
	}

	public static String getPropertyStringValue(Hashtable h, String name) {
		if (h == null || name == null)
			return null;

		Object o = h.get(name);
		if (o instanceof String)
			return (String) o;
		return null;
	}

	private boolean readAppearanceTable() throws Exception {
		skipTagBitData();

		if (!tagHasEndFlag) {
			logger.trace("readAppearanceTable: End (no end tag)");
			return true;
		}

		boolean loop = true;
		while (loop) {
			readTag();

			switch (tagTag) {
			case ED_APPEARANCE_TAG:
				readAppearance();
				break;
			case GEN_END_TAG:
				loop = false;
				break;
			default:
				logger.debug("readAppearanceTable: Unexpected tag type " + tagTag);
				skipObject();
				break;
			}
		}

		if (logger.isTraceEnabled()) {
			logger.trace("Appearance Table Size " + (appearanceList != null ? appearanceList.size() : 0));
		}
		return true;
	}

	private boolean readAppearance() throws Exception {
		Appearance app = new Appearance();
		appearanceList.add(app);

		if ((tagBits & APPEARANCE_BASE_COLOR) != 0) {
			app.base_color = new float[3];
			for (int i = 0; i < 3; i++)
				app.base_color[i] = readf();
		}
		if ((tagBits & APPEARANCE_AMBIENT_COLOR) != 0) {
			app.ambient_color = new float[3];
			for (int i = 0; i < 3; i++)
				app.ambient_color[i] = readf();
		}
		if ((tagBits & APPEARANCE_DIFFUSE_COLOR) != 0) {
			app.diffuse_color = new float[3];
			for (int i = 0; i < 3; i++)
				app.diffuse_color[i] = readf();
		}
		if ((tagBits & APPEARANCE_EMISSIVE_COLOR) != 0) {
			app.emissive_color = new float[3];
			for (int i = 0; i < 3; i++)
				app.emissive_color[i] = readf();
		}
		if ((tagBits & APPEARANCE_SPECULAR) != 0) {
			app.specular = new float[4];
			for (int i = 0; i < 4; i++)
				app.specular[i] = readf();
		}
		if ((tagBits & APPEARANCE_OPACITY) != 0) {
			app.opacity = new float[1];
			app.opacity[0] = readf();
		}

		skipTagBitData();

		// optionally read the shape end flag
		if (tagHasEndFlag)
			readTag();

		return true;
	}

	private boolean readSymbolTableSection() throws Exception {
		readTag();
		if (tagTag != SECTION_TYPE_SYMBOLS)
			return false;

		int symbolCount = readc(); // compressed int
		if (logger.isTraceEnabled())
			logger.trace("readSymbolTable: number of symbols = " + symbolCount);

		if (symbolCount > 0) {
			symbols = new String[symbolCount];
			for (int i = 0; i < symbolCount; i++) {
				symbols[i] = readstring();
			}
		}

		// read the symbol table end tag
		if (tagHasEndFlag)
			readTag();

		return true;
	}

	private boolean readFileMapSection() throws Exception {
		readTag();
		if (tagTag != SECTION_TYPE_FILEMAP)
			return false;

		String oid = (tagBits & FILEMAP_OID) != 0 ? reads() : null;
		String oid1_prefix = (tagBits & FILEMAP_OID1_PREFIX) != 0 ? reads() : null;
		String oid1_suffix = (tagBits & FILEMAP_OID1_SUFFIX) != 0 ? reads() : null;
		String oid2_prefix = (tagBits & FILEMAP_OID2_PREFIX) != 0 ? reads() : null;
		String oid2_suffix = (tagBits & FILEMAP_OID2_SUFFIX) != 0 ? reads() : null;
		if (oid != null && (tagBits & FILEMAP_OID_COMPLETE) == 0) { // oid not complete
			if (oid2_prefix != null)
				oid = oid2_prefix + oid;
			if (oid2_suffix != null)
				oid = oid + oid2_suffix;
		}

		// if there is no file map data already existing, then set file map parameters
		// from this file map
		if (fileMap == null || fileMap.size() == 0) {
			fileMapOid = oid;
			if (!fileMapFullyPopulateOnRead)
				oid = null;
			fileMapOid1Prefix = oid1_prefix;
			fileMapOid1Suffix = oid1_suffix;
			fileMapOid2Prefix = oid2_prefix;
			fileMapOid2Suffix = oid2_suffix;
		}

		skipTagBitData();

		if (!tagHasEndFlag)
			return true; // no file map entries in section

		if (fileMap == null)
			fileMap = new HashMap<String, String>(100);

		boolean loop = true;
		while (loop) {
			readTag();
			switch (tagTag) {
			case ED_FILEMAP_ENTRY_TAG:
				readFileMapEntry(oid, oid1_prefix, oid1_suffix, oid2_prefix, oid2_suffix);
				break;
			case GEN_END_TAG:
				loop = false;
				break;
			default:
				logger.debug("readFileMapSection: Unexpected tag type " + tagTag);
				skipObject();
				break;
			}
		}

		return true;
	}

	private boolean readFileMapEntry(String oid, String oid1_prefix, String oid1_suffix, String oid2_prefix,
			String oid2_suffix) throws Exception {
		String filename = reads();
		String oid1 = reads();
		if ((tagBits & FILEMAP_ENTRY_OID1_COMPLETE) == 0) { // not complete
			if (oid1_prefix != null)
				oid1 = oid1_prefix + oid1;
			if (oid1_suffix != null)
				oid1 = oid1 + oid1_suffix;
		}

		String oid2 = null;
		if ((tagBits & FILEMAP_ENTRY_OID2) != 0) {
			oid2 = reads();
			if ((tagBits & FILEMAP_ENTRY_OID2_COMPLETE) == 0) { // not complete
				if (oid2_prefix != null)
					oid2 = oid2_prefix + oid2;
				if (oid2_suffix != null)
					oid2 = oid2 + oid2_suffix;
			}
		} else {
			oid2 = oid;
		}

		fileMap.put(filename, oid2 == null ? oid1 : oid1 + DELIM + oid2);

		skipTagBitData();

		// optionally read the file map entry end flag
		if (tagHasEndFlag)
			readTag();

		return true;
	}

	public void readEDP(URL u, ArrayList onlySections) throws Exception {
		URLConnection urcl = u.openConnection();
		if (urcl instanceof HttpURLConnection) {
			if (((HttpURLConnection) urcl).getResponseCode() != HttpURLConnection.HTTP_OK) {
				logger.debug(">> Error retrieving URL    : " + urcl.getURL());
				logger.debug(">> HTTP response code is   : " + ((HttpURLConnection) urcl).getResponseCode());
				logger.debug(">> HTTP response message is: " + ((HttpURLConnection) urcl).getResponseMessage());
				throw new IOException("failed to read url");
			}
		}
		InputStream is = urcl.getInputStream();
		readEDP(null, is, onlySections);
		try {
			is.close();
		} catch (Exception e) {
		}
	}

	public void readEDP(InputStream is, ArrayList onlySections) throws Exception {
		readEDP(null, is, onlySections);
	}

	public void readEDP(File edpF, ArrayList onlySections) throws Exception {
		readEDP(edpF, null, onlySections);
	}

	@SuppressWarnings("unchecked") // unchecked call to add and put
	private void readEDP(File edpFilename, InputStream is, ArrayList onlySections) throws Exception {
		// check compList is populated, if not populate from rootComp
		if (compList == null || compList.size() == 0) {
			if (rootComp == null)
				throw new IOException("readEDP: no compList or rootComp set");
			populateCompList();
		}

		if (edpFilename != null)
			is = new FileInputStream(edpFilename);
		bi = new BufferedInputStream(is, BUFFER_SIZE_4096);
		input = bi;
		symbols = null;

		readFileIdHeader(edpFilename, EDP_FILE_ID_STR);
		int firstSectionOffset = read4();
		final boolean trace = logger.isTraceEnabled();
		if (trace)
			logger.trace("firstSectionOffset = " + firstSectionOffset);

		int numIntSections = read2();
		int numExtSections = read2();
		if (trace)
			logger.trace("numIntSections = " + numIntSections + " numExtSections = " + numExtSections);

		IndexEntry[] intIndex = new IndexEntry[numIntSections];
		IndexEntry[] extIndex = numExtSections == 0 ? null : new IndexEntry[numExtSections];

		for (int i = 0; i < numIntSections; i++) {
			intIndex[i] = new IndexEntry(true);
			intIndex[i].readIndex();
		}

		for (int i = 0; i < numExtSections; i++) {
			extIndex[i] = new IndexEntry(false);
			extIndex[i].readIndex();
		}

		for (int i = 0; i < numIntSections; i++)
			readSection(intIndex[i], true, onlySections);

		// free off the symbols array
		symbols = null;
		if (edpFilename != null)
			is.close();

		// read external properties sections
		ArrayList sectionList = new ArrayList(2);
		HashMap h = new HashMap(2);
		for (int i = 0; i < numExtSections; i++) {
			if (extIndex[i].type == SECTION_TYPE_PROPERTIES
					&& (onlySections == null || onlySections.contains(extIndex[i].description))) {
				ArrayList d = (ArrayList) h.get(extIndex[i].filename);
				if (d == null) {
					d = new ArrayList(2);
					h.put(extIndex[i].filename, d);
				}
				d.add(extIndex[i].description);
				sectionList.add(extIndex[i].description);
			}
		}

		for (Iterator it = h.entrySet().iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			String file = (String) entry.getKey();
			ArrayList thisOnlySections = (ArrayList) entry.getValue();
			if (trace)
				logger.trace("Read External properties from " + file + " number of sections to read "
						+ thisOnlySections.size());

			InputStream edps = getEDPInputStreamFromMap(file);
			if (edps == null) {
				readEDP(edpFilename == null ? new File(file) : new File(edpFilename.getParentFile(), file),
						thisOnlySections);
			} else {
				readEDP(edps, thisOnlySections);
			}
		}

		// remove from passed in onlySections, any that have been removed from the
		// various passed out onlySections
		if (onlySections != null) {
			ArrayList sectionsLeft = new ArrayList(2);
			for (Iterator it = h.values().iterator(); it.hasNext();) {
				ArrayList l = (ArrayList) it.next();
				for (Iterator it1 = l.iterator(); it1.hasNext();) {
					sectionsLeft.add(it1.next());
				}
			}

			for (Iterator it = sectionList.iterator(); it.hasNext();) {
				String s = (String) it.next();
				if (!sectionsLeft.contains(s))
					onlySections.remove(s);
			}
		}
	}

	private InputStream getEDPInputStreamFromMap(String file) throws Exception {
		if (edpInputStreamMap == null)
			return null;
		Object o = edpInputStreamMap.get(file);
		if (o instanceof InputStream) {
			return (InputStream) o;
		} else if (o instanceof URL) {
			URLConnection urcl = ((URL) o).openConnection();
			if (urcl instanceof HttpURLConnection) {
				if (((HttpURLConnection) urcl).getResponseCode() != HttpURLConnection.HTTP_OK) {
					logger.debug(">> Error retrieving URL    : " + urcl.getURL());
					logger.debug(">> HTTP response code is   : " + ((HttpURLConnection) urcl).getResponseCode());
					logger.debug(">> HTTP response message is: " + ((HttpURLConnection) urcl).getResponseMessage());
					throw new IOException("failed to read url");
				}
			}
			InputStream is = urcl.getInputStream();
			if (is != null) {
				edpInputStreamMap.put(file, is);
				return is;
			}
		}

		return null;
	}

	private OutputStream getEDPOutputStreamFromMap(String file) throws Exception {
		if (edpOutputStreamMap == null)
			return null;
		Object o = edpOutputStreamMap.get(file);
		if (o instanceof OutputStream) {
			return (OutputStream) o;
		}

		return null;
	}

	public void readEDM(URL u) throws Exception {
		URLConnection urcl = u.openConnection();
		if (urcl instanceof HttpURLConnection) {
			if (((HttpURLConnection) urcl).getResponseCode() != HttpURLConnection.HTTP_OK) {
				logger.debug(">> Error retrieving URL    : " + urcl.getURL());
				logger.debug(">> HTTP response code is   : " + ((HttpURLConnection) urcl).getResponseCode());
				logger.debug(">> HTTP response message is: " + ((HttpURLConnection) urcl).getResponseMessage());
				throw new IOException("failed to read url");
			}
		}
		InputStream is = urcl.getInputStream();
		readEDM(null, is);
		try {
			is.close();
		} catch (Exception e) {
		}
	}

	public void readEDM(InputStream is) throws Exception {
		readEDM(null, is);
	}

	public void readEDM(File edmF) throws Exception {
		readEDM(edmF, null);
	}

	private void readEDM(File edmFilename, InputStream is) throws Exception {
		if (edmFilename != null)
			is = new FileInputStream(edmFilename);
		bi = new BufferedInputStream(is, BUFFER_SIZE_4096);
		input = bi;
		symbols = null;

		readFileIdHeader(edmFilename, EDM_FILE_ID_STR);
		int firstSectionOffset = read4();
		final boolean trace = logger.isTraceEnabled();
		if (trace)
			logger.trace("firstSectionOffset = " + firstSectionOffset);

		int numIntSections = read2();
		int numExtSections = read2();
		if (trace)
			logger.trace("numIntSections = " + numIntSections + " numExtSections = " + numExtSections);

		IndexEntry[] intIndex = new IndexEntry[numIntSections];
		IndexEntry[] extIndex = numExtSections == 0 ? null : new IndexEntry[numExtSections];

		for (int i = 0; i < numIntSections; i++) {
			intIndex[i] = new IndexEntry(true);
			intIndex[i].readIndex();
		}

		for (int i = 0; i < numExtSections; i++) {
			extIndex[i] = new IndexEntry(false);
			extIndex[i].readIndex();
		}

		for (int i = 0; i < numIntSections; i++)
			readSection(intIndex[i], false, null);

		// free off the symbols array
		symbols = null;
		if (edmFilename != null || isCloseInputStreams())
			is.close();

		// read external file map sections
		for (int i = 0; i < numExtSections; i++) {
			if (extIndex[i].type == SECTION_TYPE_FILEMAP) {
				String file = extIndex[i].filename;
				readEDM(edmFilename == null ? new File(file) : new File(edmFilename.getParentFile(), file));
			}
		}
	}

	private void readTag() throws Exception {
		int tag = read2();

		if (tag == GEN_END_TAG) {
			tagTag = 0;
			tagSize = 0;
			tagBits = 0;
			tagHasEndFlag = false;
			tagReadCount = 0;
			return;
		}

		tagTag = tag & GEN_TAG_MASK;
		int sizeBytes = ((tag >> GEN_TAG_SIZE_SIZE_SHIFT) & 0x7);
		int bitsBytes = ((tag >> GEN_TAG_BITS_SIZE_SHIFT) & 0x7);
		tagHasEndFlag = ((tag & GEN_TAG_END_FLAG) != 0);

		// read size, in variable number of bytes
		int size = 0;
		for (int i = 0, shift = 0; i < sizeBytes; i++, shift += 8) {
			int b = read1();
			if (shift == 0) {
				size = b;
			} else {
				size |= (b << shift);
			}
		}
		tagSize = size;

		// read bits, in variable number of bytes
		int bits = 0;
		for (int i = 0, shift = 0; i < bitsBytes; i++, shift += 8) {
			int b = read1();
			if (shift == 0) {
				bits = b;
			} else {
				bits |= (b << shift);
			}
		}
		tagBits = bits;
		tagReadCount = 0;
	}

	private void skipTagBitData() throws Exception {
		if (tagSize != tagReadCount) {
			if (logger.isTraceEnabled()) {
				logger.trace("skipTagBitData: tagSize " + tagSize + "  tagReadCount " + tagReadCount);
			}
		}
		if (tagSize > tagReadCount)
			skip(tagSize - tagReadCount);
	}

	private void skipObject() throws Exception {
		skipTagBitData();

		if (tagHasEndFlag) {
			// while(true) {
			// readTag();
			// if( tagTag == GEN_END_TAG ) break;
			// skipObject();
			// }
			readTag();
		}
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////// writing of ed2 files
	/////////////////////////////////////////////////////////////////////////////////////////////////// //////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////

	public void writeEDUsingEDPMap(File edF) throws Exception {
		addWCEDPMap(null);
		writeED(edF, null, new File(""), null);
	}

	public void writeEDUsingEDPMap(OutputStream os) throws Exception {
		addWCEDPMap(null);
		writeED(null, os, new File(""), null);
	}

	public void writeED(OutputStream os) throws Exception {
		writeED(null, os, null, null);
	}

	public void writeED(File edF) throws Exception {
		writeED(edF, null, null, null);
	}

	public void writeED(OutputStream os, File edpF) throws Exception {
		writeED(null, os, edpF, null);
	}

	public void writeED(File edF, File edpF) throws Exception {
		writeED(edF, null, edpF, null);
	}

	public void writeED(OutputStream os, File edpF, OutputStream edpStream) throws Exception {
		writeED(null, os, edpF, edpStream);
	}

	public void writeED(File edF, File edpF, OutputStream edpStream) throws Exception {
		writeED(edF, null, edpF, edpStream);
	}

	private void ensureFileVersionForOrientationWriting(boolean traceLogEnabled) {
		if (writesFloatOrientation()) {
			if (traceLogEnabled && (getMajorVersion() != MAX_FLOAT_ORIENTATION_MAJOR_VERSION
					|| getMinorVersion() != MAX_FLOAT_ORIENTATION_MINOR_VERSION)) {
				logger.trace("File version will be reset for writing single "
						+ "precision orientation. Original version is: " + getFullVersionString());
			}
			setMajorVersion(MAX_FLOAT_ORIENTATION_MAJOR_VERSION);
			setMinorVersion(MAX_FLOAT_ORIENTATION_MINOR_VERSION);
		} else if (!isDoublePrecisionOrientation()) {
			if (traceLogEnabled) {
				logger.trace("File version will be reset for writing double "
						+ "precision orientation. Original version is: " + getFullVersionString());
			}
			setMajorVersion(MAJOR_VERSION);
			setMinorVersion(MINOR_VERSION);
		}
	}

	@SuppressWarnings("unchecked") // unchecked call to add and put
	private void writeED(File edFilename, OutputStream os, File edpFilename, OutputStream edpStream) throws Exception {
		if (rootComp == null)
			throw new IOException("Write ED2 Structure: root component is null");
		populateCompList(); // also populates appearanceList & appearanceMap

		initWriteBuffers();
		if (edFilename != null)
			os = new FileOutputStream(edFilename);
		bo = new BufferedOutputStream(os, BUFFER_SIZE_4096);
		output = bo;
		symbols = null;
		symbolList = new ArrayList<String>(100);
		symbolMap = new HashMap(100);

		final boolean trace = logger.isTraceEnabled();
		ensureFileVersionForOrientationWriting(trace);
		String fileIdHeader = ED2_FILE_ID_STR + getFullVersionString();
		if (trace) {
			logger.trace("Write ED2 " + (edFilename == null ? "-" : edFilename.toString()) + " (" + fileIdHeader + ")");
		}
		write(fileIdHeader.getBytes(UTF8), 0, ED2_FILE_ID_LEN);

		boolean needSymbolSection = false;
		boolean needFileMapSection = false;
		int numIntSections = 1; // structure for certain
		int numExtSections = 0;

		// find out how many property group section there are in the data
		ArrayList propSections = writeIncludeProperties ? findPropertySections() : new ArrayList(1);
		int intProps = 0;
		if (edpFilename != null) {
			for (Iterator it = propSections.iterator(); it.hasNext();) {
				String f = edpMap.get(it.next());
				if (f != null && f.equals(EDP_IN_ED))
					intProps++;
			}

			if (isWriteStructureAsSymbols() || (isWritePropertiesAsSymbols() && intProps > 0))
				needSymbolSection = true;
			numExtSections += (propSections.size() - intProps);
			numIntSections += intProps;
		} else {
			if (isWriteStructureAsSymbols() || (isWritePropertiesAsSymbols() && propSections.size() > 0))
				needSymbolSection = true;
			numIntSections += propSections.size();
		}

		if (isWriteIncludeFileMap() && fileMap != null && fileMap.size() > 0)
			needFileMapSection = true;

		if (needSymbolSection)
			numIntSections++;
		if (needFileMapSection)
			numIntSections++;

		IndexEntry[] intIndex = new IndexEntry[numIntSections];
		IndexEntry[] extIndex = numExtSections == 0 ? null : new IndexEntry[numExtSections];
		int section = 0;
		if (needSymbolSection)
			intIndex[section++] = new IndexEntry(SECTION_TYPE_SYMBOLS, compressionType, null);
		intIndex[section++] = new IndexEntry(SECTION_TYPE_STRUCTURE, compressionType, null);

		if (edpFilename != null) {
			int extsection = 0;
			for (Iterator it = propSections.iterator(); it.hasNext();) {
				String sect = (String) it.next();
				String f = edpFilename.getName();
				if (f == null || f.length() == 0) {
					f = edpMap.get(sect);
					if (f == null) {
						if (sect == null) {
							f = edFilename == null ? "properties." + EDP : setExtension(edFilename.getName(), EDP);
						} else {
							f = edFilename == null ? sect + "." + EDP : setExtension(edFilename.getName(), EDP);
						}
					}
				}

				if (f.equals(EDP_IN_ED)) {
					intIndex[section++] = new IndexEntry(SECTION_TYPE_PROPERTIES, compressionType, sect);
				} else {
					extIndex[extsection++] = new IndexEntry(SECTION_TYPE_PROPERTIES, sect, f);
				}
			}
		} else {
			for (Iterator it = propSections.iterator(); it.hasNext();) {
				intIndex[section++] = new IndexEntry(SECTION_TYPE_PROPERTIES, compressionType, (String) it.next());
			}
		}

		if (needFileMapSection)
			intIndex[section++] = new IndexEntry(SECTION_TYPE_FILEMAP, compressionType, null);

		// compute the size of the index section of the ed2 file
		int firstSectionOffset = 18; // number of bytes in header plus 4 for 2x number of sections
		for (int i = 0; i < numIntSections; i++)
			firstSectionOffset += intIndex[i].indexSize();
		for (int i = 0; i < numExtSections; i++)
			firstSectionOffset += extIndex[i].indexSize();

		write4(firstSectionOffset);
		if (trace)
			logger.trace("firstSectionOffset = " + firstSectionOffset);

		// start writing index section
		write2(numIntSections);
		write2(numExtSections);
		if (trace)
			logger.trace("numIntSections = " + numIntSections + " numExtSections = " + numExtSections);

		// process the data into the byte arrays
		for (int i = 0; i < numIntSections; i++) {
			if (intIndex[i].type != SECTION_TYPE_SYMBOLS)
				writeSection(intIndex[i]);
		}
		for (int i = 0; i < numIntSections; i++) {
			if (intIndex[i].type == SECTION_TYPE_SYMBOLS)
				writeSection(intIndex[i]);
		}

		// write out the index for each section to actual output file
		for (int i = 0; i < numIntSections; i++)
			intIndex[i].writeIndex();
		for (int i = 0; i < numExtSections; i++)
			extIndex[i].writeIndex();

		// add the internal section byte arrays to the actual output file
		for (int i = 0; i < numIntSections; i++)
			intIndex[i].writeData();

		appearanceList = null;
		appearanceMap = null;
		bo.flush();
		if (edFilename != null || isCloseOutputStreams())
			os.close();

		// write external properties sections
		writeFileNameEDPMap.clear();
		for (int i = 0; i < numExtSections; i++) {
			if (extIndex[i].type == SECTION_TYPE_PROPERTIES) {
				ArrayList d = (ArrayList) writeFileNameEDPMap.get(extIndex[i].filename);
				if (d == null) {
					d = new ArrayList(2);
					writeFileNameEDPMap.put(extIndex[i].filename, d);
				}
				d.add(extIndex[i].description);
			}
		}

		if (writeExternalEDPFiles) {
			for (Iterator it = writeFileNameEDPMap.entrySet().iterator(); it.hasNext();) {
				Map.Entry entry = (Map.Entry) it.next();
				String file = (String) entry.getKey();
				ArrayList onlySections = (ArrayList) entry.getValue();
				if (trace)
					logger.trace("Write External properties for " + file + " sections to write " + onlySections);

				if (edpStream == null) {
					OutputStream edps = getEDPOutputStreamFromMap(file);
					if (edps == null) {
						writeEDP(edFilename == null
								? (getEDPWriteDir() == null ? new File(file) : new File(getEDPWriteDir(), file))
								: new File(edFilename.getParentFile(), file), onlySections);
					} else {
						writeEDP(edps, onlySections);
					}
				} else {
					writeEDP(edpStream, onlySections);
				}
			}
		}

		if (isCloseOutputStreams() && edpStream != null)
			edpStream.close();

		if (isCloseOutputStreams() && edpOutputStreamMap != null) {
			for (Iterator it = edpOutputStreamMap.values().iterator(); it.hasNext();) {
				Object o = it.next();
				if (o instanceof OutputStream)
					((OutputStream) o).close();
			}
		}
	}

	/**
	 * Set extension for a filename.
	 *
	 * <pre>
	 *   Example Usage:
	 *    FileUtil.removeExtension( "C:\myFile", "txt" )  // returns "C:\myFile.txt"
	 *
	 * </pre>
	 * 
	 * <BR>
	 * <BR>
	 * <B>Supported API: </B>false
	 *
	 * @param filename
	 *            name of file
	 * @param extension
	 *            extension to add to file
	 *
	 * @return filename with extension added
	 **/

	public static String setExtension(String filename, String extension) {
		String currentExtension = getExtension(filename);
		String newFileName;
		if (currentExtension.equals("")) {
			newFileName = filename + "." + extension;
		} else {
			if (extension.equals("")) {
				newFileName = filename.substring(0, filename.length() - currentExtension.length() - 1);
			} else {
				newFileName = filename.substring(0, filename.length() - currentExtension.length()) + extension;
			}
		}

		return newFileName;
	}

	/**
	 * Get extension from a filename.
	 *
	 * <pre>
	 *   Example Usage:
	 *    FileUtil.removeExtension( "C:\myFile.txt" )  // returns ".txt"
	 *
	 * </pre>
	 * 
	 * @param String
	 *            - name of file
	 *
	 * @return extension of filename
	 **/

	public static String getExtension(String filename) { // NOTE: could call WTStringUtilites.tail()
		for (int i = filename.length() - 1; i >= 0; i--) {
			if (filename.charAt(i) == '.') {
				String ext = filename.substring(i + 1, filename.length());
				return ext;
			}
		}
		return new String("");
	}

	private void writeSection(IndexEntry index) throws Exception {
		startWriteSection(index);

		switch (index.type) {
		case SECTION_TYPE_STRUCTURE:
			usingStringsAsSymbols = writeStructureAsSymbols;
			writeStructureSection();
			usingStringsAsSymbols = false;
			break;
		case SECTION_TYPE_PROPERTIES:
			usingStringsAsSymbols = writePropertiesAsSymbols;
			writePropertiesSection(index.description);
			usingStringsAsSymbols = false;
			break;
		case SECTION_TYPE_SYMBOLS:
			writeSymbolTableSection();
			break;
		case SECTION_TYPE_FILEMAP:
			usingStringsAsSymbols = writeFileMapAsSymbols;
			writeFileMapSection();
			usingStringsAsSymbols = false;
			break;
		case SECTION_TYPE_VIEWSTATE:
			throw new IOException("VIEWSTATE sections are not supported in PVS/PVP/PVM files");
		default:
			throw new IOException("Unknown PVS/PVS/PVM internal file section type " + index.type);
		}

		endWriteSection(index);
	}

	private void startWriteSection(IndexEntry index) throws Exception {
		index.byte_stream = new ByteArrayOutputStream(BUFFER_SIZE_4096);

		try {
			if (zo != null)
				zo.close();
		} catch (Exception e) {
		}
		zo = null;

		if ((index.compression & 0xff) == 1) {
			zo = new BufferedOutputStream(new GZIPOutputStream(index.byte_stream), BUFFER_SIZE_4096);
		} else if ((index.compression & 0xff) == 2) {
			logger.debug("BZIP Compression not supported");
			throw new IOException("BZIP Compression not supported");
		} else if ((index.compression & 0xff) == 0) {
			zo = index.byte_stream;
		} else {
			throw new IOException("invalid compresson type ");
		}

		output = zo;
	}

	private void endWriteSection(IndexEntry index) throws Exception {
		try {
			if (zo != null)
				zo.close();
		} catch (Exception e) {
		}
		zo = null;
		output = bo;
	}

	private void setOutput(OutputStream a) {
		output = a;
	}

	private void setOutput() {
		output = zo != null ? zo : bo;
	}

	private OutputStream getOutput() {
		return output;
	}

	private void writeStructureSection() throws Exception {
		writeTag(SECTION_TYPE_STRUCTURE, 0, 0, true);

		writeAppearanceTable();

		int i = 0;
		for (Iterator it = compList.iterator(); it.hasNext();)
			writeComponent((Comp) it.next(), i++);

		writeTag(GEN_END_TAG, 0, 0, false);
	}

	private void writeAppearanceTable() throws Exception {
		if (appearanceList == null || appearanceList.size() == 0)
			return;

		writeTag(ED_APPEARANCE_TABLE_TAG, 0, 0, true);

		for (Iterator it = appearanceList.iterator(); it.hasNext();)
			writeAppearance((Appearance) it.next());

		writeTag(GEN_END_TAG, 0, 0, false);
	}

	private void writeAppearance(Appearance app) throws Exception {
		int bits = 0;
		ByteArrayOutputStream byteStream = newByteArrayOutputStream(BYTE_ARRAY_SIZE_32);
		setOutput(byteStream);

		if (app.base_color != null) {
			for (int i = 0; i < 3; i++)
				writef(app.base_color[i]);
			bits |= APPEARANCE_BASE_COLOR;
		}
		if (app.ambient_color != null) {
			for (int i = 0; i < 3; i++)
				writef(app.ambient_color[i]);
			bits |= APPEARANCE_AMBIENT_COLOR;
		}
		if (app.diffuse_color != null) {
			for (int i = 0; i < 3; i++)
				writef(app.diffuse_color[i]);
			bits |= APPEARANCE_DIFFUSE_COLOR;
		}
		if (app.emissive_color != null) {
			for (int i = 0; i < 3; i++)
				writef(app.emissive_color[i]);
			bits |= APPEARANCE_EMISSIVE_COLOR;
		}
		if (app.specular != null) {
			for (int i = 0; i < 4; i++)
				writef(app.specular[i]);
			bits |= APPEARANCE_SPECULAR;
		}
		if (app.opacity != null) {
			writef(app.opacity[0]);
			bits |= APPEARANCE_OPACITY;
		}

		setOutput();
		writeTag(ED_APPEARANCE_TAG, byteStream.size(), bits, writeEndTag);
		byteStream.writeTo(getOutput());

		if (writeEndTag)
			writeTag(GEN_END_TAG, 0, 0, false);
	}

	private void writeComponent(Comp comp, int idx) throws Exception {
		int bits = 0;
		ByteArrayOutputStream byteStream = newByteArrayOutputStream(BYTE_ARRAY_SIZE_64);
		setOutput(byteStream);

		comp.writeIdx = idx;
		writes((comp.name == null || comp.name.length() == 0) ? COMP_NO_NAME : comp.name);

		if (comp.isProxy()) {
			writes(comp.filename);
			if (comp.type != 0) {
				writec(comp.type);
				bits |= COMPONENT_TYPE;
			}
			if (comp.map_filename != null) {
				writes(comp.map_filename);
				bits |= COMPONENT_PROXY_MAP_FILENAME;
			}
			if (comp.wvs_info != null) {
				writes(comp.wvs_info);
				bits |= COMPONENT_PROXY_WVS_INFO;
			}
		} else {
			if (comp.type != 0) {
				writec(comp.type);
				bits |= COMPONENT_TYPE;
			}
			if (comp.modelUnitLength != 0) {
				writec(comp.modelUnitLength);
				bits |= COMPONENT_MODEL_UNIT_LENGTH;
			}
			if (comp.displayUnitLength != 0) {
				writec(comp.displayUnitLength);
				bits |= COMPONENT_DISPLAY_UNIT_LENGTH;
			}
			if (comp.modelUnitMass != 0) {
				writec(comp.modelUnitMass);
				bits |= COMPONENT_MODEL_UNIT_MASS;
			}
			if (comp.displayUnitMass != 0) {
				writec(comp.displayUnitMass);
				bits |= COMPONENT_DISPLAY_UNIT_MASS;
			}
			if (comp.defaultAppearance != null) {
				Object obj = appearanceMap.get(comp.defaultAppearance);
				if (obj == null)
					throw new IOException("writeComponent: appearance missing from table");
				writec(((Integer) obj).intValue());
				bits |= COMPONENT_DEFAULT_APPEARANCE;
			}
		}

		setOutput();
		writeTag(comp.isProxy() ? ED_COMPONENT_PROXY_TAG : ED_COMPONENT_TAG, byteStream.size(), bits, true);
		byteStream.writeTo(getOutput());
		byteStream = null;

		if (comp.shape != null)
			writeShape(comp);
		if (comp.thumbnail3d != null)
			writeThumbnail3D(comp);

		if (comp.viewableList != null) {
			for (Iterator it = comp.viewableList.iterator(); it.hasNext();) {
				writeViewable((CViewable) it.next());
			}
		}

		if (comp.viewList != null) {
			for (Iterator it = comp.viewList.iterator(); it.hasNext();) {
				writeView((CView) it.next());
			}
		}

		if (comp.viewStateList != null) {
			for (Iterator it = comp.viewStateList.iterator(); it.hasNext();) {
				writeViewState((CViewState) it.next());
			}
		}

		if (comp.instanceAppearanceOverideList != null) {
			for (Iterator it = comp.instanceAppearanceOverideList.iterator(); it.hasNext();) {
				writeInstanceAppearanceOveride((InstAppearance) it.next());
			}
		}

		comp = sortChildren(comp);

		for (Iterator<CompInst> it = comp.children(); it.hasNext();) {
			writeComponentInstance(it.next());
		}

		writeTag(GEN_END_TAG, 0, 0, false);
	}

	public Comp sortChildren(Comp comp) {
		// if required sort the children
		if (compInstComparator != null) {
			if (comp.childInsts.size() > 1) {
				Collections.sort(comp.childInsts, compInstComparator);
			}
		}
		return comp;
	}

	private void writeShape(Comp comp) throws Exception {
		int bits = 0;
		ByteArrayOutputStream byteStream = newByteArrayOutputStream(BYTE_ARRAY_SIZE_64);
		setOutput(byteStream);

		writes(comp.shape);
		if (comp.bbox != null) {
			for (int i = 0; i < 6; i++)
				writef(comp.bbox[i]);
			bits |= SHAPE_BBOX;
		}
		if (comp.shape_index != -1) {
			writec(comp.shape_index);
			bits |= SHAPE_INDEX;
		}

		setOutput();
		writeTag(ED_SHAPE_TAG, byteStream.size(), bits, writeEndTag);
		byteStream.writeTo(getOutput());

		if (writeEndTag)
			writeTag(GEN_END_TAG, 0, 0, false);
	}

	private void writeThumbnail3D(Comp comp) throws Exception {
		int bits = 0;
		ByteArrayOutputStream byteStream = newByteArrayOutputStream(BYTE_ARRAY_SIZE_32);
		setOutput(byteStream);

		writes(comp.thumbnail3d);
		if (comp.thumbnail3d_index != -1) {
			writec(comp.thumbnail3d_index);
			bits |= THUMBNAIL_INDEX;
		}

		setOutput();
		writeTag(ED_THUMBNAIL_3D_TAG, byteStream.size(), bits, writeEndTag);
		byteStream.writeTo(getOutput());

		if (writeEndTag)
			writeTag(GEN_END_TAG, 0, 0, false);
	}

	private void writeViewable(CViewable att) throws Exception {
		int bits = 0;
		ByteArrayOutputStream byteStream = newByteArrayOutputStream(BYTE_ARRAY_SIZE_32);
		setOutput(byteStream);

		boolean hasDrawingSheets = false;
		writes(att.filename);
		if (att.displayname != null) {
			writes(att.displayname);
			bits |= VIEWABLE_DISPLAY_NAME;
		}
		if (att.type != 0) {
			writec(att.type);
			bits |= VIEWABLE_TYPE;
		}
		if (att.viewable_type == ED_ECAD_TAG) {
			if (att.ecad_type != 0) {
				writec(att.ecad_type);
				bits |= VIEWABLE_ECAD_TYPE;
			}
		} else if (att.viewable_type == ED_DRAWING_TAG) {
			if (att.hasPreviousSheetFile()) {
				bits |= VIEWABLE_DRAWING_SHEET_OF_PREVIOUS_TYPE;
			}
			if (att.hasContinuationSheetFile()) {
				bits |= VIEWABLE_DRAWING_NEXT_DRAWING_IS_CONTINUATION;
			}
			hasDrawingSheets = att.hasDrawingSheets();
		}

		setOutput();
		writeTag(att.viewable_type, byteStream.size(), bits, hasDrawingSheets);
		byteStream.writeTo(getOutput());

		if (hasDrawingSheets) {
			byteStream = null;

			for (Iterator it = att.drawingSheets(); it.hasNext();) {
				writeDrawingSheet((CDrawingSheet) it.next());
			}

			writeTag(GEN_END_TAG, 0, 0, false);
		}
	}

	private void writeDrawingSheet(CDrawingSheet ds) throws Exception {
		int bits = 0;
		ByteArrayOutputStream byteStream = newByteArrayOutputStream(BYTE_ARRAY_SIZE_32);
		setOutput(byteStream);

		if (ds.size != 0) {
			writec(ds.size);
			bits |= DRAWING_SHEET_SIZE;
		}
		if (ds.size == 1) {
			writec(ds.units);
			writef(ds.width);
			writef(ds.height);
		}
		if (ds.orientation != 0) {
			writec(ds.orientation);
			bits |= DRAWING_SHEET_ORIENTATION;
		}
		if (ds.name != null) {
			writes(ds.name);
			bits |= DRAWING_SHEET_NAME;
		}

		setOutput();
		writeTag(ED_DRAWING_SHEET_TAG, byteStream.size(), bits, writeEndTag);
		byteStream.writeTo(getOutput());

		if (writeEndTag)
			writeTag(GEN_END_TAG, 0, 0, false);
	}

	private void writeView(CView v) throws Exception {
		int bits = 0;
		ByteArrayOutputStream byteStream = newByteArrayOutputStream(BYTE_ARRAY_SIZE_32);
		setOutput(byteStream);

		writes((v.name == null || v.name.length() == 0) ? COMP_NO_NAME : v.name);
		if (v.orientation != null && v.orientation.length == 4) {
			for (int i = 0; i < 4; i++)
				writef(v.orientation[i]);
			bits |= VIEW_ORIENTATION;
		}

		setOutput();
		writeTag(ED_VIEW_TAG, byteStream.size(), bits, writeEndTag);
		byteStream.writeTo(getOutput());

		if (writeEndTag)
			writeTag(GEN_END_TAG, 0, 0, false);
	}

	private void writeViewState(CViewState vs) throws Exception {
		int bits = 0;
		ByteArrayOutputStream byteStream = newByteArrayOutputStream(BYTE_ARRAY_SIZE_32);
		setOutput(byteStream);

		writes(vs.name);
		if (vs.filename != null) {
			writes(vs.filename);
			bits |= VIEWSTATE_FILENAME;
		}
		if (vs.index != 0) {
			writec(vs.index);
			bits |= VIEWSTATE_INDEX;
		}

		setOutput();
		writeTag(vs.viewstate_type, byteStream.size(), bits, writeEndTag);
		byteStream.writeTo(getOutput());

		if (writeEndTag)
			writeTag(GEN_END_TAG, 0, 0, false);
	}

	private void writeComponentInstance(CompInst compInst) throws Exception {
		int bits = 0;
		ByteArrayOutputStream byteStream = newByteArrayOutputStream(BYTE_ARRAY_SIZE_128);
		setOutput(byteStream);

		writec(compInst.child.writeIdx); // compressed int
		if (compInst.type != 0) {
			writec(compInst.type);
			bits |= COMPONENT_INSTANCE_RELATIONSHIP;
		}
		if (compInst.translation != null) {
			for (int i = 0; i < 3; i++)
				writed(compInst.translation[i]);
			bits |= COMPONENT_INSTANCE_TRANSLATION;
		}
		if (compInst.orientation != null) {
			if (isDoublePrecisionOrientation()) {
				for (int i = 0; i < 9; i++)
					writed(compInst.orientation[i]);
			} else {
				for (int i = 0; i < 9; i++)
					writef((float) compInst.orientation[i]);
				if (logger.isTraceEnabled()) {
					logger.trace("Wrote single precision orientation for " + " Component Instance " + compInst.id
							+ " - " + compInst.child.name + " - in " + compInst.parent.name);
				}
			}
			bits |= COMPONENT_INSTANCE_ORIENTATION;
		}
		if (compInst.id != null) {
			writes(compInst.id);
			bits |= COMPONENT_INSTANCE_ID;
		}
		if (compInst.name != null) {
			writes(compInst.name);
			bits |= COMPONENT_INSTANCE_NAME;
		}
		if (compInst.hideSelf) {
			bits |= COMPONENT_INSTANCE_HIDE_SELF;
		}
		if (compInst.hideDescendants) {
			bits |= COMPONENT_INSTANCE_HIDE_DESCENDANTS;
		}
		if (compInst.combine) {
			bits |= COMPONENT_INSTANCE_COMBINE;
		}

		boolean hasAppearance = compInst.appearanceOveride != null || (compInst.instanceAppearanceOverideList != null
				&& compInst.instanceAppearanceOverideList.size() > 0);

		setOutput();
		writeTag(ED_COMPONENT_INSTANCE_TAG, byteStream.size(), bits, hasAppearance);
		byteStream.writeTo(getOutput());
		byteStream = null;

		if (compInst.appearanceOveride != null) {
			writeAppearanceOveride(compInst.appearanceOveride, compInst.appearanceOverideSelf,
					compInst.appearanceOverideDescendants);
		}

		if (compInst.instanceAppearanceOverideList != null) {
			for (Iterator it = compInst.instanceAppearanceOverideList.iterator(); it.hasNext();) {
				writeInstanceAppearanceOveride((InstAppearance) it.next());
			}
		}

		if (hasAppearance)
			writeTag(GEN_END_TAG, 0, 0, false);
	}

	private void writeAppearanceOveride(Appearance app, boolean self, boolean descendants) throws Exception {
		int bits = 0;
		ByteArrayOutputStream byteStream = newByteArrayOutputStream(BYTE_ARRAY_SIZE_32);
		setOutput(byteStream);

		Object obj = appearanceMap.get(app);
		if (obj == null)
			throw new IOException("writeAppearanceOveride: appearance missing from table");
		writec(((Integer) obj).intValue());
		if (self == false) {
			bits |= APPEARANCE_OVERIDE_SELF;
		}
		if (descendants == false) {
			bits |= APPEARANCE_OVERIDE_DESCENDANTS;
		}

		setOutput();
		writeTag(ED_APPEARANCE_OVERIDE_TAG, byteStream.size(), bits, writeEndTag);
		byteStream.writeTo(getOutput());

		if (writeEndTag)
			writeTag(GEN_END_TAG, 0, 0, false);
	}

	private void writeInstanceAppearanceOveride(InstAppearance instApp) throws Exception {
		int bits = 0;
		ByteArrayOutputStream byteStream = newByteArrayOutputStream(BYTE_ARRAY_SIZE_64);
		setOutput(byteStream);

		Object obj = appearanceMap.get(instApp.appearance);
		if (obj == null)
			throw new IOException("writeInstanceAppearanceOveride: appearance missing from table");
		writec(((Integer) obj).intValue());
		if (instApp.recurse == false) {
			bits |= INSTANCE_APPEARANCE_OVERIDE_RECURSE;
		}
		if (instApp.instancePath != null) {
			writes(instApp.instancePath);
			bits |= INSTANCE_APPEARANCE_OVERIDE_PATH;
		}

		if (instApp.faceids != null) {
			bits |= INSTANCE_APPEARANCE_OVERIDE_FACEIDS;
			int c = instApp.faceids.length;
			writec(c);
			for (int i = 0; i < c; i++)
				writes(instApp.faceids[i]);
		}

		setOutput();
		writeTag(ED_INSTANCE_APPEARANCE_OVERIDE_TAG, byteStream.size(), bits, writeEndTag);
		byteStream.writeTo(getOutput());

		if (writeEndTag)
			writeTag(GEN_END_TAG, 0, 0, false);
	}

	@SuppressWarnings("unchecked") // unchecked call to ArrayList
	public ArrayList<String> findPropertySections() {
		groupCompAndInstMap = new HashMap<String, Set>(20);
		HashSet propSections = new HashSet(20);

		for (Iterator it = compList.iterator(); it.hasNext();) {
			findPropertySections(propSections, (Comp) it.next());
		}

		return new ArrayList(propSections);
	}

	private void findPropertySections(HashSet propSections, Comp comp) {
		if (comp.properties != null) {
			findPropertySections(propSections, comp.properties, comp);
		}

		// look on the child instances
		for (Iterator<CompInst> it = comp.children(); it.hasNext();) {

			CompInst compInst = it.next();

			if (compInst.properties != null) {
				findPropertySections(propSections, compInst.properties, comp);
			}
		}
	}

	@SuppressWarnings("unchecked") // unchecked call to add()
	private void findPropertySections(HashSet propSections, Hashtable properties, Comp comp) {
		Hashtable groupLookup = (Hashtable) properties.get(PROPERTY_GROUP_LOOKUP);

		if (groupLookup == null) {
			for (Iterator it = properties.keySet().iterator(); it.hasNext();) {
				String pname = (String) it.next();
				if (!pname.startsWith(WRITE_SKIP_PREFIX)) {
					propSections.add(null);

					Set cc = groupCompAndInstMap.get(null);
					if (cc == null) {
						cc = new HashSet(100);
						groupCompAndInstMap.put(null, cc);
					}
					cc.add(comp);

					return;
				}
			}
			return;
		}

		for (Iterator it = properties.keySet().iterator(); it.hasNext();) {

			String pname = (String) it.next();
			if (!pname.startsWith(WRITE_SKIP_PREFIX)) {
				String gname = (String) groupLookup.get(pname);
				propSections.add(gname);

				Set cc = groupCompAndInstMap.get(gname);
				if (cc == null) {
					cc = new HashSet(100);
					groupCompAndInstMap.put(gname, cc);
				}

				cc.add(comp);
			}
		}
	}

	private void writePropertiesSection(String groupName) throws Exception {
		writeTag(SECTION_TYPE_PROPERTIES, 0, 0, true);

		// get the comps that they or their child instances have properties in this
		// group
		Set posComps = groupCompAndInstMap.get(groupName);
		if (posComps != null && posComps.size() > 0) {
			int offset = 1;
			for (Iterator it = compList.iterator(); it.hasNext();) {
				Comp comp = (Comp) it.next();

				if (posComps.contains(comp)) {
					offset = writePropertyComponentRef(comp, groupName, offset);
				} else {
					offset++;
				}
			}
		} else {
			logger.error(
					"Write Property Section, no components or instances have properties for group name " + groupName);
		}

		writeTag(GEN_END_TAG, 0, 0, false);
	}

	private int writePropertyComponentRef(Comp comp, String groupName, int offset) throws Exception {
		if (logger.isTraceEnabled())
			logger.trace("Write properties for group \"" + groupName + "\"  comp \"" + comp.name + "\"");
		boolean written_comp = false;

		if (comp.properties != null) {
			Hashtable groupLookup = (Hashtable) comp.properties.get(PROPERTY_GROUP_LOOKUP);

			for (Iterator it = comp.properties.entrySet().iterator(); it.hasNext();) {
				Map.Entry entry = (Map.Entry) it.next();
				String pname = (String) entry.getKey();
				if (!pname.startsWith(WRITE_SKIP_PREFIX)) {
					String group = groupLookup == null ? null : (String) groupLookup.get(pname);
					if ((group == null && groupName == null) || (group != null && group.equals(groupName))) {
						if (!written_comp) {
							int bits = 0;
							ByteArrayOutputStream byteStream = newByteArrayOutputStream(BYTE_ARRAY_SIZE_32);
							setOutput(byteStream);
							if (offset > 1) {
								writec(offset);
								bits |= PROPERTY_COMP_REF_OFFSET;
								offset = 1;
							}
							setOutput();
							writeTag(ED_PROPERTY_COMPONENT_REF_TAG, byteStream.size(), bits, true);
							byteStream.writeTo(getOutput());
							written_comp = true;
						}

						writeProperty(pname, entry.getValue());
					}
				}
			}
		}

		// write any property_instance_ref's from compInst's
		// no nested propery_instance_ref's are currently supported on writing
		// (discarded on reading)

		int inst_offset = 1;
		for (Iterator<CompInst> inst_it = comp.children(); inst_it.hasNext();) {
			CompInst compInst = inst_it.next();
			boolean written_inst = false;

			if (compInst.properties != null) {
				Hashtable groupLookup = (Hashtable) compInst.properties.get(PROPERTY_GROUP_LOOKUP);

				for (Iterator it = compInst.properties.entrySet().iterator(); it.hasNext();) {
					Map.Entry entry = (Map.Entry) it.next();
					String pname = (String) entry.getKey();
					if (!pname.startsWith(WRITE_SKIP_PREFIX)) {
						String group = groupLookup == null ? null : (String) groupLookup.get(pname);
						if ((group == null && groupName == null) || (group != null && group.equals(groupName))) {
							// if comp ref has not been written, ie. no props on comp
							if (!written_comp) {
								int bits = 0;
								ByteArrayOutputStream byteStream = newByteArrayOutputStream(BYTE_ARRAY_SIZE_32);
								setOutput(byteStream);
								if (offset > 1) {
									writec(offset);
									bits |= PROPERTY_COMP_REF_OFFSET;
									offset = 1;
								}
								setOutput();
								writeTag(ED_PROPERTY_COMPONENT_REF_TAG, byteStream.size(), bits, true);
								byteStream.writeTo(getOutput());
								written_comp = true;
							}

							if (!written_inst) {
								int bits = 0;
								ByteArrayOutputStream byteStream = newByteArrayOutputStream(BYTE_ARRAY_SIZE_32);
								setOutput(byteStream);
								if (inst_offset > 1) {
									writec(inst_offset);
									bits |= PROPERTY_INST_REF_OFFSET;
									inst_offset = 1;
								}
								setOutput();
								writeTag(ED_PROPERTY_INSTANCE_REF_TAG, byteStream.size(), bits, true);
								byteStream.writeTo(getOutput());
								written_inst = true;
							}

							writeProperty(pname, entry.getValue());
						}
					}
				}
			}

			if (written_inst) {
				writeTag(GEN_END_TAG, 0, 0, false);
			} else {
				inst_offset++;
			}
		}

		if (written_comp) {
			writeTag(GEN_END_TAG, 0, 0, false);
		} else {
			offset++;
		}

		return offset;
	}

	private void writeProperty(String pname, Object pvalue) throws Exception {
		if (pvalue instanceof List) {
			for (Iterator it = ((List) pvalue).iterator(); it.hasNext();) {
				writeProperty(pname, it.next());
			}
			return;
		}

		int bits = 0;
		ByteArrayOutputStream byteStream = newByteArrayOutputStream(BYTE_ARRAY_SIZE_32);
		setOutput(byteStream);

		writes(pname);
		if (pvalue instanceof String) {
			writes((String) pvalue);
			bits = PROPERTY_TYPE_STRING;
		} else if (pvalue instanceof Float) {
			writef(((Float) pvalue).floatValue());
			bits = PROPERTY_TYPE_FLOAT;
		} else if (pvalue instanceof Double) {
			writed(((Double) pvalue).doubleValue());
			bits = PROPERTY_TYPE_DOUBLE;
		} else if (pvalue instanceof Byte) {
			write1(((Byte) pvalue).byteValue());
			bits = PROPERTY_TYPE_BYTE;
		} else if (pvalue instanceof Short) {
			write2(((Short) pvalue).intValue());
			bits = PROPERTY_TYPE_SHORT;
		} else if (pvalue instanceof Integer) {
			write4(((Integer) pvalue).intValue());
			bits = PROPERTY_TYPE_INT;
		} else if (pvalue instanceof Boolean) {
			write1(((Boolean) pvalue).booleanValue() ? 1 : 0);
			bits = PROPERTY_TYPE_BOOLEAN;
		} else {
			logger.debug(
					"Write Property '" + pname + "', unknown type '" + pvalue.getClass().getName() + "': " + pvalue);
		}

		setOutput();
		writeTag(ED_PROPERTY_TAG, byteStream.size(), bits, false);
		byteStream.writeTo(getOutput());
	}

	private void writeSymbolTableSection() throws Exception {
		if (logger.isTraceEnabled())
			logger.trace("Write Symbol Table size=" + symbolList.size());

		ByteArrayOutputStream byteStream = new ByteArrayOutputStream(BUFFER_SIZE_4096);
		setOutput(byteStream);

		writec(symbolList.size()); // number of symbols as compressed int
		for (Iterator<String> it = symbolList.iterator(); it.hasNext();)
			writestring(it.next());

		setOutput();
		writeTag(SECTION_TYPE_SYMBOLS, byteStream.size(), 0, true);
		byteStream.writeTo(getOutput());

		writeTag(GEN_END_TAG, 0, 0, false);
	}

	private void writeFileMapSection() throws Exception {
		final boolean trace = logger.isTraceEnabled();
		if (trace)
			logger.trace("File Map size=" + (fileMap == null ? "null" : ("" + fileMap.size())));

		int bits = 0;
		ByteArrayOutputStream byteStream = newByteArrayOutputStream(BYTE_ARRAY_SIZE_32);
		setOutput(byteStream);

		if (fileMapOid != null) {
			writes(fileMapOid);
			bits |= FILEMAP_OID;
			bits |= FILEMAP_OID_COMPLETE;
		}
		if (fileMapOid1Prefix != null) {
			writes(fileMapOid1Prefix);
			bits |= FILEMAP_OID1_PREFIX;
		}
		if (fileMapOid1Suffix != null) {
			writes(fileMapOid1Suffix);
			bits |= FILEMAP_OID1_SUFFIX;
		}
		if (fileMapOid2Prefix != null) {
			writes(fileMapOid2Prefix);
			bits |= FILEMAP_OID2_PREFIX;
		}
		if (fileMapOid2Suffix != null) {
			writes(fileMapOid2Suffix);
			bits |= FILEMAP_OID2_SUFFIX;
		}

		if (trace)
			logger.trace("File Map " + fileMapOid + "  >" + fileMapOid1Prefix + "<  >" + fileMapOid1Suffix + "<  >"
					+ fileMapOid2Prefix + "<  >" + fileMapOid2Suffix + "<");
		setOutput();
		writeTag(SECTION_TYPE_FILEMAP, byteStream.size(), bits, true);
		byteStream.writeTo(getOutput());
		byteStream = null;

		if (fileMap != null) {
			for (Map.Entry<String, String> entry : fileMap.entrySet()) {
				writeFileMapEntry(entry.getKey(), entry.getValue(), fileMapOid1Prefix, fileMapOid1Suffix,
						fileMapOid2Prefix, fileMapOid2Suffix);
			}
		}

		writeTag(GEN_END_TAG, 0, 0, false);
	}

	private void writeFileMapEntry(String filename, String oids, String prefix1, String suffix1, String prefix2,
			String suffix2) throws Exception {
		String oid1 = oids;
		String oid2 = null;

		int i = oids.indexOf(DELIM);
		if (i != -1) {
			oid1 = oids.substring(0, i);
			oid2 = oids.substring(i + 1);
		}

		int bits = 0;
		ByteArrayOutputStream byteStream = newByteArrayOutputStream(BYTE_ARRAY_SIZE_32);
		setOutput(byteStream);

		String newOid = processOid(oid1, prefix1, suffix1);
		if (newOid != null) {
			oid1 = newOid;
		} else {
			bits |= FILEMAP_ENTRY_OID1_COMPLETE;
		}
		if (oid2 != null) {
			newOid = processOid(oid2, prefix2, suffix2);
			if (newOid != null) {
				oid2 = newOid;
			} else {
				bits |= FILEMAP_ENTRY_OID2_COMPLETE;
			}
		}

		if (logger.isTraceEnabled())
			logger.trace("File Map Entry=" + filename + "  >" + oid1 + "<  >" + oid2 + "<");
		writes(filename);
		writes(oid1);
		if (oid2 != null) {
			writes(oid2);
			bits |= FILEMAP_ENTRY_OID2;
		}

		setOutput();
		writeTag(ED_FILEMAP_ENTRY_TAG, byteStream.size(), bits, writeEndTag);
		byteStream.writeTo(getOutput());

		if (writeEndTag)
			writeTag(GEN_END_TAG, 0, 0, false);
	}

	private String processOid(String oid, String prefix, String suffix) {
		if (prefix != null) {
			if (suffix != null) {
				if (oid.startsWith(prefix) && oid.endsWith(suffix)) {
					return oid.substring(prefix.length(), oid.length() - suffix.length());
				}
			} else if (oid.startsWith(prefix)) {
				return oid.substring(prefix.length());
			}
		} else if (suffix != null && oid.endsWith(suffix)) {
			return oid.substring(0, oid.length() - suffix.length());
		}

		return null;
	}

	public void writeEDP(OutputStream os, ArrayList onlySections) throws Exception {
		writeEDP(null, os, onlySections);
	}

	public void writeEDP(File edpF, ArrayList onlySections) throws Exception {
		writeEDP(edpF, null, onlySections);
	}

	private void writeEDP(File edpFilename, OutputStream os, ArrayList onlySections) throws Exception {
		// check compList is populated, if not populate from rootComp
		if (compList == null || compList.size() == 0) {
			if (rootComp == null)
				throw new IOException("writeEDP: no compList or rootComp set");
			populateCompList();
		}

		initWriteBuffers();
		if (edpFilename != null)
			os = new FileOutputStream(edpFilename);
		bo = new BufferedOutputStream(os, BUFFER_SIZE_4096);
		output = bo;
		symbols = null;
		symbolList = new ArrayList<String>(100);
		symbolMap = new HashMap<String, Integer>(100);

		final boolean trace = logger.isTraceEnabled();
		ensureFileVersionForOrientationWriting(trace);
		String fileIdHeader = EDP_FILE_ID_STR + getFullVersionString();
		if (trace) {
			logger.trace(
					"Write EDP " + (edpFilename == null ? "-" : edpFilename.toString()) + " (" + fileIdHeader + ")");
		}
		write(fileIdHeader.getBytes(UTF8), 0, ED2_FILE_ID_LEN);

		int numIntSections = isWritePropertiesAsSymbols() ? onlySections.size() + 1 : onlySections.size();

		IndexEntry[] intIndex = new IndexEntry[numIntSections];
		int section = 0;
		if (isWritePropertiesAsSymbols())
			intIndex[section++] = new IndexEntry(SECTION_TYPE_SYMBOLS, compressionType, null);
		for (Iterator it = onlySections.iterator(); it.hasNext();) {
			intIndex[section++] = new IndexEntry(SECTION_TYPE_PROPERTIES, compressionType, (String) it.next());
		}

		// compute the size of the index section of the ed2 file
		int firstSectionOffset = 18; // number of bytes in header plus 4 for 2x number of sections
		for (int i = 0; i < numIntSections; i++)
			firstSectionOffset += intIndex[i].indexSize();

		write4(firstSectionOffset);
		if (trace)
			logger.trace("EDP firstSectionOffset = " + firstSectionOffset);

		// start writing index section
		write2(numIntSections);
		write2(0); // num ext sections
		if (trace)
			logger.trace("EDP numIntSections = " + numIntSections);

		// process the data into the byte arrays
		for (int i = 0; i < numIntSections; i++) {
			if (intIndex[i].type != SECTION_TYPE_SYMBOLS)
				writeSection(intIndex[i]);
		}
		for (int i = 0; i < numIntSections; i++) {
			if (intIndex[i].type == SECTION_TYPE_SYMBOLS)
				writeSection(intIndex[i]);
		}

		// write out the index for each section to actual output file
		for (int i = 0; i < numIntSections; i++)
			intIndex[i].writeIndex();

		// add the internal section byte arrays to the actual output file
		for (int i = 0; i < numIntSections; i++)
			intIndex[i].writeData();

		bo.flush();
		if (edpFilename != null)
			os.close();
	}

	public void writeEDM(OutputStream os) throws Exception {
		writeEDM(null, os);
	}

	public void writeEDM(File edmF) throws Exception {
		writeEDM(edmF, null);
	}

	private void writeEDM(File edmFilename, OutputStream os) throws Exception {
		initWriteBuffers();
		if (edmFilename != null)
			os = new FileOutputStream(edmFilename);
		bo = new BufferedOutputStream(os, BUFFER_SIZE_4096);
		output = bo;
		symbols = null;
		symbolList = new ArrayList<String>(100);
		symbolMap = new HashMap<String, Integer>(100);

		final boolean trace = logger.isTraceEnabled();
		ensureFileVersionForOrientationWriting(trace);
		String fileIdHeader = EDM_FILE_ID_STR + getFullVersionString();
		if (trace) {
			logger.trace(
					"Write EDM " + (edmFilename == null ? "-" : edmFilename.toString()) + " (" + fileIdHeader + ")");
		}
		write(fileIdHeader.getBytes(UTF8), 0, ED2_FILE_ID_LEN);

		int numIntSections = isWriteFileMapAsSymbols() ? 2 : 1;

		IndexEntry[] intIndex = new IndexEntry[numIntSections];
		int section = 0;
		if (isWriteFileMapAsSymbols())
			intIndex[section++] = new IndexEntry(SECTION_TYPE_SYMBOLS, compressionType, null);
		intIndex[section++] = new IndexEntry(SECTION_TYPE_FILEMAP, compressionType, null);

		// compute the size of the index section of the ed2 file
		int firstSectionOffset = 18; // number of bytes in header plus 4 for 2x number of sections
		for (int i = 0; i < numIntSections; i++)
			firstSectionOffset += intIndex[i].indexSize();

		write4(firstSectionOffset);
		if (trace)
			logger.trace("EDM firstSectionOffset = " + firstSectionOffset);

		// start writing index section
		write2(numIntSections);
		write2(0); // num ext sections
		if (trace)
			logger.trace("EDM numIntSections = " + numIntSections);

		// process the data into the byte arrays
		for (int i = 0; i < numIntSections; i++) {
			if (intIndex[i].type != SECTION_TYPE_SYMBOLS)
				writeSection(intIndex[i]);
		}
		for (int i = 0; i < numIntSections; i++) {
			if (intIndex[i].type == SECTION_TYPE_SYMBOLS)
				writeSection(intIndex[i]);
		}

		// write out the index for each section to actual output file
		for (int i = 0; i < numIntSections; i++)
			intIndex[i].writeIndex();

		// add the internal section byte arrays to the actual output file
		for (int i = 0; i < numIntSections; i++)
			intIndex[i].writeData();

		bo.flush();
		if (edmFilename != null)
			os.close();
	}

	private boolean writeTag(int tag, int size, int bits, boolean hasEndFlag) throws Exception {
		if (tag > GEN_MAX_TAG)
			return false;

		int sizeBytes = getNumBytes(size);
		int bitsBytes = getNumBytes(bits);

		int t = (tag | (sizeBytes << GEN_TAG_SIZE_SIZE_SHIFT) | (bitsBytes << GEN_TAG_BITS_SIZE_SHIFT));
		if (hasEndFlag)
			t |= GEN_TAG_END_FLAG;

		write2(t); // write the tag
		for (int i = 0; i < sizeBytes; i++, size >>= 8)
			write1((size & 0xff));
		for (int i = 0; i < bitsBytes; i++, bits >>= 8)
			write1((bits & 0xff));

		return true;
	}

	private int getNumBytes(int val) {
		if (val == 0)
			return 0;
		else if (val < 0x00000100)
			return 1;
		else if (val < 0x00010000)
			return 2;
		else if (val < 0x01000000)
			return 3;

		return 4;
	}

	/////////////////////// methods to convert to/from tree structures
	/////////////////////// //////////////////////////////

	public void printStatus() throws Exception {
		if (rootComp == null) {
			logger.trace("Root Component is null");
			return;
		}

		int[] count = new int[15];
		for (int i = 0; i < 15; i++)
			count[i] = 0;
		HashSet<Structure2.Comp> doneComps = new HashSet<Structure2.Comp>(100);
		getStatus(count, doneComps, rootComp);
		if (logger.isTraceEnabled()) {
			logger.trace("Number of Comps         : " + count[0] + " (shape " + count[1] + ", bbox " + count[2]
					+ ", attach " + count[3] + ", views " + count[4] + ")");
			logger.trace("Number of Comp Instances: " + count[5] + " (id " + count[6] + ", pos " + count[7] + ", mat "
					+ count[8] + ")");
			logger.trace("Number of Comps that are proxy: " + count[11]);
		}
	}

	private void getStatus(int[] count, HashSet<Comp> doneComps, Comp comp) {
		if (doneComps.contains(comp))
			return;
		doneComps.add(comp);

		count[0]++;
		if (comp.isProxy())
			count[11]++;
		if (comp.shape != null)
			count[1]++;
		if (comp.bbox != null)
			count[2]++;
		if (comp.viewableList != null)
			count[3] += comp.viewableList.size();
		if (comp.viewList != null)
			count[4] += comp.viewList.size();

		for (Iterator<CompInst> it = comp.children(); it.hasNext();) {
			CompInst child = it.next();
			count[5]++;
			if (child.id != null)
				count[6]++;
			if (child.translation != null)
				count[7]++;
			if (child.orientation != null)
				count[8]++;
			getStatus(count, doneComps, child.child);
		}
	}

	public void printFileMap() throws Exception {
		final boolean trace = logger.isTraceEnabled();
		if (trace) {
			logger.trace("File Map OID = " + fileMapOid);
			logger.trace("File Map OID1 Prefix = " + fileMapOid1Prefix);
			logger.trace("File Map OID1 Suffix = " + fileMapOid1Suffix);
			logger.trace("File Map OID2 Prefix = " + fileMapOid2Prefix);
			logger.trace("File Map OID2 Suffix = " + fileMapOid2Suffix);
		}

		if (fileMap == null) {
			if (trace)
				logger.trace("File Map is null");
			return;
		}
		if (fileMap.size() == 0) {
			if (trace)
				logger.trace("File Map is empty");
			return;
		}

		for (Map.Entry<String, String> entry : fileMap.entrySet()) {
			String filename = entry.getKey();
			String oids = entry.getValue();
			String oid1 = oids;
			String oid2 = null;

			int i = oids.indexOf(DELIM);
			if (i != -1) {
				oid1 = oids.substring(0, i);
				oid2 = oids.substring(i + 1);
			}

			if (trace)
				logger.trace("File Map Entry=" + filename + "  >" + oid1 + "<  >" + oid2 + "<");
		}
	}

	public DefaultMutableTreeNode toTreeStructure() throws Exception {
		if (rootComp == null)
			throw new IOException("toTreeStructure: root component is null");

		// build a fully instanced structure
		CompInst compInst = new CompInst(null, rootComp); // dummy component instance for root node

		DefaultMutableTreeNode root = buildTree(compInst, null);

		return root;
	}

	@SuppressWarnings("unchecked") // unchecked calls to put
	private DefaultMutableTreeNode buildTree(CompInst compInst, DefaultMutableTreeNode parent) {
		Hashtable h = new Hashtable(10);
		DefaultMutableTreeNode n = new DefaultMutableTreeNode(h);
		if (parent != null)
			parent.add(n);

		Comp childComp = compInst.child;

		if (childComp.name != null) {
			h.put(DISPLAY_INSTANCE_NAME, childComp.name);
		} else if (compInst.name != null) {
			h.put(DISPLAY_INSTANCE_NAME, compInst.name);
		} else {
			h.put(DISPLAY_INSTANCE_NAME, "");
		}

		if (childComp.isProxy()) {
			h.put(COMPONENT_BRANCH_LINK, childComp.filename == null ? "null" : childComp.filename);
			h.put(WRITE_SKIP_PREFIX + "proxy_filename", childComp.filename == null ? "null" : childComp.filename);
			h.put(WRITE_SKIP_PREFIX + "proxy_map_filename",
					childComp.map_filename == null ? "null" : childComp.map_filename);
			h.put(WRITE_SKIP_PREFIX + "proxy_wvs_info", childComp.wvs_info == null ? "null" : childComp.wvs_info);
		}

		if (compInst.id != null)
			h.put(WRITE_SKIP_PREFIX + PVCID, compInst.id);
		if (childComp.type != 0)
			h.put(WRITE_SKIP_PREFIX + "type", Integer.toString(childComp.type));
		if (childComp.shape != null)
			h.put(WRITE_SKIP_PREFIX + SHAPESOURCE, childComp.shape);
		addNumberArray(h, WRITE_SKIP_PREFIX + BBOX, childComp.bbox, null, " M");
		addNumberArray(h, WRITE_SKIP_PREFIX + TRANSLATION, compInst.translation, null, null);
		addNumberArray(h, WRITE_SKIP_PREFIX + ORIENTATION, compInst.orientation, null, null);

		if (childComp.properties != null)
			h.putAll(childComp.properties);
		if (childComp.viewableList != null)
			h.put(WRITE_SKIP_PREFIX + "viewables", childComp.viewableList);
		if (childComp.viewList != null)
			h.put(WRITE_SKIP_PREFIX + "views", childComp.viewList);

		if (childComp.defaultAppearance != null)
			h.put(WRITE_SKIP_PREFIX + "default_appearance", childComp.defaultAppearance);
		if (compInst.appearanceOveride != null) {
			h.put(WRITE_SKIP_PREFIX + "appearance_overide", compInst.appearanceOveride);
			h.put(WRITE_SKIP_PREFIX + "appearance_overide_self", compInst.appearanceOverideSelf);
			h.put(WRITE_SKIP_PREFIX + "appearance_overide_descendants", compInst.appearanceOverideDescendants);
		}

		mergeProperties(h, compInst.properties, "INST_");

		for (Iterator<CompInst> it = childComp.children(); it.hasNext();) {
			buildTree(it.next(), n);
		}

		return n;
	}

	private void addNumberArray(Hashtable<String, String> h, String propName, Object array, String prefix,
			String suffix) {
		if (array == null)
			return;

		if (array instanceof double[]) {
			double[] d = (double[]) array;
			StringBuffer sb = new StringBuffer();
			sb.append(prefix != null ? prefix : "");
			for (int i = 0; i < d.length; i++) {
				if (i > 0)
					sb.append(" ");
				sb.append(d[i]);
			}
			if (suffix != null)
				sb.append(suffix);
			h.put(propName, sb.toString());
		} else if (array instanceof float[]) {
			float[] d = (float[]) array;
			StringBuffer sb = new StringBuffer();
			sb.append(prefix != null ? prefix : "");
			for (int i = 0; i < d.length; i++) {
				if (i > 0)
					sb.append(" ");
				sb.append(d[i]);
			}
			if (suffix != null)
				sb.append(suffix);
			h.put(propName, sb.toString());
		}
	}

	public void fromTreeStructure(DefaultMutableTreeNode root) throws Exception {
		rootComp = null;
		compList = new ArrayList<Comp>(100);
		appearanceMap = new HashMap<Object, Object>(10); // use same map as used for writing but for different purpose

		if (root != null)
			readTree(root);

		appearanceMap = null;

		if (rootComp == null && compList != null && compList.size() > 0)
			rootComp = compList.get(compList.size() - 1);

		putPropertyValue(rootComp, CONVERTED_FROM_ED, TRUE, null);
	}

	private int readTree(DefaultMutableTreeNode n) {
		// do children first
		int count = n.getChildCount();
		int[] childIdx = count == 0 ? null : new int[count];

		int i = 0;
		for (Enumeration en = n.children(); en.hasMoreElements();) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) en.nextElement();
			childIdx[i++] = readTree(child);
		}

		Hashtable h = (Hashtable) n.getUserObject();

		if (isConvertURL())
			convertPropertiesFromURL(h); // this will populate the file map as well as changing properties to filename

		String name = (String) h.get(DISPLAY_INSTANCE_NAME);
		String shape = (String) h.get(SHAPESOURCE);
		String branchLink = (String) h.get(COMPONENT_BRANCH_LINK);
		// find existing comp of same name and return index of that
		if (matchCompsOnConversion && name != null) {
			int idx = 0;
			for (Iterator it = compList.iterator(); it.hasNext(); idx++) {
				Comp c = (Comp) it.next();
				if (name.equals(c.name)) {
					if (branchLink != null) {
						if (c.isProxy() && branchLink.equals(c.filename))
							return idx;
					} else if ((shape == null && c.shape == null) || (shape != null && shape.equals(c.shape))) {
						return idx;
					}
				}
			}
		}

		Comp comp = new Comp(name, branchLink != null);
		compList.add(comp);
		if (branchLink != null) {
			comp.filename = branchLink;
			if (isConvertBranchLink())
				convertBranchLink(comp);
		} else {
			comp.shape = shape;
			comp.bbox = getBboxFromBboxString((String) h.get(BBOX));

			addViewableByViewerName(comp, DOCUMENT, h.get(DOCUMENT), null, 0);
			addViewableByViewerName(comp, DRAWING, h.get(DRAWING), null, 0);
			addViewableByViewerName(comp, IMAGE, h.get(IMAGE), null, 0);
			addViewableByViewerName(comp, OLEDOC, h.get(OLEDOC), null, 0);
			addViewableByViewerName(comp, FILE, h.get(SIMULATION), null, 0);
			addViewableByViewerName(comp, FILE, h.get(FILE), null, 0);
			addViewableByViewerName(comp, ILLUSTRATION, h.get(ILLUSTRATION), null, 0);
			addViewableByViewerName(comp, ILLUSTRATION3D, h.get(ILLUSTRATION3D), null, 0);
			addViewableByViewerName(comp, ECAD, h.get(ECAD), null, 0);
			addViewableByViewerName(comp, ECAD_SCHEMATIC, h.get(ECAD_SCHEMATIC), null, 0);
			addViewableByViewerName(comp, ECAD_PCB, h.get(ECAD_PCB), null, 0);

			String colorString = getColorValue(h);
			if (colorString != null) {
				Appearance app = (Appearance) appearanceMap.get(colorString);
				if (app == null) {
					float[] col = getColorFromColorString(colorString);
					if (col != null) {
						app = new Appearance();
						app.base_color = new float[3];
						for (int j = 0; j < 3; j++)
							app.base_color[j] = col[j];
						if (Math.abs((double) (col[3] - 1.0F)) > 0.001) {
							app.opacity = new float[1];
							app.opacity[0] = col[3];
						}
						appearanceMap.put(colorString, app);
					}
				}
				comp.defaultAppearance = app;
			}

			String[] views = getPropertyValues(h, "view");
			if (views != null) {
				boolean vc = false;
				for (int j = 0; j < views.length; j++) {
					if (addViewFromPropValue(comp, views[j]))
						vc = true;
				}
				if (vc)
					h.remove("view");
			}
		}

		String icon_id = (String) h.get(DISPLAY_ICON_ID);
		if (icon_id != null) {
			try {
				comp.type = Integer.parseInt(icon_id);
			} catch (Exception ex) {
			}
		}

		Hashtable groupLookup = (Hashtable) h.get(PROPERTY_GROUP_LOOKUP);
		for (Iterator it = h.entrySet().iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			String pname = (String) entry.getKey();
			String pnl = pname.toLowerCase();
			if (!pnl.startsWith(WRITE_SKIP_PREFIX) && !pnl.equals(SHAPESOURCE) && !pnl.equals(LOCATION)
					&& !pnl.equals(LENGTH_UNIT) && !pnl.equals(SHAPETYPE) && !pnl.equals(BBOX) && !pnl.equals(PVCID)
					&& !pnl.equals(COLOR) && !pnl.equals(IDSHAPESOURCE) && !pnl.equals(TEXTURE_LOOKUP)
					&& !pnl.equals(XREF_LOOKUP) && !pnl.equals(DOCUMENT) && !pnl.equals(DRAWING) && !pnl.equals(IMAGE)
					&& !pnl.equals(OLEDOC) && !pnl.equals(ECAD) && !pnl.equals(SIMULATION) && !pnl.equals(FILE)
					&& !pnl.equals(ILLUSTRATION) && !pnl.equals(ILLUSTRATION3D) && !pnl.equals(ECAD_SCHEMATIC)
					&& !pnl.equals(ECAD_PCB)) {
				if (comp.properties == null)
					comp.properties = new Hashtable(5);
				String group = groupLookup == null ? null : (String) groupLookup.get(pname);

				// we need to force the Part/EPMDoc/Doc properties to be in the default group,
				// so
				// they will be in the pvs file not the pvp file
				if (pnl.equals(PART_OBJECT_ID) || pnl.equals(EPMDOC_OBJECT_ID) || pnl.equals(DOC_OBJECT_ID))
					group = null;

				Object o = entry.getValue();
				// if( o instanceof String ) {
				// try {
				// o = Integer.valueOf( (String)o );
				// } catch(NumberFormatException e1) {
				// try {
				// o = Double.valueOf( (String)o );
				// } catch(NumberFormatException e2) {}
				// }
				// }
				addPropertyValue(comp.properties, pname, o, group);
			}
		}

		// create the child instances
		double stom = getScaleToM((String) h.get(LENGTH_UNIT));
		i = 0;
		for (Enumeration en = n.children(); en.hasMoreElements();) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) en.nextElement();

			Comp childComp = compList.get(childIdx[i++]);
			CompInst compInst = new CompInst(comp, childComp);

			Hashtable ch = (Hashtable) child.getUserObject();
			String locationValue = getLocationValue(ch);
			if (locationValue != null) {
				Matrix4d mat = getMatrix4dFromLocationString(locationValue, stom);
				if (mat != null) {
					compInst.translation = getTranslationFromMatrix4d(mat);
					compInst.orientation = getOrientationFromMatrix4d(mat);
				}

				if (matchCompsOnConversion) {
					addPropertyValue(compInst, ED_LOCATION, locationValue, null);
				} else {
					addPropertyValue(childComp, ED_LOCATION, locationValue, null);
				}
			}
			compInst.id = (String) ch.get(PVCID);
		}

		return compList.size() - 1;
	}

	/**
	 * Determines the scale to Meters for a given unit string representing the units
	 * of IN, INCH, MM, CM, DM, M, FT <BR>
	 * <BR>
	 * <B>Supported API: </B>false
	 *
	 * @return scale factor
	 **/
	private static final double DTOR = Math.PI / 180.0;
	private static final double DMM = 0.001;
	private static final double DCM = 0.01;
	private static final double DDM = 0.1;
	private static final double DM = 1.0;
	private static final double DIN = 0.0254;
	private static final double DFT = 0.3048;

	public static double getScaleToM(String unit) {
		if (unit == null)
			return DMM;

		String s = unit.toUpperCase();

		if (s.equals("MM"))
			return DMM;
		else if (s.equals("CM"))
			return DCM;
		else if (s.equals("DM"))
			return DDM;
		else if (s.equals("M"))
			return DM;
		else if (s.equals("FT"))
			return DFT;
		else if (s.equals("INCH") || s.equals("IN"))
			return DIN;
		else if (s.endsWith("INCH")) {
			try {
				return Double.parseDouble(s.substring(0, s.length() - 4)) * DIN;
			} catch (NumberFormatException e) {
			}
		}

		return DMM;
	}

	private void convertBranchLink(Comp comp) {
		String url = comp.filename;
		int i = url.indexOf("/servlet/");
		if (i >= 0 && url.indexOf("WVSContentHelper") > 0) {
			url = url.substring(i + 1);
			comp.filename = url;
			comp.map_filename = url + "&edm=1";
		}
	}

	@SuppressWarnings("unchecked") // unchecked call to addPropertyValue()
	private void convertPropertiesFromURL(Hashtable p) {
		if (p == null)
			return;

		if (getFileMap() == null)
			setFileMap(new HashMap(10));

		convertPropertyFromURL(p, SHAPESOURCE);
		convertPropertyFromURL(p, DRAWING);
		convertPropertyFromURL(p, DOCUMENT);
		convertPropertyFromURL(p, IMAGE);
		convertPropertyFromURL(p, FILE);
		convertPropertyFromURL(p, ECAD);
		convertPropertyFromURL(p, SIMULATION);
		convertPropertyFromURL(p, OLEDOC);
		convertPropertyFromURL(p, ILLUSTRATION);
		convertPropertyFromURL(p, ILLUSTRATION3D);
		convertPropertyFromURL(p, ECAD_SCHEMATIC);
		convertPropertyFromURL(p, ECAD_PCB);

		convertLookupPropertyFromURL(p, TEXTURE_LOOKUP);
		convertLookupPropertyFromURL(p, XREF_LOOKUP);

		// if htmldoc propery exists, remove values that are to properties pages
		String[] htmldoc = getPropertyValues(p, HTMLDOC);
		if (htmldoc != null) {
			p.remove(HTMLDOC);
			for (int i = 0; i < htmldoc.length; i++) {
				if (!htmldoc[i].endsWith(PART_HTMLDOC) && !htmldoc[i].endsWith(EPM_HTMLDOC)
						&& !htmldoc[i].endsWith(DOC_HTMLDOC) && !htmldoc[i].endsWith(PARTMASTER_HTMLDOC)
						&& !htmldoc[i].endsWith(PRODUCTINSTANCE_HTMLDOC)
						&& !htmldoc[i].endsWith(PRODUCTCONFIGURATION_HTMLDOC) && !htmldoc[i].endsWith(OBJECT_HTMLDOC)) {
					Structure2.addPropertyValue(p, HTMLDOC, htmldoc[i]);
				}
			}
		}
	}

	private void convertPropertyFromURL(Hashtable<String, String> p, String name) {
		Object o = p.get(name);
		if (o == null) {
			return;
		} else if (o instanceof String) {
			try {
				String file = processValueFromURL(null, (String) o);
				if (file != null)
					p.put(name, file);
			} catch (Exception e) {
				logger.error("Unexpected exception", e);
			}
		} else if (o instanceof List) {
			try {
				@SuppressWarnings("unchecked")
				ListIterator<String> i = ((List) o).listIterator();
				while (i.hasNext()) {
					String file = processValueFromURL(null, i.next());
					if (file != null)
						i.set(file);
				}
			} catch (Exception e) {
				logger.error("Unexpected exception", e);
			}
		}
	}

	@SuppressWarnings("unchecked") // unchecked calls to set() and put()
	private void convertLookupPropertyFromURL(Hashtable p, String name) {
		Object o = p.get(name);
		if (o == null) {
			return;
		} else if (o instanceof String) {
			try {
				String v = (String) o;
				int ind = v.indexOf(':');
				if (ind > 0 && ind < v.length() - 1) {
					String file = processValueFromURL(v.substring(0, ind), v.substring(ind + 1));
					if (file != null)
						p.put(name, v.substring(0, ind) + ":" + file);
				}
			} catch (Exception e) {
				logger.error("Unexpected exception", e);
			}
		} else if (o instanceof List) {
			try {
				ListIterator i = ((List) o).listIterator();
				while (i.hasNext()) {
					String v = (String) i.next();
					int ind = v.indexOf(':');
					if (ind > 0 && ind < v.length() - 1) {
						String file = processValueFromURL(v.substring(0, ind), v.substring(ind + 1));
						if (file != null)
							i.set(v.substring(0, ind) + ":" + file);
					}
				}
			} catch (Exception e) {
				logger.error("Unexpected exception", e);
			}
		}
	}

	private String processValueFromURL(String name, String in) throws Exception {
		if (name == null)
			name = getVisTextFromProductViewURL(in);
		if (name == null)
			return null;

		StringTokenizer tok = new StringTokenizer(in, "?&=");
		String ciRef = null;
		String chRef = null;
		while (tok.hasMoreTokens()) {
			String t = tok.nextToken();
			if (t.equalsIgnoreCase("HttpOperationItem")) {
				ciRef = SandR(tok.nextToken(), "%3A", ":");
			} else if (t.equalsIgnoreCase("ContentHolder")) {
				chRef = SandR(tok.nextToken(), "%3A", ":");
			}
		}
		if (ciRef == null || chRef == null)
			return null;

		String existingValue = (String) getFileMap().get(name);
		if (existingValue == null) { // no entry for this file name exists so add one
			addFileMapEntry(name, ciRef, chRef);
			return name;
		} else if (existingValue.equals(ciRef + DELIM + chRef)) { // correct entry for this file name already exists
			return name;
		} else { // entry for this file name exists, but to different file, so rename this new
					// file
			int cnt = 1;
			while (true) {
				String newName = "v" + cnt + "-" + name;
				if (getFileMap().get(newName) == null) {
					addFileMapEntry(newName, ciRef, chRef);
					return newName;
				}
				cnt++;
			}
		}
	}

	/* This is dupped in common.Utilities */
	public static String SandR(String in, String s, String r) {
		StringBuffer buf = new StringBuffer(in.length());
		int slen = s.length();
		int pos, ind = 0;

		while ((pos = in.indexOf(s, ind)) >= 0) {
			buf.append(in.substring(ind, pos));
			buf.append(r);
			ind = pos + slen;
		}

		buf.append(in.substring(ind));

		return buf.toString();
	}

	public static String getVisTextFromProductViewURL(String url) {
		int i = url.indexOf(END_URLHIDE);
		if (i < 0)
			return null;
		return url.substring(i + LEN_END_URLHIDE);
	}

	private boolean addViewFromPropValue(Comp comp, String v) {
		if (comp == null || v == null)
			return false;

		StringTokenizer tok = new StringTokenizer(v);
		if (tok.countTokens() != 17)
			return false;

		double[] m = new double[16];
		for (int i = 0; i < 16; i++) {
			try {
				m[i] = Double.parseDouble(tok.nextToken());
			} catch (NumberFormatException nfe) {
				return false;
			}
		}
		String name = tok.nextToken();
		if (name == null || name.length() == 0)
			return false;

		Matrix4d mat = new Matrix4d();
		mat.m00 = m[0];
		mat.m10 = m[1];
		mat.m20 = m[2];
		mat.m30 = m[3];
		mat.m01 = m[4];
		mat.m11 = m[5];
		mat.m21 = m[6];
		mat.m31 = m[7];
		mat.m02 = m[8];
		mat.m12 = m[9];
		mat.m22 = m[10];
		mat.m32 = m[11];
		mat.m03 = m[12];
		mat.m13 = m[13];
		mat.m23 = m[14];
		mat.m33 = m[15];
		Quat4d quat = new Quat4d();
		mat.get(quat);

		float[] o = new float[4];
		o[0] = (float) quat.x;
		o[1] = (float) quat.y;
		o[2] = (float) quat.z;
		o[3] = (float) quat.w;
		new CView(comp, name, o);

		return true;
	}

	/////////////////////////////////// utilites
	/////////////////////////////////// /////////////////////////////////////

	public static float[] getBboxFromBboxString(String bbox) {
		if (bbox != null) {
			try {
				float[] f = new float[6];
				StringTokenizer tok = new StringTokenizer(bbox);
				if (tok.countTokens() != 6 && tok.countTokens() != 7)
					return null;
				for (int i = 0; i < 6; i++)
					f[i] = Float.parseFloat(tok.nextToken());
				if (tok.hasMoreTokens()) {
					float stom = (float) getScaleToM(tok.nextToken());
					for (int i = 0; i < 6; i++)
						f[i] *= stom;
				}
				return f;
			} catch (Exception e) {
				logger.error("Unexpected exception", e);
			}
		}

		return null;
	}

	public static float[] getColorFromColorString(String color) {
		if (color != null) {
			try {
				float[] f = new float[4];
				StringTokenizer tok = new StringTokenizer(color);
				if (tok.countTokens() != 3 && tok.countTokens() != 4)
					return null;
				for (int i = 0; i < 3; i++)
					f[i] = Float.parseFloat(tok.nextToken());
				f[3] = tok.hasMoreTokens() ? Float.parseFloat(tok.nextToken()) : 1.0F;
				return f;
			} catch (Exception e) {
				logger.error("Unexpected exception", e);
			}
		}

		return null;
	}

	public static Matrix4d getMatrix4dFromLocationString(String location, double stom) {
		if (location != null) {
			try {
				double ax, ay, az, x, y, z;
				double scale = 1.0;
				StringTokenizer tok = new StringTokenizer(location);
				ax = Double.parseDouble(tok.nextToken());
				ay = Double.parseDouble(tok.nextToken());
				az = Double.parseDouble(tok.nextToken());
				x = Double.parseDouble(tok.nextToken()) * stom;
				y = Double.parseDouble(tok.nextToken()) * stom;
				z = Double.parseDouble(tok.nextToken()) * stom;

				if (tok.hasMoreTokens())
					scale = Double.parseDouble(tok.nextToken());

				return getMatrix4dFromLocation(ax, ay, az, x, y, z, scale);
			} catch (Exception e) {
				logger.error("Unexpected exception", e);
			}
		}

		return null;
	}

	/**
	 * Creates a Matrix4d from rotation angles (in degrees) and translations <BR>
	 * <BR>
	 * <B>Supported API: </B>false
	 *
	 * @return a new Matrix4d
	 **/
	public static Matrix4d getMatrix4dFromLocation(double ax, double ay, double az, double x, double y, double z,
			double scale) {
		Matrix4d m1 = new Matrix4d();
		Matrix4d m2 = new Matrix4d();

		m1.rotZ(az * DTOR);
		m2.rotY(ay * DTOR);
		m1.mul(m2);
		m2.rotX(ax * DTOR);
		m1.mul(m2);

		m1.setTranslation(new Vector3d(x, y, z));

		m1.setScale(scale);

		return m1;
	}

	public static Matrix4d getMatrix4dFromTranslationAndOrientation(double[] position, double[] matrix) {
		Matrix4d mat = new Matrix4d();
		mat.setIdentity();

		if (matrix != null) {
			Matrix3d rot = new Matrix3d();
			rot.m00 = matrix[0];
			rot.m10 = matrix[1];
			rot.m20 = matrix[2];
			rot.m01 = matrix[3];
			rot.m11 = matrix[4];
			rot.m21 = matrix[5];
			rot.m02 = matrix[6];
			rot.m12 = matrix[7];
			rot.m22 = matrix[8];
			mat.setRotationScale(rot);
		}

		if (position != null) {
			Vector3d v = new Vector3d();
			v.x = position[0];
			v.y = position[1];
			v.z = position[2];
			mat.setTranslation(v);
		}

		return mat;
	}

	public static double[] getTranslationFromMatrix4d(Matrix4d mat) {
		Vector3d v = new Vector3d();
		mat.get(v);
		if (Math.abs(v.x) > TOL || Math.abs(v.y) > TOL || Math.abs(v.z) > TOL) {
			double[] d = new double[3];
			d[0] = v.x;
			d[1] = v.y;
			d[2] = v.z;
			return d;
		}

		return null; // return null if not non-zero value
	}

	public static double[] getOrientationFromMatrix4d(Matrix4d mat) {
		Matrix3d rot = new Matrix3d();
		mat.getRotationScale(rot);
		if (!rot.epsilonEquals(identityMatrix, TOL)) {
			double[] d = new double[9];
			d[0] = rot.m00;
			d[1] = rot.m10;
			d[2] = rot.m20;
			d[3] = rot.m01;
			d[4] = rot.m11;
			d[5] = rot.m21;
			d[6] = rot.m02;
			d[7] = rot.m12;
			d[8] = rot.m22;
			return d;
		}

		return null; // retun null if not non-identity
	}

	// perform a deep copy of a structure
	public static Comp copyStructure(Comp n) {
		return copyStructure(n, true);
	}

	// perform a deep copy of a structure
	public static Comp copyStructure(Comp n, boolean deep) {
		Comp newComp = (Comp) n.clone();
		Hashtable nh = n.properties;

		Hashtable<Object, Object> th = null;
		Object k = null, v = null;
		if (nh != null && nh.size() > 0) {
			th = new Hashtable<Object, Object>(nh.size());

			for (Iterator it = nh.entrySet().iterator(); it.hasNext();) {
				Map.Entry entry = (Map.Entry) it.next();
				k = entry.getKey();
				v = entry.getValue();
				if (v instanceof String) {
					th.put(k, v);
				} else if (v instanceof ArrayList) {
					th.put(k, ((ArrayList) v).clone());
				} else if (v instanceof Hashtable) {
					th.put(k, ((Hashtable) v).clone());
				} else {
					th.put(k, v);
				}
			}
		} else {
			th = new Hashtable<Object, Object>();
		}
		newComp.properties = th;

		if (deep) {
			for (Iterator<CompInst> it = n.children(); it.hasNext();) {
				CompInst child = it.next();
				CompInst newCompInst = (CompInst) child.clone();
				newComp.childInsts.add(newCompInst);
				newCompInst.parent = newComp;
				newCompInst.child = copyStructure(child.child);
			}
		}

		return newComp;
	}

	public static void mergeStructure(Comp n, Comp subRoot) {
		mergeStructure(n, subRoot, false, false);
	}

	public static void mergeStructure(Comp n, Comp subRoot, boolean allChildren) {
		mergeStructure(n, subRoot, allChildren, false);
	}

	public static void mergeStructure(Comp n, Comp subRoot, boolean allChildren, boolean partOnlyCheck) {
		n.setFieldsFrom(subRoot, false);
		n.addPropertiesFrom(subRoot);

		// now add the children of subRoot to n and remove them from subRoot
		HashSet<String> ids = null;
		for (Iterator<CompInst> it = subRoot.children(); it.hasNext();) {
			CompInst compInst = it.next();
			if (allChildren || isComponentMergable(compInst, partOnlyCheck)) {
				if (ids == null) {
					ids = new HashSet<String>();
					for (Iterator<CompInst> it1 = n.children(); it1.hasNext();) {
						CompInst ci = it1.next();
						if (ci.id != null)
							ids.add(ci.id);
					}
				}

				CompInst instCopy = (CompInst) compInst.clone();
				instCopy.parent = n;
				instCopy.child = compInst.child;
				n.childInsts.add(instCopy);

				if (ids.contains(instCopy.id)) {
					int i = 1;
					while (ids.contains(instCopy.id + "-" + i))
						i++;
					instCopy.id += "-" + i;
				}
			}
		}
	}

	public static boolean isComponentMergable(Comp comp) {
		return isComponentMergable(comp, false);
	}

	public static boolean isComponentMergable(Comp comp, boolean partOnlyCheck) {
		if (comp.isProxy())
			return comp.map_filename == null;
		if (comp.properties != null)
			return isComponentMergable(comp.properties, partOnlyCheck);
		return true;
	}

	public static boolean isComponentMergable(CompInst inst) {
		return isComponentMergable(inst, false);
	}

	public static boolean isComponentMergable(CompInst inst, boolean partOnlyCheck) {
		if (inst.type == DOCUMENT_REFERENCE_B)
			return false;
		return isComponentMergable(inst.child, partOnlyCheck);
	}

	public static boolean isComponentMergable(Hashtable h, boolean partOnlyCheck) {
		if (partOnlyCheck) {
			return h != null && h.get(PART_OBJECT_ID) == null
					&& (h.get(IGNORE_ON_MERGE) == null || h.get(EPMDOC_OBJECT_ID) != null)
					&& h.get(DOC_OBJECT_ID) == null && h.get(DOCUMENT_REFERENCE) == null;
		} else {
			return h != null && h.get(PART_OBJECT_ID) == null && h.get(EPMDOC_OBJECT_ID) == null
					&& h.get(DOC_OBJECT_ID) == null && h.get(IGNORE_ON_MERGE) == null
					&& h.get(DOCUMENT_REFERENCE) == null;
		}
	}

	@SuppressWarnings("unchecked") // unchecked calls to put() and get()
	public static Hashtable mergeProperties(Hashtable nh, Hashtable rh, String prefix) {
		if (rh != null && rh.size() > 0) {
			if (nh == null) {
				nh = new Hashtable(rh.size());
			}

			for (Enumeration e = rh.keys(); e.hasMoreElements();) {
				String pname = (String) e.nextElement();

				// merge the property group entries together
				if (pname.equals(PROPERTY_GROUP_LOOKUP)) {
					Hashtable nh_groups = (Hashtable) nh.get(PROPERTY_GROUP_LOOKUP);
					Hashtable rh_groups = (Hashtable) rh.get(PROPERTY_GROUP_LOOKUP);

					if (prefix != null) {
						Hashtable t = new Hashtable(rh_groups.size());
						for (Enumeration ee = rh_groups.keys(); ee.hasMoreElements();) {
							String k = (String) ee.nextElement();
							t.put(prefix + k, rh_groups.get(k));
						}
						rh_groups = t;
					}

					if (nh_groups != null) {
						for (Enumeration ee = nh_groups.keys(); ee.hasMoreElements();) {
							Object o = ee.nextElement();
							rh_groups.put(o, nh_groups.get(o));
						}
					}
					nh.put(PROPERTY_GROUP_LOOKUP, rh_groups);
				} else {
					nh.put(prefix == null ? pname : prefix + pname, rh.get(pname));
				}
			}
		}

		return nh;
	}

	public static CViewable addViewableByViewerName(Comp comp, String viewer, Object filename, Object displayname) {
		return addViewableByViewerName(comp, viewer, filename, displayname, 0);
	}

	public static CViewable addViewableByViewerName(Comp comp, String viewer, Object filename, Object displayname,
			int type) {
		CViewable ret = null;

		if (viewer == null || filename == null)
			return ret;

		// if the "filename" and displayname are lists, they must be the same size and
		// order.
		if (filename instanceof List) {
			Iterator d = displayname == null ? null : ((List) displayname).iterator();
			for (Iterator it = ((List) filename).iterator(); it.hasNext();) {
				ret = addViewableByViewerName(comp, viewer, it.next(), d == null ? null : d.next(), type);
			}
			return ret; // only returns the last one created
		}

		if (viewer.equalsIgnoreCase("comp") || viewer.equalsIgnoreCase("link"))
			viewer = FILE;

		if (viewer.equalsIgnoreCase(DOCUMENT)) {
			ret = comp.newDocumentCViewable((String) filename, (String) displayname, type);
		} else if (viewer.equalsIgnoreCase(DRAWING)) {
			ret = comp.newDrawingCViewable((String) filename, (String) displayname, type);
		} else if (viewer.equalsIgnoreCase(SHAPESOURCE)) {
			comp.shape = (String) filename;
		} else if (viewer.equalsIgnoreCase(IMAGE)) {
			ret = comp.newImageCViewable((String) filename, (String) displayname, type);
		} else if (viewer.equalsIgnoreCase(OLEDOC)) {
			ret = comp.newOLEDocCViewable((String) filename, (String) displayname, type);
		} else if (viewer.equalsIgnoreCase(ILLUSTRATION)) {
			ret = comp.newIllustrationCViewable((String) filename, (String) displayname, type);
		} else if (viewer.equalsIgnoreCase(ILLUSTRATION3D)) {
			ret = comp.new3DIllustrationCViewable((String) filename, (String) displayname, type);
		} else if (viewer.equalsIgnoreCase(ECAD)) {
			ret = comp.newECADCViewable((String) filename, (String) displayname, type);
		} else if (viewer.equalsIgnoreCase(ECAD_SCHEMATIC)) {
			ret = comp.newECADCViewable((String) filename, (String) displayname, type);
			if (ret != null)
				ret.ecad_type = ECAD_TYPE_SCHEMATIC;
		} else if (viewer.equalsIgnoreCase(ECAD_PCB)) {
			ret = comp.newECADCViewable((String) filename, (String) displayname, type);
			if (ret != null)
				ret.ecad_type = ECAD_TYPE_PCB;
		} else {
			ret = comp.newFileCViewable((String) filename, (String) displayname, type);
		}

		return ret;
	}

	/*
	 * ***** methods for reading/writing byte swapped data, ed2 files are always
	 * little endian *****
	 */

	// methods to read data in little endean format

	private int ch1, ch2, ch3, ch4;
	private byte buffer[] = new byte[8];
	private int intBuffer[] = new int[8];

	protected final void skip(int len) throws IOException {
		int n = 0;
		while (n < len) {
			int count = (int) input.skip((len - n));
			n += count;
			if (count == 0)
				break;
		}
		tagReadCount += n;
	}

	protected final void read(byte[] b, int off, int len) throws IOException {
		int n = 0;
		while (n < len) {
			int count = input.read(b, off + n, len - n);
			if (count < 0)
				throw new IOException();
			n += count;
		}
		tagReadCount += len;
	}

	protected final int read1() throws IOException {
		tagReadCount++;
		return input.read();
	}

	protected final int read2() throws IOException {
		ch1 = input.read();
		ch2 = input.read();
		tagReadCount += 2;

		return ((ch2 << 8) + ch1) & 0xffff;
	}

	protected final int read4() throws IOException {
		ch1 = input.read();
		ch2 = input.read();
		ch3 = input.read();
		ch4 = input.read();
		tagReadCount += 4;

		return ((ch4 << 24) + (ch3 << 16) + (ch2 << 8) + ch1);
	}

	protected final long read8() throws IOException {
		read(buffer, 0, 8);
		return (((long) buffer[7] << 56) + ((long) (buffer[6] & 255) << 48) + ((long) (buffer[5] & 255) << 40)
				+ ((long) (buffer[4] & 255) << 32) + ((long) (buffer[3] & 255) << 24) + ((buffer[2] & 255) << 16)
				+ ((buffer[1] & 255) << 8) + ((buffer[0] & 255) << 0));
	}

	protected final float readf() throws IOException {
		return Float.intBitsToFloat(read4());
	}

	protected final double readd() throws IOException {
		return Double.longBitsToDouble(read8());
	}

	/* read string length count */
	protected final int readc() throws IOException {
		int val = 0;

		boolean extend = true;
		for (int shift = 0; extend; shift += 7) {
			int b = read1(); // read next byte
			extend = ((b & 0x80) != 0); // if high bit set then there is another byte in len

			b &= 0x7f; // get just the low 7 bits
			if (shift > 0) {
				val |= (b << shift);
			} else {
				val = b;
			}
		}

		return val;
	}

	/* read string data */
	protected final String readstring() throws IOException {
		int count = readc();
		return reads(count);
	}

	/* read string/symbol data */
	protected final String reads() throws IOException {
		int count = readc();

		boolean isSymbol = ((count & 0x01) != 0);

		count >>= 1; // low byte of count is flag for synbol. Divide by 2 to get real count

		if (isSymbol)
			return symbols[count];

		return reads(count);
	}

	protected final String reads(int count) throws IOException {
		if (count < 1)
			return null;

		int c, rc = 0;
		byte buff[] = new byte[count];
		while ((c = input.read(buff, rc, count - rc)) != -1 && (rc += c) < count)
			;
		tagReadCount += count;
		return new String(buff, UTF8).intern();
	}

	// methods to write data in little endean format
	protected ByteArrayOutputStream byteStream32;
	protected ByteArrayOutputStream byteStream64;
	protected ByteArrayOutputStream byteStream128;

	protected void initWriteBuffers() {
		byteStream32 = new ByteArrayOutputStream(BYTE_ARRAY_SIZE_32);
		byteStream64 = new ByteArrayOutputStream(BYTE_ARRAY_SIZE_64);
		byteStream128 = new ByteArrayOutputStream(BYTE_ARRAY_SIZE_128);
	}

	protected ByteArrayOutputStream newByteArrayOutputStream(int size) {
		switch (size) {
		case BYTE_ARRAY_SIZE_32:
			byteStream32.reset();
			return byteStream32;
		case BYTE_ARRAY_SIZE_64:
			byteStream64.reset();
			return byteStream64;
		case BYTE_ARRAY_SIZE_128:
			byteStream128.reset();
			return byteStream128;
		default:
			return new ByteArrayOutputStream(size);
		}
	}

	protected final void write(byte[] b, int off, int len) throws IOException {
		output.write(b, off, len);
	}

	protected final void write1(int i) throws IOException {
		output.write(i);
	}

	protected final void write2(int i) throws IOException {
		output.write(i);
		output.write((i >>> 8)); // unsigned bit shift
	}

	protected final void write4(int i) throws IOException {
		output.write(i);
		output.write((i >>> 8));
		output.write((i >>> 16));
		output.write((i >>> 24));
	}

	protected final void write8(long v) throws IOException {
		buffer[7] = (byte) (v >>> 56);
		buffer[6] = (byte) (v >>> 48);
		buffer[5] = (byte) (v >>> 40);
		buffer[4] = (byte) (v >>> 32);
		buffer[3] = (byte) (v >>> 24);
		buffer[2] = (byte) (v >>> 16);
		buffer[1] = (byte) (v >>> 8);
		buffer[0] = (byte) (v >>> 0);
		output.write(buffer, 0, 8);
	}

	protected final void writef(float f) throws IOException {
		write4(Float.floatToIntBits(f));
	}

	protected final void writed(double d) throws IOException {
		write8(Double.doubleToLongBits(d));
	}

	protected final void writec(int val) throws IOException {
		if (val == 0) {
			output.write(0);
			return;
		}

		int i;
		for (i = 0; val != 0; i++) {
			int b = (val & 0x7f);
			val >>= 7;
			if (val != 0)
				b |= 0x80;
			intBuffer[i] = b;
		}

		for (int j = 0; j < i; j++)
			output.write(intBuffer[j]);
	}

	protected final void writestring(String s) throws IOException {
		if (s == null || s.length() == 0) {
			writec(0);
			return;
		}

		byte[] b = s.getBytes(UTF8);
		writec(b.length);
		output.write(b);
	}

	protected final void writes(String s) throws IOException {
		if (s == null || s.length() == 0) {
			writec(0);
			return;
		}

		if (usingStringsAsSymbols) {
			Object idx = symbolMap.get(s);
			if (idx != null) {
				writec((((Integer) idx).intValue() << 1) | 0x01);
				return;
			}
			writec((symbolList.size() << 1) | 0x01);
			symbolMap.put(s, Integer.valueOf(symbolList.size()));
			symbolList.add(s);
			return;
		}

		byte[] b = s.getBytes(UTF8);
		writec(b.length << 1);
		output.write(b);
	}

	/////////////////////////////////////////////////////////////////////////////////////////

	// inner class to represent an index entry
	public class IndexEntry {
		public boolean internalEntry;
		public int type;
		public int compression;
		public String description;
		public int length;
		public String filename;
		public ByteArrayOutputStream byte_stream = null;

		public IndexEntry(boolean ie) {
			internalEntry = ie;
		}

		public IndexEntry(int t, int c, String d) {
			internalEntry = true;
			type = t;
			compression = c;
			description = d;
		}

		public IndexEntry(int t, String d, String f) {
			internalEntry = false;
			type = t;
			description = d;
			filename = f;
		}

		public void readIndex() throws IOException {
			final boolean trace = logger.isTraceEnabled();
			if (internalEntry) {
				type = read1();
				compression = read1();
				description = readstring();
				length = read4();
				if (trace)
					logger.trace("Read Internal: type=" + type + " compression=" + (compression & 0xff) + " encryption="
							+ (compression & 0xff00) + " desc=>>" + description + "<< length=" + length);
			} else {
				type = read1();
				description = readstring();
				filename = readstring();
				if (trace)
					logger.trace("Read External: type=" + type + " desc=>>" + description + "<< filename=>>" + filename
							+ "<<");
			}
		}

		public void writeIndex() throws IOException {
			final boolean trace = logger.isTraceEnabled();
			if (internalEntry) {
				length = byte_stream == null ? 0 : byte_stream.size();

				write1(type);
				write1(compression);
				writestring(description);
				write4(length);
				if (trace)
					logger.trace(
							"Write Internal: type=" + type + " compression=" + (compression & 0xff) + " encryption="
									+ (compression & 0xff00) + " description=>>" + description + "<< length=" + length);
			} else {
				write1(type);
				writestring(description);
				writestring(filename);
				if (trace)
					logger.trace("Write External: type=" + type + " description=>>" + description + "<< filename=>>"
							+ filename + "<<");
			}
		}

		public void writeData() throws IOException {
			if (logger.isTraceEnabled())
				logger.trace("Write Data for section type " + type + " size " + length);
			if (byte_stream != null)
				byte_stream.writeTo(getOutput());
		}

		public int indexSize() throws IOException {
			int s = 0;

			OutputStream save_output = output;
			LengthOutputStream los = new LengthOutputStream();
			output = los;

			if (internalEntry) {
				writes(description);
				s = 6 + los.getLength();
			} else {
				writes(description);
				writes(filename);
				s = 1 + los.getLength();
			}

			los.close();
			output = save_output;

			return s;
		}
	}

	/**
	 * get all property values from a posibly multi-valued property <BR>
	 * <BR>
	 * <B>Supported API: </B>false
	 *
	 * @return property values
	 **/
	@SuppressWarnings("unchecked") // uncheckd call to toArray()
	public static String[] getPropertyValues(Hashtable h, String name) {
		if (h == null || name == null)
			return null;

		Object o = h.get(name);

		if (o == null) {
			return null;
		} else if (o instanceof String) {
			String[] s = new String[1];
			s[0] = (String) o;
			return s;
		} else if (o instanceof List) {
			String[] s = new String[((List) o).size()];
			((List) o).toArray(s);
			return s;
		}

		return null;
	}

	public static String getColorValue(Hashtable h) {
		String[] values = getPropertyValues(h, COLOR);
		if (values == null)
			return null;

		// if it is a normal case with just the one value, return it
		// if( values.length == 1 ) return values[0];

		// look at each value and return the first one that has 3 or 4 numbers
		for (int i = 0; i < values.length; i++) {
			if (values[i] != null) {
				StringTokenizer tok = new StringTokenizer(values[i], " ");
				if (tok.countTokens() == 3 || tok.countTokens() == 4) {
					boolean ok = true;
					while (tok.hasMoreTokens()) {
						try {
							Float.parseFloat(tok.nextToken());
						} catch (NumberFormatException nfe) {
							ok = false;
							break;
						}
					}
					// all the args parsed to floats, go with this one
					if (ok)
						return values[i];
				}
			}
		}

		return null;
	}

	/**
	 * try to get the value of the location property, for the case when there are
	 * multiple values, ie. a customer attribute called location is messing things
	 * up. <BR>
	 * <BR>
	 * <B>Supported API: </B>false
	 *
	 * @return location value
	 **/
	public static String getLocationValue(Hashtable h) {
		String[] values = getPropertyValues(h, LOCATION);
		if (values == null)
			return null;

		// if it is a normal case with just the one value, return it
		// if( values.length == 1 ) return values[0];

		// look at each value and return the first one that has 6 or 7 numbers
		for (int i = 0; i < values.length; i++) {
			if (values[i] != null) {
				StringTokenizer tok = new StringTokenizer(values[i], " ");
				if (tok.countTokens() == 6 || tok.countTokens() == 7) {
					boolean ok = true;
					while (tok.hasMoreTokens()) {
						try {
							Double.parseDouble(tok.nextToken());
						} catch (NumberFormatException nfe) {
							ok = false;
							break;
						}
					}
					// all the args parsed to doubles, go with this one
					if (ok)
						return values[i];
				}
			}
		}

		return null;
	}

	/**
	 * add the property name/value on the hashtable, allows milti-value properties
	 * <BR>
	 * <BR>
	 * <B>Supported API: </B>false
	 *
	 **/
	public static void addPropertyValue(Hashtable<String, Object> h, String name, String value) {
		addPropertyValue(h, name, value, null);
	}

	/**
	 * add the property name/value on the hashtable, allows milti-value properties,
	 * with group <BR>
	 * <BR>
	 * <B>Supported API: </B>false
	 *
	 **/
	public static void addPropertyValue(Hashtable<String, Object> h, String name, String value, String group) {
		if (h == null || name == null || value == null)
			return;

		Object o = h.get(name);

		if (o == null) {
			h.put(name, value);

			if (group != null && group.length() > 0) {
				@SuppressWarnings("unchecked")
				Hashtable<String, String> g = (Hashtable) h.get(PROPERTY_GROUP_LOOKUP);
				if (g == null) {
					g = new Hashtable<String, String>(5);
					h.put(PROPERTY_GROUP_LOOKUP, g);
				}
				g.put(name, group);
			}
		} else if (o instanceof String) {
			if (value.equals(o))
				return; // value already there, don't duplicate
			List<Object> l = new ArrayList<Object>(2);
			l.add(o);
			l.add(value);
			h.put(name, l);
		} else if (o instanceof List) {
			@SuppressWarnings("unchecked")
			List<String> l = (List) o;
			if (l.contains(value))
				return; // value already there, don't duplicate
			l.add(value);
		}
	}

	public static class LengthOutputStream extends OutputStream {
		private int count = 0;

		public int getLength() {
			return count;
		}

		public LengthOutputStream() {
			super();
			count = 0;
		}

		public void write(byte[] b) throws IOException {
			count += b.length;
		}

		public void write(byte[] b, int off, int len) throws IOException {
			count += len;
		}

		public void write(int i) throws IOException {
			count++;
		}
	}

	////////////////// classes to provide the memory representation of the structure
	////////////////// ///////////////

	public static Comp newComp(String n) {
		return new Comp(n);
	}

	public static Comp newComp(String n, boolean p) {
		return new Comp(n, p);
	}

	public static CompInst newCompInst(Comp p, Comp c) {
		return new CompInst(p, c);
	}

	public static class Comp implements Cloneable {
		public ArrayList<CompInst> childInsts = new ArrayList<CompInst>(1);
		public String name;
		public int type = 0;
		public boolean proxy; // if true this Comp is a CompProxy

		public int modelUnitLength = 0;
		public int displayUnitLength = 0;
		public int modelUnitMass = 0;
		public int displayUnitMass = 0;
		public Appearance defaultAppearance = null;

		public String shape = null;
		public float[] bbox = null;
		public int shape_index = -1;

		public String thumbnail3d = null;
		public int thumbnail3d_index = -1;

		public ArrayList<CViewable> viewableList = null;
		public ArrayList<Cloneable> viewList = null;
		public ArrayList<CViewState> viewStateList = null;
		public ArrayList<InstAppearance> instanceAppearanceOverideList = null;

		public String filename = null;
		public String map_filename = null;
		public String wvs_info = null;

		public Hashtable properties = null;
		public int writeIdx; // used only when being written

		public Comp() {
			this(null);
		}

		public Comp(boolean p) {
			this(null, p);
		}

		public Comp(String n) {
			name = n;
			proxy = false;
		}

		public Comp(String n, boolean p) {
			name = n;
			proxy = p;
		}

		public void setFieldsFrom(Comp c, boolean replaceProperties) {
			name = c.name;
			type = c.type;
			proxy = c.proxy;
			modelUnitLength = c.modelUnitLength;
			displayUnitLength = c.displayUnitLength;
			modelUnitMass = c.modelUnitMass;
			displayUnitMass = c.displayUnitMass;
			defaultAppearance = c.defaultAppearance;
			shape = c.shape;
			bbox = c.bbox;
			shape_index = c.shape_index;
			thumbnail3d = c.thumbnail3d;
			thumbnail3d_index = c.thumbnail3d_index;
			viewableList = c.viewableList;
			viewList = c.viewList;
			viewStateList = c.viewStateList;
			instanceAppearanceOverideList = c.instanceAppearanceOverideList;
			filename = c.filename;
			map_filename = c.map_filename;
			wvs_info = c.wvs_info;
			if (replaceProperties)
				properties = c.properties;
		}

		public void addPropertiesFrom(Comp c) {
			properties = mergeProperties(properties, c.properties, null);
		}

		public boolean isProxy() {
			return proxy;
		}

		public String toString() {
			if (proxy)
				return "<proxy: " + name + " " + filename + " " + map_filename + wvs_info + ">";
			return "<" + name + " " + type + " " + shape + ">";
		}

		public Hashtable getProperties() {
			return properties;
		}

		public Iterator viewables() {
			return viewableList == null ? null : viewableList.iterator();
		}

		public Iterator views() {
			return viewList == null ? null : viewList.iterator();
		}

		public Iterator viewStates() {
			return viewStateList == null ? null : viewStateList.iterator();
		}

		public Iterator instanceAppearanceOverides() {
			return instanceAppearanceOverideList == null ? null : instanceAppearanceOverideList.iterator();
		}

		public Iterator<CompInst> children() {
			return childInsts.iterator();
		}

		public void clearChildren() {
			childInsts.clear();
		}

		public void addChild(CompInst inst) {
			if (inst.parent != null)
				inst.parent.childInsts.remove(inst);
			childInsts.add(inst);
			inst.parent = this;
		}

		public void addAllChildrenFromComp(Comp fromComp) {
			for (Iterator<CompInst> it = fromComp.childInsts.iterator(); it.hasNext();) {
				CompInst child = it.next();
				childInsts.add(child);
				child.parent = this;
			}
			fromComp.childInsts.clear();
		}

		public void removeAllChildren() {
			for (Iterator<CompInst> it = childInsts.iterator(); it.hasNext();) {
				it.next().parent = null;
			}
			childInsts.clear();
		}

		public void removeChild(CompInst inst) {
			childInsts.remove(inst);
			inst.parent = null;
		}

		public Object clone() {
			try {
				Comp c = (Comp) super.clone();
				c.childInsts = new ArrayList<CompInst>(1);
				return c;
			} catch (CloneNotSupportedException e) {
				return null;
			}
		}

		public String getSourceFileName() {
			if (proxy) {
				if (wvs_info != null) {
					int i = wvs_info.indexOf(PART_INFO);
					if (i >= 0)
						return wvs_info.substring(0, i);
					i = wvs_info.indexOf(FORM_INFO);
					if (i >= 0)
						return wvs_info.substring(0, i);
					return wvs_info;
				}

			} else if (properties != null) {
				return (String) properties.get(PV_SOURCE_FILENAME);
			}
			return null;
		}

		public String getSourcePartName() {
			if (proxy) {
				if (wvs_info != null) {
					int i = wvs_info.indexOf(PART_INFO);
					if (i >= 0) {
						int j = wvs_info.indexOf(FORM_INFO);
						if (j < 0) {
							return wvs_info.substring(i + PART_INFO.length());
						} else {
							return wvs_info.substring(i + PART_INFO.length(), j);
						}
					}
				}
			} else if (properties != null) {
				return (String) properties.get(PV_SOURCE_PARTNAME);
			}
			return null;
		}

		public String getSourceFormName() {
			if (proxy) {
				int i = wvs_info.indexOf(FORM_INFO);
				if (i >= 0)
					return wvs_info.substring(i + FORM_INFO.length());
			} else if (properties != null) {
				return (String) properties.get(PV_SOURCE_FORMNAME);
			}
			return null;
		}

		public CViewable newCViewable(int t, String f, String d, int tt) {
			return new CViewable(this, t, f, d, tt);
		}

		public CViewable newDrawingCViewable(String f, String d, int tt) {
			return new CViewable(this, ED_DRAWING_TAG, f, d, tt);
		}

		public CViewable newImageCViewable(String f, String d, int tt) {
			return new CViewable(this, ED_IMAGE_TAG, f, d, tt);
		}

		public CViewable newDocumentCViewable(String f, String d, int tt) {
			return new CViewable(this, ED_DOCUMENT_TAG, f, d, tt);
		}

		public CViewable newFileCViewable(String f, String d, int tt) {
			return new CViewable(this, ED_FILE_TAG, f, d, tt);
		}

		public CViewable newOLEDocCViewable(String f, String d, int tt) {
			return new CViewable(this, ED_OLEDOC_TAG, f, d, tt);
		}

		public CViewable newIllustrationCViewable(String f, String d, int tt) {
			return new CViewable(this, ED_ILLUSTRATION_TAG, f, d, tt);
		}

		public CViewable new3DIllustrationCViewable(String f, String d, int tt) {
			return new CViewable(this, ED_ILLUSTRATION3D_TAG, f, d, tt);
		}

		public CViewable newECADCViewable(String f, String d, int tt) {
			return new CViewable(this, ED_ECAD_TAG, f, d, tt);
		}

		public CView newCView(String n, float[] o) {
			return new CView(this, n, o);
		}

		public CViewState newCViewState(int t, String n) {
			return new CViewState(this, t, n);
		}
	}

	public static class CompInst implements Cloneable {
		public Comp parent;
		public Comp child;
		public int type = NORMAL_TYPE_B;
		public double[] translation = null;
		public double[] orientation = null;
		public String id = null;
		public String name = null;
		public boolean hideSelf = false;
		public boolean hideDescendants = false;
		public boolean combine = false;
		public Appearance appearanceOveride = null;
		public boolean appearanceOverideSelf = true;
		public boolean appearanceOverideDescendants = true;
		public ArrayList<InstAppearance> instanceAppearanceOverideList = null;
		public Hashtable properties = null;

		public CompInst() {
			this(null, null);
		}

		public CompInst(Comp p, Comp c) {
			parent = p;
			child = c;
			if (parent != null)
				parent.childInsts.add(this);
		}

		public Iterator instanceAppearanceOverides() {
			return instanceAppearanceOverideList == null ? null : instanceAppearanceOverideList.iterator();
		}

		public String toString() {
			return "<" + name + " " + id + ">";
		}

		public Object clone() {
			try {
				CompInst c = (CompInst) super.clone();
				c.parent = null;
				c.child = null;
				return c;
			} catch (CloneNotSupportedException e) {
				return null;
			}
		}
	}

	public static class CompInstComparator implements Comparator<CompInst>, Serializable {
		private static final long serialVersionUID = -2961660556176558347L;

		private int sortChildren = 1;

		public CompInstComparator(int sortChildren) {
			if (sortChildren == 1 || sortChildren == -1) {
				this.sortChildren = sortChildren;
			}
		}

		public int getSortChildren() {
			return sortChildren;
		}

		public int compare(CompInst i1, CompInst i2) {
			return i1.child.name.compareTo(i2.child.name) * sortChildren;
		}
	}

	public static class CViewable implements Cloneable {
		public int viewable_type;
		public String filename;
		public String displayname;
		public int type;
		public int ecad_type;
		public boolean sheetOfPrevious;
		public boolean nextDrawingIsContinuation;
		public ArrayList<CDrawingSheet> drawingSheetList = null;

		public CViewable(Comp c, int t, String f, String d, int tt) {
			this(c, t, f, d, tt, 0, false, false);
		}

		public CViewable(Comp c, int t, String f, String d, int tt, int ett, boolean sop, boolean ndic) {
			viewable_type = t;
			filename = f;
			displayname = d;
			type = tt;
			ecad_type = ett;
			sheetOfPrevious = sop;
			nextDrawingIsContinuation = ndic;
			if (c != null) {
				if (c.viewableList == null)
					c.viewableList = new ArrayList<CViewable>(1);
				c.viewableList.add(this);
			}
		}

		public int getViewableType() {
			return viewable_type;
		}

		public boolean isDrawing() {
			return viewable_type == ED_DRAWING_TAG;
		}

		public boolean isDocument() {
			return viewable_type == ED_DOCUMENT_TAG;
		}

		public boolean isImage() {
			return viewable_type == ED_IMAGE_TAG;
		}

		public boolean isFile() {
			return viewable_type == ED_FILE_TAG;
		}

		public boolean isOLEDoc() {
			return viewable_type == ED_OLEDOC_TAG;
		}

		public boolean isIllustration() {
			return viewable_type == ED_ILLUSTRATION_TAG;
		}

		public boolean is3DIllustration() {
			return viewable_type == ED_ILLUSTRATION3D_TAG;
		}

		public boolean isECAD() {
			return viewable_type == ED_ECAD_TAG;
		}

		public boolean hasDrawingSheets() {
			return drawingSheetList != null && drawingSheetList.size() > 0;
		}

		public boolean hasPreviousSheetFile() {
			return sheetOfPrevious;
		}

		public boolean hasContinuationSheetFile() {
			return nextDrawingIsContinuation;
		}

		public Iterator drawingSheets() {
			return drawingSheetList == null ? null : drawingSheetList.iterator();
		}

		public String toString() {
			return "<Att " + viewable_type + " " + filename + " " + displayname + " " + type + " " + ecad_type + " "
					+ sheetOfPrevious + " " + nextDrawingIsContinuation + " " + drawingSheetList + ">";
		}

		public Object clone() {
			try {
				CViewable c = (CViewable) super.clone();
				return c;
			} catch (CloneNotSupportedException e) {
				return null;
			}
		}
	}

	public static class CDrawingSheet implements Cloneable {
		public int size;
		public int units;
		public float width, height;
		public int orientation;
		public String name;

		public CDrawingSheet(CViewable c, int s, int u, float w, float h, int o, String n) {
			size = s;
			units = u;
			width = w;
			height = h;
			orientation = o;
			name = n;
			if (c != null) {
				if (c.drawingSheetList == null)
					c.drawingSheetList = new ArrayList<CDrawingSheet>(1);
				c.drawingSheetList.add(this);
			}
		}

		public String toString() {
			return "<DrwSht " + size + " (" + units + " " + width + " " + height + ") " + orientation + " " + name
					+ ">";
		}

		public Object clone() {
			try {
				CDrawingSheet c = (CDrawingSheet) super.clone();
				return c;
			} catch (CloneNotSupportedException e) {
				return null;
			}
		}
	}

	public static class CView implements Cloneable {
		public String name;
		public float[] orientation;

		public CView(Comp c, String n, float[] o) {
			name = n;
			orientation = o;
			if (c != null) {
				if (c.viewList == null)
					c.viewList = new ArrayList<Cloneable>(1);
				c.viewList.add(this);
			}
		}

		public String toString() {
			return "<View " + name + " "
					+ (orientation == null ? "null"
							: orientation[0] + " " + orientation[1] + " " + orientation[2] + " " + orientation[3])
					+ ">";
		}

		public Object clone() {
			try {
				CView c = (CView) super.clone();
				return c;
			} catch (CloneNotSupportedException e) {
				return null;
			}
		}
	}

	public static class CViewState implements Cloneable {
		public int viewstate_type;
		public String name;
		public int index = 0;
		public String filename = null;

		public CViewState(Comp c, int t, String n) {
			viewstate_type = t;
			name = n;
			if (c != null) {
				if (c.viewStateList == null)
					c.viewStateList = new ArrayList<CViewState>(1);
				c.viewStateList.add(this);
			}
		}

		public String toString() {
			return "<ViewState " + viewstate_type + " " + name + " " + index + " " + filename + ">";
		}

		public Object clone() {
			try {
				CViewState c = (CViewState) super.clone();
				return c;
			} catch (CloneNotSupportedException e) {
				return null;
			}
		}
	}

	public static class Appearance implements Cloneable {
		public float[] base_color = null; // [3] range 0.0 - 1.0
		public float[] ambient_color = null; // [3] range 0.0 - 1.0
		public float[] diffuse_color = null; // [3] range 0.0 - 1.0
		public float[] emissive_color = null; // [3] range 0.0 - 1.0
		public float[] specular = null; // [4] range 0.0 - 128.0.0
		public float[] opacity = null; // [1] range 0.0 - 1.0

		public Appearance() {
		}

		public String toString() {
			StringBuffer buf = new StringBuffer();
			if (base_color != null) {
				for (int i = 0; i < 3; i++)
					buf.append(base_color[i]).append(" ");
			}
			buf.append("| ");
			if (ambient_color != null) {
				for (int i = 0; i < 3; i++)
					buf.append(ambient_color[i]).append(" ");
			}
			buf.append("| ");
			if (diffuse_color != null) {
				for (int i = 0; i < 3; i++)
					buf.append(diffuse_color[i]).append(" ");
			}
			buf.append("| ");
			if (emissive_color != null) {
				for (int i = 0; i < 3; i++)
					buf.append(emissive_color[i]).append(" ");
			}
			buf.append("| ");
			if (specular != null) {
				for (int i = 0; i < 4; i++)
					buf.append(specular[i]).append(" ");
			}
			buf.append("| ");
			if (opacity != null) {
				buf.append(opacity[0]).append(" ");
			}

			return "<App " + buf.toString() + ">";
		}

		public Object clone() {
			try {
				Appearance c = (Appearance) super.clone();
				return c;
			} catch (CloneNotSupportedException e) {
				return null;
			}
		}
	}

	public static class InstAppearance implements Cloneable {
		public Appearance appearance = null;
		public boolean recurse = true;
		public String instancePath = null;
		public String[] faceids = null;

		public InstAppearance(CompInst ci, Appearance a) {
			appearance = a;
			if (ci != null) {
				if (ci.instanceAppearanceOverideList == null)
					ci.instanceAppearanceOverideList = new ArrayList<InstAppearance>(1);
				ci.instanceAppearanceOverideList.add(this);
			}
		}

		public InstAppearance(Comp c, Appearance a) {
			appearance = a;
			if (c != null) {
				if (c.instanceAppearanceOverideList == null)
					c.instanceAppearanceOverideList = new ArrayList<InstAppearance>(1);
				c.instanceAppearanceOverideList.add(this);
			}
		}

		public String toString() {
			return "<InstApp " + instancePath + ">";
		}

		public Object clone() {
			try {
				InstAppearance c = (InstAppearance) super.clone();
				return c;
			} catch (CloneNotSupportedException e) {
				return null;
			}
		}
	}
}
