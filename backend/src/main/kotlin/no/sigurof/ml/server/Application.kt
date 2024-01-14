package no.sigurof.ml.server

import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.routing.routing
import java.util.concurrent.ConcurrentHashMap
import kotlinx.serialization.Serializable
import no.sigurof.ml.server.web.rest.restModule
import no.sigurof.ml.server.web.rest.restRoutes
import no.sigurof.ml.server.web.websockets.webSocketRoutes
import no.sigurof.ml.server.web.websockets.webSocketsModule
import no.sigurof.ml.server.web.websockets.webSocketsSerializersModule

fun startKtorServer() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        restModule()
        webSocketsSerializersModule
        webSocketsModule()
        routing {
            restRoutes()
            webSocketRoutes()
        }
    }
        .start(wait = true)
}

val sessions = ConcurrentHashMap<String, Session>()

@Serializable
class Model

data class Session(
    var awaitingUserResponse: Boolean = false,
    var progress: Int,
    var result: String,
    var isActive: Boolean = true,
    var model: Model,
)
