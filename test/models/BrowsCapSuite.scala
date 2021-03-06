package models

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.OneInstancePerTest

class BrowseCapSuite extends FunSuite with ShouldMatchers with OneInstancePerTest {

  val sampleBrowsCapXml = <browsercaps>
                            <comments>
                              <comment>First line</comment>
                              <comment>Second line</comment>
                            </comments>
                            <gjk_browscap_version>
                              <item value="4911" name="Version"></item>
                              <item value="Thu, 03 Nov 2011 07:00:27 -0000" name="Released"></item>
                            </gjk_browscap_version>
                            <browsercapitems>
                              <browscapitem name="DefaultProperties">
                                <item name="Comments" value="DefaultProperties" />
                                <item name="Pattern" value="DefaultProperties" />
                                <item name="Browser" value="DefaultProperties" />
                                <item name="Version" value="0" />
                                <item name="MajorVer" value="0" />
                                <item name="MinorVer" value="0" />
                                <item name="Platform" value="" />
                                <item name="Alpha" value="false" />
                                <item name="Beta" value="false" />
                                <item name="Win16" value="false" />
                                <item name="Win32" value="false" />
                                <item name="Win64" value="false" />
                                <item name="Frames" value="false" />
                                <item name="IFrames" value="false" />
                                <item name="Tables" value="false" />
                                <item name="Cookies" value="false" />
                                <item name="BackgroundSounds" value="false" />
                                <item name="JavaScript" value="false" />
                                <item name="VBScript" value="false" />
                                <item name="JavaApplets" value="false" />
                                <item name="ActiveXControls" value="false" />
                                <item name="isBanned" value="false" />
                                <item name="isMobileDevice" value="false" />
                                <item name="isSyndicationReader" value="false" />
                                <item name="Crawler" value="false" />
                                <item name="CssVersion" value="0" />
                                <item name="AolVersion" value="0" />
                                <item name="MasterParent" value="true" />
                                <item name="SortOrder" value="1" />
                                <item name="InternalID" value="7294" />
                              </browscapitem>
                              <browscapitem name="Ask">
                                <item name="Parent" value="DefaultProperties" />
                                <item name="Comments" value="Ask" />
                                <item name="Pattern" value="Ask" />
                                <item name="Browser" value="Ask" />
                                <item name="Frames" value="true" />
                                <item name="IFrames" value="true" />
                                <item name="Tables" value="true" />
                                <item name="Crawler" value="true" />
                                <item name="MasterParent" value="true" />
                                <item name="SortOrder" value="100" />
                                <item name="InternalID" value="4163" />
                              </browscapitem>
                              <browscapitem name="Ask">
                                <item name="Parent" value="Ask" />
                                <item name="Pattern" value="Mozilla/?.0 (compatible; Ask Jeeves/Teoma*)" />
                                <item name="Browser" value="Teoma" />
                                <item name="MasterParent" value="false" />
                                <item name="SortOrder" value="100" />
                                <item name="InternalID" value="57" />
                              </browscapitem>
                            </browsercapitems>
                          </browsercaps>

  val sampleBrowsCap = BrowsCap(sampleBrowsCapXml)

  test("browscap has comment") {
    sampleBrowsCap.comments should be ("First line\nSecond line")
  }

  test("browscap has version and released") {
    sampleBrowsCap.version should be ("4911")
    sampleBrowsCap.released should be ("Thu, 03 Nov 2011 07:00:27 -0000")
  }

  test("browscap has items") {
    sampleBrowsCap.items.size should be (3)
  }

  test("browscapitem default handling") {
    val default = sampleBrowsCap.defaultProperties.get
    val ask = sampleBrowsCap.items(1)
    val teoma = sampleBrowsCap.items(2)

    default.attr("Browser") should be ("DefaultProperties")
    ask.attr("Browser") should be ("Ask")
    teoma.attr("Browser") should be ("Teoma")

    default.attr("SortOrder") should be ("1")
    ask.attr("SortOrder") should be ("100")
    teoma.attr("SortOrder") should be ("100")

    default.attr("Version") should be ("0")
    ask.attr("Version") should be ("0")
    teoma.attr("Version") should be ("0")
  }

  test("browscapitem children") {
    val default = sampleBrowsCap.defaultProperties.get
    default.children.length should be (1)
    default.children(0).name should be ("Ask")
  }

  test("browscap parent") {
    val m = sampleBrowsCap.firstMatch("Mozilla/1.0 (compatible; Ask Jeeves/Teoma)")
    m should not be (None)
    m.get.name should be ("Ask")

    m.get.parent should not be (None)
    val parent = m.get.parent.get
    parent.name should be ("Ask")

    parent.parent should not be (None)
    val grandParent = parent.parent.get
    grandParent.name should be ("DefaultProperties")
  }

  test("browscap parent attr lookup") {
    val m = sampleBrowsCap.firstMatch("Mozilla/1.0 (compatible; Ask Jeeves/Teoma)")

    m.get.attr("InternalID") should be ("57")  // target node
    m.get.attr("Crawler") should be ("true")   // parent node
    m.get.attr("isBanned") should be ("false") // default node
  }

  test("browscapitem look up by internal id") {
    var m = sampleBrowsCap.getByInternalID(57)
    m should not be (None)
    m.get.name should be ("Ask")

    m = sampleBrowsCap.getByInternalID(7294)
    m should not be (None)
    m.get.name should be ("DefaultProperties")
  }

  test("browscapitem look up by master parent name") {
    var m = sampleBrowsCap.getMasterParentByName("Ask")
    m should not be (None)
    m.get.name should be ("Ask")
    m.get.attr("InternalID") should be ("4163")
  }

  test("browscap default xml") {
    val bc = BrowsCap()
    val default = bc.defaultProperties.get
  }

  test("browscap firstMatch") {
    val bc = BrowsCap()
    var m = bc.firstMatch("Mozilla/1.0 (compatible; Ask Jeeves/Teoma)")
    m should not be (None)
    m.get.name should be ("Ask")

    m = bc.firstMatch("Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)")
    m should not be (None)
    m.get.name should be ("Google")

    m = bc.firstMatch("Googlebot/2.1 (+http://www.googlebot.com/bot.html)")
    m should not be (None)
    m.get.name should be ("Google")

    m = bc.firstMatch("Googlebot/2.1 (+http://www.google.com/bot.html)")
    m should not be (None)
    m.get.name should be ("Google")
  }
}
