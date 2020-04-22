
[jetty]: http://eclipse.org/jetty/
[maven]: http://maven.apache.org/
[java]: https://www.oracle.com/java/index.html
[git]: https://git-scm.com/
[rest]: https://en.wikipedia.org/wiki/Representational_state_transfer
[make]: https://www.gnu.org/software/make
[cdapp]: https://github.com/cytoscape/cy-community-detection

Community Detection REST Service
===================================

[![Build Status](https://travis-ci.com/cytoscape/communitydetection-rest-server.svg?branch=master)](https://travis-ci.com/cytoscape/communitydetection-rest-server) 
[![Coverage Status](https://coveralls.io/repos/github/cytoscape/communitydetection-rest-server/badge.svg)](https://coveralls.io/github/cytoscape/communitydetection-rest-server)

Provides formated and readily callable [REST][rest] service for several popular Community Detection algorithms. 
The service is used by the [Cytoscape Community Detection App][cdapp]

This service runs using an embedded [Jetty][jetty] server and is invoked
from the command line. 


Requirements
============

* Centos 6+, Ubuntu 12+, and most other linux distributions should work
* [Java][java] 8+ **(jdk to build)**
* [Make][make] **(to build)**
* [Maven][maven] 3.3 or higher **(to build)** -- tested with 3.6

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

### Step 1 Create directories and configuration file

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

# Json fragment that is a mapping of algorithm names to docker images
communitydetection.algorithm.map = {"infomap": "coleslawndex/infomap"}

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


### Step 2 Run the service

```bash
java -jar communitydetection.jar --mode runserver --conf communitydetection.conf
```

Input/Output Format
================

TODO

Community Detection Algorithms
===========================

### Louvain

See https://github.com/idekerlab/cdlouvain

### Infomap

Packaged from [mapequation](https://www.mapequation.org/code.html)

- A method based on Flux. Detect communities from dynamics on the network.
- Result hierarchy shallower than louvain, but can possess overlapping communites.
- Work on any graph
- `param` overlap: true/false, whether allow overlapping communites to be detected. When set to true and the network is large, can render substantial increase in runtime.  *default: false*
- `param` direct: true/false, whether the input network is directed or not.  *default: false*
- `param` markovtime: a positive number indicating link flow/ cost of moving between modules. Higher for less communities.  *default: 0.75*

### CliXO

Packaged from [https://github.com/fanzheng10/CliXO](https://github.com/fanzheng10/CliXO)
Original paper: Kramer M, Dutkowski J, Yu M, Bafna V, Ideker T. Inferring gene ontologies from pairwise similarity data. Bioinformatics, 30: i34-i42. 2014. doi: 10.1093/bioinformatics/btu282

- A classical method that iteratively detects hierarchy based on cliques from changing edge weight threshold.
- Only work on graph with edge weights.
- If want the runtime to be reasonable, the maximum size of network is of hundreds of nodes.
- `param` alpha: threshold between cluster layers  *default:0.1*
- `param` beta: merge similarity for overlapping clusters  *default:0.5*


COPYRIGHT AND LICENSE
=====================

[Click here](LICENSE)

Acknowledgements
================

TODO
