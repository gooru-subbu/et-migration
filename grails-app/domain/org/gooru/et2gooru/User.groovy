package org.gooru.et2gooru

class User {
	Integer    id
	String username
	String firstName
	String lastName
	String userType
	String email
	String oauthProvider
	Boolean accountActive
	Date accountCreated
	Date lastUpdated
	Boolean accountDisabled

	static hasMany = [masterBankItems: MasterBankItem,
	                  masterBankQuestions: MasterBankQuestion]
    
    static mapping = {
    	datasource 'lookup'
		table 'user'
		version false
        id column: 'id'
	}
}
