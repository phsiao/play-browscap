package models

import java.util.NoSuchElementException

class BrowsCapItem(val name: String, val attrs: Map[String, String], val parent: Option[BrowsCapItem] = None) {

  var children: List[BrowsCapItem] = List()

  /** Returns attribute value in attrs given attribute name.
    *
    * If the attribute is not defined in this instance's attrs, use parent's attrs recursively
    */
  def attr(name: String): String = {
    attrs.get(name) match {
      case Some(x) => x
      case None => if (parent.isDefined) parent.get.attr(name) else throw new NoSuchElementException
    }
  }

  def hasAttr(name: String): Boolean = {
    attrs.get(name) match {
      case Some(x) => true
      case None => false
    }
  }

  lazy val pattern = attr("Pattern").
                       replace("(", "\\(").
                       replace(")", "\\)").
                       replace("?", "(.?)").
                       replace("*", "(.*)").
                       r.pattern

  def matches(ua: String): Boolean = {
    pattern.matcher(ua).matches
  }
}

/** Builds a BrowsCapItem object given a XML node
  */
object BrowsCapItem {
  def apply(node: scala.xml.Node, default: Option[BrowsCapItem] = None) = {
    val name = node.attribute("name").get.head.text
    val attrs = XmlUtils.extract_items(node)
    new BrowsCapItem(name, attrs, default)
  }
}

