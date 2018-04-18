package org.transmartproject.das.mydas

import grails.util.Holders
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import transmart.mydas.AcghService
import uk.ac.ebi.mydas.configuration.DataSourceConfiguration
import uk.ac.ebi.mydas.configuration.PropertyType
import uk.ac.ebi.mydas.datasource.RangeHandlingAnnotationDataSource
import uk.ac.ebi.mydas.exceptions.BadReferenceObjectException
import uk.ac.ebi.mydas.exceptions.CoordinateErrorException
import uk.ac.ebi.mydas.exceptions.DataSourceException
import uk.ac.ebi.mydas.exceptions.UnimplementedFeatureException
import uk.ac.ebi.mydas.model.DasAnnotatedSegment
import uk.ac.ebi.mydas.model.DasEntryPoint
import uk.ac.ebi.mydas.model.DasType
import uk.ac.ebi.mydas.model.Range

import javax.servlet.ServletContext

/**
 * @author Ruslan Forostianov
 */
@CompileStatic
class AcghDS implements RangeHandlingAnnotationDataSource {

	AcghService acghService
	List<DasEntryPoint> entryPoints
	Long resultInstanceId
	String conceptKey

	void init(ServletContext servletContext, Map<String, PropertyType> stringPropertyTypeMap,
	          DataSourceConfiguration dataSourceConfiguration) throws DataSourceException {
		resultInstanceId = dataSourceConfiguration.getMatcherAgainstDsn().group(1).toLong()
		String ckEncoded = dataSourceConfiguration.getMatcherAgainstDsn().group(2)
		if (ckEncoded) {
			conceptKey = new String(ckEncoded.decodeBase64())
		}
		acghService = Holders.applicationContext.getBean('acghService', AcghService)
	}

	void destroy() {}

	DasAnnotatedSegment getFeatures(String segmentId, Integer maxbins)
			throws BadReferenceObjectException, DataSourceException {

		acghService.getFeatures(resultInstanceId, conceptKey, [segmentId], maxbins).first()
	}

	Collection<DasAnnotatedSegment> getFeatures(Collection<String> segmentIds, Integer maxbins)
			throws UnimplementedFeatureException, DataSourceException {

		acghService.getFeatures resultInstanceId, conceptKey, segmentIds, maxbins
	}

	DasAnnotatedSegment getFeatures(String segmentId, Integer maxbins, Range range)
			throws BadReferenceObjectException, DataSourceException, UnimplementedFeatureException {

		acghService.getFeatures(resultInstanceId, conceptKey, [segmentId], maxbins, range).first()
	}

	Collection<DasAnnotatedSegment> getFeatures(Collection<String> segmentIds, Integer maxbins, Range range)
			throws UnimplementedFeatureException, DataSourceException {

		acghService.getFeatures resultInstanceId, conceptKey, segmentIds, maxbins, range
	}

	DasAnnotatedSegment getFeatures(String segmentId, int start, int stop, Integer maxbins)
			throws BadReferenceObjectException, CoordinateErrorException, DataSourceException {

		acghService.getFeatures(resultInstanceId, conceptKey, [segmentId], maxbins, new Range(start, stop)).first()
	}

	DasAnnotatedSegment getFeatures(String segmentId, int start, int stop, Integer maxbins, Range rows)
			throws BadReferenceObjectException, CoordinateErrorException, DataSourceException, UnimplementedFeatureException {

		acghService.getFeatures(resultInstanceId, conceptKey, [segmentId], maxbins, rows).first()
	}

	@CompileDynamic
	Collection<DasType> getTypes() throws DataSourceException {
		acghService.acghDasTypes
	}

	//Optional
	Integer getTotalCountForType(DasType dasType) throws DataSourceException {}

	//Optional
	URL getLinkURL(String s, String s1) throws UnimplementedFeatureException, DataSourceException {}

	Collection<DasEntryPoint> getEntryPoints(Integer start, Integer stop)
			throws UnimplementedFeatureException, DataSourceException {
		getEntryPoints()
	}

	String getEntryPointVersion() throws UnimplementedFeatureException, DataSourceException {
		acghService.acghEntryPointVersion
	}

	int getTotalEntryPoints() throws UnimplementedFeatureException, DataSourceException {
		getEntryPoints().size()
	}

	List<DasEntryPoint> getEntryPoints() {
		if (entryPoints == null) {
			entryPoints = acghService.getEntryPoints(resultInstanceId)
		}
		entryPoints
	}
}
