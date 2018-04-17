package transmart.mydas

import org.transmartproject.core.dataquery.TabularResult
import org.transmartproject.core.dataquery.assay.Assay
import org.transmartproject.core.dataquery.highdim.AssayColumn
import org.transmartproject.core.dataquery.highdim.HighDimensionDataTypeResource
import org.transmartproject.core.dataquery.highdim.HighDimensionResource
import org.transmartproject.core.dataquery.highdim.assayconstraints.AssayConstraint
import org.transmartproject.core.dataquery.highdim.chromoregion.RegionRow
import org.transmartproject.core.dataquery.highdim.dataconstraints.DataConstraint
import org.transmartproject.core.dataquery.highdim.projections.Projection
import org.transmartproject.core.ontology.ConceptsResource
import org.transmartproject.core.querytool.QueriesResource
import uk.ac.ebi.mydas.exceptions.DataSourceException
import uk.ac.ebi.mydas.exceptions.UnimplementedFeatureException
import uk.ac.ebi.mydas.extendedmodel.DasMethodE
import uk.ac.ebi.mydas.model.DasAnnotatedSegment
import uk.ac.ebi.mydas.model.DasEntryPoint
import uk.ac.ebi.mydas.model.DasFeature
import uk.ac.ebi.mydas.model.DasType
import uk.ac.ebi.mydas.model.Range

abstract class TransmartDasServiceAbstract {

	protected abstract void getSpecificFeatures(RegionRow region, Collection<Assay> assays, Map<String, String> params,
	                                            Collection<DasType> dasTypes, Map<String,
	                                            List<DasFeature>> featuresPerSegment)

	HighDimensionResource highDimensionResourceService
	HighDimensionDataTypeResource resource

	QueriesResource queriesResourceService
	ConceptsResource conceptsResourceService

	String version = '1.0'
	DasMethodE dasMethod
	String projectionName

	protected Collection<DasType> dasTypes

	List<DasEntryPoint> getEntryPoints(Long resultInstanceId) {
		List query = getRegionQuery(resultInstanceId)
		TabularResult<AssayColumn, RegionRow> regions = resource.retrieveData(*query)
		regions.rows.collect { RegionRow it ->
			new DasEntryPoint(
					it.chromosome, // segmentId
					it.start.intValue(), // startCoordinate
					it.end.intValue(), // stopCoordinate
					'', // type
					'', // version
					null, // orientation
					'', // description
					false) // hasSubparts
		}
	}

	List<DasAnnotatedSegment> getFeatures(Long resultInstanceId, String conceptKey, Collection<String> segmentIds = [],
	                                      Integer maxbins = null, Range range = null,
	                                      Map<String, String> params = null, Collection<DasType> dasTypes = dasTypes)
			throws UnimplementedFeatureException, DataSourceException {

		List query = getRegionQuery(resultInstanceId, conceptKey, segmentIds, range)
		TabularResult<AssayColumn, RegionRow> regionResult = resource.retrieveData(*query)
		List<AssayColumn> assays = regionResult.indicesList
		Map<String, List<DasFeature>> featuresPerSegment = [:]
		try {
			for (RegionRow region in regionResult.rows) {
				if (!featuresPerSegment[region.chromosome]) {
					featuresPerSegment[region.chromosome] = []
				}
				getSpecificFeatures region, assays, params, dasTypes, featuresPerSegment
			}
		}
		finally {
			regionResult.close()
		}

		List<DasAnnotatedSegment> segments = segmentIds.collect {
			new DasAnnotatedSegment(it, range?.getFrom() ?: null, range?.getTo() ?: null, version, it, featuresPerSegment[it] ?: [])
		}

		reduceBins segments, maxbins
	}

	protected List getRegionQuery(Long resultInstanceId, String conceptKey,
	                              Collection<String> segmentIds = [], Range range = null) {

		List assayConstraints = [
				resource.createAssayConstraint(AssayConstraint.PATIENT_SET_CONSTRAINT,
						result_instance_id: resultInstanceId)]

		if (conceptKey) {
			assayConstraints << resource.createAssayConstraint(
					AssayConstraint.ONTOLOGY_TERM_CONSTRAINT,
					concept_key: conceptKey)
		}

		List dataConstraints = [
				resource.createDataConstraint(
						DataConstraint.DISJUNCTION_CONSTRAINT,
						subconstraints: [
								(DataConstraint.CHROMOSOME_SEGMENT_CONSTRAINT): segmentIds.collect {
									Map ret = [chromosome: it]
									if (range) {
										ret.start = range.from
										ret.end = range.to
									}
									ret
								}
						]
				)
		]

		Projection projection = resource.createProjection([:], projectionName)

		[assayConstraints, dataConstraints, projection]
	}

	private List<DasAnnotatedSegment> reduceBins(List<DasAnnotatedSegment> segments, Integer maxbins) {
		if (maxbins) {
			//TODO Reduce to maxbins
			segments
		}
		else {
			segments
		}
	}
}
