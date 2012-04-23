import org.apache.commons.lang.SystemUtils

scmVersion = ''
scmMsg = ''
scmTag = ''

target(tagRelease: "Tags a release.  A release tag may not overwrite an existing tag") {
    tagRelease(scmTag)
}

target(tagRelease: "Tags a build.  A build tag may overwrite existing tags") {
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
    executeCmd("git tag -d ${version}")
    tag(version)
}

def tagRelease(version) {
    tag(version)
}

def tag(version) {
    executeCmd(['git', 'tag', version])
}

def add(path) {
    executeCmd(['git', 'add', path])
}

def commit(msg) {
    executeCmd(["git", "commit", "-m", '"' + msg + '"'])
}

def addAndCommit(path, msg) {
    add(path)
    commit(msg)
}

def push() {
    executeCmd(['git', 'push', '--tags'])
}
def branchName() {
    def output = executeCmd(osCmdWrapper(['git', 'branch']))
    return output.replaceAll(/\*|\s/, '')
}
def executeCmd(cmd, Long timeout=1000 * 60 * 3) {
    def proc = cmd.execute()
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