package no.sigurof.ml.server.web.websockets

import kotlinx.serialization.Serializable
import no.sigurof.ml.server.Model

@Serializable
internal sealed class ServerEvent {
    @Serializable
    data class Update(val message: String, val cost: Double) : ServerEvent()

    @Serializable
    data object AskSetModel : ServerEvent()

    @Serializable
    data object Complete : ServerEvent()

    @Serializable
    data class ClientError(val message: String) : ServerEvent()
}

@Serializable
internal sealed class ClientEvent {
    abstract val sessionId: String

    fun assertSessionIdNotBlank() {
        require(sessionId.isNotBlank()) { "sessionId cannot be blank" }
    }

    @Serializable
    data class Continue(override val sessionId: String) : ClientEvent() {
        init {
            assertSessionIdNotBlank()
        }
    }

    @Serializable
    data class NewModel(
        override val sessionId: String,
        val override: Boolean = false,
        val model: Model,
    ) :
        ClientEvent() {
        init {
            assertSessionIdNotBlank()
        }
    }
}
