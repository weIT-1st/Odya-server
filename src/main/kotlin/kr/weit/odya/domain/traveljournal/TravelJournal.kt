package kr.weit.odya.domain.traveljournal

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table
import kr.weit.odya.domain.user.User
import kr.weit.odya.support.domain.BaseModifiableEntity
import java.time.LocalDate

@Table(
    indexes = [
        Index(name = "travel_journal_user_id_index", columnList = "user_id"),
    ],
)
@Entity
@SequenceGenerator(
    name = "TRAVEL_JOURNAL_SEQ_GENERATOR",
    sequenceName = "TRAVEL_JOURNAL_SEQ",
    initialValue = 1,
    allocationSize = 1,
)
class TravelJournal(
    @Id
    @Column(columnDefinition = "NUMERIC(19, 0)")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "TRAVEL_JOURNAL_SEQ_GENERATOR")
    val id: Long = 0L,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    val user: User,

    travelJournalInformation: TravelJournalInformation,

    travelCompanions: List<TravelCompanion>,

    travelJournalContents: List<TravelJournalContent>,
) : BaseModifiableEntity() {
    @Embedded
    var travelJournalInformation: TravelJournalInformation = travelJournalInformation
        protected set

    @OneToMany(cascade = [CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE], orphanRemoval = true)
    @JoinColumn(name = "travel_journal_id", nullable = false, updatable = false, columnDefinition = "NUMERIC(19, 0)")
    private val mutableTravelCompanions: MutableList<TravelCompanion> =
        travelCompanions.toMutableList() // backing property 사용 시 쿼리 에러 발생 (ORA-00911: 문자가 부적합합니다)
    val travelCompanions: List<TravelCompanion>
        get() = mutableTravelCompanions

    @OneToMany(cascade = [CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE], orphanRemoval = true)
    @JoinColumn(name = "travel_journal_id", nullable = false, updatable = false, columnDefinition = "NUMERIC(19, 0)")
    private val mutableTravelJournalContents: MutableList<TravelJournalContent> = travelJournalContents.toMutableList()
    val travelJournalContents: List<TravelJournalContent>
        get() = mutableTravelJournalContents

    val title: String
        get() = travelJournalInformation.title

    val travelStartDate: LocalDate
        get() = travelJournalInformation.travelStartDate

    val travelEndDate: LocalDate
        get() = travelJournalInformation.travelEndDate

    val visibility: TravelJournalVisibility
        get() = travelJournalInformation.visibility

    fun changeTravelJournalInformation(travelJournalInformation: TravelJournalInformation) {
        this.travelJournalInformation = travelJournalInformation
    }

    fun deleteTravelJournalContent(travelJournalContent: TravelJournalContent) {
        mutableTravelJournalContents.remove(travelJournalContent)
    }

    fun addTravelCompanions(travelCompanions: List<TravelCompanion>) {
        mutableTravelCompanions.addAll(travelCompanions)
    }
}
