package com.tangerine.api.item.repository

import com.tangerine.api.item.entity.ItemEntity
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ItemRepository : JpaRepository<ItemEntity, Long?> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM ItemEntity i WHERE i.id = :id")
    fun findByIdWithPessimisticLock(id: Long): ItemEntity?
}
