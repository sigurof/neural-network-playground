package no.sigurof.plugins

import io.ktor.server.application.Application
import io.ktor.server.routing.routing
import no.sigurof.routes.machineLearningRouting

fun Application.configureRouting() {
    routing {
        machineLearningRouting()
    }
}
