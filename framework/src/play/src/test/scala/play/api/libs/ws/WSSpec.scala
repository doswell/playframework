/*
 * Copyright (C) 2009-2013 Typesafe Inc. <http://www.typesafe.com>
 */
package play.api.libs.ws

import org.specs2.mutable._
import org.specs2.mock.Mockito

import com.ning.http.client.{
  Response => AHCResponse,
  Cookie => AHCCookie
}
import java.util

object WSSpec extends Specification with Mockito {

  "WS" should {
    "support several query string values for a parameter" in {
      val req = WS.url("http://playframework.com/")
          .withQueryString("foo"->"foo1", "foo"->"foo2")
          .prepare("GET").build
       req.getQueryParams.get("foo").contains("foo1") must beTrue
       req.getQueryParams.get("foo").contains("foo2") must beTrue
       req.getQueryParams.get("foo").size must equalTo (2)
    }

    "support a proxy server" in {
      val proxy = ProxyServer(protocol = Some("https"), host = "localhost", port = 8080, principal = Some("principal"), password = Some("password"))
      val req = WS.url("http://playframework.com/").withProxyServer(proxy).prepare("GET").build
      val actual = req.getProxyServer

      actual.getProtocolAsString must be equalTo "https"
      actual.getHost must be equalTo "localhost"
      actual.getPort must be equalTo 8080
      actual.getPrincipal must be equalTo "principal"
      actual.getPassword must be equalTo "password"
    }

    "support a proxy server" in {
      val proxy = ProxyServer(host = "localhost", port = 8080)
      val req = WS.url("http://playframework.com/").withProxyServer(proxy).prepare("GET").build
      val actual = req.getProxyServer

      actual.getProtocolAsString must be equalTo "http"
      actual.getHost must be equalTo "localhost"
      actual.getPort must be equalTo 8080
      actual.getPrincipal must beNull
      actual.getPassword must beNull
    }

  }

  "WS Response" should {
    "get cookies from an AHC response" in {

      val ahcResponse : AHCResponse = mock[AHCResponse]
      val (domain, name, value, path, maxAge, secure) = ("example.com", "someName", "someValue", "/", 1000, false)

      val ahcCookie : AHCCookie = new AHCCookie(domain, name, value, path, maxAge, secure)
      ahcResponse.getCookies returns util.Arrays.asList(ahcCookie)

      val response = Response(ahcResponse)

      val cookies : Seq[Cookie] = response.cookies
      val cookie = cookies(0)

      cookie.domain must ===("example.com")
      cookie.name must beSome("someName")
      cookie.value must beSome("someValue")
      cookie.path must ===("/")
      cookie.maxAge must ===(1000)
      cookie.secure must beFalse
    }

    "get a single cookie from an AHC response" in {
      val ahcResponse : AHCResponse = mock[AHCResponse]
      val (domain, name, value, path, maxAge, secure) = ("example.com", "someName", "someValue", "/", 1000, false)

      val ahcCookie : AHCCookie = new AHCCookie(domain, name, value, path, maxAge, secure)
      ahcResponse.getCookies returns util.Arrays.asList(ahcCookie)

      val response = Response(ahcResponse)

      val optionCookie = response.cookie("someName")
      optionCookie must beSome[Cookie].which { cookie =>
        cookie.domain must ===("example.com")
        cookie.name must beSome("someName")
        cookie.value must beSome("someValue")
        cookie.path must ===("/")
        cookie.maxAge must ===(1000)
        cookie.secure must beFalse
      }
    }
  }

}
