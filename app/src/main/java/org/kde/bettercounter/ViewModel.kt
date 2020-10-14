package org.kde.bettercounter

import android.app.Application
import android.content.Context
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.kde.bettercounter.boilerplate.AppDatabase
import org.kde.bettercounter.persistence.Counter
import org.kde.bettercounter.persistence.Repository

class ViewModel(application: Application) : AndroidViewModel(application) {

    private val repo : Repository
    private val addCounterObservers = HashMap<LifecycleOwner, Observer<String>>()
    private val counterMap = HashMap<String, MutableLiveData<Counter>>()

    init {
        val db  = AppDatabase.getInstance(application)
        val prefs =  application.getSharedPreferences("prefs", Context.MODE_PRIVATE)
        repo = Repository(db.entryDao(), prefs)
        viewModelScope.launch(Dispatchers.IO) {
            for (name in repo.getCounterList()) {
                counterMap[name] = MutableLiveData(repo.getCounter(name)) // cache it
                for ((_, observer) in addCounterObservers) {
                    observer.onChanged(name)
                }
            }
        }
    }

    fun saveCounterOrder(value : List<String>) = repo.setCounterList(value)

    fun addCounter(name : String) {
        repo.setCounterList(repo.getCounterList().toMutableList() + name)
        viewModelScope.launch(Dispatchers.IO) {
            for ((_, observer) in addCounterObservers) {
                counterMap[name] = MutableLiveData(repo.getCounter(name)) // cache it
                observer.onChanged(name)
            }
        }
    }

    fun observeNewCounter(owner : LifecycleOwner, observer: Observer<String>) {
        addCounterObservers[owner] = observer
        for (name in counterMap.keys) { //notify the ones we already have
            observer.onChanged(name)
        }
    }

    fun renameCounter(oldName : String, newName : String) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.renameCounter(oldName, newName)
            var counter = counterMap.remove(oldName)
            if (counter != null) {
                counterMap[newName] = counter
                counter.postValue(repo.getCounter(newName))
            }
        }
    }

    fun incrementCounter(name : String) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.addEntry(name)
            counterMap[name]?.postValue(repo.getCounter(name))
        }
    }

    fun decrementCounter(name : String) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.removeEntry(name)
            counterMap[name]?.postValue(repo.getCounter(name))
        }
    }

    fun getCounter(name : String) : LiveData<Counter> {
        return counterMap[name]!!
    }

    fun counterExists(name: String): Boolean = repo.getCounterList().contains(name)

    fun deleteCounter(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.removeAllEntries(name)
        }
    }

}