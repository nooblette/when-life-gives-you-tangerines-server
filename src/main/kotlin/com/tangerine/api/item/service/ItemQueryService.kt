package com.tangerine.api.item.service

import com.tangerine.api.item.domain.Item
import com.tangerine.api.item.mapper.toDomains
import com.tangerine.api.item.repository.ItemQueryRepository
import org.springframework.stereotype.Service

@Service
class ItemQueryService(
    private val itemQueryRepository: ItemQueryRepository,
) {
    fun findAll(): List<Item> {
        val t = itemQueryRepository.findAll()
        return t.toDomains()
    }
}
