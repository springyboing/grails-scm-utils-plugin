includeTargets << grailsScript("_GrailsEvents")
includeTargets << new File("$scmUtilsPluginDir/scripts/_Scm.groovy")


eventReleaseEvent = { version, msg ->
    println "### ReleaseEvent - New version created: ${version}"

    scmVersion = version
    scmMsg = msg
    //scmRelease()
}

eventSnapshotReleaseEvent = { version, msg ->
    println "### SnapshotReleaseEvent - New version created: ${version}"

    scmVersion = version
    scmMsg = msg
    //scmSnapshotRelease()
}

eventIncludeBranchEvent = {
    println "### IncludeBranchEvent - ${it}"

    println "includeBranch: " + argsMap

    whichBranch()
}