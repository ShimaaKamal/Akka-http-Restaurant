package com.example

import java.io.FileInputStream
import akka.actor.{ Actor, ActorLogging, Props }
import play.api.libs.json.{ Json }
import net.liftweb.json._

case class RestaurantData(enName: String, arName: String, state: String, routingMethod: String, logo: String, coverPhoto: String,
  enDescription: String,
  arDescription: String,
  shortNumber: String,
  facebookLink: String,
  twitterLink: String,
  youtubeLink: String,
  website: String,
  onlinePayment: Boolean,
  client: Boolean,
  pendingInfo: Boolean,
  pendingMenu: Boolean,
  closed: Boolean)
case class Restaurant(uuid: String, data: RestaurantData)
case class Restaurants(restaurants: Seq[Restaurant])

object RestaurantRegistryActor {
  final case class ActionPerformed(description: String)
  final case object GetRestaurants
  final case class CreateRestaurant(restaurant: Restaurant)
  final case class GetRestaurant(id: String)
  final case class GetOpenedRestaurant(status: Boolean)
  final case class UpdateRestaurant(uuid: String, restaurant: Restaurant)

  def props: Props = Props[RestaurantRegistryActor]
}

class RestaurantRegistryActor extends Actor with ActorLogging {
  import RestaurantRegistryActor._

  implicit val formats = DefaultFormats

  var restaurants = List[Restaurant]()

  //reading file and map json string into object of restaurants
  val in = new FileInputStream("sample-restaurant-data.json")

  val fileData = Json.parse(in)

  val fileDataAfterParsing = parse(fileData.toString())

  val rests = (fileDataAfterParsing).children

  for (rest <- rests) {
    restaurants = restaurants :+ rest.extract[Restaurant]
  }

  def receive: Receive = {
    case GetRestaurants =>
      sender() ! Restaurants(restaurants.toSeq)

    case GetOpenedRestaurant(status) =>
      sender() ! Restaurants(restaurants.filter(_.data.closed == status).toSeq)

    case CreateRestaurant(restaurant) =>
      restaurants = restaurants :+ restaurant
      sender() ! ActionPerformed(s"Restaurant ${restaurant.uuid} created.")

    case UpdateRestaurant(uuid, restaurant) =>
      val index = restaurants.indexWhere(_.uuid == uuid)
      if (index >= 0) {
        restaurants = restaurants.updated(index, restaurant)
        sender() ! ActionPerformed(s"Restaurant ${uuid} updated.")
      }
      sender() ! ActionPerformed(s"Restaurant ${uuid} not exist to update.")

  }

}