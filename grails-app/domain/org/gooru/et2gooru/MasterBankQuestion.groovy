package org.gooru.et2gooru

class MasterBankQuestion {
	Integer    id

	String title
	String studentDirection
	String description
	String answerType
	String autocheck
	String keyword
	String answer
	String answerTrueFalse
	String showAnswer
	Byte questionDifficulty
	String questionVisibility
	String randomizeAnswer
	String timedQuestion
	Integer deletedBy
	String statusDeleted
	Integer isMathQuestion
	Date addedDate
	Date updatedDate
	Integer searchable
	Integer originalQuestionId
	Integer creatorId

	static hasMany = [masterBankQuestionAnswerkeys: MasterBankQuestionAnswerkey]
	                  
    static mapping = {
    	datasource 'lookup'
		version false
        id column: 'id'
	}
}