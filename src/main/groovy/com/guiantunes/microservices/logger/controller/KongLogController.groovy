package com.guiantunes.microservices.logger.controller

import com.guiantunes.microservices.logger.model.Api
import com.guiantunes.microservices.logger.model.ApiCall
import com.guiantunes.microservices.logger.model.ApiEndpoint
import com.guiantunes.microservices.logger.services.GraphService
import com.guiantunes.microservices.logger.services.KongService
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Created by guilhermeantunes on 25/05/16.
 */
@RestController
class KongLogController {

    private static Logger logger = LoggerFactory.getLogger(KongLogController.class)

    @Autowired
    KongService kongService

    @Autowired
    GraphService graphService

    /**
     * Endpoint that receives messages from Kong Server parses it and send it to Graph
     * @param body
     * @return
     */
    @RequestMapping ("/logKong")
    def logKong (@RequestBody String body) {

        def slurper = new JsonSlurper()
        def kongJson = slurper.parseText(body)
        String prettyPrint = new JsonBuilder(kongJson).toPrettyString()

        // Identifies the client
        Api client = new Api()
        client.name = kongService.getUsernameFromKong(kongJson.authenticated_entity.consumer_id)

        // Identify the endpoint
        Api server = new Api()
        server.name = kongJson.api.name

        // Identify endpoint
        ApiEndpoint apiEndpoint = new ApiEndpoint()
        apiEndpoint.name = kongJson.request.uri
        apiEndpoint.owner = server

        // Create a call to be persisted
        ApiCall apiCall = new ApiCall()
        apiCall.client = client
        apiCall.endpoint = apiEndpoint

        // Persist Call
        graphService.saveApiCall(apiCall)

        logger.debug("Logging instance: {}", prettyPrint)

        // Reply
        kongJson
    }

}
