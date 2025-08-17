package com.tangerine.api.item.service

import com.tangerine.api.item.domain.Item
import com.tangerine.api.item.mapper.toDomains
import com.tangerine.api.item.repository.ItemRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ItemQueryService(
    private val itemRepository: ItemRepository,
) {
    @Transactional(readOnly = true)
    fun findAll(): List<Item> = itemRepository.findAll().toDomains()
}
