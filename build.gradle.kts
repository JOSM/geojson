import com.github.spotbugs.SpotBugsTask

plugins {
  id("org.openstreetmap.josm") version "0.5.3"
  id("com.github.ben-manes.versions") version "0.20.0"
  id("com.github.spotbugs") version "1.6.5"
  checkstyle
  eclipse
  java
}

checkstyle {
  toolVersion = "8.13"
  isIgnoreFailures = true
}
spotbugs {
  toolVersion = "3.1.8"
}
tasks.withType(SpotBugsTask::class) {
  reports {
    xml.isEnabled = false
    html.isEnabled = true
  }
  ignoreFailures = true
}


java {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
  mavenCentral()
}
dependencies {
    testImplementation("org.awaitility:awaitility:3.1.2")
    testImplementation("junit:junit:4.12")
    testImplementation("org.openstreetmap.josm:josm-unittest:SNAPSHOT"){ isChanging = true }
}

// See https://discuss.gradle.org/t/how-to-add-a-single-extra-resource-to-the-existing-resources/17070/2
tasks.withType(ProcessResources::class).getByName("processResources") {
  from(projectDir) {
    include("LICENSE", "README.md")
  }
}

base.archivesBaseName = "geojson"

josm {
  i18n {
    pathTransformer = getPathTransformer("github.com/JOSM/geojson/blob")
  }
}
