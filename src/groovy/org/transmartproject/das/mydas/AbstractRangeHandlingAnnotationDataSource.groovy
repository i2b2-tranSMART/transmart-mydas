package org.transmartproject.das.mydas

import grails.util.Holders
import groovy.transform.CompileStatic
import transmart.mydas.VcfServiceAbstract
import uk.ac.ebi.mydas.datasource.RangeHandlingAnnotationDataSource

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
@CompileStatic
abstract class AbstractRangeHandlingAnnotationDataSource implements RangeHandlingAnnotationDataSource {

	Long resultInstanceId
	String conceptKey

	protected <T> T getBean(String name, Class<T> clazz) {
		Holders.applicationContext.getBean name, clazz
	}

	protected VcfServiceAbstract service(String name) {
		getBean name, VcfServiceAbstract
	}
}
