package com.tangerine.api.item.repository

import com.tangerine.api.item.entity.ItemEntity
import jakarta.persistence.LockModeType
import jakarta.persistence.QueryHint
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.QueryHints
import org.springframework.stereotype.Repository

@Repository
interface ItemRepository : JpaRepository<ItemEntity, Long?> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(
        QueryHint(name = "jakarta.persistence.query.timeout", value = "5000"),
    )
    @Query("SELECT i FROM ItemEntity i WHERE i.id = :id")
    fun findByIdWithPessimisticLock(id: Long): ItemEntity?
}
