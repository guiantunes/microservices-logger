# microservices-logger

This is a simple application that receives calls from an API Gateway (Kong) and store
the information in a Graph database (Neo4j) in order to make it possible to make data analysis
over this data.

# Kong

This implementation is relying in [Kong](https://getkong.org/) [HTTP Log Plugin](https://getkong.org/plugins/http-log/).

In order to make it work you should configure all your apis to send logging information to this application.

# Neo4j

For simplicity this implementation is using an embedded Neo4j database. The database is acessed
over the [TinkerPop Api](http://tinkerpop.apache.org/) so it would be easy to adapt the application
to support a different database if necessary.

# Architecture

![Architecture Diagram](/docs/Architecture.png)

