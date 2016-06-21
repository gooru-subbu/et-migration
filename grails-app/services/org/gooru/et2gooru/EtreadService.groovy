package org.gooru.et2gooru

import grails.transaction.Transactional
import org.gooru.et2gooru.User
import org.gooru.et2gooru.MasterBankItem
import org.gooru.et2gooru.MasterBankItemTypes
import org.gooru.et2gooru.MasterBankItemQuestionsLink
import org.gooru.et2gooru.MasterBankQuestion
import org.gooru.et2gooru.MasterBankQuestionAnswerkey

@Transactional
class EtreadService {

	static dataSource = 'lookup'
	
	def gooruwriteService

    def migrateETData(boolean migrateUsers, boolean migrateAssessments) {
    	////////
        log.info "Start: " + (new Date()).getTime()
    	gooruwriteService.beginMigration()
        
		def usrList = User.findAllByUserTypeAndAccountActiveAndEmailNotEqual('teacher', true, '')
		
		// USER MIGRATION  ...... 
		log.info "Number of Active Users Found in ET = " + usrList?.size()		
		usrList?.eachWithIndex { it, idx ->
		
			def gId = gooruwriteService.createUser(it)
			if (gId) {
				
				// ASSESSMENT MIGRATION  ....
				
				getMasterBankItemForUserAndCreateInGooru(it, gId)	
									
				// ASSESSMENT MIGRATION DONE ....
				
				log.info "Completed migrating ET User : ${it.id}"
			} else {
				log.error "Unknown error while trying to migrate User - ET ID: ${it.id}"
			}
		}
		// USER MIGRATION DONE .......

		log.info "Completed migrating Users"

		////////		
        log.info "End: " + (new Date()).getTime()
    	gooruwriteService.endMigration()        
    }
    
    
    def getMasterBankItemForUserAndCreateInGooru(User user, def gooruUserId) {
    	
    	def itemList = MasterBankItem.findAllByTeacherUidAndTicketTypeInList(user.id, [1,2,4,6])
    	
    	def itemTypesList = MasterBankItemTypes.findAllByIdInList([1,2,4,6])
		def namesMap = [:]
		itemTypesList?.each {
			namesMap[it.id] = it.itemDescription
		}		

		log.info "Number of Assessments Found for User : ${user.id} ET = ${ itemList?.size() } "
    	itemList?.eachWithIndex { it, idx ->
    			def assmtId = gooruwriteService.createAssessment(it, namesMap[it.ticketType], gooruUserId)
    			
    			// QUESTION MIGRATION ....
    			    			
    			getQuestionForMasterBankItemAndCreateInGooru(it, user, gooruUserId, assmtId)
    			
    			// QUESTION MIGRATION DONE....
    	}
		log.info "Completed migrating Assessments for User : ${user.id} "
    }
    
    def getQuestionForMasterBankItemAndCreateInGooru(MasterBankItem mbi, User user, def gooruUserId, def assmtId) {
	    final String MC_TYPE = "MC" 
	    final String FA_TYPE = "FA"

    	def qList = MasterBankItemQuestionsLink.findAllByItemId(mbi.id)
		log.info "Number of Questions found for MBI item id : ${mbi.id} for user : ${user.id} in ET = ${ qList?.size() }"
		
		def answerList
		qList.each { it ->

			def mQ = MasterBankQuestion.findById(it.questionId)
			if ( mQ && MC_TYPE.equalsIgnoreCase(mQ.answerType) ) {
		    	answerList = MasterBankQuestionAnswerkey.findAllByQuestionId(mQ.id)
			} 
			else 
				answerList = null
			
			// TBD :: 
			// Do the checks to see if we should really migrate this question....we do not want junk in target
			//    check for: 1. null title AND null studentDirection
			//               2. if MC type: null answer.title
			boolean bSkip = false
			if ( !mQ ) bSkip = true
			else if ( !mQ.title  && !mQ.studentDirection ) bSkip = true
			else if ( MC_TYPE.equalsIgnoreCase(mQ.answerType) && !answerList ) bSkip = true
			else if ( FA_TYPE.equalsIgnoreCase(mQ.answerType) && !mQ.answer ) bSkip = true
				
			if (!bSkip) {
				def gQId = gooruwriteService.createQuestion(mbi.id, mQ, it.pos, assmtId, gooruUserId, answerList)
				
			} else {
				log.info "Skipped the Question as there is no question text or answerOptions to migrate - ID: ${it.questionId}"
			}
		}
		
		log.info "Completed migrating Questions for Assessment : ${mbi.id} "		
    }
    
}
