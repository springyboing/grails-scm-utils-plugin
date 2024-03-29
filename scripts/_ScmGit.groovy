import org.apache.commons.lang.SystemUtils

scmVersion = ''
scmMsg = ''
scmTag = ''
scmFiles = ['application.properties', 'ScmUtilsGrailsPlugin.groovy']

target(tagRelease: "Tags a release.  A release label may not overwrite an existing label") {
    tagRelease(scmTag)
}

target(tagBuild: "Tags a build.  A build label may overwrite existing tags") {
    tagBuild(scmTag)
}

target(scmRelease: "Commits changed files, release tags and build tags the last release") {
    // Add then Commit
    scmFiles.each{ file ->
        add(file)
    }
    commit(scmMsg)

    // TagRelease
    tagRelease("Release-" + scmVersion)
    tagBuild('last-release')
}

target(scmSnapshotRelease: "Commits changed files and build tags the next release") {
    // Add then Commit
    scmFiles.each{ file ->
        add(file)
    }
    commit(scmMsg)

    tagBuild('next-release')
}

target(whichBranch: "Checks your project dir and sets the branch name") {
    branchN = branchName()
}

def tagBuild(version) {
    executeCmd("git label -d ${version}")
    tag(version)
}

def tagRelease(version) {
    tag(version)
}

def tag(version) {
    executeCmd(['git', 'label', version])
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
    def output = executeCmd(['git', 'branch'])
    return output.find(/\*\s(.*)/) { matcher, value -> value }
}
def executeCmd(cmd) {
    cmd = osCmdWrapper(cmd)

    println "DryRun: " + argsMap.dryRun
    println "\texecuteCmd: " + cmd
    def proc = cmd.execute()
    proc.waitFor()

    // Obtain output
    println "\tstderr: ${proc.err.text}"
    //println "stdout: ${proc.in.text}" // *out* from the external program is *in* for groovy
    return proc.text
}
def osCmdWrapper(cmd) {
    if (SystemUtils.IS_OS_WINDOWS) {
        return  ['cmd', '/c'] + cmd
    }
    return ['echo'] + cmd
}