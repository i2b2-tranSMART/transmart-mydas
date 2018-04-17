package transmart.mydas

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.transmartproject.core.dataquery.assay.Assay
import org.transmartproject.core.dataquery.highdim.acgh.AcghValues
import org.transmartproject.core.dataquery.highdim.acgh.CopyNumberState
import org.transmartproject.core.dataquery.highdim.chromoregion.RegionRow
import uk.ac.ebi.mydas.extendedmodel.DasMethodE
import uk.ac.ebi.mydas.extendedmodel.DasTypeE
import uk.ac.ebi.mydas.model.DasFeature
import uk.ac.ebi.mydas.model.DasFeatureOrientation
import uk.ac.ebi.mydas.model.DasPhase
import uk.ac.ebi.mydas.model.DasType

import javax.annotation.PostConstruct

@CompileStatic
class AcghService extends TransmartDasServiceAbstract {

	static transactional = false

	String acghEntryPointVersion = '1.0'

	private Map<DasType, CopyNumberState> dasTypeToCopyNumberStateMapping = [
			(new DasTypeE('acgh-loss-frequency', null, null, 'acgh-loss-frequency'))    : CopyNumberState.LOSS,
			(new DasTypeE('acgh-normal-frequency', null, null, 'acgh-normal-frequency')): CopyNumberState.NORMAL,
			(new DasTypeE('acgh-gain-frequency', null, null, 'acgh-gain-frequency'))    : CopyNumberState.GAIN,
			(new DasTypeE('acgh-amp-frequency', null, null, 'acgh-amp-frequency'))      : CopyNumberState.AMPLIFICATION,
			(new DasTypeE('acgh-inv-frequency', null, null, 'acgh-inv-frequency'))      : CopyNumberState.INVALID
	].asImmutable() as Map

	private Map<CopyNumberState, DasType> copyNumberStateToDasTypeMapping =
			dasTypeToCopyNumberStateMapping.collectEntries { Map.Entry<DasType, CopyNumberState> it ->
				[(it.value): it.key]
			}.asImmutable() as Map

	@PostConstruct
	void init() {
		resource = highDimensionResourceService.getSubResourceForType('acgh')
		//TODO Choose correct cvId(3-d parameter) from http://www.ebi.ac.uk/ontology-lookup/browse.do?ontName=SO
		dasMethod = new DasMethodE('acgh', 'acgh', 'acgh-cv-id')
		projectionName = 'acgh_values'
		dasTypes = dasTypeToCopyNumberStateMapping.keySet()
	}

	protected void getSpecificFeatures(RegionRow region, Collection<Assay> assays, Map<String, String> params,
	                                   Collection<DasType> dasTypes, Map<String, List<DasFeature>> featuresPerSegment) {

		Map<DasType, Integer> countPerDasType = countAcghDasTypesForRow(region, assays, dasTypes)

		for (Map.Entry<DasType, Integer> typeCountEntry in countPerDasType.entrySet()) {

			double freq = typeCountEntry.value / (double) assays.size()

			featuresPerSegment[region.chromosome] << new DasFeature(
					typeCountEntry.key.id + '-' + region.id, // featureId
					typeCountEntry.key.id + '-' + region.id, // featureLabel
					typeCountEntry.key, // type
					dasMethod, // method
					region.start.intValue(), // startCoordinate
					region.end.intValue(), // endCoordinate
					freq, // score
					DasFeatureOrientation.ORIENTATION_ANTISENSE_STRAND, // orientation
					DasPhase.PHASE_NOT_APPLICABLE, // phase
					[], // notes
					[:], // links
					[], // targets
					[], // parents
					[]) // parts
		}
	}

	private List<CopyNumberState> convertToCopyNumberState(Collection<DasType> dasTypes) {
		dasTypes.collect { DasType it -> dasTypeToCopyNumberStateMapping[it] }
	}

	@CompileDynamic
	private Map<CopyNumberState, Integer> countAcghCopyNumberStatesForRow(
			RegionRow<AcghValues> row, Collection<Assay> assays, Collection<CopyNumberState> states) {

		//TODO Use countBy
		states.collectEntries { CopyNumberState state ->
			[(state): assays.count { Assay assay ->
				state == row.getAt(assay).copyNumberState
			}]
		}
	}

	private Map<DasType, Integer> countAcghDasTypesForRow(RegionRow row, Collection<Assay> assays, Collection<DasType> dasTypes) {

		Map<CopyNumberState, Integer> acghCopyNumberStatesForRowMap =
				countAcghCopyNumberStatesForRow(row, assays, convertToCopyNumberState(dasTypes))

		acghCopyNumberStatesForRowMap.collectEntries { Map.Entry<CopyNumberState, Integer> it ->
			[(copyNumberStateToDasTypeMapping[it.key]): it.value]
		} as Map
	}
}
