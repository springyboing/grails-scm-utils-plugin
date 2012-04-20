includeTargets << grailsScript("_GrailsEvents")

if (isGitScm()) {
    includeTargets << new File("$scmUtilsPluginDir/scripts/_ScmGit.groovy")
//    event "StatusUpdate", ["Git spotted "]
} else if (isSvnScm()) {
    includeTargets << new File("$scmUtilsPluginDir/scripts/_ScmSvn.groovy")
//    event "StatusUpdate", ["SVN spotted "]
}

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

def isGitScm() {
    return isScmAvailable('.git')
}
def isSvnScm() {
    return isScmAvailable('.svn')
}
def isScmAvailable(dir) {
    return new File(getRootDir(), dir).exists()
}
def getRootDir() {
    def rootDirOverride = buildConfig.scmutils.rootDir
    if (rootDirOverride) {
         return new File(rootDirOverride)
    }
    return new File('.')
}