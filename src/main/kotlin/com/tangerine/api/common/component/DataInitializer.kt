package com.tangerine.api.common.component

import com.tangerine.api.item.common.UnitType.KG
import com.tangerine.api.item.entity.ItemEntity
import com.tangerine.api.item.repository.ItemCommandRepository
import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Profile("local")
@Component
class DataInitializer(
    private val itemCommandRepository: ItemCommandRepository,
) {
    @PostConstruct
    fun init() {
        if (itemCommandRepository.count() == 0L) {
            itemCommandRepository.saveAll(
                listOf(
                    ItemEntity(name = "제주 노지 감귤 (10~15개입)", quantity = 10, unit = KG, price = 12000),
                    ItemEntity(name = "제주 노지 감귤 (20~25개입)", quantity = 15, unit = KG, price = 20000),
                ),
            )
        }
    }
}
