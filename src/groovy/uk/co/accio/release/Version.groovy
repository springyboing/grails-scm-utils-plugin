package uk.co.accio.release


class Version {

    final static String SNAPSHOT = "SNAPSHOT"
    final String originalVersion
    Integer major
    Integer minor
    Integer patch
    String remaining
    String tag
    Boolean snapshot

    static Version parse(String version) {
        def versionMatcher = (version =~ /(\d*)(?:\.(\d*))?(?:\.(\d*))?(.*)/)
        Integer major = versionMatcher[0][1] as Integer
        Integer minor = versionMatcher[0][2] as Integer
        Integer patch = versionMatcher[0][3] as Integer
        String remaining = versionMatcher[0][4]

        Boolean snapshot = remaining.contains(SNAPSHOT)
        return new Version(originalVersion: version, major: major, minor: minor, patch: patch, remaining: remaining, snapshot: snapshot)
    }

    Version nextMajorVersion() {
        return new Version()
    }

    Version nextMinorVersion() {
        return new Version()
    }

    Version nextPatchVersion() {
        return new Version()
    }

    Version snapshotVersion() {
        return new Version()
    }

    Version releaseVersion() {
        return new Version()
    }

    Version withoutTag() {
        return new Version()
    }

    String toString() {
        return major + "." + minor + "." + patch + (tag ? "-" + tag : '') + (snapshot ? "-" + SNAPSHOT : '')
    }
}

