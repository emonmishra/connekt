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
package com.flipkart.connekt.busybees.streams.flows

import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import com.flipkart.connekt.busybees.streams.errors.ConnektPNStageException
import com.flipkart.connekt.commons.factories.{ConnektLogger, LogFile}
import com.flipkart.connekt.commons.iomodels.ConnektRequest
import com.flipkart.connekt.commons.services.StencilService
import com.flipkart.connekt.commons.utils.StringUtils._
import com.flipkart.connekt.busybees.streams.errors.ConnektErrorHelper._

class RenderFlow extends GraphStage[FlowShape[ConnektRequest, ConnektRequest]] {

  val in = Inlet[ConnektRequest]("Render.In")
  val out = Outlet[ConnektRequest]("Render.Out")

  override def shape: FlowShape[ConnektRequest, ConnektRequest] = FlowShape.of(in, out)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = {
    new GraphStageLogic(shape) {
      setHandler(in, new InHandler {
        override def onPush(): Unit = {
          val m = grab(in)
          ConnektLogger(LogFile.PROCESSORS).debug(s"RenderFlow:: ON_PUSH for ${m.id}")

          try {
            ConnektLogger(LogFile.PROCESSORS).info(s"RenderFlow:: onPush:: Received Message: ${m.getJson}")
            lazy val cRD = m.templateId.flatMap(StencilService.get(_)).map(StencilService.render(_, m.channelDataModel)).get

            val mRendered = m.copy(channelData = Option(m.channelData) match {
              case Some(cD) => cD
              case None => cRD
            })

            if(isAvailable(out)) {
              push(out, mRendered)
              ConnektLogger(LogFile.PROCESSORS).debug(s"RenderFlow:: PUSHED downstream for ${m.id}")
            }
          } catch {
            case e: Throwable =>
              ConnektLogger(LogFile.PROCESSORS).error(s"RenderFlow:: onPush :: Error", e)
              if(!hasBeenPulled(in)) {
                pull(in)
                ConnektLogger(LogFile.PROCESSORS).debug(s"RenderFlow:: PULLED upstream for ${m.id}")
              }

              throw new ConnektPNStageException(m.id, m.deviceId, "connekt_render_failure", m.appName, m.platform, "", e.getMessage, e)
          }
        }
      })

      setHandler(out, new OutHandler {
        override def onPull(): Unit = {
          ConnektLogger(LogFile.PROCESSORS).info(s"RenderFlow:: onPull")
          if(!hasBeenPulled(in)) {
            pull(in)
            ConnektLogger(LogFile.PROCESSORS).debug(s"RenderFlow:: PULLED upstream on downstream pull.")
          }
        }
      })
    }
  }
}
