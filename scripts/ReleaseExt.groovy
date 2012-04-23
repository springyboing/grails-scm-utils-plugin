import org.apache.commons.lang.SystemUtils

includeTargets << grailsScript("_GrailsEvents")
includeTargets << grailsScript("_GrailsArgParsing")
//includeTargets << new File("$scmUtilsPluginDir/scripts/_Scm.groovy")

// http://semver.org/


target(default: "The description of the script goes here!") {
    depends(parseArguments)

    if (argsMap.dryRun) {
         dryRun = true
    }
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

    if (argsMap.'hideBranch') {
        includeBranch = false
    }
    if (argsMap.'includeMaster') {
        ignoreTrunkMaster = false
    }
    if (argsMap.scm) {
        scmSystem = argsMap.scm
    }

    event("IncludeBranchEvent", [])
    println "ReleaseExt: " + branchN

    def oldVersion = currentVersion()
    println "CurrentVersion: " +  oldVersion
    def nextVersion = nextVersion(oldVersion)
    println "NextVersion: " +  nextVersion
    
    if (!dryRun) {
        updateVersion(nextVersion)
    }
}

target(release: "Removes Snapshot from version") {
}

dryRun = false
scmSystem = 'svn'

major = null
minor = null
patch = null
remaining = null
branchN = 'xXx'

releaseMajor = false
releaseMinor = false
releasePatch = false

release = false
includeBranch = true
ignoreTrunkMaster = true

def nextVersion(version) {

    def versionMatcher = (version =~ /(\d*)(?:\.(\d*))?(?:\.(\d*))?(.*)/)
    major = versionMatcher[0][1]
    minor = versionMatcher[0][2]
    patch = versionMatcher[0][3]
    remaining = versionMatcher[0][4]
    println "Major: " + major
    println "Minor: " + minor
    println "Patch: " + patch
    println "Remaining: " + remaining

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
//        def branch = getBranch()
        version = branchVersion(version, branchN)
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

def updateVersion(newVersion) {
    if (isPluginProject) {
        updatePluginVersion(newVersion)
    } else {
        updateApplicationVersion(newVersion)
    }
}
def updatePluginVersion(newVersion) {

    File file = new File(pluginSettings.basePluginDescriptor.filename)
    String descriptorContent = file.text

    def pattern = ~/def\s*version\s*=\s*"(.*)"/
    def matcher = (descriptorContent =~ pattern)

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
def updateApplicationVersion(newVersion) {
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
            def proc = osCmdWrapper(['git', 'branch']).execute()
            proc.waitForOrKill(3000)
            branch = proc.text.replaceAll(/\*|\s/, '')
            break
        case 'svn':
            def proc = osCmdWrapper(['svn', 'info']).execute()
            proc.waitForOrKill(9000)
            def output = proc.text
            branch = output.find(/URL: .*\/((?:branches|tags)\/[^\/^\s]+|trunk)/) { match, value -> value.find(/[^\/]+$/) }
    }
    return branch
}

def osCmdWrapper(cmd) {
    if (SystemUtils.IS_OS_WINDOWS) {
        return  ['cmd', '/c'] + cmd
    }
    return cmd
}