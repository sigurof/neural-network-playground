package no.sigurof.plugins

import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import no.sigurof.routes.customerRouting;
import no.sigurof.routes.getOrderRoute
import no.sigurof.routes.listOrdersRoute
import no.sigurof.routes.totalizeOrderRoute

fun Application.configureRouting() {
    routing {
        customerRouting()
        listOrdersRoute()
        getOrderRoute()
        totalizeOrderRoute()
    }
    routing {

        get("/") {
            call.respondText("Hello World!")
        }

        // The first step is to get the backend to return a trained neural network based on a few parameters:
        /*
        * - the training data, defined as an array of pairs of input output arrays
        *   - this gives us the desired input/output dimensions of the network
        * - the dimensions of hidden layers
        * */

        get("/ml/network") {
//            plot2x2Network()
            call.respondText("Getting you a network!")
        }
    }
}
