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
package com.flipkart.connekt.commons.factories

import com.flipkart.connekt.commons.dao._
import com.flipkart.connekt.commons.helpers.KafkaProducerHelper
import com.flipkart.connekt.commons.services.{KeyChainService, _}
import com.typesafe.config.Config
import org.apache.hadoop.hbase.client.Connection

object ServiceFactory {

  var serviceCache = Map[ServiceType.Value, TService]()

  def initPNMessageService(requestDao: PNRequestDao, userConfiguration: TUserConfiguration, queueProducerHelper: KafkaProducerHelper, kafkaConsumerConfig: Config, schedulerService: SchedulerService) = {
    serviceCache += ServiceType.PN_MESSAGE -> new MessageService(requestDao, userConfiguration, queueProducerHelper, kafkaConsumerConfig, schedulerService)
  }

  def initEmailMessageService(requestDao: EmailRequestDao,userConfiguration: TUserConfiguration, queueProducerHelper: KafkaProducerHelper, kafkaConsumerConfig: Config ): Unit ={
    serviceCache += ServiceType.EMAIL_MESSAGE -> new MessageService(requestDao, userConfiguration, queueProducerHelper, kafkaConsumerConfig, null)
  }

  def initCallbackService(emailCallbackDao: EmailCallbackDao, smsCallbackDao: SmsCallbackDao, smsRequestDao: SmsRequestDao, pnCallbackDao: PNCallbackDao, pnRequestInfoDao: PNRequestDao, emailRequestDao: EmailRequestDao, queueProducerHelper: KafkaProducerHelper) = {
    serviceCache += ServiceType.CALLBACK -> new CallbackService(pnCallbackDao, emailCallbackDao, smsCallbackDao, smsRequestDao, pnRequestInfoDao, emailRequestDao, queueProducerHelper)

  def initAuthorisationService(priv: PrivDao, userInfo: TUserInfo) = {
    serviceCache += ServiceType.AUTHORISATION -> new AuthorisationService(priv, userInfo)
    serviceCache += ServiceType.USER_INFO -> new UserInfoService(userInfo)
  }

  def initStorageService(dao: TKeyChainDao) = {
    serviceCache += ServiceType.KEY_CHAIN -> new KeyChainService(dao)
  }

  def initProjectConfigService(dao:UserProjectConfigDao): Unit ={
    serviceCache += ServiceType.APP_CONFIG -> new UserProjectConfigService(dao)
  }

  def initStatsReportingService(dao : StatsReportingDao): Unit ={
    val instance = new ReportingService(dao)
    instance.init()
    serviceCache += ServiceType.STATS_REPORTING -> instance
  }

  def initSchedulerService(hConnection: Connection): Unit ={
    val instance = new SchedulerService(hConnection)
    serviceCache += ServiceType.SCHEDULER -> instance
  }

  def initStencilService(dao: TStencilDao) = serviceCache += ServiceType.STENCIL -> new StencilService(dao)

  def initAppLevelConfigService(dao: TAppLevelConfiguration) = serviceCache += ServiceType.APP_LEVEL_CONFIG -> new AppLevelConfigService(dao)

  def getSchedulerService = serviceCache(ServiceType.SCHEDULER).asInstanceOf[SchedulerService]

  def getPNMessageService = serviceCache(ServiceType.PN_MESSAGE).asInstanceOf[TMessageService]
  def getEmailMessageService = serviceCache(ServiceType.EMAIL_MESSAGE).asInstanceOf[TMessageService]

  def getSMSMessageService = serviceCache(ServiceType.SMS).asInstanceOf[TMessageService]

  def getCallbackService = serviceCache(ServiceType.CALLBACK).asInstanceOf[TCallbackService]

  def getAuthorisationService = serviceCache(ServiceType.AUTHORISATION).asInstanceOf[TAuthorisationService]

  def getAppLevelConfigService = serviceCache(ServiceType.APP_LEVEL_CONFIG).asInstanceOf[TAppLevelConfigService]

  def getUserInfoService = serviceCache(ServiceType.USER_INFO).asInstanceOf[UserInfoService]

  def getKeyChainService = serviceCache(ServiceType.KEY_CHAIN).asInstanceOf[TStorageService]

  def getReportingService = serviceCache(ServiceType.STATS_REPORTING).asInstanceOf[ReportingService]

  def getStencilService = serviceCache(ServiceType.STENCIL).asInstanceOf[TStencilService]

  def getUserProjectConfigService = serviceCache(ServiceType.APP_CONFIG).asInstanceOf[UserProjectConfigService]

}

object ServiceType extends Enumeration {
  val PN_MESSAGE, TEMPLATE, CALLBACK, USER_INFO, AUTHORISATION, KEY_CHAIN, STATS_REPORTING, SCHEDULER, STENCIL, APP_LEVEL_CONFIG, SMS = Value
}
