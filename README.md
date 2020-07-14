
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
=============

* MacOS, Centos 6+, Ubuntu 12+, and most other Linux distributions should work
* [Java][java] 11+ **(jdk to build)**
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

The following steps cover running the Community Detection REST Service locally.


**NOTE:** The instructions below should work on MacOS and Linux, but have not been tested on Windows

### Step 1 Install required software

To run Community Detection REST Service locally the following software must
be installed: 

* [Java][java] 11+

* [Docker]

### Step 2 Create directories

Open a terminal and run the following commands to create `communitydetection`, 
needed subdirectories, and to set `communitydetection` as the current working directory.

```bash
# create directories communitydetection/logs and communitydetection/tasks
mkdir -p communitydetection/logs communitydetection/tasks
cd communitydetection
```

### Step 3 Download or build Community Detection REST Service Application

The Community Detection REST Service Application can be obtained in one of two ways:

 1. From Releases link on this repository, download the the Community Detection REST Service Application jar file
    to the `communitydetection` directory created in [Step 1](communitydetection-rest-server#step-1-create-directories-and-configuration-files).

**OR**

 2. To build follow [these instructions](communitydetection-rest-server#building-community-detection-rest-service)
    and the jar file it will be located under the `target/` directory upon successful build

**NOTE:** The Community Detection REST Service Application jar file is named in this format: `communitydetection-rest-<VERSION>-jar-with-dependencies.jar`

### Step 3 Create main configuration file

This step creates a template configuration file used by the Community Detection REST Service. 
An example `communitydetection.conf` file can be found [here](https://github.com/cytoscape/communitydetection-rest-server/blob/master/systemdservice/communitydetection.conf)

```bash
# Generate template configuration file
java -jar communitydetection-rest-0.7.1-jar-with-dependencies.jar --mode exampleconf > communitydetection.conf
```

**NOTE:** Be sure to replace `/tmp` with full path to `logs` and `tasks` in `communitydetection.conf` file

### Step 4 Obtain the algorithms configuration file

The algorithms configuration file denotes what algorithms 
are accessible by the Community Detection REST Service.

Download [this file](https://github.com/cytoscape/communitydetection-rest-server/blob/master/systemdservice/communitydetectionalgorithms.json)
saving it to the same directory where `communitydetection.conf` file resides. 
Be sure the file name remains `communitydetectionalgorithms.json`


### Step 3 Run the service

From the open terminal with current working directory still set to `communitydetection`
run the following command: 

```bash
java -jar communitydetection-rest-0.7.1-jar-with-dependencies.jar --mode runserver --conf communitydetection.conf
```

### Step 4 Test the service

Open a browser and visit: http://localhost:8081/cd/ 

If successful a swagger page should be displayed.


### 

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
