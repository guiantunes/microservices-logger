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

It's important to understand some conventions that we are assuming to enable the graph to be built. An API named 'api1' should call
other apis as an authenticated entity with the same 'api1' as username. In order to avoid out of context calls that could break our graph, 
further configuration is required. Let's protect our newly created API with the key-auth plugin.

```{r, engine='bash'}

curl -i -X POST \
	--url http://<kong-url>:8001/apis/<api-name/plugins \
	--data 'name=key-auth'
	
```

Now only authenticated users can make a call to this api. Next step is to create a consumer for this API so this API can call others

```{r, engine='bash'}

curl -i -X POST \
	--url http://<kong-url>:8001/consumers \
	--data 'username=<api-name>'

curl -i -X POST \
	--url http://<kong-url>:8001/consumers/<api-name>/key-auth/ -d ""
	
```





