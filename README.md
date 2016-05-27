# microservices-logger

This is a simple application that receives calls from an API Gateway (Kong) and store
the information in a Graph database (Neo4j) in order to make it possible to make data analysis
over this data.

## Kong

This implementation is relying in [Kong](https://getkong.org/) [HTTP Log Plugin](https://getkong.org/plugins/http-log/).

In order to make it work you should configure all your apis to send logging information to this application.

## Neo4j

For simplicity this implementation is using an embedded Neo4j database. The database is acessed
over the [TinkerPop Api](http://tinkerpop.apache.org/) so it would be easy to adapt the application
to support a different database if necessary.

## Architecture

![Architecture Diagram](/docs/Architecture.png)

The application is designed to sit aside of a kong installation receiving calls from the http-log plugin. The endpoint
that needs to be configured in kong is named "/logKong" and by default configured to be listening on the port 8080.

The current implementation is also embedding a neo4j community database accessed by the gremlin-neo4j plugin from tinkerpop.

Inside the database, the graph respects the following schema:

![Graph Schema](/docs/graph_schema.png)

## Setup

This version ships with an example docker-compose.yml that specifies a kong container connected to a cassandra container and 
a container with a containerized version of this application. It's already working but it doesn't ship with any configured API

### Kong setup

If you need help setting up Kong please refer to its [documentation](https://getkong.org/docs/). This document will assume that it's already running

#### Add an API

```{r, engine='bash'}

curl -i -X POST \
	--url http://<kong-url>:8001/apis/ \
	--data 'name=<api-name>'
	--data 'upstream_url=<api-url>'
	--data 'request_host=<api-host>'
	
```
This will add an api to Kong enabling you to fire calls from clients like this:

```{r, engine='bash'}

curl -i -X POST \
	--url http://<kong-url>:8000/ \
	--header 'Host: <api-host>'
	
```

And Kong will proxy the call to the API server.

#### Conventions and consumers creation

It's important to understand some conventions that we are assuming to enable the graph to be built. An API named 'api1' should call
other apis as an authenticated entity with the same 'api1' as username. In order to avoid out of context calls that could break our graph,
further configuration is required. Let's protect our newly created API with the key-auth plugin.

```{r, engine='bash'}

curl -i -X POST \
	--url http://<kong-url>:8001/apis/<api-name/plugins \
	--data 'name=key-auth'
	
```

Now only authenticated users can make a call to this api. Next step is to create a consumer and a key for this API so this API can call others

```{r, engine='bash'}

curl -i -X POST \
	--url http://<kong-url>:8001/consumers \
	--data 'username=<api-name>'

curl -i -X POST \
	--url http://<kong-url>:8001/consumers/<api-name>/key-auth/ -d ""
	
```

A key for this api is now generated and it can call other apis with that key. You should replicate that process for every microservice
you have in your system. An example call to this API would be like that:

```{r, engine='bash'}

curl -i -X GET \
	--url http://<kong-url>:8000/<api1-endpoint> \
	--header 'Host: <api1-host>'
	--header 'apikey: <api2-key>'

```

All this is useless if we don't actually collect the information being transacted so we need to setup the microservices-logger 
application so we can start understand the connections.

#### Microservices-logger configuration

As soon as we have all our microservices configured, we should add the http-log plugin to every API that
we want to monitor. We can achieve that using the command below:

```{r, engine='bash'}

  curl -X POST http://<kong-url>:8001/apis/<api-name>/plugins \
    --data "name=http-log" \
    --data "config.http_endpoint=http://<microservices-logger-url>:8080/logKong" \
    --data "config.method=POST" \
    --data "config.timeout=1000" \
    --data "config.keepalive=1000"

```

With that, all the calls to this API will now be tracked by the application and the information
collected stored into the Graph.

## Visualization

This implementation does not provide any visualization tool yet. If you want to explore the
graph so you can visualize dependencies, how much a particular endpoint is called and so
on, I would recommend you to start a standalone Neo4j instance configuring it to read the data
produced by the application.

The application is pre configured to store neo4j data into the /data path. If you are using
it outside of a container you can just configure your neo4j instance to read data from there.

```

# neo4j-server.properties:

org.neo4j.server.database.location=/data

```

If you're using the containerized version, you should just start a neo4j container using volumes
from the microservices-logger application.

You'll be able to view the data like this:

![Graph Visualization](/docs/graph.png)

If you have any doubt or have any problem, please file an issue into the repo.

