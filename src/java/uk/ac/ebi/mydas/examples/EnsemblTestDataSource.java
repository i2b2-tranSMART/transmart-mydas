package uk.ac.ebi.mydas.examples;

import uk.ac.ebi.mydas.configuration.DataSourceConfiguration;
import uk.ac.ebi.mydas.configuration.PropertyType;
import uk.ac.ebi.mydas.datasource.RangeHandlingAnnotationDataSource;
import uk.ac.ebi.mydas.exceptions.BadReferenceObjectException;
import uk.ac.ebi.mydas.exceptions.CoordinateErrorException;
import uk.ac.ebi.mydas.exceptions.DataSourceException;
import uk.ac.ebi.mydas.exceptions.UnimplementedFeatureException;
import uk.ac.ebi.mydas.extendedmodel.DasUnknownFeatureSegment;
import uk.ac.ebi.mydas.model.DasAnnotatedSegment;
import uk.ac.ebi.mydas.model.DasComponentFeature;
import uk.ac.ebi.mydas.model.DasEntryPoint;
import uk.ac.ebi.mydas.model.DasFeature;
import uk.ac.ebi.mydas.model.DasType;
import uk.ac.ebi.mydas.model.Range;

import javax.servlet.ServletContext;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class EnsemblTestDataSource implements RangeHandlingAnnotationDataSource {
	ServletContext servletContext;
	Map<String, PropertyType> globalParameters;
	DataSourceConfiguration config;
	EnsemblTestManager ensembl;

	public void init(ServletContext servletContext, Map<String, PropertyType> globalParameters,
	                 DataSourceConfiguration dataSourceConfig) throws DataSourceException {
		this.servletContext = servletContext;
		this.globalParameters = globalParameters;
		config = dataSourceConfig;
		ensembl = new EnsemblTestManager();
	}

	public void destroy() {
		ensembl.close();
	}

	public DasAnnotatedSegment getFeatures(String segmentId, int start, int stop, Integer maxbins)
			throws BadReferenceObjectException, DataSourceException {
		if (maxbins == null) {
			maxbins = -1;
		}
		return ensembl.getSubmodelBySegmentId(segmentId, start, stop, maxbins);
	}

	public DasAnnotatedSegment getFeatures(String segmentId, Integer maxbins)
			throws BadReferenceObjectException, DataSourceException {
		if (maxbins == null) {
			maxbins = -1;
		}
		return ensembl.getSubmodelBySegmentId(segmentId, -1, -1, maxbins);
	}

	public Collection<DasAnnotatedSegment> getFeatures(Collection<String> featureIdCollection, Integer maxbins)
			throws UnimplementedFeatureException, DataSourceException {
		Collection<DasAnnotatedSegment> segments = ensembl.getSubmodelByFeatureId(featureIdCollection);
		Collection<DasAnnotatedSegment> segmentsResponse = new ArrayList<>();
		for (String featureId : featureIdCollection) {
			boolean found = false;
			for (DasAnnotatedSegment segment : segments) {
				for (DasFeature feature : segment.getFeatures()) {
					if (feature.getFeatureId().equals(featureId)) {
						segmentsResponse.add(new DasAnnotatedSegment(segment.getSegmentId(), segment.getStartCoordinate(),
								segment.getStopCoordinate(), segment.getVersion(),
								segment.getSegmentLabel(), Collections.singleton(feature)));
						found = true;
						break;
					}
					else if (lookInside((DasComponentFeature) feature, featureId, segmentsResponse, segment)) {
						found = true;
						break;
					}
				}
			}
			if (!found) {
				segmentsResponse.add(new DasUnknownFeatureSegment(featureId));
			}
		}
		return segmentsResponse;
	}

	private boolean lookInside(DasComponentFeature component, String featureId,
	                           Collection<DasAnnotatedSegment> segmentsResponse,
	                           DasAnnotatedSegment segment) throws DataSourceException {
		if (component.hasSubParts()) {
			for (DasComponentFeature subcomponent : component.getReportableSubComponents()) {
				if (subcomponent.getFeatureId().equals(featureId)) {
					segmentsResponse.add(new DasAnnotatedSegment(segment.getSegmentId(), segment.getStartCoordinate(),
							segment.getStopCoordinate(), segment.getVersion(), segment.getSegmentLabel(),
							Collections.singleton((DasFeature) subcomponent)));
					return true;
				}
				else if (lookInside(subcomponent, featureId, segmentsResponse, segment)) {
					return true;
				}
			}
		}
		return false;
	}

	public Collection<DasType> getTypes() {
		return ensembl.getTypes();
	}

	public URL getLinkURL(String field, String id) throws UnimplementedFeatureException {
		throw new UnimplementedFeatureException("No implemented");
	}

	public Collection<DasEntryPoint> getEntryPoints(Integer start, Integer stop) throws DataSourceException {
		return ensembl.getEntryPoints(start, stop);
	}

	public String getEntryPointVersion() {
		return ensembl.getDatabase();
	}

	public int getTotalEntryPoints() throws DataSourceException {
		return ensembl.getEntryPoints(null, null).size();
	}

	public Integer getTotalCountForType(DasType type) throws DataSourceException {
		return ensembl.getTotalCountForType(type.getId());
	}

	public DasAnnotatedSegment getFeatures(String segmentId, Integer maxbins, Range rows)
			throws BadReferenceObjectException, DataSourceException, UnimplementedFeatureException {
		return null;
	}

	public Collection<DasAnnotatedSegment> getFeatures(Collection<String> featureIdCollection,
	                                                   Integer maxbins, Range rows)
			throws UnimplementedFeatureException, DataSourceException {
		return null;
	}

	public DasAnnotatedSegment getFeatures(String segmentId, int start, int stop, Integer maxbins, Range rows)
			throws BadReferenceObjectException, CoordinateErrorException, DataSourceException, UnimplementedFeatureException {
		return null;
	}
}
