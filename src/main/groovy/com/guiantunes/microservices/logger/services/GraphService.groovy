package com.guiantunes.microservices.logger.services

import com.guiantunes.microservices.logger.model.ApiCall
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource
import org.apache.tinkerpop.gremlin.structure.Graph
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * Created by guilhermeantunes on 26/05/16.
 *
 * Helper class that encapsulates Graph Access
 */
@Service
class GraphService {

    final Logger logger = LoggerFactory.getLogger(GraphService.class)

    @Autowired
    Graph graph

    /**
     * Save an api call to the Graph
     * @param apiCall Call to be saved
     * @return nothing
     */
    def saveApiCall (ApiCall apiCall) {

        // Traversal
        GraphTraversalSource g = graph.traversal()

        def clientVertex
        def serverVertex
        def endPointVertex
        def apiCallEdge

        // look for the client api vertex
        def clientVertexTraversal = g.V().has("api", "name", apiCall.client.name)


        // if we have it already
        if (clientVertexTraversal.hasNext()){

            // grab a reference to it
            clientVertex = clientVertexTraversal.next()

        } else {

            // if not, create a new vertex
            clientVertex = g.addV("api").property("name", apiCall.client.name).next()

        }

        // look for the server api vertex
        def serverVertexTraversal = g.V().has("api", "name", apiCall.endpoint.owner.name)

        // if we have it already
        if (serverVertexTraversal.hasNext()) {

            // grab a reference to it
            serverVertex = serverVertexTraversal.next()
        } else {

            // if not, create a new vertex
            serverVertex = g.addV("api").property("name", apiCall.endpoint.owner.name).next()
        }

        // look for the endpoint vertex from the server vertex
        def endPointVertexTraversal = g.V(serverVertex).out("owns").has("endpoint", "name", apiCall.endpoint.name)

        // if we found it
        if (endPointVertexTraversal.hasNext()) {

            // grab a reference to it
            endPointVertex = endPointVertexTraversal.next()
        } else {

            // if not create a new vertex
            endPointVertex = g.addV("endpoint").property("name",apiCall.endpoint.name).next()

            // add an edge from the server vertex to the newly created endpoint vertex
            serverVertex.addEdge("owns",endPointVertex)
        }

        // if we have an edge representing the call from the client to the endpoint
        if (g.V(clientVertex).out("calls").has("endpoint","name",apiCall.endpoint.name).in("owns").has("api", "name", apiCall.endpoint.owner.name).hasNext()) {

            // grab a reference to the edge
            apiCallEdge = g.V(clientVertex).outE("calls").and(g.V(endPointVertex).inE("calls")).next()

            // add a new call to the counter
            apiCallEdge.property("calls_count", apiCallEdge.property("calls_count").value() + 1)
        } else {

            // if not we create a new edge representing the call with a initial count of 1
            apiCallEdge = clientVertex.addEdge("calls", endPointVertex, "calls_count", 1)
        }

        logger.debug("Client Vertex: {}", clientVertex)
        logger.debug("Server Vertex: {}", serverVertex)
        logger.debug("Endpoint Vertex: {}", endPointVertex)
        logger.debug("ApiCall Edge: {}", apiCallEdge)

        // commit the transaction
        graph.tx().commit()

    }

}
