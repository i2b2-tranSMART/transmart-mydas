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
class VcfInfoDS implements RangeHandlingAnnotationDataSource {

	String infoField
	VcfServiceAbstract vcfService
	Long resultInstanceId
	String conceptKey
	List<DasEntryPoint> entryPoints

	void init(ServletContext servletContext, Map<String, PropertyType> stringPropertyTypeMap,
	          DataSourceConfiguration dataSourceConfiguration) throws DataSourceException {
		infoField = dataSourceConfiguration.getMatcherAgainstDsn().group(1)
		resultInstanceId = dataSourceConfiguration.getMatcherAgainstDsn().group(2).toLong()
		String ckEncoded = dataSourceConfiguration.getMatcherAgainstDsn().group(3)
		if (ckEncoded) {
			conceptKey = new String(ckEncoded.decodeBase64())
		}
		vcfService = Holders.applicationContext.getBean('vcfInfoService', VcfServiceAbstract)
	}

	void destroy() {}

	DasAnnotatedSegment getFeatures(String segmentId, Integer maxbins)
			throws BadReferenceObjectException, DataSourceException {

		vcfService.getFeatures(resultInstanceId, conceptKey, [segmentId], maxbins,
				null, [infoField: infoField]).first()
	}

	DasAnnotatedSegment getFeatures(String segmentId, int start, int stop, Integer maxbins)
			throws BadReferenceObjectException, CoordinateErrorException, DataSourceException {

		vcfService.getFeatures(resultInstanceId, conceptKey, [segmentId], maxbins,
				new Range(start, stop), [infoField: infoField]).first()
	}

	DasAnnotatedSegment getFeatures(String segmentId, int start, int stop,
	                                Integer maxbins, Range range)
			throws BadReferenceObjectException, CoordinateErrorException,
					DataSourceException, UnimplementedFeatureException {

		vcfService.getFeatures(resultInstanceId, conceptKey, [segmentId], maxbins,
				range, [infoField: infoField]).first()
	}

	DasAnnotatedSegment getFeatures(String segmentId, Integer maxbins, Range range)
			throws BadReferenceObjectException, DataSourceException, UnimplementedFeatureException {

		vcfService.getFeatures(resultInstanceId, conceptKey, [segmentId], maxbins,
				range, [infoField: infoField]).first()
	}

	Collection<DasAnnotatedSegment> getFeatures(Collection<String> segmentIds, Integer maxbins,
	                                            Range range)
			throws UnimplementedFeatureException, DataSourceException {

		vcfService.getFeatures(resultInstanceId, conceptKey, segmentIds, maxbins,
				range, [infoField: infoField])
	}

	Collection<DasAnnotatedSegment> getFeatures(Collection<String> segmentIds, Integer maxbins)
			throws UnimplementedFeatureException, DataSourceException {

		vcfService.getFeatures(resultInstanceId, conceptKey, segmentIds, maxbins,
				null, [infoField: infoField])
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
}
