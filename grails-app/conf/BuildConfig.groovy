grails.project.work.dir = 'target'

grails.project.dependency.resolver = 'maven'
grails.project.dependency.resolution = {

	inherits 'global'
	log 'warn'
	legacyResolve false

	repositories {
		mavenLocal()
		grailsCentral()
		mavenCentral()
		mavenRepo 'http://ec2-35-170-59-132.compute-1.amazonaws.com:8080/artifactory/libs-snapshots'
		mavenRepo 'http://ec2-35-170-59-132.compute-1.amazonaws.com:8080/artifactory/libs-releases'
		mavenRepo 'http://ec2-35-170-59-132.compute-1.amazonaws.com:8080/artifactory/plugins-releases'
		mavenRepo 'http://ec2-35-170-59-132.compute-1.amazonaws.com:8080/artifactory/plugins-snapshots'
		mavenRepo 'https://repo.transmartfoundation.org/content/repositories/public/'
	}

	dependencies {
		compile 'net.sf.opencsv:opencsv:2.3'
		compile 'org.transmartproject:transmart-core-api:18.1-SNAPSHOT'
		compile 'uk.ac.ebi.mydas:mydas:1.7.0.transmart-16.2'
		test 'org.grails:grails-datastore-test-support:1.0.2-grails-2.4'
	}

	plugins {
		build ':release:3.1.2', ':rest-client-builder:2.1.1', {
			export = false
		}
	}
}
