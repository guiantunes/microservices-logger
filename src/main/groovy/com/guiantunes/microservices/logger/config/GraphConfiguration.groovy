package com.guiantunes.microservices.logger.config

import org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jGraph
import org.apache.tinkerpop.gremlin.structure.Graph
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Created by guilhermeantunes on 26/05/16.
 *
 * Create a graph
 */
@Configuration
class GraphConfiguration {

    /**
     * Create a new Graph, in this example we are using a neo4j embedded database
     * @return
     */
    @Bean
    Graph configureGraph () {
        Graph g = Neo4jGraph.open('/data')

        g
    }
}
