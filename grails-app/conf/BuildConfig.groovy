grails.project.work.dir = 'target'

grails.project.dependency.resolver = 'maven'
grails.project.dependency.resolution = {

	inherits 'global'
	log 'warn'
	legacyResolve false

	repositories {
		mavenLocal() // Note: use 'grails maven-install' to install required plugins locally
		grailsCentral()
		mavenCentral()
		mavenRepo 'https://repo.transmartfoundation.org/content/repositories/public/'
	}

	dependencies {
		compile 'net.sf.opencsv:opencsv:2.3'
		compile 'org.transmartproject:transmart-core-api:16.2'
		compile 'uk.ac.ebi.mydas:mydas:1.7.0.transmart-16.2'
	}

	plugins {
		build ':release:3.1.2', ':rest-client-builder:2.1.1', {
			export = false
		}
	}
}
