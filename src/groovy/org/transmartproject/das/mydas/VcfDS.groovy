package org.transmartproject.das.mydas

import grails.util.Holders
import groovy.transform.CompileStatic
import transmart.mydas.VcfServiceAbstract
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
 * @author j.hudecek
 */
@CompileStatic
class VcfDS implements RangeHandlingAnnotationDataSource {

	VcfServiceAbstract vcfService
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
	}

	void destroy() {}

	DasAnnotatedSegment getFeatures(String segmentId, Integer maxbins)
			throws BadReferenceObjectException, DataSourceException {

		vcfService.getFeatures(resultInstanceId, conceptKey, [segmentId], maxbins).first()
	}

	DasAnnotatedSegment getFeatures(String segmentId, int start, int stop, Integer maxbins)
			throws BadReferenceObjectException, CoordinateErrorException, DataSourceException {

		vcfService.getFeatures(resultInstanceId, conceptKey, [segmentId],
				maxbins, new Range(start, stop)).first()
	}

	DasAnnotatedSegment getFeatures(String segmentId, int start, int stop,
	                                Integer maxbins, Range range)
			throws BadReferenceObjectException, CoordinateErrorException,
					DataSourceException, UnimplementedFeatureException {

		vcfService.getFeatures(resultInstanceId, conceptKey, [segmentId], maxbins, range).first()
	}

	DasAnnotatedSegment getFeatures(String segmentId, Integer maxbins,Range range)
			throws BadReferenceObjectException, DataSourceException, UnimplementedFeatureException {

		vcfService.getFeatures(resultInstanceId, conceptKey, [segmentId], maxbins, range).first()
	}

	Collection<DasAnnotatedSegment> getFeatures(Collection<String> segmentIds,
	                                            Integer maxbins, Range range)
			throws UnimplementedFeatureException, DataSourceException {

		vcfService.getFeatures(resultInstanceId, conceptKey, segmentIds, maxbins, range)
	}

	Collection<DasAnnotatedSegment> getFeatures(Collection<String> segmentIds, Integer maxbins)
			throws UnimplementedFeatureException, DataSourceException {

		vcfService.getFeatures(resultInstanceId, conceptKey, segmentIds, maxbins)
	}

	// TODO
	Collection<DasType> getTypes() throws DataSourceException {}

	// TODO
	Integer getTotalCountForType(DasType dasType) throws DataSourceException {}

	// TODO
	URL getLinkURL(String field, String id) throws UnimplementedFeatureException, DataSourceException {}

	// TODO
	Collection<DasEntryPoint> getEntryPoints(Integer integer, Integer integer2)
			throws UnimplementedFeatureException, DataSourceException {}

	// TODO
	String getEntryPointVersion() throws UnimplementedFeatureException, DataSourceException {}

	// TODO
	int getTotalEntryPoints() throws UnimplementedFeatureException, DataSourceException { 0 }

	protected <T> T getBean(String name, Class<T> clazz) {
		Holders.applicationContext.getBean name, clazz
	}

	protected VcfServiceAbstract service(String name) {
		getBean name, VcfServiceAbstract
	}
}
