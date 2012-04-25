includeTargets << grailsScript("_GrailsEvents")
includeTargets << grailsScript("_GrailsArgParsing")
includeTargets << grailsScript('_GrailsPackage')

// http://semver.org/

dryRun = false
releaseMajor = false
releaseMinor = false
releasePatch = false
//release = false
version = null

target(default: "The description of the script goes here!") {
    depends(parseArguments)

    dryRun = argsMap.dryRun
    releaseMajor = argsMap.major
    releaseMinor = argsMap.minor
    releasePatch = argsMap.patch

    debug()

    release()
}

target(release: "Removes Snapshot from version") {
    depends(classpath, compile)

    version = classLoader.loadClass('uk.co.accio.release.Version').newInstance().parse(currentVersion())
    def oldVersion = version
    println "Version: " + version
    println "Next Version: " + incrimentVersion(version)

    event("ReleaseStart", [version])

    if (version.snapshot) {     // if SNAPSHOT
        version = version.releaseVersion() // Remove snapshot label
        event("ReleaseVersionUpdate", [version])    // Listener can add label to version
        //  SCM Branch gets added as a version label via an event listener...
        updateVersion(version.toString())
        event("ReleaseVersionUpdated", [oldVersion, version])   // Listener saves version change
    }

    println "Incriment version: " + (releaseMajor || releaseMinor || releasePatch) + " & SNAPSHOT"
    if (releaseMajor || releaseMinor || releasePatch) {

        // Store the new old version...
        oldVersion = version

        version = incrimentVersion(version) // Incriment for next release
        version = version.snapshotVersion() // Add SNAPSHOT to version label
        event("ReleaseVersionUpdate", [version, ])    // Listener can add label to version
        //  SCM Branch gets added as a version label via an event listener...
        updateVersion(version.toString())
        event("ReleaseVersionUpdated", [oldVersion, version])   // Listener saves version change
    }

    event("ReleaseComplete", [version])
}
def debug() {
    println "dryRun: " + dryRun
    println "releaseMajor: " + releaseMajor
    println "releaseMinor: " + releaseMinor
    println "releasePatch: " + releasePatch
}

def incrimentVersion(cVersion) {
    if (releaseMajor) {
        return cVersion.nextMajorVersion()
    } else if (releaseMinor) {
        return cVersion.nextMinorVersion()
    } else if (releasePatch) {
        return cVersion.nextPatchVersion()
    }
    cVersion
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