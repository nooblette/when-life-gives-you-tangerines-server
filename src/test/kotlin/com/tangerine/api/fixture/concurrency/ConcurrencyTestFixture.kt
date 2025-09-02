package com.tangerine.api.fixture.concurrency

import mu.KotlinLogging
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.TimeUnit

private val logger = KotlinLogging.logger {}

fun <T, R> submitConcurrencyTask(
    task: (T) -> R,
    request: T,
    threadCount: Int,
): List<TaskResult> =
    (1..threadCount)
        .map {
            CompletableFuture.supplyAsync {
                val commonPool = ForkJoinPool.commonPool()

                logger.debug(
                    """Thread Pool 정보 - 현재 스레드 : ${Thread.currentThread().name}
                        "병렬 처리 레벨: ${commonPool.parallelism}"
                        "활성 스레드 수: ${commonPool.activeThreadCount}"
                        "실행 중인 스레드 수: ${commonPool.runningThreadCount}"
                        "대기 중인 작업 수: ${commonPool.queuedSubmissionCount}"
                        "총 풀 크기: ${commonPool.poolSize}"
                    """,
                )

                runTask(index = it, task = task, request = request)
            }
        }.map { it.get(20, TimeUnit.SECONDS) }

private fun <T, R> runTask(
    index: Int,
    task: (T) -> R,
    request: T,
): TaskResult =
    try {
        val result = task(request)
        TaskResult.Success(index, result)
    } catch (e: Exception) {
        TaskResult.Failure(index, e)
    }

sealed class TaskResult {
    data class Success<R>(
        val threadIndex: Int,
        val result: R,
    ) : TaskResult()

    data class Failure(
        val threadIndex: Int,
        val exception: Exception,
    ) : TaskResult()
}
