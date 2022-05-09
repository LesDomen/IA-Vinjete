package si.um.feri.ia.les.nakup

import jakarta.websocket.server.PathParam
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
class NakupResource {

    @Autowired
    lateinit var repository: EVinjetaRepository

    @GetMapping("/nakup")
    fun kupiVinjeto(@RequestParam("tablica") tablica: String,@RequestParam("trajanje") trajanje: Integer): String {

        return try {
            repository.insert(
                EVinjeta(
                    reg = tablica,
                    datumOd = LocalDate.now(),
                    datumDo = LocalDate.now().plusDays(trajanje.toLong())
                )
            )
            "OK"
        } catch (e: Exception) {
            "Not OK"
        }

    }

}