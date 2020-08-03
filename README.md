# CreoViewRWExtension #

This Extension allows to read CreoView .pvz files from FileRepositories or URLs and return the product structure metadata that is included in the .pvs file in it. Metadata is:
- attributes
- transformation/position information
- component paths
- bounding boxes


It support different output formats. See sample data sets in /data :

1. a 'native' export that is based on how data is represented in the Windchill WVS Structure2 class. 
2. a pvs2json compatible format. We support the slightly extended version from Steve Ghee that also includes bounding box info.