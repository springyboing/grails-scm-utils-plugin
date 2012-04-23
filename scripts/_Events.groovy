includeTargets << grailsScript("_GrailsEvents")
includeTargets << new File("$scmUtilsPluginDir/scripts/_Scm.groovy")


eventReleaseEvent = { version, msg ->
    println "### ReleaseEvent - New version created: ${version}"

    scmVersion = version
    scmMsg = "ReleaseEvent - New version created: ${version}"
    scmRelease()
}

eventSnapshotReleaseEvent = { version ->
    println "### SnapshotReleaseEvent - New version created: ${version}"

    scmVersion = version
    scmMsg = "SnapshotReleaseEvent - New version created: ${version}"
    scmSnapshotRelease()
}

eventReleaseCurrentVersionEvent = {
    println "### ReleaseCurrentVersionEvent - The current version is ${it}"
}
eventReleaseNextVersionEvent = {
    println "### ReleaseNextVersionEvent(Begin) - The next version will be ${it}"
//    whichBranch()
//    theNextVersion = branchVersion(theNextVersion, branchN)
    println "### ReleaseNextVersionEvent(End) - The next version will be ${theNextVersion}"
}
eventReleaseUpdateVersionStartEvent = {
    println "### ReleaseUpdateVersionStartEvent - ${it}"
}
eventReleaseUpdateVersionEndEvent = {
    println "### ReleaseUpdateVersionEndEvent - ${it}"
}


eventReleaseStart = { version ->
    println "### eventReleaseStart [${version}]"
    println "Current version: " + version
}
eventReleaseVersionUpdate = { version ->
    println "### eventReleaseVersionUpdate [${version}]"
}
eventReleaseVersionUpdated = { oldVersion, newVersion ->
    println "### eventReleaseVersionUpdated [${oldVersion} -> ${newVersion}]"
    scmVersion = version.toString()
    scmMsg = "Release - New version created: ${version}"
    if (version.snapshot) {
        scmMsg = "Release version ${version}"
        scmRelease()
    } else {
        scmMsg = "ReleaseSnapshot version ${version}"
        scmSnapshotRelease()
    }
}
eventReleaseComplete = { version ->
    println "### eventReleaseVersionUpdate [${version}]"
    println "Completed version: " + version
}




def branchVersion(version, branch) {

    // if using svn or git strip the branch name.
//    if (ignoreTrunkMaster) {
//        branch = (branch =~ /master|trunk/).matches() ? '' : branch
//    }

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
def unsnapshotVersion(version) {
    def snapshotMatcher = (version =~ /-SNAPSHOT/)
    version = snapshotMatcher.replaceFirst("")
    return version
}