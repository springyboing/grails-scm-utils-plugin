grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.target.level = 1.6

grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // uncomment to disable ehcache
        // excludes 'ehcache'
    }
    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    repositories {
        grailsCentral()
    }
    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.
    }
    plugins {
        build(":tomcat:$grailsVersion",
                ':release:1.0.0.RC1') {
            excludes "svn", 'nekohtml'
            export = false
        }
    }
}

// Alternative project root so we can find .svn or .git folder to identify SCM.
// scmutils.rootDir = '/Users/timredding/Documents/projects/Text-Vault/'