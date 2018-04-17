package transmart.mydas

import org.transmartproject.core.dataquery.assay.Assay
import org.transmartproject.core.dataquery.highdim.chromoregion.RegionRow
import org.transmartproject.core.dataquery.highdim.vcf.VcfValues
import uk.ac.ebi.mydas.model.DasFeature
import uk.ac.ebi.mydas.model.DasFeatureOrientation
import uk.ac.ebi.mydas.model.DasPhase
import uk.ac.ebi.mydas.model.DasType

import javax.annotation.PostConstruct

/**
 * @author j.hudecek
 */
class SummaryMAFService extends VcfServiceAbstract {

	static transactional = false

	@PostConstruct
	void init() {
		super.init()
		dasTypes = [projectionName]
	}

	protected void getSpecificFeatures(RegionRow region, Collection<Assay> assays, Map<String, String> params,
	                                   Collection<DasType> dasTypes, Map<String, List<DasFeature>> featuresPerSegment) {
		constructSegmentFeaturesMap([region], getSummaryMafFeature, featuresPerSegment)
	}

	private Closure<List<DasFeature>> getSummaryMafFeature = { VcfValues val ->
		Double maf = val?.cohortInfo?.minorAlleleFrequency
		if (!maf || maf <= 0) {
			return []
		}

		Map linkMap = val.rsId == '.' ? [:]
				: [(new URL('http://www.ncbi.nlm.nih.gov/projects/SNP/snp_ref.cgi?rs=' + val.rsId)): 'NCBI SNP Ref']

		[new DasFeature(
				'smaf-' + val.rsId, // feature id - any unique id that represent this feature
				'Minor Allele Frequency', // feature label
				new DasType('smaf', '', '', ''), // das type
				dasMethod,// das method TODO: pls find out what is actually means
				val.position.toInteger(), // start pos
				val.position.toInteger(), // end pos
				maf, // value - this is where Minor Allele Freq (MAF) value is placed
				DasFeatureOrientation.ORIENTATION_NOT_APPLICABLE,
				DasPhase.PHASE_NOT_APPLICABLE,
				getCommonNotes(val), // notes
				linkMap, // links
				[], // targets
				[], // parents
				[]) // parts
		]
	}
}
