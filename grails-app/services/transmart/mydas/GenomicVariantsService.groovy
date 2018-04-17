package transmart.mydas

import org.transmartproject.core.dataquery.assay.Assay
import org.transmartproject.core.dataquery.highdim.chromoregion.RegionRow
import org.transmartproject.core.dataquery.highdim.vcf.GenomicVariantType
import org.transmartproject.core.dataquery.highdim.vcf.VcfValues
import uk.ac.ebi.mydas.model.DasFeature
import uk.ac.ebi.mydas.model.DasFeatureOrientation
import uk.ac.ebi.mydas.model.DasPhase
import uk.ac.ebi.mydas.model.DasType

import javax.annotation.PostConstruct

/**
 * @author j.hudecek
 */
class GenomicVariantsService extends VcfServiceAbstract {

	static transactional = false

	@PostConstruct
	void init() {
		super.init()
		dasTypes = [projectionName];
	}

	protected void getSpecificFeatures(RegionRow region, Collection<Assay> assays, Map<String, String> params,
	                                   Collection<DasType> dasTypes, Map<String, List<DasFeature>> featuresPerSegment) {
		constructSegmentFeaturesMap([region], getGenomicTypeFeature, featuresPerSegment)
	}

	private Closure<List<DasFeature>> getGenomicTypeFeature = { VcfValues val ->

		Map linkMap = val.rsId == '.' ? [:]
				: [(new URL('http://www.ncbi.nlm.nih.gov/projects/SNP/snp_ref.cgi?rs=' + val.rsId)): 'NCBI SNP Ref']

		List<DasFeature> results = []
		val.cohortInfo.genomicVariantTypes.eachWithIndex { GenomicVariantType genomicVariantType, int index ->
			if (!genomicVariantType) {
				return
			}

			results << new DasFeature(
					'gv-' + val.rsId + '-' + genomicVariantType, // feature id - any unique id that represent this feature
					'Genomic Variant Type', // feature label
					new DasType(genomicVariantType.toString(), '', '', ''), // das type
					dasMethod, // das method TODO: pls find out what is actually means
					val.position.toInteger(), // start pos
					val.position.toInteger(), // end pos
					null, // value - this is where Minor Allele Freq (MAF) value is placed
					DasFeatureOrientation.ORIENTATION_NOT_APPLICABLE,
					DasPhase.PHASE_NOT_APPLICABLE,
					//notes
					getCommonNotes(val) + [
									'CurrentALT=' + val.cohortInfo.alleles[index],
									'Type=' + genomicVariantType],
					linkMap, // links
					[], // targets
					[], // parents
					[]) // parts
		}

		results
	}
}
