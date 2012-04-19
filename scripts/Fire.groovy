includeTargets << grailsScript("_GrailsArgParsing")
includeTargets << grailsScript("_GrailsEvents")

target(default: "The description of the script goes here!") {

    simulateRelease()
}

def simulateRelease() {
    
    def version = new File('blur.txt').text as Integer

    // Change version
    new File('blur.txt').write(++version as String)

    event("ReleaseEvent", [version, "Version ${version} Release"])

    // Change version
    new File('blur.txt').write(++version as String)

    event("SnapshotReleaseEvent", [version, "Snapshot release for ${version}"])
}