package dev.zhihexireng.core

import spock.lang.Specification

class HashUtilsTest extends Specification {
    def "sha256"() {
        expect:
        def sha256 = HashUtils.sha256("dkdkdk".bytes)
        println sha256
    }
}
