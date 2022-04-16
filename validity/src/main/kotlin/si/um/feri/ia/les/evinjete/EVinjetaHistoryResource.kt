package si.um.feri.ia.les.evinjete

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoCursor
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.bson.BsonDocument
import org.bson.Document
import java.util.logging.Logger
import javax.inject.Inject
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Path("/history")
class EVinjetaHistoryResource {

    private val LOG: Logger = Logger.getLogger(EVinjetaHistoryResource::class.java.name)

    @Inject
    protected lateinit var mongo: MongoClient

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    fun eVinjeteHistory(): List<EVinjetaValidityResponseAPI> {
        LOG.info("Getting eVinjeta history")
        val seznam = mongo.getDatabase("vinjeta").getCollection("validity").find().iterator()
        return buildResponseFromIterator(seznam)
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    fun eVinjeteHistoryByTablice(@PathParam("id") id: String): List<EVinjetaValidityResponseAPI> {
        LOG.info("Getting eVinjeta history for $id")
        val seznam = mongo.getDatabase("vinjeta").getCollection("validity")
            .find(BsonDocument.parse("{\"registration\": \"$id\"}")).iterator()

        return buildResponseFromIterator(seznam)
    }

    private fun buildResponseFromIterator(seznam: MongoCursor<Document>): List<EVinjetaValidityResponseAPI> {
        val returnSeznam = mutableListOf<EVinjetaValidityResponseAPI>()
        while (seznam.hasNext()) {
            val dokument = seznam.next()
            returnSeznam.add(
                EVinjetaValidityResponseAPI(
                    dokument.getString("registration"),
                    dokument.getBoolean("validity"),
                    Instant.fromEpochMilliseconds(dokument.getLong("timestamp")).toLocalDateTime(
                        TimeZone.currentSystemDefault()
                    ),
                    dokument.getString("matchedVinjetaID")
                )
            )
        }
        return returnSeznam
    }


}