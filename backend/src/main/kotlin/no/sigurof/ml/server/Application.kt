package no.sigurof.ml.server

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.routing.Route
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSocketServerSession
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.DefaultWebSocketSession
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import no.sigurof.ml.routes.machineLearningRouting
import no.sigurof.ml.server.plugins.configureSerialization

class Connection(val session: DefaultWebSocketSession) {
    companion object {
        val lastId = AtomicInteger(0)
    }

    val name = "user${lastId.getAndIncrement()}"
}

fun startKtorServer() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        mySerializersModule
        restModule()
        install(WebSockets) {
            contentConverter = KotlinxWebsocketSerializationConverter(Json)
            pingPeriod = Duration.ofSeconds(15)
            timeout = Duration.ofSeconds(15)
            maxFrameSize = Long.MAX_VALUE
            masking = false
        }
        routing {
            machineLearningRouting()
            webSocketRouting()
        }
    }
        .start(wait = true)
}

// val module = SerializersModule {
//        polymorphic(ClientEvent::class) {
//            subclass(ClientEvent.Start::class)
//            subclass(ClientEvent.Continue::class)
//        }
//    }
// Setting up a serializersModule for ClientEvent

fun Application.restModule() {
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Delete)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.AccessControlAllowOrigin) // Explicitly allow this header
        allowCredentials = true
        allowNonSimpleContentTypes = true
        anyHost()
    }
    configureSerialization()
}

@Serializable
data class Customer(val id: Int, val firstName: String, val lastName: String)

// data class StartEvent(val sessionId: String)

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

@Serializable
sealed class ServerEvent {
    @Serializable
    @SerialName("Update")
    data class Update(val message: String) : ServerEvent()

    @Serializable
    @SerialName("AskSetModel")
    data object AskSetModel : ServerEvent()

    @Serializable
    @SerialName("ConfirmSetModel")
    data object ConfirmSetModel : ServerEvent()

    @Serializable
    @SerialName("complete")
    data object Complete : ServerEvent()
}

@Serializable
sealed class ClientEvent {
    abstract val sessionId: String?

    @Serializable
    data class GetModels(override val sessionId: String?) : ClientEvent()

    @Serializable
    @SerialName("Continue")
    data class Continue(override val sessionId: String?) : ClientEvent()

    @Serializable
    @SerialName("NewModel")
    data class NewModel(
        override val sessionId: String,
        val override: Boolean = false,
        val hiddenLayers: List<Int>,
        val sizeDataSet: Int,
    ) :
        ClientEvent()

//    @Serializable
//    @SerialName("Train")
//    data class Train(
//        override val sessionId: String,
//        val hiddenLayers: List<Int>,
//        val sizeDataSet: Int,
//    ) : ClientEvent()
}

val mySerializersModule =
    SerializersModule {
        polymorphic(ClientEvent::class) {
            subclass(ClientEvent.NewModel::class)
            subclass(ClientEvent.Continue::class)
//            subclass(ClientEvent.GetModels::class)
        }
        polymorphic(ServerEvent::class) {
            subclass(ServerEvent.Update::class)
            subclass(ServerEvent.AskSetModel::class)
            subclass(ServerEvent.ConfirmSetModel::class)
            subclass(ServerEvent.Complete::class)
        }
    }

val json = Json { serializersModule = mySerializersModule }

fun expensiveCalculationFlow(event: ClientEvent): Flow<ServerEvent> =
    flow {
        val state = sessions.getOrPut(event.sessionId) { Session(false, 0, "", true, Model()) }
        emit(ServerEvent.Update("Starting!"))
        for (i in (state.progress + 1)..60) {
            delay(3000)
            val update = "Update $i of 60"
            state.progress = i
            state.result = update
            emit(ServerEvent.Update(update))
        }
        emit(ServerEvent.Update("Calculation Result"))
        state.result = "Calculation Result"
    }

fun onClientDisconnected(sessionId: String?) {
    println("Session $sessionId disconnected")
    if (sessionId != null) {
        sessions[sessionId]?.let { state ->
            state.isActive = false
        }
    }
}

fun deserializeEvent(text: String): ClientEvent {
    return json.decodeFromString(ClientEvent.serializer(), text)
}

suspend fun WebSocketServerSession.sendServerEvent(data: ServerEvent) {
    val text = json.encodeToString(ServerEvent.serializer(), data)
    send(Frame.Text(text))
}

// suspend fun WebSocketServerSession.handleTrain(event: ClientEvent.Train) {
//    println("Handling train event")
//    // if there is no model associated with the current session, throw an error
//    val session = sessions[event.sessionId]
//    if (session?.model == null) {
//        error("No model associated with session ${event.sessionId}.")
//    }
//    // if there is a model, train it
//    expensiveCalculationFlow(event).collect { update ->
//        println("Sending update $update")
//        sendSerialized(update)
//    }
suspend fun WebSocketServerSession.handleContinueWithModel(event: ClientEvent.Continue) {
    println("Continuation event received.")
    expensiveCalculationFlow(event)
        .collect { update ->
            println("Sending update $update")
            sendServerEvent(update)
        }
}

suspend fun WebSocketServerSession.handleNewModel(event: ClientEvent.NewModel) {
    println("Handling new model event")
    val session = sessions[event.sessionId]
    if (session?.model == null || event.override) {
        // if model is new or override is true, set model and train
        println("Setting new model")
        sessions[event.sessionId] = Session(false, 0, "", true, Model())
        expensiveCalculationFlow(event)
            .collect { update ->
                println("Sending update $update")
                sendServerEvent(update)
            }
    } else {
        // ... if sessionId collides, ask user to confirm override
        println("Asking client to confirm")
        sendServerEvent(ServerEvent.AskSetModel)
    }
}

// }

fun Route.webSocketRouting() {
    webSocket("/ml/network") {
        println("Connection opened!")
        var sessionId: String? = null
        try {
            while (true) {
                val frame = incoming.receive() // Receive incoming frame
                if (frame is Frame.Text) {
                    val text = frame.readText()
                    val event = deserializeEvent(text)
                    sessionId = event.sessionId
                    when (event) { // Deserialize the text into an Event
//                        is ClientEvent.Train -> handleTrain(event)
                        is ClientEvent.NewModel -> handleNewModel(event)
                        is ClientEvent.Continue -> handleContinueWithModel(event)
                        else -> error("Unknown event $event")
//                        is ClientEvent.GetModels -> {
//                            val models: List<SessionDto> = sessions.map { it.toResponse() }
//                            val response = json.encodeToString()
//                            send(Frame.Text(response))
//                        }
                    }
                }
            }
        } catch (e: Exception) {
            println("Connection with session $sessionId lost: ${e.localizedMessage}")
        } finally {
            onClientDisconnected(sessionId)
        }
    }
}
