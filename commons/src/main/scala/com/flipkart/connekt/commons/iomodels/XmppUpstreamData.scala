/*
 *         -╥⌐⌐⌐⌐            -⌐⌐⌐⌐-
 *      ≡╢░░░░⌐\░░░φ     ╓╝░░░░⌐░░░░╪╕
 *     ╣╬░░`    `░░░╢┘ φ▒╣╬╝╜     ░░╢╣Q
 *    ║╣╬░⌐        ` ╤▒▒▒Å`        ║╢╬╣
 *    ╚╣╬░⌐        ╔▒▒▒▒`«╕        ╢╢╣▒
 *     ╫╬░░╖    .░ ╙╨╨  ╣╣╬░φ    ╓φ░╢╢Å
 *      ╙╢░░░░⌐"░░░╜     ╙Å░░░░⌐░░░░╝`
 *        ``˚¬ ⌐              ˚˚⌐´
 *
 *      Copyright © 2016 Flipkart.com
 */
package com.flipkart.connekt.commons.iomodels

import com.fasterxml.jackson.annotation.{JsonIgnoreProperties, JsonProperty}
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.flipkart.connekt.commons.utils.StringUtils._

@JsonIgnoreProperties(ignoreUnknown = true)
case class XmppUpstreamData (
                              @JsonProperty("message_id")@JsonProperty(required = true) messageId: String,
                              @JsonProperty(required = true) from: String,
                              @JsonProperty(required = true) category: String,
                              @JsonProperty(required = true) data: ObjectNode) extends XmppUpstreamResponse(messageId, from, category) {

  override def getPnCallbackEvent():Option[PNCallbackEvent] = {
    val eventType: JsonNode = data.get("eventType")
    if (eventType != null) {
      Some(PNCallbackEvent(messageId = messageId,
        clientId = { val path = category.split('.'); path(path.length-1)},
        deviceId = Option(data.get("deviceId")).map(_.asText()).getOrElse(from),
        eventType = eventType.asText(),
        platform = "android",
        appName = Option(data.get("appName")).map(_.asText()).getOrElse("NA"),
        contextId = Option(data.get("contextId")).map(_.asText()).orEmpty,
        cargo = Option(data.get("cargo")).map(_.asText()).orNull,
        timestamp = Option(data.get("timestamp").asText()).map(_.toLong).getOrElse(System.currentTimeMillis)
      ))
    } else None
  }
}

/** Sample from GCM
  * <message id="">
  *   <gcm xmlns="google:mobile:data">
  *     {
  *     "category":"com.example.yourapp", // to know which app sent it
  *     "data":
  *     {
  *     "hello":"world",
  *     },
  *     "message_id":"m-123",
  *     "from":"REGID"
  *     }
  *     </gcm>
  *     </message>
*/