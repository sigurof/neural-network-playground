package no.sigurof.ml.server.web.websockets

import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.routing.Route
import io.ktor.server.websocket.WebSocketServerSession
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import java.time.Duration
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import no.sigurof.ml.neuralnetwork.trainNetworkMock
import no.sigurof.ml.server.Session
import no.sigurof.ml.server.sessions

fun Application.webSocketsModule() {
    install(WebSockets) {
        contentConverter = KotlinxWebsocketSerializationConverter(Json)
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }
}

fun Route.webSocketRoutes() {
    webSocket("/ml/network") {
        println("Connection opened!")
        var sessionId: String? = null
        try {
            while (true) {
                when (val frame = incoming.receive()) {
                    is Frame.Text -> {
                        deserializeEvent(frame.readText())
                            ?.let { event ->
                                sessionId = event.sessionId
                                when (event) {
                                    is ClientEvent.NewModel -> handleNewModel(event)
                                    is ClientEvent.Continue -> handleContinueWithModel(event)
                                }
                            }
                    }

                    else -> error("Unknown frame $frame")
                }
            }
        } catch (e: Exception) {
            println("Connection with session $sessionId lost: ${e.localizedMessage}")
        } finally {
            onClientDisconnected(sessionId)
        }
    }
}

private suspend fun WebSocketServerSession.deserializeEvent(text: String): ClientEvent? {
    try {
        return json.decodeFromString(ClientEvent.serializer(), text)
    } catch (e: IllegalArgumentException) {
        sendServerEvent(ServerEvent.ClientError("Invalid session ID: ${e.message}"))
        return null
    }
}

private suspend fun WebSocketServerSession.sendServerEvent(data: ServerEvent) {
    val text = json.encodeToString(ServerEvent.serializer(), data)
    send(Frame.Text(text))
}

private suspend fun WebSocketServerSession.handleContinueWithModel(event: ClientEvent.Continue) {
    val state = sessions[event.sessionId] ?: error("Session ${event.sessionId} not found")
    trainNetworkMock(startI = state.progress)
        .collect { (index, value) ->
            state.progress = index
            println("Sending update $index")
            sendServerEvent(ServerEvent.Update("Update $index of 60", value))
        }
}

private suspend fun WebSocketServerSession.handleNewModel(event: ClientEvent.NewModel) {
    println("New model: $event")
    val session = sessions[event.sessionId]
    if (session?.model == null || event.override) {
        // if model is new or override is true, set model and train
        sessions[event.sessionId] =
            Session(progress = 0, result = "", model = event.model)
        val state = sessions[event.sessionId] ?: error("Session ${event.sessionId} not found")
        trainNetworkMock(startI = state.progress)
            .collect { (index, value) ->
                state.progress = index
                println("Sending update $index")
                sendServerEvent(ServerEvent.Update("Update $index of 60", value))
            }
    } else {
        // ... if sessionId collides, ask user to confirm override
        println("Asking client to confirm")
        sendServerEvent(ServerEvent.AskSetModel)
    }
}

internal val webSocketsSerializersModule =
    SerializersModule {
        polymorphic(ClientEvent::class) {
            subclass(ClientEvent.NewModel::class)
            subclass(ClientEvent.Continue::class)
        }
        polymorphic(ServerEvent::class) {
            subclass(ServerEvent.Update::class)
            subclass(ServerEvent.AskSetModel::class)
            subclass(ServerEvent.Complete::class)
            subclass(ServerEvent.ClientError::class)
        }
    }

val json = Json { serializersModule = webSocketsSerializersModule }

private fun onClientDisconnected(sessionId: String?) {
    println("Session $sessionId disconnected")
//    if (sessionId != null) {
//        sessions[sessionId]?.let { state ->
//            state.isActive = false
//        }
//    }
}
