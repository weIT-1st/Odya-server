package kr.weit.odya.domain

import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

// 해당 엔티티는 수정가능한 엔티티에 적용한다
@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class BaseTimeEntity {

    @CreatedDate
    @Column(nullable = false, updatable = false)
    lateinit var createdDate: LocalDateTime
        protected set

    @LastModifiedDate
    lateinit var updatedDate: LocalDateTime
        protected set
}
