package org.transmartproject.das.mydas

import groovy.transform.CompileStatic
import uk.ac.ebi.mydas.configuration.DataSourceConfiguration
import uk.ac.ebi.mydas.configuration.PropertyType
import uk.ac.ebi.mydas.datasource.RangeHandlingAnnotationDataSource
import uk.ac.ebi.mydas.exceptions.DataSourceException

import javax.servlet.ServletContext

/**
 * @author rnugraha
 */
@CompileStatic
class CohortMAFDS extends VcfDS implements RangeHandlingAnnotationDataSource {

	void init(ServletContext servletContext, Map<String, PropertyType> stringPropertyTypeMap,
	          DataSourceConfiguration dataSourceConfiguration) throws DataSourceException {
		super.init(servletContext, stringPropertyTypeMap, dataSourceConfiguration)
		vcfService = service('cohortMAFService')
	}
}
