import org.apache.commons.lang.SystemUtils

includeTargets << grailsScript("_GrailsArgParsing")

scmVersion = ''
scmMsg = ''
scmTag = ''
scmFiles = ['application.properties', 'ScmUtilsGrailsPlugin.groovy']

scmUsername = (buildConfig.scmutils.username) ?: ''
scmPassword = (buildConfig.scmutils.password) ?: ''

target(tagRelease: "Tags a release.  A release tag may not overwrite an existing tag") {
    tagRelease(scmTag)
}

target(tagBuild: "Tags a build.  A build tag may overwrite existing tags") {
    tagBuild(scmTag)
}

target(scmRelease: "Commits changed files, release tags and build tags the last release") {
    // Add & Commit
    addAndCommit('blur.txt', scmMsg)
    // TagRelease
    tagRelease("Release-" + scmVersion)
    tagBuild('last-release')
}

target(scmSnapshotRelease: "Commits changed files and build tags the next release") {
    // Add & Commit
    addAndCommit('blur.txt', scmMsg)
    tagBuild('next-release')
}

target(whichBranch: "Checks your project dir and sets the branch name") {
    branchN = branchName()
}

def tagBuild(version) {

    def workingDir = getWorkingDir()
    println "WorkingDir: " + workingDir

    executeCmd(['svn', '--non-interactive', 'delete', workingDir + '../tags' + version])
    tag(version)
}

def tagRelease(version) {
    tag(version)
}

def tag(version) {

    def workingDir = getWorkingDir()
    println "WorkingDir: " + workingDir
    executeCmd(['svn', '--non-interactive', 'copy', '.', workingDir + "../tags/" + version])
}

def add(path) {
    executeCmd(['svn', '--non-interactive', 'add', path])
}

def commit(msg) {
    executeCmd(["svn", '--non-interactive', "commit", "-m", '"' + msg + '"'])
}

def addAndCommit(path, msg) {
    add(path)
    commit(msg)
}

def branchName() {
    def procOut = executeCmd(['svn', 'info'])
    return procOut.find(/URL: .*\/((?:branches|tags)\/[^\/^\s]+|trunk)/) { match, value -> value.find(/[^\/]+$/) }
}

def executeCmd(cmd, Long timeout=1000 * 60 * 3) {
    def proc = osCmdWrapper(cmd.execute())
	proc.waitForOrKill(timeout)

    // Obtain output
    //println "stderr: ${proc.err.text}"
    //println "stdout: ${proc.in.text}" // *out* from the external program is *in* for groovy
    return proc.text
}
def osCmdWrapper(cmd) {
    if (SystemUtils.IS_OS_WINDOWS) {
        return  ['cmd', '/c'] + cmd
    }
    return cmd
}

def getWorkingDir() {
    def cmd = ['svn', 'info']
    def process = new ProcessBuilder(cmd).redirectErrorStream(true).start()
    process.inputStream.eachLine {
        println it
        if (it.startsWith('URL: ')) {
            return it.replaceFirst('URL: ')
        }
    }
    return null
}
