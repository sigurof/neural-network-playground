package no.sigurof.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import no.sigurof.ml.Matrix
import no.sigurof.ml.NeuralNetwork
import no.sigurof.ml.NeuralNetworkBuilder
import no.sigurof.models.NeuralNetworkParams
import no.sigurof.routes.customerRouting
import no.sigurof.routes.getOrderRoute
import no.sigurof.routes.listOrdersRoute
import no.sigurof.routes.machineLearningRouting
import no.sigurof.routes.totalizeOrderRoute

fun Application.configureRouting() {
    routing {
        customerRouting()
        listOrdersRoute()
        getOrderRoute()
        totalizeOrderRoute()
        machineLearningRouting()
    }
}
