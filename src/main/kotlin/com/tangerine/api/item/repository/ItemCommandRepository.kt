package com.tangerine.api.item.repository

import com.tangerine.api.item.entity.ItemEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ItemCommandRepository : JpaRepository<ItemEntity, Long?>
