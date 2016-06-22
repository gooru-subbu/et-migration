package org.gooru.et2gooru
import grails.converters.*
import org.gooru.et2gooru.UserIdentity


class MigrationController {

	def etreadService
	def gooruwriteService
	
	def InstanceCounter = 0
	
    def index() {
    	boolean migrateUsers = true
    	boolean migrateAssessments = true
    	    	
    	if (InstanceCounter == 0) {
			def returnVal = etreadService.migrateETData(migrateUsers, migrateAssessments)
			
			InstanceCounter = InstanceCounter + 1
		}

    	render "user and assessment migration done"
    }       
    
    
    def postRun() {
    	gooruwriteService.postMigrationRun()
    	
    	render "done"
    } 
    
    
    def test() {
		def lstUsrs = UserIdentity.findAll()
		def retVal = ""
		lstUsrs?.each {
			retVal = retVal + it.userId + " - " + it.emailId + " - " + it.clientId + " \n <br/> "
		}
		render retVal 
    }
}
