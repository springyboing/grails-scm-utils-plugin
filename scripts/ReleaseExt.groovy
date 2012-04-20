includeTargets << grailsScript("_GrailsEvents")
includeTargets << grailsScript("_GrailsArgParsing")

// http://semver.org/


target(default: "The description of the script goes here!") {
    depends(parseArguments)

    def oldVersion = currentVersion()
    println "CurrentVersion: " +  oldVersion

    if (argsMap.release) {
         release = true
    }
    if (argsMap.major) {
        releaseMajor = true
    } else if (argsMap.minor) {
        releaseMinor = true
    } else if (argsMap.patch) {
        releasePatch = true
    }

    if (argsMap.branch) {
        includeBranch = true
    }
    if (argsMap.ignoreMaster) {
        ignoreTrunkMaster = true
    }
    if (argsMap.scm) {
        scmSystem = argsMap.scm
    }

    println "NextVersion: " +  nextVersion(oldVersion)
    if (!dryRun) {
     //   updateVersion()
    }
}

target(release: "Removes Snapshot from version") {
}

dryRun = true
scmSystem = 'git'

major = null
minor = null
patch = null
remaining = null

releaseMajor = false
releaseMinor = false
releasePatch = false

release = false
includeBranch = true
ignoreTrunkMaster = false

def nextVersion(version) {

    def versionMatcher = (version =~ /(\d?)(?:\.(\d))?(?:\.(\d))?.*/)
    major = versionMatcher[0][1]
    minor = versionMatcher[0][2]
    patch = versionMatcher[0][3]
    remaining = versionMatcher[0][4]
    println "Major: " + major
    println "Minor: " + minor
    println "Patch: " + patch
    println "Remaining: " + remaining

//    println "Major: " + nextMajorVersion(major, minor, patch, remaining)
//    println "Minor: " + nextMinorVersion(major, minor, patch, remaining)
//    println "Patch: " + nextPatchVersion(major, minor, patch, remaining)

    if (releaseMajor) {
        version = nextMajorVersion(major, minor, patch, remaining)
    } else if (releaseMinor) {
        version = nextMinorVersion(major, minor, patch, remaining)
    } else if (releasePatch) {
        version = nextPatchVersion(major, minor, patch, remaining)
    }

    if (release) {
        version = unsnapshotVersion(version)
    } else {
        version = snapshotVersion(version)
    }
    if (includeBranch) {
        def branch = getBranch()
        version = branchVersion(version, branch)
    }
    return version
}

def nextMajorVersion(major, minor, patch, remaining) {
    def next = ++(major as Integer)
    return "${next}.0.0${remaining ?: ''}"
}
def nextMinorVersion(major, minor, patch, remaining) {
    def next = ++(minor as Integer)
    return "${major}.${next}.0${remaining ?: ''}"
}
def nextPatchVersion(major, minor, patch, remaining) {
    def next = ++(patch as Integer)
    return "${major}.${minor}.${next}${remaining ?: ''}"
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

    println "Branch [${branch}]: "

    // if using svn or git strip the branch name.
    if (ignoreTrunkMaster) {
        branch = (branch =~ /master|trunk/).matches() ? '' : branch
    }

    def strippedSnapshot = unsnapshotVersion(version)
    def applySnapshotBack = strippedSnapshot != version
    def branchMatcher = (strippedSnapshot =~ /-[\w\d-]*/)
    if (branchMatcher.count != 0) {
         strippedSnapshot = branchMatcher.replaceFirst("")
    }
    version = strippedSnapshot + (branch ? '-' + branch : '')

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
        oldVersion = matcher[0][1]
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
        return pluginVersion()
    } else {
        return applicationVersion()
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
        oldVersion = matcher[0][1]
    }
    return oldVersion
}

def getBranch() {
    def branch = ''
    switch(scmSystem) {
        case 'git':
            def proc = ['cmd', '/c', 'git', 'branch'].execute()
            proc.waitForOrKill(3000)
            branch = proc.text.replaceAll(/\*|\s/, '')
            break
        case 'svn':
            def proc = ['cmd', '/c', 'svn', 'info'].execute()
            proc.waitForOrKill(3000)
            branch = proc.text.eachLine { line ->
                if (line.startsWith('URL:')) {

                    //svn info | grep '^URL:' | egrep -o '(tags|branches)/[^/]+|trunk' | egrep -o '[^/]+$'
                    //def matcher = (descriptorContent =~ /(tags|branches)\/+|trunk/)
                }
            }
            break
    }
    return branch
}