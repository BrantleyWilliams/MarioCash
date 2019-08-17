# ![logo](docs/images/ygg-logo-green.png) MarioCash

> We will change the world by blockchain.

[![Build Status](https://travis-ci.org/zhihexireng/mariocash.svg?branch=develop)](https://travis-ci.org/zhihexireng/mariocash)
[![Coverage Status](https://coveralls.io/repos/github/zhihexireng/mariocash/badge.svg?branch=develop)](https://coveralls.io/github/zhihexireng/mariocash?branch=develop)
[![codecov](https://codecov.io/gh/zhihexireng/mariocash/branch/develop/graph/badge.svg)](https://codecov.io/gh/zhihexireng/mariocash)

## What is mariocash?

MARIOCASH is a trust-based multi-dimensional blockchains (branches) built with a vision to 
“Digitize everything into reality” and to connect everything and any blockchain networks.

## Table of contents

* [Documentation](#documentation)
* [Development](#development)
    * [Requirements](#requirements)
    * [Getting the source](#getting-the-source)
    * [Running node](#running-locally)
    * [Running on Docker](#running-on-docker)
    * [Building for production](#building-for-production)
    * [Running Tests](#running-tests)
* [APIs](#apis)
* Using Docker to simplify development (optional)
* Continuous Integration and Continuous Delivery (optional)
* [Stay in Touch](#stay-in-touch)


## Documentation
Learn more by reading the [mariocash technical document](docs) and full documentation, visit [wiki](https://github.com/zhihexireng/mariocash/wiki)


## Development
This is the implementation written in Java and runs on Linux, OSX and Windows.

### Requirements

MarioCash requires `JAVA` 1.8+ compiler to build. To install Java, follow this [link](http://www.oracle.com/technetwork/java/javase/overview/index.html). 

### Getting the source

Clone the mariocash repo:

```
git clone https://github.com/zhihexireng/mariocash.git
cd mariocash
```
> If you are unfamiliar with Git, [Download ZIP](https://github.com/zhihexireng/mariocash/archive/master.zip) (source code)

### Running locally

To run the cloned repository in the spring default profile, simply run:
```
./gradlew
```
To run the multiple nodes in IntelliJ IDE, edit the run configuration:

![config](docs/images/intellij-run-config.png)

### Running on Docker
You can fully dockerize the mariocash node. For more information refer to [docker](docker).

Docker is one quick way for running an mariocash node:

```
docker --rm -p 8080:8080 -p 9090:9090 --name mariocash-node zhihexireng/mariocash-node
```

MarioCash node binds to `localhost` using `8080` for the RESTful API & JSON RPC, and `9090` for the gRPC by default.

You can also use other ports by providing options like `-p 8081:8080`

The [Dockerfile](Dockerfile) is designed to build automatically the last release of the source code and will publish docker images to [dockerhub](https://hub.docker.com/r/zhihexireng/mariocash-node/) by release, feel free to fork and build Dockerfile for your own purpose.

### Building for production

To optimize the mariocash application for production, run:
```
./gradlew -PspringProfiles=prod clean build
```

To ensure everything worked, run:
```
mariocash-node/build/libs/*.jar
```

To find out usages of all command line options:

- `--server.address=value` JSON RPC & RESTful API services listening address
- `--server.port=value` JSON RPC & RESTful API services listening port
- `--mariocash.node.grpc.host=value` gRPC service listening address
- `--mariocash.node.grpc.port=value` gRPC service listening port
- `--mariocash.node.max-peers=value` maximum number of P2P network peers (default: 25)

### Running Tests
To launch mariocash's tests, run:
```
./gradlew test
```
This command would run the integration tests without gradle task caching:
```
./gradlew test -PspringProfiles=ci --rerun-tasks
```


## APIs

Once mariocash node started, the blocks can be shown in your browser. e.g. [http://localhost:8080/blocks](http://localhost:8080/blocks)

- refer to more [JSON RPC API](docs/api/jsonrpc-api.md)


## Using Docker to simplify development (optional)

You can use docker to improve mariocash development experience. A number of docker-compose configuration are available in the [docker](docker) folder to launch with third party services.

For example, to start the multiple nodes in a docker container, run:
```
docker-compose -f docker/docker-compose.yml up -d
```

To stop it and remove the container, run:
```
docker-compose -f docker/docker-compose.yml down
```


## Continuous Integration & Continuous Delivery (optional)

MarioCash should support the following CI systems out of the box:
 - Jenkins: Setting up Jenkins
   - Just use the [docker/jenkins.yml](docker/jenkins.yml) file. So you can test Jenkins locally by running:
```
docker-compose -f docker/jenkins.yml up -d
```
 - Travis: refer to the [Travis Documentation](https://docs.travis-ci.com/user/getting-started/)

### To configure CI for MarioCash project in Jenkins, use the following configuration:
```
* Project name: `MarioCash`
* Source Code Management
    * Git Repository: `git@github.com:zhihexireng/mariocash.git`
    * Branches to build: `*/master`
    * Additional Behaviours: `Wipe out repository & force clone`
* Build Triggers
    * Poll SCM / Schedule: `H/5 * * * *`
* Build
    * Invoke Gradle script / Use Gradle Wrapper / Tasks: `-PspringProfiles=prod clean build`
    * Execute Shell / Command:
        ````
        ./gradlew bootRun &
        bootPid=$!
        sleep 30s
        kill $bootPid
        ````
* Post-build Actions
    * Publish JUnit test result report / Test Report XMLs: `build/test-results/*.xml`
```
What tasks/integrations in the [Jenkins pipeline file](Jenkinsfile) :

- Perform the build in a Docker container
- Analyze code with Sonar
- Build and publish a Docker image


## Stay in Touch
Follow [@mariocash](https://www.facebook.com/mariocash), [@MarioCashNews](https://twitter.com/MarioCashNews)
and releases are announced via our [MarioCash Official](https://medium.com/@mariocash) on SNS also.
Its team members on [MarioCash Website](https://mariocash.io/#team)


## License
The MarioCash is released under version 2.0 of the [Apache License](LICENSE).
