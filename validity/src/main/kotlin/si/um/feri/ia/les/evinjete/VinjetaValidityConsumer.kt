package si.um.feri.ia.les.evinjete

import com.mongodb.client.MongoClient
import io.quarkus.runtime.ShutdownEvent
import io.quarkus.runtime.StartupEvent
import io.vertx.core.json.Json
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone
import org.bson.Document
import java.util.*
import java.util.concurrent.Executors
import java.util.logging.Logger
import javax.enterprise.context.ApplicationScoped
import javax.enterprise.event.Observes
import javax.inject.Inject
import javax.jms.*


@ApplicationScoped
class VinjetaValidityConsumer : Runnable {

    private val LOG: Logger = Logger.getLogger(VinjetaValidityConsumer::class.java.name)

    @Inject
    protected lateinit var connectionFactory: ConnectionFactory

    @Inject
    protected lateinit var mongo: MongoClient

    private val scheduler = Executors.newSingleThreadExecutor()

    fun onStart(@Observes ev: StartupEvent?) {
        scheduler.submit(this)
    }

    fun onStop(@Observes ev: ShutdownEvent?) {
        scheduler.shutdown()
    }

    private fun saveToMongo(response: EVinjetaValidityResponse) {
        val collection = mongo.getDatabase("evinjete").getCollection("validity")

        collection.insertOne(
            Document().append("registration", response.reg).append("validity", response.valid)
                .append("matchedVinjetaID", response.matchedVinjetaID)
                .append("timestamp", response.timestamp)
        )
    }



    private fun getVinjete(): List<EVinjeta> {
        return try {
            mongo.getDatabase("evinjete").getCollection("vinjeta").find().map {
                EVinjeta(
                    id = it.getObjectId("_id").toHexString(),
                    reg = it.getString("reg"),
                    datumOd = it.getDate("datumOd").toInstant().toKotlinInstant().toLocalDateTime(TimeZone.currentSystemDefault()),
                    datumDo = it.getDate("datumDo").toInstant().toKotlinInstant().toLocalDateTime(TimeZone.currentSystemDefault())
                )
            }.toList()
        } catch (e: Exception) {
            LOG.severe("Error while getting vinjete from mongo: ${e.message}")
            emptyList()
        }
    }

    override fun run() {
        try {
            connectionFactory.createContext(JMSContext.AUTO_ACKNOWLEDGE).use { context ->
                val consumer: JMSConsumer = context.createConsumer(context.createQueue("validity"))
                val producer: JMSProducer = context.createProducer()
                val responseQueue = context.createQueue("validity-responses")
                while (true) {
                    val message: Message = consumer.receive()
                    val tablicaZaPreverit = (message.getBody(String::class.java))
                    LOG.info("Začetek preverjanja za $tablicaZaPreverit")
                    getVinjete().filter { it.reg == tablicaZaPreverit }.let outerFilter@{ seznamEVinjetZaTablico ->
                        lateinit var eVinjetaValidityResponse: EVinjetaValidityResponse
                        if (seznamEVinjetZaTablico.isEmpty()) {
                            LOG.info("Ni vinjete za tablico $tablicaZaPreverit")
                            eVinjetaValidityResponse = EVinjetaValidityResponse(
                                matchedVinjetaID = "",
                                reg = tablicaZaPreverit,
                                timestamp = Clock.System.now().toEpochMilliseconds(),
                                valid = false
                            )
                            producer.send(responseQueue, Json.encode(eVinjetaValidityResponse))
                            saveToMongo(eVinjetaValidityResponse)
                            return@outerFilter
                        }


                        seznamEVinjetZaTablico.filter {
                            it.datumOd.toJavaLocalDateTime()
                                .isBefore(java.time.LocalDateTime.now()) && it.datumDo.toJavaLocalDateTime()
                                .isAfter(java.time.LocalDateTime.now())
                        }.maxByOrNull { it.datumDo }?.let {
                            LOG.info("Vinjeta za tablico $tablicaZaPreverit je veljavna")
                            eVinjetaValidityResponse = EVinjetaValidityResponse(
                                matchedVinjetaID = it.id,
                                reg = tablicaZaPreverit,
                                timestamp = Clock.System.now()
                                    .toEpochMilliseconds(),
                                valid = true
                            )
                            producer.send(responseQueue, Json.encode(eVinjetaValidityResponse))
                            saveToMongo(eVinjetaValidityResponse)
                            return@outerFilter
                        }



                        seznamEVinjetZaTablico.filter {
                            it.datumOd.toJavaLocalDateTime()
                                .isAfter(java.time.LocalDateTime.now()) || it.datumDo.toJavaLocalDateTime()
                                .isBefore(java.time.LocalDateTime.now())
                        }.maxByOrNull { it.datumDo }!!.let {
                            LOG.info("Vinjeta za tablico $tablicaZaPreverit je neveljavna")
                            eVinjetaValidityResponse = EVinjetaValidityResponse(
                                matchedVinjetaID = it.id,
                                reg = tablicaZaPreverit,
                                timestamp = Clock.System.now()
                                    .toEpochMilliseconds(),
                                valid = false
                            )
                            saveToMongo(eVinjetaValidityResponse)
                            producer.send(responseQueue, Json.encode(eVinjetaValidityResponse))
                        }

                    }
                }
            }
        } catch (e: JMSException) {
            throw RuntimeException(e)
        }
    }
}