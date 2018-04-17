package uk.ac.ebi.mydas.examples;

import uk.ac.ebi.mydas.exceptions.DataSourceException;
import uk.ac.ebi.mydas.model.DasSequence;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class FastaParser {
	private Scanner scanner;
	private Map<String, DasSequence> sequences = new HashMap<>();
	private String currentSequence;
	private String currentHeader;
	private String fileName;

	public FastaParser(InputStream fastaDoc, String fileName) {
		this.fileName = fileName;
		scanner = new Scanner(fastaDoc);
	}

	private void processLineByLine() throws DataSourceException {
		try {
			//first use a Scanner to get each line
			while (scanner.hasNextLine()) {
				processLine(scanner.nextLine());
			}
			String id = currentHeader.split(" ")[0];
			sequences.put(id, new DasSequence(id, currentSequence, 1, fileName, currentHeader));
		}
		finally {
			//ensure the underlying stream is always closed
			scanner.close();
		}
	}

	private void processLine(String aLine) throws DataSourceException {
		if (aLine.startsWith(">")) {
			if (currentSequence != null) {
				String id = currentHeader.split(" ")[0];
				sequences.put(id, new DasSequence(id, currentSequence, 1, fileName, currentHeader));
			}
			currentHeader = aLine.substring(1).trim();
			currentSequence = "";
		}
		else {
			currentSequence += aLine.trim();
		}
	}

	public Map<String, DasSequence> parse() throws DataSourceException {
		processLineByLine();
		return sequences;
	}
}
