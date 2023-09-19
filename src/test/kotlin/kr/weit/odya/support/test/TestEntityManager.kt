package kr.weit.odya.support.test

import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager

fun TestEntityManager.flushAndClear() {
    flush()
    clear()
}
