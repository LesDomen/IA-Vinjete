package si.um.feri.ia.les.evinjete

import kotlinx.datetime.LocalDateTime

data class EVinjeta(
    val id: String,
    val reg: String,
    val datumOd: LocalDateTime,
    val datumDo: LocalDateTime
)
