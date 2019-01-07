/*
 * Copyright 2019 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package config

import javax.inject.{Inject, Singleton}
import org.joda.time.DateTime
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.{RunMode, ServicesConfig}

@Singleton
class ApplicationConfig @Inject()(runModeConfiguration: Configuration, runMode: RunMode, servicesConfig: ServicesConfig) {

  val contactFormServiceIdentifier = "API"
  val betaFeedbackUrl = "/contact/beta-feedback"
  val betaFeedbackUnauthenticatedUrl = "/contact/beta-feedback-unauthenticated"
  val thirdPartyDeveloperUrl = servicesConfig.baseUrl("third-party-developer")
  val thirdPartyApplicationProductionUrl = thirdPartyApplicationUrl("third-party-application-production")
  val thirdPartyApplicationProductionBearerToken = bearerToken("third-party-application-production")
  val thirdPartyApplicationProductionUseProxy = useProxy("third-party-application-production")
  val thirdPartyApplicationSandboxUrl = thirdPartyApplicationUrl("third-party-application-sandbox")
  val thirdPartyApplicationSandboxBearerToken = bearerToken("third-party-application-sandbox")
  val thirdPartyApplicationSandboxUseProxy = useProxy("third-party-application-sandbox")
  val deskproUrl = servicesConfig.baseUrl("hmrc-deskpro")
  lazy val hotjarId = runModeConfiguration.getOptional[Int](s"${runMode.env}.hotjar.id").getOrElse(0)
  lazy val hotjarEnabled = runModeConfiguration.getOptional[Boolean](s"${runMode.env}.features.hotjar").getOrElse(false)
  lazy val contactPath = runModeConfiguration.getOptional[String](s"${runMode.env}.contactPath").getOrElse("")
  lazy val reportAProblemPartialUrl = s"$contactPath/contact/problem_reports_ajax?service=$contactFormServiceIdentifier"
  lazy val reportAProblemNonJSUrl = s"$contactPath/contact/problem_reports_nonjs?service=$contactFormServiceIdentifier"
  lazy val apiDocumentationFrontendUrl = buildUrl("platform.frontend").getOrElse(servicesConfig.baseUrl("api-documentation-frontend"))
  lazy val thirdPartyDeveloperFrontendUrl = buildUrl("platform.frontend").getOrElse(servicesConfig.baseUrl("third-party-developer-frontend"))
  lazy val productionApiBaseUrl = buildUrl("platform.api.production")
  lazy val sandboxApiBaseUrl = buildUrl("platform.api.sandbox")
  lazy val sessionTimeoutInSeconds = getConfig("session-timeout-in-seconds", runModeConfiguration.getOptional[Int])
  lazy val analyticsToken = runModeConfiguration.get[String](s"${runMode.env}.google-analytics.token")
  lazy val analyticsHost = runModeConfiguration.getOptional[String](s"${runMode.env}.google-analytics.host").getOrElse("auto")
  lazy val securedCookie = runModeConfiguration.getOptional[Boolean](s"${runMode.env}.cookie.secure").getOrElse(true)
  lazy val isExternalTestEnvironment = runModeConfiguration.getOptional[Boolean]("isExternalTestEnvironment").getOrElse(false)
  lazy val title = if (isExternalTestEnvironment) "Developer Sandbox" else "Developer Hub"
  lazy val jsonEncryptionKey = getConfig("json.encryption.key")
  lazy val strategicSandboxEnabled = runModeConfiguration.getOptional[Boolean]("strategicSandboxEnabled").getOrElse(false)
  lazy val currentTermsOfUseVersion = runModeConfiguration.getOptional[String]("currentTermsOfUseVersion").getOrElse("")
  lazy val currentTermsOfUseDate = DateTime.parse(runModeConfiguration.getOptional[String]("currentTermsOfUseDate").getOrElse(""))

  // API Subscription Fields
  val apiSubscriptionFieldsProductionUrl = apiSubscriptionFieldsUrl("api-subscription-fields-production")
  val apiSubscriptionFieldsProductionBearerToken = bearerToken("api-subscription-fields-production")
  val apiSubscriptionFieldsProductionUseProxy = useProxy("api-subscription-fields-production")
  val apiSubscriptionFieldsSandboxUrl = apiSubscriptionFieldsUrl("api-subscription-fields-sandbox")
  val apiSubscriptionFieldsSandboxBearerToken = bearerToken("api-subscription-fields-sandbox")
  val apiSubscriptionFieldsSandboxUseProxy = useProxy("api-subscription-fields-sandbox")

  private def getConfig(key: String) =
    runModeConfiguration.getOptional[String](key).getOrElse {
      sys.error(s"[$key] is not configured!")
    }

  private def getConfig[T](key: String, block: String => Option[T]) =
    block(key).getOrElse {
      sys.error(s"[$key] is not configured!")
    }

  private def buildUrl(key: String) = {
    (runModeConfiguration.getOptional[String](s"${runMode.env}.$key.protocol"), runModeConfiguration.getOptional[String](s"${runMode.env}.$key.host")) match {
      case (Some(protocol), Some(host)) => Some(s"$protocol://$host")
      case (None, Some(host)) => Some(s"https://$host")
      case _ => None
    }
  }

  private def serviceUrl(key: String)(serviceName: String): String = {
    if (useProxy(serviceName)) s"${servicesConfig.baseUrl(serviceName)}/${servicesConfig.getConfString(s"$serviceName.context", key)}"
    else servicesConfig.baseUrl(serviceName)
  }

  private def apiSubscriptionFieldsUrl = serviceUrl("api-subscription-fields")(_)

  private def thirdPartyApplicationUrl = serviceUrl("third-party-application")(_)

  private def useProxy(serviceName: String) = servicesConfig.getConfBool(s"$serviceName.use-proxy",  defBool = false)

  private def bearerToken(serviceName: String) = servicesConfig.getConfString(s"$serviceName.bearer-token", "")
}
