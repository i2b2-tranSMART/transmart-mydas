package uk.ac.ebi.mydas.examples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.mydas.exceptions.DataSourceException;
import uk.ac.ebi.mydas.model.DasAnnotatedSegment;
import uk.ac.ebi.mydas.model.DasFeature;
import uk.ac.ebi.mydas.model.DasFeatureOrientation;
import uk.ac.ebi.mydas.model.DasMethod;
import uk.ac.ebi.mydas.model.DasPhase;
import uk.ac.ebi.mydas.model.DasType;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GenotypeManager {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	// the types used in this data source
	private List<DasType> types;

	// Types to be used in the data source: chromosome, gene, transcript and exon
	private DasType geneType;

	// As this data source just have one method, it can be defined as a parameter to facilitate its use
	private DasMethod method;

	private Connection connection;

	private String database = "genotype1";

	public GenotypeManager(String databaseUrl, String databaseUser, String databasePass) throws DataSourceException {

		method = new DasMethod("not_recorded", "not_recorded", "ECO:0000037");

		//String url = "jdbc:mysql://localhost:3306/" + database;
		// String url = "jdbc:mysql://ensembldb.ensembl.org:5306/"+database;
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			connection = DriverManager.getConnection(databaseUrl, databaseUser, databasePass);
		}
		catch (InstantiationException | ClassNotFoundException | IllegalAccessException e) {
			throw new DataSourceException("Problems loading the MySql driver", e);
		}
		catch (SQLException e) {
			throw new DataSourceException("Problems connecting to the ensembl database", e);
		}
		// Initialize types
		types = new ArrayList<>();
		try {
			Statement s = connection.createStatement();
			s.executeQuery("select distinct bases from genotype;");
			ResultSet rs = s.getResultSet();
			while (rs.next()) {
				String typeId = rs.getString("bases");
				logger.debug("typeid={}", typeId);
				types.add(new DasType(typeId, "", "SO:0000694", ""));

			}
			rs.close();
			s.close();
		}
		catch (SQLException e) {
			throw new DataSourceException("Problems executing the sql query", e);
		}
	}

	public void close() {
		try {
			connection.close();
		}
		catch (Exception ignored) {}
	}

	public DasAnnotatedSegment getSubmodelBySegmentId(String segmentId, int start, int stop) {
		return getSubmodelBySegmentId(segmentId, start, stop, -1);
	}

	public DasAnnotatedSegment getSubmodelBySegmentId(String segmentId, int start, int stop, int maxbins) {
		String sql = "select * from genotype where chromosome=" + segmentId + " and position > " + start + " and position < " + stop + ";";
		logger.debug(sql);
		Collection<DasFeature> features = getGenotypeFeatures(sql);
		DasAnnotatedSegment segment = null;
		try {
			segment = new DasAnnotatedSegment(segmentId, start, stop, "1.0", segmentId, features);
		}
		catch (DataSourceException e) {
			logger.error(e.getMessage(), e);
		}
		return segment;
	}

	private Collection<DasFeature> getGenotypeFeatures(String sql) {
		Collection<DasFeature> features = new ArrayList<>();
		try {
			Statement s = connection.createStatement();
			s.executeQuery(sql);
			ResultSet rs = s.getResultSet();
			while (rs.next()) {
				String typeId = rs.getString("bases");
				String id = rs.getString("rs_id");
				int position = rs.getInt("position");
				DasType type = new DasType(typeId, "", "SO:0000694", "");
				DasMethod method = null;
				try {
					method = new DasMethod("23AndMe", "microarray", "");
				}
				catch (DataSourceException e1) {
					logger.error(e1.getMessage(), e1);
				}

				DasFeature feature = null;
				try {
					feature = new DasFeature(id, id, type, method, position, position, 1d,
							DasFeatureOrientation.ORIENTATION_NOT_APPLICABLE, DasPhase.PHASE_NOT_APPLICABLE,
							null, null, null, null, null);
				}
				catch (DataSourceException e) {
					logger.error(e.getMessage(), e);
				}
				features.add(feature);
			}
			rs.close();
			s.close();
		}
		catch (SQLException e) {
			try {
				throw new DataSourceException("Problems executing the sql query", e);
			}
			catch (DataSourceException e1) {
				logger.error(e1.getMessage(), e1);
			}
		}

		return features;
	}

	public List<DasType> getTypes() {
		if (logger.isDebugEnabled()) {
			logger.debug("Getting types from genotype manager");
			for (DasType type : types) {
				logger.debug(type.getCvId());
			}
		}
		return types;
	}

	public int getTotalCountForType(String typeId) throws DataSourceException {

		int count = 0;
		try {
			PreparedStatement ps = connection.prepareStatement(
					"SELECT count(bases) as num from genotype where bases=?");
			ps.setString(1, typeId);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				count = rs.getInt("num");
			}
			rs.close();
			ps.close();
		}
		catch (SQLException e) {
			throw new DataSourceException("Problems executing the sql query", e);
		}

		return count;
	}

	public String getDatabase() {
		return database;
	}
}
