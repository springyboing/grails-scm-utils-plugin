import org.tmatesoft.svn.core.SVNAuthenticationException

includeTargets << grailsScript("_GrailsEvents")

target(default: "The description of the script goes here!") {
    // TODO: Implement script here

    // find branch name if applicable
    def branchName = ''

    currentVersion()
    updateVersion()
}

target(release: "Removes Snapshot from version") {

}

minor = true
release = false
includeBranch = true

def nextVersion(version) {

    if (minor) {
        version = nextMinorVersion(version)
    } else {
        version = nextMajorVersion(version)
    }
    if (release) {
        version = unsnapshotVersion(version)
    } else {
        version = snapshotVersion(version)
    }
    if (includeBranch) {
        //version = branchVersion(version, "master")
        println "master: " + branchVersion(version, "master")
        println "trunk: " + branchVersion(version, "trunk")
        println "fix-23-poo: " + branchVersion(version, "fix-23-poo")
        println "fix-23-poo: " + branchVersion("1.3-fix-100-poo", "fix-23-poo")
        println "fix-23-poo: " + branchVersion("1.3-fix-100-poo-SNAPSHOT", "fix-23-poo")
    }
}

def nextMinorVersion(version) {
    def versionMatcher = (version =~ /[\d\.]*/)
    def numericVersion =  versionMatcher[0]
    numericVersion = ++numericVersion
    version = versionMatcher.replaceFirst(numericVersion)
    return version
}
def nextMajorVersion(version) {
    def versionMatcher = (version =~ /[\d\.]*/)
    def numericVersion =  versionMatcher[0]
    numericVersion = ++(numericVersion as Integer)
    version = versionMatcher.replaceFirst(numericVersion)
    return version
}
def snapshotVersion(version) {
    def snapshotMatcher = (version =~ /-SNAPSHOT/)
    if (!snapshotMatcher.count) {
        version += '-SNAPSHOT'
    }
    return version
}
def unsnapshotVersion(version) {
    def snapshotMatcher = (version =~ /-SNAPSHOT/)
    version = snapshotMatcher.replaceFirst("")
    return version
}
def unbranchVersion(version) {
    def strippedSnapshot = unsnapshotVersion(version)
    def applySnapshotBack = strippedSnapshot != version
    def branchMatcher = (strippedSnapshot =~ /-[\w\d-]*/)
    if (!branchMatcher.count) {
        println "I match existing branch: " + branchMatcher[0]
        strippedSnapshot = branchMatcher.replaceFirst("")
    }
    version = strippedSnapshot
    
    if (applySnapshotBack) {
         version += "-SNAPSHOT"
    }

    return version
}
def branchVersion(version, branch) {

    println "Branch [${branch}]: " + (branch =~ /master|trunk/).matches()
    // if using svn or git strip the branch name.
    branch = (branch =~ /master|trunk/).matches() ? '' : "-${branch}"

    println "Orignal Version: " + version
    def strippedSnapshot = unsnapshotVersion(version)
    println "strippedSnapshot: " + strippedSnapshot
    def applySnapshotBack = strippedSnapshot != version
    println "applySnapshotBack: " + applySnapshotBack
    def branchMatcher = (strippedSnapshot =~ /-[\w\d-]*/)
    if (branchMatcher.count != 0) {
        println "I match existing branch : " + branchMatcher[0]
        strippedSnapshot = branchMatcher.replaceFirst("")
    }
    version = strippedSnapshot + branch

    if (applySnapshotBack) {
         version += '-SNAPSHOT'
    }

    return version
}



def updateVersion() {
    if (isPluginProject) {
        updatePluginVersion()
    } else {
        updateApplicationVersion()
    }
}
def updatePluginVersion() {
    if (!pluginSettings.basePluginDescriptor.filename) {
        grailsConsole.error "PluginDescripter not found to set version"
        exit 1
    }

    File file = new File(pluginSettings.basePluginDescriptor.filename)
    String descriptorContent = file.text

    def pattern = ~/def\s*version\s*=\s*"(.*)"/
    def matcher = (descriptorContent =~ pattern)

    String oldVersion = ''
    if (matcher.size() > 0) {
        oldVersion = matcher[0][0]
    }

    String newVersion = nextVersion(oldVersion)
    String newVersionString = "def version = \"${newVersion}\""

    if (matcher.size() > 0) {
        descriptorContent = descriptorContent.replaceFirst(/def\s*version\s*=\s*".*"/, newVersionString)
    }
    else {
        descriptorContent = descriptorContent.replaceFirst(/\{/, "{\n\t$newVersionString // added by set-version")
    }

    file.withWriter { it.write descriptorContent }
    event("StatusFinal", ["Plugin version updated to $newVersion"])
}
def updateApplicationVersion() {

    def oldVersion = metadata.'app.version'
    String newVersion = nextVersion(oldVersion)
    metadata.'app.version' = newVersion
    metadata.persist()
    event("StatusFinal", ["Application version updated to $newVersion"])
}
def currentVersion() {
    if (isPluginProject) {
        println "Plugin version: " + pluginVersion()
    } else {
        println "Application version: " + applicationVersion()
    }

}

def applicationVersion() {
    return  metadata.'app.version'
}
def pluginVersion() {
    File file = new File(pluginSettings.basePluginDescriptor.filename)
    String descriptorContent = file.text

    def pattern = ~/def\s*version\s*=\s*"(.*)"/
    def matcher = (descriptorContent =~ pattern)

    String oldVersion = ''
    if (matcher.size() > 0) {
        oldVersion = matcher[0][0]
    }
    return oldVersion
}