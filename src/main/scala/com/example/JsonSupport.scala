package com.example

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.example.RestaurantRegistryActor.ActionPerformed

import spray.json._

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {

  implicit val actionPerformedJsonFormat = jsonFormat1(ActionPerformed)

  implicit val restDataFormat = jsonFormat(RestaurantData, "enName", "arName",
    "state",
    "routingMethod",
    "logo",
    "coverPhoto",
    "enDescription",
    "arDescription",
    "shortNumber",
    "facebookLink",
    "twitterLink",
    "youtubeLink",
    "website",
    "onlinePayment",
    "client",
    "pendingInfo",
    "pendingMenu",
    "closed")

  implicit val restFormat = jsonFormat(Restaurant, "uuid", "data")
  implicit val restsFormat = jsonFormat(Restaurants, "restaurants")
}
//#json-support
