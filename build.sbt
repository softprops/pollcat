organization := "me.lessis"

name := "pollcat"

version := "0.1.0"

seq(heroicSettings: _*)

seq(coffeeSettings:_*)

seq(lessSettings:_*)

(resourceManaged in (Compile, CoffeeKeys.coffee)) <<= (resourceManaged in Compile)(_ / "www" / "js")

(resourceManaged in (Compile, LessKeys.less)) <<= (resourceManaged in Compile)(_ / "www" / "css")

libraryDependencies ++= Seq(
  "net.databinder" %% "unfiltered-netty-server" % "0.5.3",
  "net.databinder" %% "unfiltered-netty-websockets" % "0.5.3",
  "net.databinder" %% "unfiltered-json" % "0.5.3",
  "net.debasishg" %% "redisclient" % "2.4.2",
  "net.databinder" %% "dispatch-oauth" % "0.8.6"
)
