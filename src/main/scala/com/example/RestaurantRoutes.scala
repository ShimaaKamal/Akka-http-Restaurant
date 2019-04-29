package com.example

import akka.actor.{ ActorRef, ActorSystem }
import akka.event.Logging

import scala.concurrent.duration._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.http.scaladsl.server.directives.MethodDirectives.post
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.http.scaladsl.server.directives.PathDirectives.path

import scala.concurrent.Future
import com.example.RestaurantRegistryActor._
import akka.pattern.ask
import akka.util.Timeout

trait RestaurantRoutes extends JsonSupport {
  implicit def system: ActorSystem

  lazy val log = Logging(system, classOf[RestaurantRoutes])

  def restaurantRegistryActor: ActorRef

  // Required by the `ask` (?) method below
  implicit lazy val timeout = Timeout(5.seconds)

  //#all-routes
  lazy val restRoutes: Route =
    pathPrefix("api" / "restaurant") {
      concat(
        pathEnd {
          concat(
            get {
              val restaurants: Future[Restaurants] =
                (restaurantRegistryActor ? GetRestaurants).mapTo[Restaurants]
              complete(restaurants)
            },
            get {
              parameters('status.as[Boolean]) { (status) =>
                val openRestaurants: Future[Restaurants] =
                  (restaurantRegistryActor ? GetOpenedRestaurant(status)).mapTo[Restaurants]
                complete(openRestaurants)
              }
            },
            post {
              entity(as[Restaurant]) { restaurant =>
                val restaurantCreated: Future[ActionPerformed] =
                  (restaurantRegistryActor ? CreateRestaurant(restaurant)).mapTo[ActionPerformed]
                onSuccess(restaurantCreated) { performed =>
                  log.info("Created Restaurant: {}", performed.description)
                  complete((StatusCodes.Created, performed))
                }
              }
            })
        },
        path(Segment) { uuid =>
          concat(
            put {
              entity(as[Restaurant]) { restaurant =>
                val restaurantUpdated: Future[ActionPerformed] =
                  (restaurantRegistryActor ? UpdateRestaurant(uuid, restaurant)).mapTo[ActionPerformed]
                onSuccess(restaurantUpdated) { performed =>
                  log.info("Updated Restaurant: {}", performed.description)
                  complete((StatusCodes.Created, performed))
                }
              }
            })
        })
    }
}
