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
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.receiveDeserialized
import io.ktor.server.websocket.sendSerialized
import io.ktor.server.websocket.timeout
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.DefaultWebSocketSession
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import java.time.Duration
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
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

fun Route.webSocketRouting() {
    webSocket("/ml/network") {
        try {
            for (frame in incoming) {
                println("received frame: $frame")
            }
        } catch (e: Exception) {
            println(e.localizedMessage)
        }
    }

    // DEMO from https://ktor.io/docs/websocket-serialization.html#add_dependencies
    webSocket("/customer/1") {
        for (frame in incoming) {
            sendSerialized(Customer(1, "Jane", "Smith"))
        }
    }
    webSocket("/customer") {
        try {
            while (true) {
                val customer = receiveDeserialized<Customer>()
                println("A customer with id ${customer.id} is received by the server.")
            }
        } catch (e: Exception) {
            println(e.localizedMessage)
        }
    }
    webSocket("/echo") {
        send(Frame.Text("Please enter your name"))
        for (frame: Frame in incoming) {
            frame as? Frame.Text ?: continue
            val receivedText = frame.readText()
            if (receivedText.equals("bye", ignoreCase = true)) {
                close(CloseReason(CloseReason.Codes.NORMAL, "Client said BYE"))
            } else {
                send(Frame.Text("Hi, $receivedText!"))
            }
        }
    }
    val connections = Collections.synchronizedSet<Connection?>(LinkedHashSet())
    webSocket("/chat") {
        println("Adding user!")
        val thisConnection = Connection(this)
        connections += thisConnection
        try {
            send(Frame.Text("You are connected! There are ${connections.count()} users here."))
            for (frame in incoming) {
                frame as? Frame.Text ?: continue
                val receivedText = frame.readText()
                val textWithUsername = "[${thisConnection.name}]: $receivedText"
                connections.forEach {
                    it.session.send(Frame.Text(textWithUsername))
                }
            }
        } catch (e: Exception) {
            println(e.localizedMessage)
        } finally {
            println("Removing $thisConnection!")
            connections -= thisConnection
        }
    }
}
