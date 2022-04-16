package si.um.feri.ia.les.nakup

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository
import java.time.LocalDate


@Document(collection = "vinjeta")
data class EVinjeta(
    @Id
    val id: String?,
    val reg: String,
    val datumOd: LocalDate,
    val datumDo: LocalDate
) {
    constructor(reg: String, datumOd: LocalDate, datumDo: LocalDate) : this(
        id = null,
        reg = reg,
        datumOd = datumOd,
        datumDo = datumDo
    )
}


interface EVinjetaRepository : MongoRepository<EVinjeta?, String?> {
    fun findByReg(reg: String?): List<EVinjeta?>
}