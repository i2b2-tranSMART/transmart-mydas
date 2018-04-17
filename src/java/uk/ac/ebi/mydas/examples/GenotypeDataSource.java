package uk.ac.ebi.mydas.examples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.mydas.configuration.DataSourceConfiguration;
import uk.ac.ebi.mydas.configuration.PropertyType;
import uk.ac.ebi.mydas.datasource.RangeHandlingAnnotationDataSource;
import uk.ac.ebi.mydas.exceptions.BadReferenceObjectException;
import uk.ac.ebi.mydas.exceptions.CoordinateErrorException;
import uk.ac.ebi.mydas.exceptions.DataSourceException;
import uk.ac.ebi.mydas.exceptions.UnimplementedFeatureException;
import uk.ac.ebi.mydas.model.DasAnnotatedSegment;
import uk.ac.ebi.mydas.model.DasEntryPoint;
import uk.ac.ebi.mydas.model.DasType;
import uk.ac.ebi.mydas.model.Range;

import javax.servlet.ServletContext;
import java.net.URL;
import java.util.Collection;
import java.util.Map;

public class GenotypeDataSource implements RangeHandlingAnnotationDataSource {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	ServletContext servletContext;
	Map<String, PropertyType> globalParameters;
	DataSourceConfiguration config;
	GenotypeManager genotypeManager;

	public void init(ServletContext servletContext, Map<String, PropertyType> globalParameters,
	                 DataSourceConfiguration dataSourceConfig) throws DataSourceException {
		this.servletContext = servletContext;
		this.globalParameters = globalParameters;
		config = dataSourceConfig;

		String databaseUrl = "";
		if (config.getDataSourceProperties().containsKey("databaseUrl")) {
			databaseUrl = config.getDataSourceProperties().get("databaseUrl").getValue();
		}

		String databaseUser = "";
		if (config.getDataSourceProperties().containsKey("databaseUser")) {
			databaseUser = config.getDataSourceProperties().get("databaseUser").getValue();
		}

		String databasePass = "";
		if (config.getDataSourceProperties().containsKey("databasePass")) {
			databasePass = config.getDataSourceProperties().get("databasePass").getValue();
		}

		if (databaseUrl == null || databaseUrl.equals("") ||
				databaseUser == null || databaseUser.equals("") ||
				databasePass == null || databasePass.equals("")) {
			throw new DataSourceException("a database url must be set such in the configuration for example: ");
		}
		try {
			logger.debug("connection params={} user:{} pass:{}", databaseUrl, databaseUser, databasePass);
			genotypeManager = new GenotypeManager(databaseUrl, databaseUser, databasePass);
		}
		catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void destroy() {
		genotypeManager.close();
	}

	public DasAnnotatedSegment getFeatures(String segmentId, int start, int stop, Integer maxbins)
			throws BadReferenceObjectException, DataSourceException {
		if (maxbins == null) {
			maxbins = -1;
		}
		return genotypeManager.getSubmodelBySegmentId(segmentId, start, stop, maxbins);
	}

	public DasAnnotatedSegment getFeatures(String segmentId, Integer maxbins)
			throws BadReferenceObjectException, DataSourceException {
		if (maxbins == null) {
			maxbins = -1;
		}
		return genotypeManager.getSubmodelBySegmentId(segmentId, -1, -1, maxbins);
	}

	public Collection<DasType> getTypes() {
		return genotypeManager.getTypes();
	}

	public URL getLinkURL(String field, String id) throws UnimplementedFeatureException {
		throw new UnimplementedFeatureException("No implemented");
	}

	public String getEntryPointVersion() {
		return genotypeManager.getDatabase();
	}

	public Integer getTotalCountForType(DasType type) throws DataSourceException {
		return genotypeManager.getTotalCountForType(type.getId());
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
			throws BadReferenceObjectException, CoordinateErrorException,
			DataSourceException, UnimplementedFeatureException {
		return null;
	}

	public Collection<DasAnnotatedSegment> getFeatures(Collection<String> featureIdCollection, Integer maxbins)
			throws UnimplementedFeatureException, DataSourceException {
		return null;
	}

	public Collection<DasEntryPoint> getEntryPoints(Integer start, Integer stop) {
		return null;
	}

	public int getTotalEntryPoints() {
		return 0;
	}
}
