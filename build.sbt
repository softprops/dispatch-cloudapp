organization := "me.lessis"

name := "dispatch-cloudapp"

version := "0.1.0-SNAPSHOT"

libraryDependencies += "net.databinder.dispatch" %% "core" % "0.9.0-alpha2"

crossScalaVersions :=
  Seq("2.8.0", "2.8.1", "2.8.2", "2.9.0", "2.9.0-1", "2.9.1")

homepage := Some(url("https://github.com/softprops/dispatch-cloudapp"))

publishMavenStyle := true

publishTo <<= version { v =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT")) Some("snapshots" at nexus + "content/repositories/snapshots") 
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}
 
publishArtifact in Test := false

licenses <<= (version)(v => Seq(
  ("MIT" -> url("https://github.com/softprops/dispatch-cloudapp/tree/%s/LICENSE".format(v)))))

pomExtra := (
  <scm>
    <url>git@github.com:softprops/dispatch-cloudapp.git</url>
    <connection>scm:git:git@github.com:softprops/dispatch-cloudapp.git</connection>
  </scm>
  <developers>
    <developer>
      <id>softprops</id>
      <name>Doug Tangren</name>
      <url>https://github.com/softprops</url>
    </developer>
  </developers>)
