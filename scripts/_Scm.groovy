if (isGitScm()) {
    println "GIT"
    includeTargets << new File("$scmUtilsPluginDir/scripts/_ScmGit.groovy")
} else if (isSvnScm()) {
    println "SVN"
    includeTargets << new File("$scmUtilsPluginDir/scripts/_ScmSvn.groovy")
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