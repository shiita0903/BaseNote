package jp.shiita.basenote.notes

import android.arch.lifecycle.ViewModel

class NotesViewModel : ViewModel() {

}

//class MainViewModel @Inject constructor(private val repository: GitHubRepository) : ViewModel() {
//
//    val ownerId = MutableLiveData<String>()
//    val repos: LiveData<List<Repo>>
//
//    init {
//        repos = ownerId.switchMap {
//            if (it.isEmpty()) AbsentLiveData.create()
//            else repository.loadRepos(it)
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .onErrorResumeNext(Flowable.empty())
//                    .toLiveData()
//        }
//    }
//
//}