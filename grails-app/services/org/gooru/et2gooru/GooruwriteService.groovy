package org.gooru.et2gooru

import grails.util.Environment
import grails.transaction.Transactional
import java.util.UUID
import groovy.json.*
import org.gooru.et2gooru.UserDemographic
import org.gooru.et2gooru.UserIdentity
import org.gooru.et2gooru.User
import org.gooru.et2gooru.MasterBankItem
import org.gooru.et2gooru.Utils
import groovy.sql.Sql

@Transactional
class GooruwriteService {

	static datasource = 'datasource'
	static String CONST_CLIENT_ID = "ba956a97-ae15-11e5-a302-f8a963065976"	  // TBD: may want to change this to something other than our Gooru ID
    final Integer MAX_QUESTION_LIMIT = 20000
	
	//
	// DB Connection Init 
	//
    def beginMigration() {
    	Utils.initWriteConnection()
    }
    
	//
	// DB Connection Close 
	//
    def endMigration() {
    	Utils.closeWriteConnection()
    }
    
	//
	// Create User in Gooru 
	//
    def createUser(User user) {
    	// create user demographic ... this is an upsert...can take time
    	def udId = createUserDemographic(user)
    	if (udId) {
	    	// now create user identity ... this is an upsert...can take time
    		createUserIdentity(user, udId)
    	}
    	
    	return udId  
    }
        
	//
	// Create User Demographic in Gooru 
	//
    private def createUserDemographic(User user) {
    	
		try {
	    	def userDemographic = new UserDemographic()
			if ( !userDemographic.isMigrated(user.id) ) {
				//
				// Need to put an entry in ET_USER_DEMOGRAPHIC table...
				//
				boolean bInsertItem
				def retVal = UserDemographic.findByEmailId(user.email)
				if (retVal) {
					bInsertItem = false
					userDemographic.id = retVal.id
				   	userDemographic.firstname = retVal.firstname
			    	userDemographic.lastname = retVal.lastname
			    	userDemographic.emailId = retVal.emailId  
			
					// for some accounts we see that createdAT is null but not updatedAT. 
					// So, we set updatedAT and if createdAT is null, then set it to updatedAT value...
			    	userDemographic.updatedAt = (retVal.updatedAt) ? retVal.updatedAt : Utils.getDateWithoutTimezone()
			    	userDemographic.createdAt = (retVal.createdAt) ? retVal.createdAt : userDemographic.updatedAt 					
					
				} else {
					bInsertItem = true
				   	userDemographic.firstname = user.firstName
			    	userDemographic.lastname = user.lastName
			    	userDemographic.emailId = user.email  
			
					// for some accounts we see that createdAT is null but not updatedAT. 
					// So, we set updatedAT and if createdAT is null, then set it to updatedAT value...
			    	userDemographic.updatedAt = (user.lastUpdated) ? user.lastUpdated : Utils.getDateWithoutTimezone()
			    	userDemographic.createdAt = (user.accountCreated) ? user.accountCreated : userDemographic.updatedAt 					
				}
	    		userDemographic.saveIt(user.id, bInsertItem)
	    		log.info  "User Demographic created successfully for ET user ID: " + user.id + " Gooru ID: " + userDemographic.id
			} else {
	    		log.info  "User Demographic already existing for ET user ID: " + user.id + " Gooru ID: " + userDemographic.id
			}
    		
    		return userDemographic.id    		
		} catch ( Exception e ) {
			log.error "Failed to create user_demographic in Gooru DB for ET user ID: " + user.id + " \n  ${e.message}"
		}
		return null
    }
    
	//
	// Create User Identity in Gooru 
	//
    private boolean createUserIdentity(User user, def demograhpic_id) {
		try {	
	    	def userId = new UserIdentity()
			if ( !userId.isMigrated(user.id) ) {
				//
				// Need to put an entry in ET_USER_IDENTITY table...
				//
				boolean bInsertItem
				def lstUsrs = UserIdentity.findAllByEmailId(user.email)
		    	if (lstUsrs && (lstUsrs.size() > 0)) {
		    		bInsertItem = false
					userId.loginType = lstUsrs[0].loginType
					userId.provisionType = lstUsrs[0].provisionType
					userId.emailConfirmStatus = lstUsrs[0].emailConfirmStatus
					userId.status = lstUsrs[0].status
					userId.clientId = lstUsrs[0].clientId
					userId.referenceId = lstUsrs[0].referenceId
					userId.emailId = lstUsrs[0].emailId
					userId.userId = lstUsrs[0].userId
					
					userId.updatedAt = (lstUsrs[0].updatedAt) ? lstUsrs[0].updatedAt : Utils.getDateWithoutTimezone()
					userId.createdAt = (lstUsrs[0].createdAt) ? lstUsrs[0].createdAt : userId.updatedAt
				} else {
					bInsertItem = true
					userId.loginType = "credential"
					userId.provisionType = "registered"
					userId.emailConfirmStatus = false
					userId.status = "active"
					userId.clientId = CONST_CLIENT_ID
					userId.referenceId = user.id
					userId.emailId = user.email
					userId.userId = demograhpic_id
					
					// for some accounts we see that createdAT is null but not updatedAT. 
					// So, we set updatedAT and if createdAT is null, then set it to updatedAT value...
					userId.updatedAt = (user.lastUpdated) ? user.lastUpdated : Utils.getDateWithoutTimezone()
					userId.createdAt = (user.accountCreated) ? user.accountCreated : userId.updatedAt
				}
    		
	    		userId.saveIt(user.id, bInsertItem)
	    		log.info "User Identity created successfully for ET user ID: " + user.id
	    	} else {
	    		log.info  "User Identity already existing for ET user ID: " + user.id
			}
			
    		return true
		} catch ( Exception e ) {
			log.error "Failed to create user_identity in Gooru DB for ET user ID: " + user.id + " \n  ${e.message}"
		}
		return false
    }
           
 	//
	// Create Assessment in Gooru 
	//
    def createAssessment(MasterBankItem item, def ticketDesc, def gooruUserId) {
		try {
	    	def gColl = new Collection()
	    	if (!gColl.isMigrated(item.id)) {
		    	gColl.title = ticketDesc + " - " + item.title
		    	gColl.ownerId = gooruUserId
		    	gColl.creatorId = gooruUserId
		    	gColl.modifierId = gooruUserId
		    	gColl.format = "assessment"
		
		    	gColl.metadata  = getDOK( item.ticketDifficultyRating )
		    	gColl.setting = getAssessmentSetting(item.ticketType, item.isScored)
		    	gColl.grading = "system"
				gColl.license = 128 // "public domain"  // TBD: Can we assume this value to be fixed?
				gColl.creatorSystem = "Exit Ticket Migration"
		    	
				// for some records we see that createdAT is null but not updatedAT. 
				// So, we set updatedAT and if createdAT is null, then set it to updatedAT value...
				gColl.updatedAt = (item.updatedDate) ? item.updatedDate : Utils.getDateWithoutTimezone()
				gColl.createdAt = (item.addedDate) ? item.addedDate : gColl.updatedAt
	
				gColl.saveIt(item.id)
				log.info  "Created assessment in Gooru DB successfully for ET Assessment ID: " + item.id + " : Gooru ID: " + gColl.id
			} else {
	    		log.info  "Assessment already existing for ET item ID: " + item.id + " Gooru ID: " + gColl.id
			}
			
			return gColl.id
		} catch ( Exception e ) {
			log.error "Failed to create assessment in Gooru DB for ET Assessment ID: " + item.id + " \n  ${e.message}"
		}
    	
    	return null
    }
    
 	//
	// Create Question in Gooru 
	//
    def createQuestion(def masterBankItemId, MasterBankQuestion mbq, def sequence_id, def assessment_id, def gooruUserId, def etAnswerList) {
		try {
	    	def gQuestion = new Content()
	    	if (!gQuestion.isMigrated(mbq.id, masterBankItemId)) {
	    
				gQuestion.title  = mbq.title
			   	gQuestion.updatedAt = (mbq.updatedDate) ? mbq.updatedDate : Utils.getDateWithoutTimezone()
				gQuestion.createdAt = (mbq.addedDate) ? mbq.addedDate : gQuestion.updatedAt
				
			   	gQuestion.creatorId = gooruUserId
			   	gQuestion.modifierId = gooruUserId
			   	
			   	gQuestion.contentFormat = "question"
			   	gQuestion.contentSubformat = getContentSubformat(mbq.answerType)
			   	
			   	// Make sure to update the mimetex pointers....and any image CDN urls...
			   	def desc = "<b>" + mbq.title + "</b><br>" + getUpdatedQuestionText(mbq.studentDirection)
		    	if (mbq.answerType.equalsIgnoreCase("FA")) desc = desc + "<p>Answer: _______ </p> "
			   	
		    	if (desc.length() >= MAX_QUESTION_LIMIT) gQuestion.description = desc[0..MAX_QUESTION_LIMIT-1]
		    	else gQuestion.description = desc
			   	
			   	gQuestion.answer = getAnswerObject(mbq.answerType, mbq, etAnswerList)
		
			   	gQuestion.metadata = getDOK(mbq.questionDifficulty)
			   	gQuestion.collectionId = assessment_id
			   	gQuestion.sequenceId = sequence_id + 1 	// ET is 0-based index; while Gooru is 1-based
			   	gQuestion.info = '{ "exit_ticket_id" : ' + mbq.id + '}'
			   	gQuestion.license = 128 // "public domain"  // TBD: Can we assume this value to be fixed?
			   	gQuestion.creatorSystem = "Exit Ticket Migration"
			   	
				gQuestion.saveIt(mbq.id, masterBankItemId)
				log.info  "Created question in Gooru DB successfully for ET Question ID: ${mbq.id} Of Item Id: ${masterBankItemId} : Gooru ID: " + gQuestion.id
			} else {
				log.info  "Question already exists in Gooru DB for ET Question ID: ${mbq.id} Of Item Id: ${masterBankItemId} : Gooru ID: " + gQuestion.id
			}
				
			return gQuestion.id
		} catch ( Exception e ) {
			log.error  "Failed to create question in Gooru DB for ET Question ID: ${mbq.id} Of Item Id: ${masterBankItemId} \n ${e.message}"
		}		
    	
    	return null    	
    }
    
    //
    // Helper for Content SubFormat
    private def getContentSubformat(def answerType) {
    	if (answerType.equalsIgnoreCase("MC")) return "multiple_choice_question"
    	else if (answerType.equalsIgnoreCase("TF")) return "true_false_question"
    	else if (answerType.equalsIgnoreCase("FA")) return "fill_in_the_blank_question"
    	else return null
    }
    
    //
    // Helper for constructing Answer Object
    private def getAnswerObject(def answerType, def mbQuestion, def etAnswer) {    
    	def retVal = []
    	if (answerType.equalsIgnoreCase("MC")) {
    		etAnswer?.each { ansOption ->    		
    			def builder = new groovy.json.JsonBuilder()
		       	def root = builder.answer {
		       					answer_text 			getUpdatedQuestionText(ansOption?.title)
		       					is_correct  			ansOption.isCorrect
		       					sequence     			ansOption.position
		       					answer_type  			"html"
		       					}
	    		retVal.add( JsonOutput.toJson(root["answer"]) )
			} 
    	} else if (answerType.equalsIgnoreCase("TF")) {
    		retVal.add( '{ "answer_text" : "True", "is_correct" : ' + (( mbQuestion.answerTrueFalse.equalsIgnoreCase("0") ) ? 0 : 1) + ', "sequence" : 1, "answer_type" : "html" }' )
    		retVal.add( '{ "answer_text" : "False", "is_correct" : ' + (( mbQuestion.answerTrueFalse.equalsIgnoreCase("0") ) ? 1 : 0) + ', "sequence" : 2, "answer_type" : "html" }' )
    	} else if (answerType.equalsIgnoreCase("FA")) {
    		//
    		// alternate options can exist for FA separated by ";"
    		//
    		def answerOption = getUpdatedQuestionText(mbQuestion.answer)
	    	def etOptionSeparator = ";"
	    	def listOfOptions = answerOption.tokenize(etOptionSeparator)
	    	def alternateOptions

	    	if (listOfOptions && listOfOptions.size() > 1)
	    		alternateOptions = listOfOptions[1..listOfOptions.size()-1]
			
			if (alternateOptions) {
				def builder = new groovy.json.JsonBuilder()
		       	def root = builder.answer {
		       					answer_text 			listOfOptions[0]
		       					is_correct  			1
		       					sequence     			1
		       					answer_type  			"html"
		       					alternate_answer_text 	alternateOptions
		       					}
	    		retVal.add( JsonOutput.toJson(root["answer"]) )
    		} else {
				def builder = new groovy.json.JsonBuilder()
		       	def root = builder.answer {
		       					answer_text 			listOfOptions[0]
		       					is_correct  			1
		       					sequence     			1
		       					answer_type  			"html"
		       					}
	    		retVal.add( JsonOutput.toJson(root["answer"]) )
    		}
    	} else return '[]'
		return retVal.toString()
    }

    //
    // Helper for updating the CDN and mimetex.cgi pointers in Questions
    private def getUpdatedQuestionText(def inString) {
    	if (!inString) return inString
    	
    	def etMimeTex 	 = "//js1.exittix.com/mimetex/mimetex.cgi" 
    	def gooruMimeTex = "//mtex.gooru.org/cgi-bin/mimetex.cgi"
    	
    	def etS3Bucket  = "https://s3.amazonaws.com/upload_question_images/"
    	def gooruCDNUrl = "//cdn.gooru.org/"
    	
    	def inputString = inString
    	if (inputString.contains(etMimeTex)) 
			inputString = inputString.replaceAll(etMimeTex, gooruMimeTex)
						
    	if (inputString.contains(etS3Bucket)) 
			inputString = inputString.replaceAll(etS3Bucket, gooruCDNUrl)

    	return inputString
    }
    
    //
    // Helper for getting DOK values 
    private def getDOK(def ticketDifficulty) {
    	// TBD -- can we assume these values to be fixed or do we need a look up first...
    	
    	if (ticketDifficulty == 1.0) return '{ "depth_of_knowledge" : 10 }'
    	if (ticketDifficulty == 2.0) return '{ "depth_of_knowledge" : 11 }'
    	if (ticketDifficulty == 3.0) return '{ "depth_of_knowledge" : 12 }'
    }
    
    //
    // Helper for getting Assessment setting 
    private def getAssessmentSetting(def ticketType, def isScored) {
		switch (ticketType) {
			case 1: // can be scored or not scored based on master_bank_item.is_scored flag
				if (isScored)
					return '{ "bidirectional_play" : false, "attempts_allowed" : 1, "randomize_play" : false, "show_hints" : false, "show_explanation" : false,  "show_feedback" : "summary", "show_key" : "never", "contributes_to_mastery" : false, "contributes_to_performance" : true }'
				else
					return '{ "bidirectional_play" : true, "attempts_allowed" : -1, "randomize_play" : false, "show_hints" : true, "show_explanation" : true,  "show_feedback" : "summary", "show_key" : "never", "contributes_to_mastery" : false, "contributes_to_performance" : true }'
		
			case 2: // always scored
				return '{ "bidirectional_play" : false, "attempts_allowed" : 1, "randomize_play" : false, "show_hints" : false, "show_explanation" : false, "show_feedback" : "summary", "show_key" : "never", "contributes_to_mastery" : true, "contributes_to_performance" : true }'
						
			case 4: // always scored
				return '{ "bidirectional_play" : false, "attempts_allowed" : 1, "randomize_play" : false, "show_hints" : false, "show_explanation" : false, "show_feedback" : "immediate", "show_key" : "summary", "contributes_to_mastery" : true, "contributes_to_performance" : true }'
						
			case 6: // never scored
				return '{ "bidirectional_play" : true, "attempts_allowed" : -1, "randomize_play" : false, "show_hints" : true, "show_explanation" : true, "show_feedback" : "summary", "show_key" : "summary", "contributes_to_mastery" : false, "contributes_to_performance" : true }'
		}
    }
    
    
    //
    // Post migration script...
    //
    def postMigrationRun() {
		log.info "Start PostMigrationRun: " + (new Date()).getTime()

		def sql = Utils.getWriteDBConnection()
		
		def sqlWrite = (Environment.isDevelopmentMode()) ?
							Sql.newInstance( 'jdbc:postgresql://localhost:5432/nucleus', 'nucleus', 'nucleus', 'org.postgresql.Driver' ) :
							Sql.newInstance( 'jdbc:postgresql://gooru-30-prod-postgresdb.c1ybherlivcq.us-west-1.rds.amazonaws.com/', 'nucleus', 'nucleus', 'org.postgresql.Driver' )
				
		def moreToGet = true
		def offset = 1
		def maxRows = 1000
		def totalCount
		def resultSet
		
		// user_demographic table move...
		resultSet = sql.firstRow("select count(*) from et_user_demographic where et_insert_flag = true")
		totalCount = resultSet.getAt(0)
		if (totalCount < offset) moreToGet = false  
		
		while (moreToGet) {
			try {
			sqlWrite.withTransaction {
				def result = sqlWrite.withBatch('insert into user_demographic(firstname, lastname, email_id, created_at, updated_at, user_category, id) values (?,?,?,?,?,?,?)' ) { stmt ->
					sql.eachRow("select * from  et_user_demographic where et_insert_flag = true", offset, maxRows) { row -> 
						stmt.addBatch([row.firstname, row.lastname, row.email_id, row.created_at, row.updated_at, Sql.OTHER(row.user_category), Sql.OTHER(row.id)])
					}
					
				}
			}
			} catch (Exception e) {
				log.error  "Failed to move from Et_user_demographic to user_demographic !! \n ${e.message}"
				// continue to do as much as you can....
			}
			offset = offset + maxRows
			if (totalCount < offset) moreToGet = false  
		}
		moreToGet = true
		offset = 1
		
		// user_identity table move...
		resultSet = sql.firstRow("select count(*) from et_user_identity where et_insert_flag = true")
		totalCount = resultSet.getAt(0)
		if (totalCount < offset) moreToGet = false  
		
		while (moreToGet) {
			try {
			sqlWrite.withTransaction {
				def result = sqlWrite.withBatch('insert into user_identity(login_type, provision_type, email_id, reference_id, user_id, email_confirm_status, status, created_at, updated_at, client_id) values (?,?,?,?,?,?,?,?,?,?)' ) { stmt ->
				
					sql.eachRow("select * from  et_user_identity where et_insert_flag = true", offset, maxRows) { row ->
						stmt.addBatch([Sql.OTHER(row.login_type), Sql.OTHER(row.provision_type), row.email_id, row.reference_id, row.user_id, row.email_confirm_status, Sql.OTHER(row.status), row.created_at, row.updated_at, row.client_id ])
					}
					
				}
			}
			} catch (Exception e) {
				log.error  "Failed to move from Et_user_identity to user_identity !! \n ${e.message}"
				// continue to do as much as you can....
			}
			offset = offset + maxRows
			if (totalCount < offset) moreToGet = false  
		}
		moreToGet = true
		offset = 1
				
		// collection table move...
		resultSet = sql.firstRow("select count(*) from et_collection")
		totalCount = resultSet.getAt(0)
		if (totalCount < offset) moreToGet = false  
		
		while (moreToGet) {
			try {
			sqlWrite.withTransaction {
				def result = sqlWrite.withBatch('insert into collection(title, created_at, updated_at, owner_id, creator_id, modifier_id, format, metadata, setting, grading, license, creator_system) values (?,?,?,?,?,?,?,?,?,?,?,?)' ) { stmt ->
				
					sql.eachRow("select * from  et_collection", offset, maxRows) { row ->
						stmt.addBatch( [row.title, row.created_at, row.updated_at, row.owner_id, row.creator_id, row.modifier_id, Sql.OTHER(row.format), Sql.OTHER(row.metadata), Sql.OTHER(row.setting), Sql.OTHER(row.grading), row.license, row.creator_system] )
					}
					
				}
			}
			} catch (Exception e) {
				log.error  "Failed to move from Et_collection to collection !! \n ${e.message}"
				// continue to do as much as you can....
			}
			
			offset = offset + maxRows
			if (totalCount < offset) moreToGet = false  
		}
		moreToGet = true
		offset = 1

		// content table move...
		resultSet = sql.firstRow("select count(*) from et_content")
		totalCount = resultSet.getAt(0)
		if (totalCount < offset) moreToGet = false  
		
		while (moreToGet) {
			try {
			sqlWrite.withTransaction {			
				def result = sqlWrite.withBatch(" insert into content(title, created_at, updated_at, creator_id, modifier_id, description, " +
        							   " content_format, content_subformat, answer, metadata, collection_id, sequence_id, " +
        							   " info, license, creator_system  ) " + 
            						   " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ") { stmt ->
				
					sql.eachRow("select * from  et_content", offset, maxRows) { row ->
						stmt.addBatch( [row.title, 
            						   row.created_at, 
            						   row.updated_at, 
            						   row.creator_id, 
            						   row.modifier_id, 
            						   row.description, 
            						   Sql.OTHER(row.content_format), 
            						   Sql.OTHER(row.content_subformat), 
            						   Sql.OTHER(row.answer), 
            						   Sql.OTHER(row.metadata), 
            						   row.collection_id, 
            						   row.sequence_id, 
            						   Sql.OTHER(row.info), 
            						   row.license, 
            						   row.creator_system] )
					}
					
				}
			}
			} catch (Exception e) {
				log.error  "Failed to move from Et_content to content !! \n ${e.message}"
				// continue to do as much as you can....
			}
			
			offset = offset + maxRows
			if (totalCount < offset) moreToGet = false  
		}
		
		sqlWrite.close()
		
		endMigration() // close connection
		
		log.info "End PostMigrationRun: " + (new Date()).getTime()
		
    }
    
}