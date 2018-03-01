package com.app.android.ui.main

import android.support.v7.util.DiffUtil
import com.app.android.data.model.Task
import com.app.android.data.source.TaskRepository
import com.app.android.ui.base.Diff
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

/**
 *
 * @author at-vinhhuynh
 */
class MainViewModel(private val taskRepository: TaskRepository) {
    internal var progressBarStatus = BehaviorSubject.create<Boolean>()
    internal val updateListTask = PublishSubject.create<DiffUtil.DiffResult>()
    internal var tasks = mutableListOf<Task>()

    internal fun getTasks() = taskRepository.getListTask()
            .doOnSubscribe {
                progressBarStatus.onNext(true)
            }
            .doFinally {
                progressBarStatus.onNext(false)
            }
            .onErrorReturn {
                listOf()
            }
            .flatMap {
                Observable.create { e: ObservableEmitter<Task> ->
                    val diff = Diff(tasks, it)
                            .areItemsTheSame { oldItem, newItem ->
                                oldItem.id == newItem.id
                            }
                            .areContentsTheSame { oldItem, newItem ->
                                oldItem.title == newItem.title
                                oldItem.description == newItem.description
                            }
                            .calculateDiff()

                    tasks.clear()
                    tasks.addAll(it)
                    updateListTask.onNext(diff)
                    e.onComplete()
                }
            }
}