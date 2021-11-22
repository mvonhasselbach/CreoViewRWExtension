# CreoViewRWExtension #

This Thingworx Extension allows to read CreoView .pvz and .pvs files from a TWX FileRepository and 
return the product structure metadata that is included in the .pvs file in it. Metadata is:
- attributes
- transformation/position information (relative and absolute, in Matrix- and Euler representation)
- component paths
- bounding boxes
- views
- locators if included


It support different output formats. See sample data sets in [./data](./data) :

1. the **DEFAULT** format is the Vuforia Studio *metadata.json* format. This version of it has all the content of what Vuforia Studio can produce for static experiences
   PLUS the following additional attributes in the **__PV_SystemProperties** section:
	- **Component Bounds**: Bounding Box info in 6 numbers (x,y,z of min point, x,y,z of max point) represented as a space-separated string. 
	  The coordinates will be relative to the origin of the component.
	- **Model Bounds**: Bounding Box in Model space, i.e. relative to the origin of the root assembly in the structure (global coordinates)
	- **Instance Location**: position of the origin of this component, relative to its direct parent assembly, represented as 6 numbers :
	  3 euler angles attitude, heading and bank and 3 translation coordinates x,y,z
	- **Model Location**: position of the origin of the component in Model space (global location)
	- **Instance Transformation**: 4x4 Transformation Matrix of the location information relative to its direct parent assembly, 
	  represented as a space separated string with 16 numbers
	- **Model Transformation**: 4x4 Transformation Matrix representation of the global location
	- **Views**: name and float[4] form of orientation
	- **Locators**: label, id and data in 3x3 Matrix format.
2. the **WT_SED2_NESTED** 'native' export format that is based on how data is represented in the Windchill WVS Structure2 class.
   This format will be the basis for writing .pvs files as well. There is a **WT_SED2_FLAT** format as well that represents all nodes
   of the structure as a flat list, indexed by the ComponentID Path of each node, similar to the **Default** format.
   Both formats are more complete than the other formats. They can include Viewable, Views, ViewState, Appearance and Locators information when they are included in the pvz.
3. the **PVS2JSON** format from Steve Ghee which is slightly extended and also includes bounding box info. (Sorry - This is not yet implemented!)

Furthermore the Extension supports filtering of the returned attributes/properties. This can be handy to remove sensitive information 
and also reduce the size of the returned data.

The pvz file reader supports nested/referenced pvs files that are typically generated by Positioning Assemblies. The Reader will try to resolve them and merge 
the proxy nodes and their pvs structure representation so that you'll end up with one big structure. 


## Usage ##

The Extension will add a *CreoViewRWHelper* Resource to Thingworx, that provides services for pvz and pvs reading and writing.
The Extension also has a sample Thing **CreoViewRWTester** that has the services for direct use. You will have to put a .pvz or .pvs 
file in the SystemRepository and specify the path to this file when invoking the **GetPVZDefaultJSON** service. The output will be JSON formatted.

You can use this service in multiple different scenarios where you want to work with dynamic geometry data in experiences, e.g.
- You want to pull a CreoView representation from Windchill and want to show metadata properties for the geometry in your experiences
- You want to dynamically compose the pvz based on a metadata filter and use the generated filtered pvz in the experience.
- You want to filter specific sensitive attributes from the metadata but still show other metadata in your experience.
- You want to calculate rough proximity of components to the viewer or selected other components based on global bounding box info of the components.
- You want to merge metadata from different sources and show them in the experience in the same way you show Windchill/CAD metadata.


Endless possibilities...
A lot of those things can also be done client-side inside the Experience. This extension gives you a choice where to do it.
