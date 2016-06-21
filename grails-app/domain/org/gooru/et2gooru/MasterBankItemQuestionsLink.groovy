package org.gooru.et2gooru

class MasterBankItemQuestionsLink {
	Integer    id

	Integer pos
	Integer itemId
	Integer questionId

	static belongsTo = []

	static mapping = {
    	datasource 'lookup'
		version false
        id column: 'id'
	}
}
