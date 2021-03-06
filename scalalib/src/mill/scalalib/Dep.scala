package mill.scalalib
import mill.util.JsonFormatters._
import upickle.default.{macroRW, ReadWriter => RW}
sealed trait Dep
object Dep{

  implicit def parse(signature: String) = {
    signature.split(':') match{
      case Array(a, b, c) => Dep.Java(a, b, c, cross = false)
      case Array(a, b, "", c) => Dep.Java(a, b, c, cross = true)
      case Array(a, "", b, c) => Dep.Scala(a, b, c, cross = false)
      case Array(a, "", b, "", c) => Dep.Scala(a, b, c, cross = true)
      case Array(a, "", "", b, c) => Dep.Point(a, b, c, cross = false)
      case Array(a, "", "", b, "", c) => Dep.Point(a, b, c, cross = true)
      case _ => throw new Exception(s"Unable to parse signature: [$signature]")
    }
  }
  def apply(org: String, name: String, version: String, cross: Boolean): Dep = {
    this(coursier.Dependency(coursier.Module(org, name), version), cross)
  }
  case class Java(dep: coursier.Dependency, cross: Boolean) extends Dep
  object Java{
    implicit def rw: RW[Java] = macroRW
    def apply(org: String, name: String, version: String, cross: Boolean): Dep = {
      Java(coursier.Dependency(coursier.Module(org, name), version), cross)
    }
  }
  implicit def default(dep: coursier.Dependency): Dep = new Java(dep, false)
  def apply(dep: coursier.Dependency, cross: Boolean) = Scala(dep, cross)
  case class Scala(dep: coursier.Dependency, cross: Boolean) extends Dep
  object Scala{
    implicit def rw: RW[Scala] = macroRW
    def apply(org: String, name: String, version: String, cross: Boolean): Dep = {
      Scala(coursier.Dependency(coursier.Module(org, name), version), cross)
    }
  }
  case class Point(dep: coursier.Dependency, cross: Boolean) extends Dep
  object Point{
    implicit def rw: RW[Point] = macroRW
    def apply(org: String, name: String, version: String, cross: Boolean): Dep = {
      Point(coursier.Dependency(coursier.Module(org, name), version), cross)
    }
  }
  implicit def rw = RW.merge[Dep](
    Java.rw, Scala.rw, Point.rw
  )
}