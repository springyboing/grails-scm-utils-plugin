package uk.co.accio.release


class Version {

    final static String SNAPSHOT = "SNAPSHOT"
    final static String NO_LABEL = null
    final String originalVersion
    Integer major
    Integer minor
    Integer patch
    String label = NO_LABEL
    Boolean snapshot

    private Version() {
    }
    private Version(String version) {
        this.originalVersion = version
    }

    static Version parse(String version) {
        def versionMatcher = (version =~ /(\d*)(?:\.(\d*))?(?:\.(\d*))?(.*)/)
        Integer major = versionMatcher[0][1] as Integer
        Integer minor = versionMatcher[0][2] as Integer
        Integer patch = versionMatcher[0][3] as Integer
        String remaining = versionMatcher[0][4]

        Boolean snapshot = remaining.contains(SNAPSHOT)
        String label = NO_LABEL
        if (snapshot) {
            remaining = remaining.replaceAll('-SNAPSHOT', '')
        }
        label = remaining.replaceAll(/^-/, '')

        def versionObj = new Version(version)
        versionObj.major = major
        versionObj.minor = minor
        versionObj.patch = patch
        versionObj.label = label ? label : NO_LABEL
        versionObj.snapshot = snapshot
        return versionObj
    }

    Version nextMajorVersion() {
        def version = new Version(originalVersion)
        version.major = this.major + 1
        version.minor = 0
        version.patch = 0
        version.label = this.label
        version.snapshot = this.snapshot
        return version
    }

    Version nextMinorVersion() {
        def version = new Version(this.originalVersion)
        version.major = this.major
        version.minor = this.minor + 1
        version.patch = 0
        version.label = this.label
        version.snapshot = this.snapshot
        return version
    }

    Version nextPatchVersion() {
        def version = new Version(this.originalVersion)
        version.major = this.major
        version.minor = this.minor
        version.patch = this.patch + 1
        version.label = this.label
        version.snapshot = this.snapshot
        return version
    }

    Version snapshotVersion() {
        def version = new Version(this.originalVersion)
        version.major = this.major
        version.minor = this.minor
        version.patch = this.patch
        version.label = this.label
        version.snapshot = true
        return version
    }

    Version releaseVersion() {
        def version = new Version(this.originalVersion)
        version.major = this.major
        version.minor = this.minor
        version.patch = this.patch
        version.label = this.label
        version.snapshot = false
        return version
    }

    Version withLabel(String label) {
        def version = new Version(this.originalVersion)
        version.major = this.major
        version.minor = this.minor
        version.patch = this.patch
        version.label = label
        version.snapshot = false
        return version
    }

    Version withoutLabel() {
        def version = new Version(this.originalVersion)
        version.major = this.major
        version.minor = this.minor
        version.patch = this.patch
        version.label = NO_LABEL
        version.snapshot = false
        return version
    }

    String toString() {
        return major + "." + minor + "." + patch + (label ? "-" + label : '') + (snapshot ? "-" + SNAPSHOT : '')
    }
}

