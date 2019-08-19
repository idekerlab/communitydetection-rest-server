
[jetty]: http://eclipse.org/jetty/
[maven]: http://maven.apache.org/
[java]: https://www.oracle.com/java/index.html
[git]: https://git-scm.com/

[make]: https://www.gnu.org/software/make

Community Detection REST Service
==================================

[![Build Status](https://travis-ci.org/coleslaw481/communitydetection_rest_server.svg?branch=master)](https://travis-ci.org/coleslaw481/communitydetection_rest_server) 
[![Coverage Status](https://coveralls.io/repos/github/coleslaw481/communitydetection_rest_server/badge.svg)](https://coveralls.io/github/coleslaw481/communitydetection_rest_server)

Provides REST service for several CommunityDetection algorithms.

This service runs using an embedded [Jetty][jetty] server and is invoked
from the command line. 


Requirements
============

* Centos 6+, Ubuntu 12+, and most other linux distributions should work
* [Java][java] 8+ **(jdk to build)**
* [Make][make] **(to build)**
* [Maven][maven] 3.3 or higher **(to build)** -- tested with 3.6

Special software to install (cause we haven't put these into maven central)

* [ndex-enrichment-rest-model](https://github.com/coleslaw481/communitydetection-rest-model) built and installed via `mvn install`



Building Community Detection REST Service
=========================================

Commands build Community Detection REST Service assuming machine has [Git][git] command line tools 
installed and above Java modules have been installed.

```Bash
# In lieu of git one can just download repo and unzip it
git clone https://github.com/coleslaw481/communitydetection_rest_server.git

cd communitydetection_rest_server
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

# Json fragment that is a mapping of algorithm names to docker images
communitydetection.algorithm.map = {"louvain": "coleslawndex/testlouvain"}

# Sets HOST URL prefix (value is prefixed to Location header when query is invoked. Can be commented out)
# communitydetection.host.url = http://ndexbio.org
# Sets directory where log files will be written for Jetty web server
runserver.log.dir = /tmp/logs

# Sets port Jetty web service will be run under
runserver.port = 8081

# sets Jetty Context Path for Community Detection
runserver.contextpath = /

# Valid log levels DEBUG INFO WARN ERROR ALL
runserver.log.level = INFO

```

Replace **/tmp** paths with full path location to **communitydetection** directory 
created earlier.


### Step 2 Run the service

```bash
java -jar communitydetection.jar --mode runserver --conf communitydetection.conf
```

COPYRIGHT AND LICENSE
=====================

TODO

Acknowledgements
================

TODO
