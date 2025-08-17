package com.tangerine.api.item.controller

import com.tangerine.api.item.api.response.ItemResponses
import com.tangerine.api.item.mapper.toResponses
import com.tangerine.api.item.service.ItemQueryService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/items")
class ItemController(
    private val itemQueryService: ItemQueryService,
) {
    @GetMapping
    fun findAll(): ItemResponses = ItemResponses(itemQueryService.findAll().toResponses())
}
