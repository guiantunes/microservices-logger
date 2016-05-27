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



