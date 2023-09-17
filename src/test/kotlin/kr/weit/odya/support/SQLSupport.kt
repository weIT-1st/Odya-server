package kr.weit.odya.support

import org.h2gis.functions.spatial.predicates.ST_Within
import org.locationtech.jts.geom.Geometry
import java.sql.SQLException

class SQLSupport {

    companion object {
        // 귀찮게도 Oracle과 H2의 ST_Relate 의 인자 순서가 반대다.
        // 따라서 H2의 인자 순서를 오라클이랑 동일하게 맞춘다
        @Throws(SQLException::class)
        @JvmStatic
        fun within(a: Geometry, b: Geometry): Boolean {
            return ST_Within.isWithin(b, a)
        }
    }
}
