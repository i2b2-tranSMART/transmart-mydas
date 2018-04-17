package uk.ac.ebi.mydas.examples;

import uk.ac.ebi.mydas.model.DasAnnotatedSegment;
import uk.ac.ebi.mydas.model.DasFeature;
import uk.ac.ebi.mydas.model.DasFeatureOrientation;
import uk.ac.ebi.mydas.model.DasMethod;
import uk.ac.ebi.mydas.model.DasPhase;
import uk.ac.ebi.mydas.model.DasType;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;

public class GFF2Parser {
	private Scanner scanner;
	private List<DasAnnotatedSegment> segments;
	private List<DasType> types;
	private String version;
	private String date;
	private int featureid = 1;

	public GFF2Parser(InputStream gffdoc) {
		scanner = new Scanner(gffdoc);
		segments = new ArrayList<>();
		types = new ArrayList<>();
	}

	private final void processLineByLine() throws Exception {
		try {
			while (scanner.hasNextLine()) {
				processLine(scanner.nextLine());
			}
		}
		finally {
			scanner.close();
		}
	}

	private void processLine(String aLine) throws Exception {
		if (aLine.startsWith("##")) {
			processComment(aLine.substring(2));
		}
		else {
			processFeature(aLine);
		}
	}

	private void processComment(String aComment) throws Exception {
		if (aComment.startsWith("gff-version")) {
			version = aComment.substring(12);
		}
		else if (aComment.startsWith("date")) {
			date = aComment.substring(5);
		}
		else if (aComment.startsWith("sequence-region")) {
			processSequenceRegion(aComment.substring(16).trim());
		}
	}

	private void processSequenceRegion(String sequenceRegion) throws Exception {
		String[] parts = sequenceRegion.split(" ");
		if (parts.length != 3) {
			throw new Exception("Parsing Error: a sequence-region doesn't have the right number of fields [" + sequenceRegion + "]");
		}
		segments.add(new DasAnnotatedSegment(parts[0], Integer.valueOf(parts[1]), Integer.valueOf(parts[2]),
				"FromFile", parts[0], new ArrayList<DasFeature>()));
	}

	private void processFeature(String afeature) throws Exception {
		String[] parts = afeature.split("\t");
		if (parts.length < 8) {
			throw new Exception("Parsing Error: A feature doesn't have the right number of fields [" + afeature + "]");
		}
		Double score = null;
		if (!parts[5].equals(".")) {
			try {
				score = Double.parseDouble(parts[5]);
			}
			catch (NumberFormatException nfe) {
				throw new Exception("Parsing Error: the feature " + parts[2] + " has a bad score field [" + parts[6] + "]", nfe);
			}
		}

		DasFeatureOrientation orientation = null;
		if (parts[6].equals("+")) {
			orientation = DasFeatureOrientation.ORIENTATION_SENSE_STRAND;
		}
		else if (parts[6].equals("-")) {
			orientation = DasFeatureOrientation.ORIENTATION_ANTISENSE_STRAND;
		}
		else if (parts[6].equals(".")) {
			orientation = DasFeatureOrientation.ORIENTATION_NOT_APPLICABLE;
		}
		else {
			throw new Exception("Parsing Error: the feature " + parts[2] + " has a bad orientation field [" + parts[6] + "]");
		}

		DasPhase phase = null;
		if (parts[7].equals("0")) {
			phase = DasPhase.PHASE_READING_FRAME_0;
		}
		else if (parts[7].equals("1")) {
			phase = DasPhase.PHASE_READING_FRAME_1;
		}
		else if (parts[7].equals("2")) {
			phase = DasPhase.PHASE_READING_FRAME_2;
		}
		else if (parts[7].equals(".")) {
			phase = DasPhase.PHASE_NOT_APPLICABLE;
		}
		else {
			throw new Exception("Parsing Error: the feature " + parts[2] + " has a bad orientation field [" + parts[7] + "]");
		}

		DasFeature feature = new DasFeature("GFF_feature_" + (featureid++), null,
				getType(parts[2]), new DasMethod(parts[1], parts[1], null),
				Integer.parseInt(parts[3]), Integer.parseInt(parts[4]), score, orientation, phase,
				null, null, null, null, null);
		boolean added = false;
		for (DasAnnotatedSegment segment : segments) {
			if (segment.getSegmentId().equals(parts[0])) {
				segment.getFeatures().add(feature);
				if (feature.getStartCoordinate() < segment.getStartCoordinate()) {
					DasAnnotatedSegment newSegment = new DasAnnotatedSegment(segment.getSegmentId(),
							feature.getStartCoordinate(), segment.getStopCoordinate(), segment.getVersion(),
							segment.getSegmentLabel(), segment.getFeatures());
					segments.add(newSegment);
					segments.remove(segment);
					segment = newSegment;
				}
				if (feature.getStopCoordinate() > segment.getStopCoordinate()) {
					DasAnnotatedSegment newSegment = new DasAnnotatedSegment(segment.getSegmentId(),
							segment.getStartCoordinate(), feature.getStopCoordinate(), segment.getVersion(),
							segment.getSegmentLabel(), segment.getFeatures());
					segments.add(newSegment);
					segments.remove(segment);
					segment = newSegment;
				}

				added = true;
				break;
			}
		}

		if (!added) {
			List<DasFeature> features = new ArrayList<>(1);
			features.add(feature);
			segments.add(new DasAnnotatedSegment(parts[0], Integer.valueOf(parts[3]), Integer.valueOf(parts[4]),
					"FromFile", parts[0], features));
		}
	}

	private DasType getType(String type) {
		for (DasType t : types) {
			if (t.getId().equals(type)) {
				return t;
			}
		}
		DasType newtype = new DasType(type, null, null, null);
		types.add(newtype);
		return newtype;
	}

	public Collection<DasAnnotatedSegment> parse() throws Exception {
		processLineByLine();
		return segments;
	}

	public static void main(String[] a) {
		try {
			GFF2Parser parser = new GFF2Parser((new FileInputStream(
					"/Users/4ndr01d3/Documents/EBI/MyDasTemplate/src/main/webapp/CHROMOSOME_MtDNA.gff")));
			parser.parse();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<DasType> getTypes() {
		return types;
	}

	public String getVersion() {
		return version;
	}

	public String getDate() {
		return date;
	}
}
