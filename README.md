
[jetty]: http://eclipse.org/jetty/
[maven]: http://maven.apache.org/
[java]: https://www.oracle.com/java/index.html
[git]: https://git-scm.com/
[rest]: https://en.wikipedia.org/wiki/Representational_state_transfer
[make]: https://www.gnu.org/software/make
[cdapp]: https://github.com/cytoscape/cy-community-detection
[docker]: https://www.docker.com/

Community Detection REST Service
===================================

[![Build Status](https://travis-ci.com/cytoscape/communitydetection-rest-server.svg?branch=master)](https://travis-ci.com/cytoscape/communitydetection-rest-server) 
[![Coverage Status](https://coveralls.io/repos/github/cytoscape/communitydetection-rest-server/badge.svg)](https://coveralls.io/github/cytoscape/communitydetection-rest-server)
[![Documentation Status](https://readthedocs.org/projects/cdaps/badge/?version=latest&token=d51549910b0a9d03167cce98f0f550cbacc48ec26e849a72a75a36c1cb474847)](https://cdaps.readthedocs.io/en/latest/?badge=latest)

Provides formatted and readily callable [REST][rest] service for several popular Community Detection algorithms. 
The service is used by the [Cytoscape Community Detection App][cdapp]

This service runs using an embedded [Jetty][jetty] server and is invoked
from the command line. 


Requirements
============

* Centos 6+, Ubuntu 12+, and most other linux distributions should work
* [Java][java] 8+ **(jdk to build)**
* [Make][make] **(to build)**
* [Maven][maven] 3.3 or higher **(to build)** -- tested with 3.6
* [Docker] **(to run algorithms)**

Building Community Detection REST Service
=========================================

Commands build Community Detection REST Service assuming machine has [Git][git] command line tools 
installed and above Java modules have been installed.

```Bash
# In lieu of git one can just download repo and unzip it
git clone https://github.com/cytoscape/communitydetection-rest-server.git

cd communitydetection-rest-server
mvn clean test install
```

The above command will create a jar file under **target/** named  
**communitydetection-rest-\<VERSION\>-jar-with-dependencies.jar** that
is a command line application


Running Community Detection REST Service
===========================================

The following steps cover how to create the Enrichment database.
In the steps below **communitydetection.jar** refers to the jar
created previously named **communitydetection-rest-\<VERSION\>-jar-with-dependencies.jar**

### Step 1 Create directories and configuration files

```bash
# create directory
mkdir -p communitydetection/logs communitydetection/tasks
cd communitydetection

# Generate template configuration file
java -jar communitydetection.jar --mode exampleconf > communitydetection.conf
```

The `communitydetection.conf` file will look like the following:

```bash
# Example configuration file for Community Detection service

# Sets Community Detection task directory where results from queries are stored
communitydetection.task.dir = /tmp/tasks

# Sets number of workers to use to run tasks
communitydetection.number.workers = 1

# Docker command to run
communitydetection.docker.cmd = docker

# Algorithm/ docker command timeout in seconds. Anything taking longer will be killed
communitydetection.algorithm.timeout = 180

# Path to file containing json of algorithms
communitydetection.algorithm.map = communitydetectionalgorithms.json

# Sets HOST URL prefix (value is prefixed to Location header when query is invoked. Can be commented out)
# communitydetection.host.url = http://ndexbio.org
# Sets directory where log files will be written for Jetty web server
runserver.log.dir = /tmp/logs

# Sets port Jetty web service will be run under
runserver.port = 8081

# Sets Jetty Context Path for Community Detection (the endpoint assumes /cd so if apache doesnt redirect from there then add /cd here
runserver.contextpath = /cd

# Valid log levels DEBUG INFO WARN ERROR ALL
runserver.log.level = INFO

```

Replace **/tmp** paths with full path location to **communitydetection** directory 
created earlier.

```bash
# Generate algorithms template file
java -jar communitydetection.jar --mode examplealgo > communitydetectionalgorithms.json
```

The `communitydetectionalgorithms.json` will look like the following:

```bash
{
  "algorithms" : {
    "louvain" : {
      "name" : "louvain",
      "displayName" : null,
      "description" : "Runs louvain community detection algorithm",
      "version" : "2.0.0",
      "dockerImage" : "ecdymore/slouvaintest",
      "inputDataFormat" : "EDGELIST",
      "outputDataFormat" : "COMMUNITYDETECTRESULT",
      "customParameters" : [ {
        "name" : "--directed",
        "displayName" : "Generate directed graph",
        "description" : "If set, generate directed graph",
        "type" : "flag",
        "defaultValue" : null,
        "validationType" : null,
        "validationHelp" : null,
        "validationRegex" : null,
        "minValue" : null,
        "maxValue" : null
      }, {
        "name" : "--configmodel",
        "displayName" : "Configuration Model",
        "description" : "Configuration model which must be one of following:: RB, RBER, CPM, Suprise, Significance, Default",
        "type" : "value",
        "defaultValue" : "Default",
        "validationType" : "string",
        "validationHelp" : "Must be one of following: RB, RBER, CPM, Suprise, Significance, Default",
        "validationRegex" : "RB|RBER|CPM|Suprise|Significance|Default",
        "minValue" : null,
        "maxValue" : null
      } ]
    },
    "gprofilersingletermv2" : {
      "name" : "gprofilersingletermv2",
      "displayName" : null,
      "description" : "Uses gprofiler to find best term below pvalue cut offusing a list of genes as input",
      "version" : "1.0.0",
      "dockerImage" : "coleslawndex/gprofilersingletermv2",
      "inputDataFormat" : "GENELIST",
      "outputDataFormat" : "MAPPEDTERMJSON",
      "customParameters" : [ {
        "name" : "--maxpval",
        "displayName" : "Maximum Pvalue",
        "description" : "Maximum pvalue to allow for results",
        "type" : "value",
        "defaultValue" : "0.00001",
        "validationType" : "number",
        "validationHelp" : "Must be a number",
        "validationRegex" : null,
        "minValue" : null,
        "maxValue" : null
      } ]
    },
    "clixo" : {
      "name" : "clixo",
      "displayName" : null,
      "description" : "Runs clixo community detection algorithm",
      "version" : "2.0.0",
      "dockerImage" : "coleslawndex/clixo:1.0",
      "inputDataFormat" : "EDGELIST",
      "outputDataFormat" : "COMMUNITYDETECTRESULT",
      "customParameters" : [ {
        "name" : "--alpha",
        "displayName" : "Alpha",
        "description" : "Threshold between clusters",
        "type" : "value",
        "defaultValue" : "0.1",
        "validationType" : "number",
        "validationHelp" : null,
        "validationRegex" : null,
        "minValue" : null,
        "maxValue" : null
      }, {
        "name" : "--beta",
        "displayName" : "Beta",
        "description" : "Merge similarity for overlapping clusters",
        "type" : "value",
        "defaultValue" : "0.5",
        "validationType" : "number",
        "validationHelp" : null,
        "validationRegex" : null,
        "minValue" : null,
        "maxValue" : null
      } ]
    },
    "infomap" : {
      "name" : "infomap",
      "displayName" : null,
      "description" : "Runs infomap community detection algorithm",
      "version" : "2.0.0",
      "dockerImage" : "ecdymore/sinfomaptest",
      "inputDataFormat" : "EDGELIST",
      "outputDataFormat" : "COMMUNITYDETECTRESULT",
      "customParameters" : [ {
        "name" : "--directed",
        "displayName" : "Assume Directed Links",
        "description" : "If set, infomap assumes directed links",
        "type" : "flag",
        "defaultValue" : null,
        "validationType" : null,
        "validationHelp" : null,
        "validationRegex" : null,
        "minValue" : null,
        "maxValue" : null
      }, {
        "name" : "--enableoverlapping",
        "displayName" : "Enable Overlapping",
        "description" : "If set, Let nodes be part of different and overlapping modules. Applies to ordinary networks by first representing the memoryless dynamics with memory nodes.",
        "type" : "flag",
        "defaultValue" : null,
        "validationType" : null,
        "validationHelp" : null,
        "validationRegex" : null,
        "minValue" : null,
        "maxValue" : null
      }, {
        "name" : "--markovtime",
        "displayName" : "Markov time",
        "description" : "Scale link flow with this value to change the cost of moving between modules. Higher for less modules",
        "type" : "value",
        "defaultValue" : "0.75",
        "validationType" : "number",
        "validationHelp" : "Should be a number",
        "validationRegex" : null,
        "minValue" : null,
        "maxValue" : null
      } ]
    }
  }
}

```


### Step 3 Run the service

```bash
java -jar communitydetection.jar --mode runserver --conf communitydetection.conf
```



Algorithms
===========

Each algorithm has its own repository and is packaged as a
[Docker](docker) image

### Community Detection Algorithms

 * [CliXO](https://github.com/idekerlab/cdclixo)

 * [Infomap](https://github.com/idekerlab/cdinfomap)

 * [Louvain](https://github.com/idekerlab/cdlouvain)

 * [OSLOM](https://github.com/idekerlab/cdoslom)

### Functional Enrichment/Term Mapping Algorithms

 * [Enrichr](https://github.com/idekerlab/cdenrichrgenestoterm)

 * [iQuery](https://github.com/idekerlab/cdiquerygenestoterm)

 * [gProfiler](https://github.com/idekerlab/cdgprofilergenestoterm)

COPYRIGHT AND LICENSE
=====================

[Click here](LICENSE)

Acknowledgements
================

TODO
