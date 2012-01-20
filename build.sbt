organization := "me.lessis"

name := "pollcat"

version := "0.1.0"

seq(coffeeSettings:_*)

seq(lessSettings:_*)

(resourceManaged in (Compile, CoffeeKeys.coffee)) <<= (resourceManaged in Compile)(_ / "www" / "js")

(resourceManaged in (Compile, LessKeys.less)) <<= (resourceManaged in Compile)(_ / "www" / "css")

libraryDependencies ++= Seq(
  "net.databinder" %% "unfiltered-netty-server" % "0.5.3",
  "net.databinder" %% "unfiltered-netty-websockets" % "0.5.3",
  "net.debasishg" %% "redisclient" % "2.4.2"
)
