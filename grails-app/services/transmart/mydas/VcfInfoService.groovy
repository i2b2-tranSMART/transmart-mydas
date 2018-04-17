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
class VcfInfoService extends VcfServiceAbstract {

	static transactional = false

	@PostConstruct
	void init() {
		super.init()
		dasTypes = [projectionName]
	}

	protected void getSpecificFeatures(RegionRow region, Collection<Assay> assays, Map<String, String> params,
	                                   Collection<DasType> dasTypes, Map<String, List<DasFeature>> featuresPerSegment) {
		if (!featuresPerSegment[region.chromosome]) {
			featuresPerSegment[region.chromosome] = []
		}

		featuresPerSegment[region.chromosome].addAll(getInfoAndFeature(region, params.infoField))
	}

	private Closure getInfoAndFeature = { VcfValues val, String infoField ->
		String infoFieldValue = val.infoFields[infoField]
		if (null == infoFieldValue) {
			return []
		}

		Map linkMap = val.rsId == '.' ? [:]
				: [(new URL('http://www.ncbi.nlm.nih.gov/projects/SNP/snp_ref.cgi?rs=' + val.rsId)): 'NCBI SNP Ref']

		[new DasFeature(
				'vcfInfo-' + val.position, // feature id - any unique id that represent this feature
				'VCF Info Field', // feature label
				new DasType('vcfInfo', '', '', ''), // das type
				dasMethod, // das method TODO: pls find out what is actually means
				val.position.toInteger(), // start pos
				val.position.toInteger(), // end pos
				// value - this is where we place the value from the info field
				// This purposely fails if the value cannot be converted to double,
				// in which case an error 500 will be returned.
				//TODO: in dalliance-plugin, check if an infofield is supported
				// before adding it based on meta info from vcf
				infoFieldValue as double,
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
