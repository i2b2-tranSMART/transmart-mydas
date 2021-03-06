package uk.ac.ebi.mydas.examples;

import uk.ac.ebi.mydas.configuration.DataSourceConfiguration;
import uk.ac.ebi.mydas.configuration.PropertyType;
import uk.ac.ebi.mydas.datasource.ReferenceDataSource;
import uk.ac.ebi.mydas.exceptions.BadReferenceObjectException;
import uk.ac.ebi.mydas.exceptions.DataSourceException;
import uk.ac.ebi.mydas.exceptions.UnimplementedFeatureException;
import uk.ac.ebi.mydas.extendedmodel.DasUnknownFeatureSegment;
import uk.ac.ebi.mydas.model.DasAnnotatedSegment;
import uk.ac.ebi.mydas.model.DasEntryPoint;
import uk.ac.ebi.mydas.model.DasFeature;
import uk.ac.ebi.mydas.model.DasSequence;
import uk.ac.ebi.mydas.model.DasType;
import uk.ac.ebi.mydas.model.Range;

import javax.servlet.ServletContext;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Data Source that reads a GFF 2 file which path has been specified in the
 * configuration file as a property of the datasource element
 * Methods related to a Reference server (getSequence, getEntryPointVersion and getEntryPoints)
 * do not provide real data related to the GFF File.
 */
public class GFFFileReferenceDataSource implements ReferenceDataSource {

	ServletContext servletContext;
	Map<String, PropertyType> globalParameters;
	DataSourceConfiguration config;
	String path;
	String path2;
	private Collection<DasAnnotatedSegment> segments;
	private Collection<DasType> types;
	private Map<String, DasSequence> sequences;

	/**
	 * The path is recovery from the configuration, the file is then parsed and
	 * keep in memory as a DasSegment collection object that is queried for each method
	 */
	public void init(ServletContext servletContext, Map<String, PropertyType> globalParameters,
	                 DataSourceConfiguration dataSourceConfig) throws DataSourceException {
		this.servletContext = servletContext;
		this.globalParameters = globalParameters;
		config = dataSourceConfig;
		path = config.getDataSourceProperties().get("gff_file").getValue();
		path2 = config.getDataSourceProperties().get("fasta_file").getValue();
		try {
			GFF2Parser parser = new GFF2Parser(new FileInputStream(servletContext.getRealPath(path)));
			segments = parser.parse();
			types = parser.getTypes();
		}
		catch (FileNotFoundException e) {
			throw new DataSourceException("The data source cannot be loaded. The file couldn't be oppened", e);
		}
		catch (Exception e) {
			throw new DataSourceException("The data source cannot be loaded because of parsing problems", e);
		}
		try {
			FastaParser parser2 = new FastaParser(new FileInputStream(servletContext.getRealPath(path2)), path2);
			sequences = parser2.parse();
		}
		catch (FileNotFoundException e) {
			throw new DataSourceException("The reference data source cannot be loaded. The fasta file couldn't be oppened", e);
		}
		catch (Exception e) {
			throw new DataSourceException("The reference data source cannot be loaded because of parsing problems with the fasta file", e);
		}
	}

	/**
	 * Nothing to destroy
	 */
	public void destroy() {}

	/**
	 * Look into the list of segments for the one with the same ID. if is not there it throws a BadReferenceObjectException
	 */
	public DasAnnotatedSegment getFeatures(String segmentId, Integer maxbins)
			throws BadReferenceObjectException, DataSourceException {
		for (DasAnnotatedSegment segment : segments) {
			if (segment.getSegmentId().equals(segmentId)) {
				return segment;
			}
		}
		throw new BadReferenceObjectException("The id is not in the file", segmentId);
	}

	/**
	 * return the already built list of types.
	 */
	public Collection<DasType> getTypes() {
		return types;
	}

	public Collection<DasAnnotatedSegment> getFeatures(Collection<String> featureIdCollection, Integer maxbins)
			throws UnimplementedFeatureException, DataSourceException {
		Collection<DasAnnotatedSegment> segmentsResponse = new ArrayList<>();
		for (String featureId : featureIdCollection) {
			boolean found = false;
			for (DasAnnotatedSegment segment : segments) {
				for (DasFeature feature : segment.getFeatures()) {
					if (feature.getFeatureId().equals(featureId)) {
						segmentsResponse.add(new DasAnnotatedSegment(segment.getSegmentId(),
								segment.getStartCoordinate(), segment.getStopCoordinate(), segment.getVersion(),
								segment.getSegmentLabel(), Collections.singleton(feature)));
						found = true;
					}
				}
			}
			if (!found) {
				segmentsResponse.add(new DasUnknownFeatureSegment(featureId));
			}
		}
		return segmentsResponse;
	}

	/**
	 * count the number of times that the type id appears in all the segments
	 */
	public Integer getTotalCountForType(DasType type) {
		int count = 0;
		for (DasAnnotatedSegment segment : segments) {
			for (DasFeature feature : segment.getFeatures()) {
				if (type.getId().equals(feature.getType().getId())) {
					count++;
				}
			}
		}
		return count;
	}

	public URL getLinkURL(String field, String id) throws UnimplementedFeatureException {
		throw new UnimplementedFeatureException("No implemented");
	}

	public DasSequence getSequence(String segmentId) throws BadReferenceObjectException {
		DasSequence seq = sequences.get(segmentId);
		if (seq == null) {
			throw new BadReferenceObjectException("", segmentId);
		}
		return seq;
	}

	public String getEntryPointVersion() {
		return "1.0";
	}

	/**
	 * Just for testing purposes, it does not retrieve real data.
	 *
	 * @param start Initial row position on the entry points collection for this server
	 * @param stop  Final row position ont the entry points collection for this server
	 * @return A sub ordered collection of entry points from the start row to the stop row
	 */
	public Collection<DasEntryPoint> getEntryPoints(Integer start, Integer stop) throws DataSourceException {
		List<DasEntryPoint> entryPoints = new ArrayList<>();
		for (String id : sequences.keySet()) {
			DasSequence seq = sequences.get(id);
			entryPoints.add(new DasEntryPoint(id, seq.getStopCoordinate(), seq.getStartCoordinate(),
					"DNA", "1.0", null, seq.getLabel(), false));
		}

		if (start != null && stop != null) {
			return entryPoints.subList(start, stop);
		}

		return entryPoints;
	}

	public int getTotalEntryPoints() {
		return sequences.size();
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
}
