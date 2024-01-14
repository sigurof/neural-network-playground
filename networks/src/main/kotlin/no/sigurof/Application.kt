package no.sigurof
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import no.sigurof.plugins.configureRouting
import no.sigurof.plugins.configureSerialization

fun startKtorServer(){
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}
fun Application.module() {
    configureRouting()
    configureSerialization()
}
