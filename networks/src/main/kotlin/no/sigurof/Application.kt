package no.sigurof
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.cors.routing.CORS
import no.sigurof.plugins.configureRouting
import no.sigurof.plugins.configureSerialization

fun startKtorServer(){
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}
fun Application.module() {
    install(CORS) {
        // The methods for your server routes
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Delete)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.AccessControlAllowOrigin) // Explicitly allow this header
        allowCredentials = true
        allowNonSimpleContentTypes = true
        anyHost() // or allowHost(yourHost)
    }
    configureRouting()
    configureSerialization()
}
