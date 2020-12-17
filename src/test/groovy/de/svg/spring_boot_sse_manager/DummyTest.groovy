package de.svg.spring_boot_sse_manager;

/**
 * Dummy test.
 *
 * @author Marvin Oßwald
 * @since 16.12.2020
 */
class DummyTest extends AbstractTest {

    def "Check something important"() {

        when: "do something"
        Integer checkAssignment = 1
        then: "check result"
        checkAssignment == 1
    }
}
