package uk.co.accio.release

import grails.plugin.spock.UnitSpec
import spock.lang.Unroll

class VersionSpec extends UnitSpec {

    @Unroll("Parse this #version to retrieve #major, #minor, #patch, #tag, #snapshot")
    def "Parse version strings"() {

        when:
        def myVersion = Version.parse(version)

        then:
        myVersion.major ==  major
        myVersion.minor ==  minor
        myVersion.patch ==  patch
        myVersion.tag ==  tag
        myVersion.snapshot == snapshot

        where:
        major | minor | patch | tag   | snapshot | version
        1     | 0     | 0     | ''    | false    | '1.0.0'
        1     | 2     | 0     | ''    | false    | '1.2.0'
        1     | 2     | 3     | ''    | false    | '1.2.3'
        1     | 2     | 3     | 'ABC' | false    | '1.2.3-ABC'
        1     | 2     | 3     | ''    | true     | '1.2.3-SNAPSHOT'
        1     | 2     | 3     | 'ABC' | true     | '1.2.3-ABC-SNAPSHOT'
    }

    @Unroll("Version #version from #major, #minor, #patch, #tag, #snapshot")
    def "Version as string"() {

        when:
        def myVersion = new Version(major: major, minor: minor, patch: patch, tag: tag, snapshot: snapshot)

        then:
        myVersion.toString() == version

        where:
        major | minor | patch | tag   | snapshot | version
        1     | 0     | 0     | ''    | false    | '1.0.0'
        1     | 2     | 0     | ''    | false    | '1.2.0'
        1     | 2     | 3     | ''    | false    | '1.2.3'
        1     | 2     | 3     | 'ABC' | false    | '1.2.3-ABC'
        1     | 2     | 3     | ''    | true     | '1.2.3-SNAPSHOT'
        1     | 2     | 3     | 'ABC' | true     | '1.2.3-ABC-SNAPSHOT'
    }

    @Unroll("Next #version from #major, #minor, #patch, #tag, #snapshot")
    def "Next Major Version"() {

        when:
        def myVersion = new Version(major: major, minor: minor, patch: patch, tag: tag, snapshot: snapshot)

        then:
        myVersion.nextMajorVersion().toString() == version

        where:
        major | minor | patch | tag   | snapshot | version
        1     | 0     | 0     | ''    | false    | '2.0.0'
        1     | 2     | 0     | ''    | false    | '2.0.0'
        1     | 2     | 3     | ''    | false    | '2.0.0'
        1     | 2     | 3     | 'ABC' | false    | '2.0.0-ABC'
        1     | 2     | 3     | ''    | true     | '2.0.0-SNAPSHOT'
        1     | 2     | 3     | 'ABC' | true     | '2.0.0-ABC-SNAPSHOT'
    }
}
