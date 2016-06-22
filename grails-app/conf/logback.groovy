import grails.util.BuildSettings
import grails.util.Environment

// See http://logback.qos.ch/manual/groovy.html for details on configuration

appender('STDOUT', ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%level %logger - %msg%n"
    }
}	

root(ERROR, ['STDOUT'])


appender('FILE', FileAppender) {
    file = "./et2gooru.log"
    append = true
    encoder(PatternLayoutEncoder) {
        pattern = "%level %logger - %msg%n"
    }
}
logger("grails.app.services.org.gooru.et2gooru", INFO, ['FILE'], false)

def targetDir = BuildSettings.TARGET_DIR
if (Environment.isDevelopmentMode() ) {
    if (targetDir) {
	    appender("FULL_STACKTRACE", FileAppender) {
	        file = "${targetDir}/stacktrace.log"
	        append = true
	        encoder(PatternLayoutEncoder) {
	            pattern = "%level %logger - %msg%n"
	        }
	    }
	    logger("StackTrace", ERROR, ['FULL_STACKTRACE'], false)
    }
}


/* FULL logging...
logback = {
    error   'org.codehaus.groovy.grails.web.servlet'        // controllers
            'org.codehaus.groovy.grails.web.pages'           // GSP
            'org.codehaus.groovy.grails.web.mapping.filter' // URL mapping
            'org.codehaus.groovy.grails.web.mapping'        // URL mapping
            'org.codehaus.groovy.grails.commons'            // core / classloading
            'org.codehaus.groovy.grails.plugins'            // plugins
            'org.codehaus.groovy.grails.orm.hibernate'      // hibernate integration
            'org.springframework'
            'org.hibernate'

    debug   'grails.app.conf', 
            'grails.app.controllers', 
            'grails.app.services.org.gooru.et2gooru',
            'grails.app.taglib.org.gooru.et2gooru',
            'grails.app.jobs', 
            'grails.app.filters.org.gooru.et2gooru', 
            'org.gooru.et2gooru'

    warn    'grails.app.conf', 
            'grails.app.controllers', 
            'grails.app.services.org.gooru.et2gooru',
            'grails.app.taglib.org.gooru.et2gooru',
            'grails.app.jobs', 
            'grails.app.filters.org.gooru.et2gooru', 
            'org.gooru.et2gooru'
            
    info    'grails.app.conf'
            'grails.app.filters'
            'grails.app.taglib'
            'grails.app.services'
            'grails.app.controllers'
            'grails.app.domain'
            'org.codehaus.groovy.grails.commons'
            'org.codehaus.groovy.grails.web'
            'org.codehaus.groovy.grails.web.mapping'
            'org.codehaus.groovy.grails.plugins'
            'grails.spring'
            'org.springframework'
            'org.hibernate'
}
*/
