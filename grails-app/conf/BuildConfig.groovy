grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"

grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // uncomment to disable ehcache
        // excludes 'ehcache'
    }
    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    legacyResolve false // whether to do a secondary resolve on plugin installation, not advised and here for backwards compatibility
    repositories {
        grailsCentral()
        mavenCentral()
        mavenRepo "http://repo.thehyve.nl/content/groups/public/"
        mavenRepo "http://mydas.googlecode.com/svn/repository/"
    }
    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.
        compile('uk.ac.ebi.mydas:mydas:1.6.8-hyve-SNAPSHOT') {
            excludes "slf4j-nop"
        }
        compile 'net.sf.opencsv:opencsv:2.3'
        compile 'org.transmartproject:transmart-core-api:1.0-SNAPSHOT'
    }

    plugins {
        build(":tomcat:$grailsVersion",
              ":release:2.2.1",
              ":rest-client-builder:1.0.3") {
            export = false
        }
    }
}