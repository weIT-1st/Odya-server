package kr.weit.odya.util

import com.zaxxer.hikari.pool.HikariProxyConnection
import org.geolatte.geom.codec.db.oracle.DefaultConnectionFinder
import java.lang.reflect.Field
import java.sql.Connection

class ConnectionFinder : DefaultConnectionFinder() {
    override fun find(con: Connection): Connection {
        val delegate: Field = (con as HikariProxyConnection).javaClass.superclass.getDeclaredField("delegate")
        delegate.setAccessible(true)
        return delegate.get(con) as Connection
    }
}
