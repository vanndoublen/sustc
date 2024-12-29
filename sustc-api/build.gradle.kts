import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.springframework.boot.gradle.tasks.bundling.BootJar
import org.springframework.boot.gradle.tasks.run.BootRun

//plugins {
//    java
//    alias(libs.plugins.spring.boot)
//    alias(libs.plugins.spring.dependencyManagement)
//    alias(libs.plugins.lombok)
//    alias(libs.plugins.shadow)
//    kotlin("jvm")
//}



dependencies {
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("com.alibaba.fastjson2:fastjson2:2.0.53")
    implementation("org.postgresql:postgresql:42.7.4")

    // You may add any utility library you want to use, such as guava.
    // ORM libraries are prohibited in this project.

    implementation("org.springframework.boot:spring-boot-starter-web")

    implementation("org.springframework:spring-web")
    implementation("org.springframework:spring-webmvc")

    // for validation annotations
    implementation("org.springframework.boot:spring-boot-starter-validation")

    //for @Data annotation and other Lombok features (maybe already have it here)
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

//    // jpa for object relational mapping
//    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    //spring security
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation(kotlin("stdlib-jdk8"))

    testImplementation ("org.springframework.security:spring-security-test")

    // added
//    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
//    implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity6:3.1.1.RELEASE")

    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
}

plugins {
    java
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependencyManagement)
    alias(libs.plugins.lombok)
    alias(libs.plugins.shadow)
    kotlin("jvm")
}

repositories {
    mavenCentral()
    google()
}

//dependencies {
//    implementation("org.springframework.boot:spring-boot-starter-security")
//    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
//    implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity6:3.1.1.RELEASE")
//    implementation("org.springframework.boot:spring-boot-starter-web")
//
//
//    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
//
//    implementation ("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
//}


tasks.withType<BootRun> {
    enabled = false
}

tasks.withType<BootJar> {
    enabled = false
}

tasks.register("submitJar") {
    group = "application"
    description = "Prepare an uber-JAR for submission"

    tasks.getByName<ShadowJar>("shadowJar") {
        archiveFileName = "sustc-api.jar"
        destinationDirectory = File("$rootDir/submit")
        dependencies {
            exclude(dependency("ch.qos.logback:logback-.*"))
        }
    }.let { dependsOn(it) }
}

tasks.clean {
    delete(fileTree("$rootDir/submit").matching { include("*.jar") })
}
repositories {
    mavenCentral()
}
kotlin {
    jvmToolchain(17)
}