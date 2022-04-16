package si.um.feri.ia.les.evinjete

import kotlinx.datetime.LocalDateTime

@kotlinx.serialization.Serializable
data class EVinjetaValidityResponse (val reg : String, val valid : Boolean, val timestamp : Long, val matchedVinjetaID : String)

@kotlinx.serialization.Serializable
data class EVinjetaValidityResponseAPI (val reg : String, val valid : Boolean,  val timestamp : LocalDateTime, val matchedVinjetaID : String)
