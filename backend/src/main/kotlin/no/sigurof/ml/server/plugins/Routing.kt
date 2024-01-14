package no.sigurof.ml.server.plugins

import io.ktor.server.application.Application
import io.ktor.server.routing.routing
import no.sigurof.ml.routes.machineLearningRouting

fun Application.configureRouting() {
    routing {
        machineLearningRouting()
    }
}
