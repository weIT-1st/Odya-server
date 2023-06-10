package kr.weit.odya.domain

import jakarta.persistence.Column
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.LastModifiedDate
import java.time.LocalDateTime

// 해당 엔티티는 수정가능한 엔티티에 적용한다
@MappedSuperclass
abstract class BaseModifiableEntity : BaseTimeEntity() {
    @LastModifiedDate
    @Column(nullable = false)
    lateinit var updatedDate: LocalDateTime
        protected set
}
