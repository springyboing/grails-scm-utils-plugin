includeTargets << grailsScript("_GrailsEvents")
includeTargets << grailsScript("_GrailsArgParsing")

// http://semver.org/

dryRun = false

releaseMajor = false
releaseMinor = false
releasePatch = false
release = false

theNextVersion = 'n/a'

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

    release()

//    def oldVersion = currentVersion()
//    event("ReleaseCurrentVersionEvent", [oldVersion])
//
//    theNextVersion = nextVersion(oldVersion)
//    event("ReleaseNextVersionEvent", [theNextVersion])
//
//    if (!dryRun) {
//        event("ReleaseUpdateVersionStartEvent", [theNextVersion])
//        updateVersion(theNextVersion)
//    }
//
//    event("ReleaseCompletedEvent", [])
}

target(release: "Removes Snapshot from version") {

    // Get the current version
    def oldVersion = currentVersion()
    event("ReleaseCurrentVersionEvent", [oldVersion])

    // Get the next version
    theNextVersion = nextVersion(oldVersion)
    event("ReleaseNextVersionEvent", [theNextVersion])
    event("ReleaseEvent", [theNextVersion, ''])

    // Update the current version
    if (!dryRun) {
        updateVersion(theNextVersion)
    }
//    event("ReleaseEvent", [theNextVersion, "Version " + theNextVersion + " Release"])

    // Get the next version
    theNextVersion = nextVersion(oldVersion)
    if (!dryRun) {
        updateVersion(theNextVersion)
    }
//    event("ReleaseNextVersionEvent", [theNextVersion])
//    event("SnapshotReleaseEvent", [theNextVersion, "Snapshot release for " + theNextVersion])
//    event("ReleaseCompletedEvent", [])
}

def nextVersion(version) {

    def versionMatcher = (version =~ /(\d*)(?:\.(\d*))?(?:\.(\d*))?(.*)/)
    def major = versionMatcher[0][1]
    def minor = versionMatcher[0][2]
    def patch = versionMatcher[0][3]
    def remaining = versionMatcher[0][4]
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
        version = nextPatchVersion(major, minor, patch, remaining)
        version = snapshotVersion(version)
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
        println "Add patch version..."
    }
    return version
}
def unsnapshotVersion(version) {
    def snapshotMatcher = (version =~ /-SNAPSHOT/)
    version = snapshotMatcher.replaceFirst("")
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