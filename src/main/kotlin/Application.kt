package org.delcom

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.github.cdimascio.dotenv.dotenv
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.response.*
import kotlinx.datetime.Instant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import org.delcom.helpers.JWTConstants
import org.delcom.helpers.configureDatabases
import org.delcom.module.appModule
import org.koin.ktor.plugin.Koin

object InstantSerializer : KSerializer<Instant> {
    override val descriptor = PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: Instant) = encoder.encodeString(value.toString())
    override fun deserialize(decoder: Decoder): Instant = Instant.parse(decoder.decodeString())
}

fun main(args: Array<String>) {
    val dotenv = dotenv {
        directory = "."
        ignoreIfMissing = false
    }
    dotenv.entries().forEach {
        System.setProperty(it.key, it.value)
    }
    EngineMain.main(args)
}

fun Application.module() {
    val jwtSecret = environment.config.property("ktor.jwt.secret").getString()

    install(Authentication) {
        jwt(JWTConstants.NAME) {
            realm = JWTConstants.REALM
            verifier(
                JWT.require(Algorithm.HMAC256(jwtSecret))
                    .withIssuer(JWTConstants.ISSUER)
                    .withAudience(JWTConstants.AUDIENCE)
                    .build()
            )
            validate { credential ->
                val userId = credential.payload.getClaim("userId").asString()
                if (!userId.isNullOrBlank()) JWTPrincipal(credential.payload) else null
            }
            challenge { _, _ ->
                call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf("status" to "error", "message" to "Token tidak valid")
                )
            }
        }
    }

    install(CORS) { anyHost() }

    install(ContentNegotiation) {
        json(Json {
            explicitNulls = false
            prettyPrint = true
            ignoreUnknownKeys = true
            serializersModule = SerializersModule {
                contextual(InstantSerializer)
            }
        })
    }

    install(Koin) { modules(appModule(jwtSecret)) }

    configureDatabases()
    configureRouting()
}