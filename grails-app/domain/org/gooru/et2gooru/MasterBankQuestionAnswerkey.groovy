package org.gooru.et2gooru

class MasterBankQuestionAnswerkey {
	Integer    id
	String title
	Integer position
	Integer isCorrect
	String rationale
	Integer questionId

	static belongsTo = []

    static mapping = {
    	datasource 'lookup'
		version false
        id column: 'id'
	}
}
