package si.um.feri.ia.les.evinjete

import com.mongodb.client.MongoClient
import javax.inject.Inject
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Path("/hello")
class GreetingResource {

    @Inject
    private lateinit var mongo: MongoClient

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    fun hello() = "Hello RESTEasy"

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    fun evinjeteHistory(): List<String> {
        val seznam = mongo.getDatabase("vinjeta").getCollection("validity").find()
        return seznam.map { it.toJson() }.toList()
    }


}