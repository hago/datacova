/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.execution

import com.hagoapp.datacova.CoVaLogger
import com.hagoapp.datacova.config.CoVaConfig
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.locks.ReentrantReadWriteLock

class Service private constructor() {

    companion object {

        private const val EXECUTION_QUERY_INTERVAL = 500L
        private const val LOADING_TASk_EXECUTION_BATCH = 10
        private const val SERVICE_STOP_MAX_ATTEMPT = 10
        private var executor: Service? = null

        @Synchronized
        fun getExecutor(): Service {
            if (executor == null) {
                executor = Service()
            }
            return executor!!
        }
    }

    private val logger = CoVaLogger.getLogger()
    private val config = CoVaConfig.getConfig()
    private var exitFlag = false
    private var serviceStopped = true
    private var workerCount: Int = 0
    private var counterLock = ReentrantReadWriteLock()

    fun startExecutionService() {
        logger.info("Start Execution Service")
        startExecutionWatcher()
        logger.info("Execution Service started")
    }

    fun stopExecutionService() {
        logger.info("Stop Execution Service")
        exitFlag = true
        for (i in 0 until SERVICE_STOP_MAX_ATTEMPT) {
            if (serviceStopped) {
                break
            }
            System.err.println("waiting for Execution Service to stop")
            Thread.sleep(2000)
        }
        logger.info("Execution Service Stopped")
    }

    private fun startExecutionWatcher() {
        serviceStopped = false
        val loader = TaskExecutionLoader()
        GlobalScope.launch {
            while (!exitFlag) {
                if (getWorkerCount() >= config.task.worker_count) {
                    logger.debug("Worker amount exceeds upper limit ${config.task.worker_count}")
                    Thread.sleep(EXECUTION_QUERY_INTERVAL)
                    continue
                }
                val te = loader.loadNextTaskExecution()
                if (te == null) {
                    logger.debug("No more task execution, take a break")
                    Thread.sleep(EXECUTION_QUERY_INTERVAL)
                    continue
                }
                runExecution(te)
            }
        }
    }

    private fun runExecution(te: TaskExecution) {
        val t = Thread(Runnable {
            try {
                val worker = ExecutionWorker(te)
                worker.execute()
            } catch (ex: Exception) {
                logger.error("Error occurs in task execution ${te.id}")
            } finally {
                decreaseWorker()
            }
        })
        t.isDaemon = true
        t.start()
        increaseWorker()
    }

    private inner class TaskExecutionLoader {

        private val queue = ConcurrentLinkedQueue<TaskExecution>()

        fun loadNextTaskExecution(): TaskExecution? {
            val dal = WorkSpaceTaskData()
            return queue.poll()
                ?: try {
                    val l = dal.getTaskExecutions(ExecutionStatus.added, count = LOADING_TASk_EXECUTION_BATCH)
                    if (l.isNotEmpty()) {
                        queue.addAll(l.takeLast(l.size - 1))
                        l[0]
                    } else null
                } catch (ex: CoDiVaException) {
                    val id = ex.message!!.toInt()
                    val result = ExecutionDetail()
                    result.addError(ex)
                    this@ExecutionService.logger.debug("Loading of task execution $id failed: $ex")
                    dal.completeTaskExecution(id, result)
                    null
                }
        }
    }

    private fun increaseWorker() {
        counterLock.write {
            workerCount++
        }
        logger.debug("a worker started, all $workerCount workers now!")
    }

    private fun decreaseWorker() {
        counterLock.write {
            workerCount--
        }
        logger.debug("a worker finished, all $workerCount workers now!")
    }

    private fun getWorkerCount(): Int {
        counterLock.read {
            return workerCount
        }
    }
}
