class TransmartMydasGrailsPlugin {
	def version = '16.2'
	def grailsVersion = '2.3 > *'
	def title = 'Transmart Mydas Plugin'
	def author = 'Ruslan Forostianov'
	def authorEmail = 'ruslan@thehyve.nl'
	def description = 'TODO'
	def documentation = 'TODO'
	def license = 'GPL3'
	def organization = [name: 'TODO', url: 'TODO']
	def developers = [[name: 'Burt Beckwith', email: 'burt_beckwith@hms.harvard.edu']]
	def issueManagement = [system: 'TODO', url: 'TODO']
	def scm = [url: 'https://github.com/tranSMART-Foundation/transmart-mydas']

	def doWithWebDescriptor = {xml ->
		def servletElement = xml.'servlet'
		def lastServlet = servletElement[servletElement.size() - 1]
		lastServlet + {
			servlet {
				'servlet-name'('MydasServlet')
				'servlet-class'('uk.ac.ebi.mydas.controller.MydasServlet')
			}
		}

		def mappingElement = xml.'servlet-mapping'
		def lastMapping = mappingElement[mappingElement.size() - 1]
		lastMapping + {
			'servlet-mapping' {
				'servlet-name'('MydasServlet')
				'url-pattern'('/das/*')
			}
		}
	}
}
