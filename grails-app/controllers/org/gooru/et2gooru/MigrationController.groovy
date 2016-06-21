package org.gooru.et2gooru
import grails.converters.*

class MigrationController {

	def etreadService
	def gooruwriteService
	
	def InstanceCounter = 0
	
    def index() {
    	boolean migrateUsers = true
    	boolean migrateAssessments = true
    	
//    	println "Instance Counter = ${InstanceCounter}"
    	
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
}
