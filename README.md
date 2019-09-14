
[jetty]: http://eclipse.org/jetty/
[maven]: http://maven.apache.org/
[java]: https://www.oracle.com/java/index.html
[git]: https://git-scm.com/

[make]: https://www.gnu.org/software/make

Community Detection REST Service
==================================

[![Build Status](https://travis-ci.org/ndexbio/communitydetection-rest-server.svg?branch=master)](https://travis-ci.org/ndexbio/communitydetection-rest-server) 
[![Coverage Status](https://coveralls.io/repos/github/ndexbio/communitydetection-rest-server/badge.svg)](https://coveralls.io/github/ndexbio/communitydetection-rest-server)

Provides formated and readily callable REST service for several popular CommunityDetection algorithms. 

This service runs using an embedded [Jetty][jetty] server and is invoked
from the command line. 


Requirements
============

* Centos 6+, Ubuntu 12+, and most other linux distributions should work
* [Java][java] 8+ **(jdk to build)**
* [Make][make] **(to build)**
* [Maven][maven] 3.3 or higher **(to build)** -- tested with 3.6

Special software to install (cause we haven't put these into maven central)

* [ndex-enrichment-rest-model](https://github.com/ndexbio/communitydetection-rest-model) built and installed via `mvn install`



Building Community Detection REST Service
=========================================

Commands build Community Detection REST Service assuming machine has [Git][git] command line tools 
installed and above Java modules have been installed.

```Bash
# In lieu of git one can just download repo and unzip it
git clone https://github.com/ndexbio/communitydetection-rest-server.git

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

Packaged from [https://louvain-igraph.readthedocs.io/en/latest/index.html](https://louvain-igraph.readthedocs.io/en/latest/index.html)

- Work by iteratively maximazing modularity in each community, building hierarchy from the bottom layer (single nodes) up to the root layer.
- Input graph(s): Can be an edgelist directory for single graph or a list of edgelist directories for multiple graphs that have some share of nodes. If given multiple graphs, will automatically perform overlapping commnunity detection.
- overlaping vs. deep community detection: cannot detect deep hierarchical clustering (hierarchy deeper than 2 layers; in other words, not just simple community assignment) and overlapping communities at the same time; in other words, 'overlap' and 'deep' cannot be both set to true.
- Graph weight: works on both weighted and unweighted graph; does not support negative edge weight
- `param` directed: true/false, whether a graph is directed or not (does not support half directed half undirected graph). *default: false*
- `param` overlap: true/false, whether or not allowing overlapping community detection. *default: false* **Please see 'Input graph(s)' and 'overlapping vs. deep community detection' for conflicts**
- `param` deep: true/false, detect hierarchy or not. *default: true* **Please see 'Input graph(s)' and 'overlapping vs. deep community detection' for conflicts**
- `param` interslice_weight: the weight on the new edge connecting same nodes shared by different graphs when multiple graphs is used. *default: 0.1*
- `param` resolution_parameter: resulotion is an indicator of the number of communities, and since louvain build communites by iteratively merging child nodes, if the resolution parameter is high, louvain will perform this merging slowly while retaining a high number of relatively small commnities and stop merging and add the root node when number of community is still high; if the resolution parameter is low, louvain will quickly merge to a small number of relatively large communities and stop. (Short version: want more communities, use higher resolution_parameter and vice versa) *default: 0.1*

### Infomap

Packaged from [mapequation](https://www.mapequation.org/code.html)

- A method based on Flux. Detect communities from dynamics on the network.
- Result hierarchy shallower than louvain, but can possess overlapping communites.
- Work on any graph
- `param` overlap: true/false, whether allow overlapping communites to be detected. When set to true and the network is large, can render substantial increase in runtime. *default: false*
- `param` direct: true/false, whether the input network is directed or not. *default: false*
- `param` markovtime: indicate link flow/ cost of moving between modules. Higher for less communities. *default: 0.75*

### CliXO

Packaged from [https://github.com/fanzheng10/CliXO](https://github.com/fanzheng10/CliXO)
Original paper: Kramer M, Dutkowski J, Yu M, Bafna V, Ideker T. Inferring gene ontologies from pairwise similarity data. Bioinformatics, 30: i34-i42. 2014. doi: 10.1093/bioinformatics/btu282

- A classical method that iteratively detects hierarchy based on cliques from changing edge weight threshold.
- Only work on graph with edge weights.
- If want the runtime to be reasonable, the maximum size of network is of hundreds of nodes.
- `param` alpha: *default:0.1*
- `param` beta: *default:0.5*


COPYRIGHT AND LICENSE
=====================

TODO

Acknowledgements
================

TODO
