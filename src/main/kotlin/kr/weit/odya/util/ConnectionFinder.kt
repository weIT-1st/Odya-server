package kr.weit.odya.util

import com.zaxxer.hikari.pool.HikariProxyConnection
import org.geolatte.geom.codec.db.oracle.DefaultConnectionFinder
import java.lang.reflect.Field
import java.sql.Connection

class ConnectionFinder : DefaultConnectionFinder() {
    // hibernate spatial이 oracle db에 한정해 커넥션을 못찾는 이슈가 있다 그것을 해결하기 위한 코드.
    // 자세한 내용은 아래 스택오버플로우 확인
    // https://stackoverflow.com/questions/47753350/couldnt-get-at-the-oraclespatial-connection-object-from-the-preparedstatement
    override fun find(con: Connection): Connection {
        val delegate: Field = (con as HikariProxyConnection).javaClass.superclass.getDeclaredField("delegate")
        delegate.setAccessible(true)
        return delegate.get(con) as Connection
    }
}
