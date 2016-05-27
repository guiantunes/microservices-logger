package com.guiantunes.microservices.logger.services

import groovy.json.JsonSlurper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * Created by guilhermeantunes on 26/05/16.
 *
 * Service class that access Kong api
 */
@Service
class KongService {

    final Logger logger = LoggerFactory.getLogger(KongService)

    /**
     * Return an username from /consumers api from Kong by an id
     * @param id to be queried
     * @return username if found
     */
    String getUsernameFromKong (String id) {

        JsonSlurper slurper = new JsonSlurper()
        def user = slurper.parse(new URL("http://kong:8001/consumers/" + id))

        user.username;
    }
}
