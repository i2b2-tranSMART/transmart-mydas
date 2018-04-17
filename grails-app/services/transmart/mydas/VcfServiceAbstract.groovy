package transmart.mydas

import groovy.transform.CompileStatic
import org.transmartproject.core.dataquery.highdim.vcf.VcfValues
import uk.ac.ebi.mydas.extendedmodel.DasMethodE
import uk.ac.ebi.mydas.model.DasFeature
import uk.ac.ebi.mydas.model.DasType

import javax.annotation.PostConstruct

/**
 * @author jhudecek
 */
@CompileStatic
abstract class VcfServiceAbstract extends TransmartDasServiceAbstract {

	protected static final String NA = 'n/a'

	@PostConstruct
	void init() {
		//TODO Choose correct cvId(3-d parameter) from http://www.ebi.ac.uk/ontology-lookup/browse.do?ontName=SO
		dasMethod = new DasMethodE('vcf', 'vcf', 'vcf-cv-id')
		version = '0.1'
		resource = highDimensionResourceService.getSubResourceForType 'vcf'

		//TODO Choose correct cvId(3-d parameter) from http://www.ebi.ac.uk/ontology-lookup/browse.do?ontName=SO
		projectionName = 'cohort'
	}

	protected Collection<DasType> getTypes() {}

	protected void constructSegmentFeaturesMap(List<VcfValues> deVariantSubjectDetails,
	                                           Closure<List<DasFeature>> featureCreationClosure,
	                                           Map<String, List<DasFeature>> featuresPerSegment) {
		for (VcfValues values in deVariantSubjectDetails) {
			if (!featuresPerSegment[values.chromosome]) {
				featuresPerSegment[values.chromosome] = []
			}

			featuresPerSegment[values.chromosome].addAll featureCreationClosure(values)
		}
	}

	protected List<String> getCommonNotes(VcfValues val) {
		['RefSNP=' + val.rsId,
		 'REF=' + val.cohortInfo.referenceAllele,
		 'ALT=' + val.cohortInfo.alternativeAlleles.join(','),
		 'MafAllele=' + val.cohortInfo.minorAllele,
		 'AlleleFrequency=' + String.format('%.2f', val.cohortInfo.minorAlleleFrequency),
		 'AlleleCount=' + (val.cohortInfo.alleleCount ?: NA),
		 'TotalAllele=' + (val.cohortInfo.totalAlleleCount ?: NA),
		 'GenomicVariantTypes=' + val.cohortInfo.genomicVariantTypes.findAll().join(','),

		 'VariantClassification=' + (val.infoFields['VC'] ?: NA),
		 'QualityOfDepth=' + (val.qualityOfDepth ?: NA),

		 'BaseQRankSum=' + (val.infoFields['BaseQRankSum'] ?: NA),
		 'MQRankSum=' + (val.infoFields['MQRankSum'] ?: NA),
		 'dbSNPMembership=' + (val.infoFields['DB'] ?: 'No')]
	}
}
