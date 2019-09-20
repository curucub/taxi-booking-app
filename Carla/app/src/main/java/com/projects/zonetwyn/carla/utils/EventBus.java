package com.projects.zonetwyn.carla.utils;

import android.annotation.SuppressLint;
import android.support.annotation.IntDef;
import android.util.SparseArray;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.PublishSubject;

public class EventBus {

    private static SparseArray<PublishSubject<Object>> subjectsMap = new SparseArray<>();
    private static Map<Object, CompositeDisposable>  subscriptionsMap = new HashMap<>();

    public static final int SUBJECT_SIGN_IN = 0;
    public static final int SUBJECT_SIGN_UP = 1;

    public static final int SUBJECT_SEARCH = 2;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({SUBJECT_SIGN_IN, SUBJECT_SIGN_UP, SUBJECT_SEARCH})
    @interface Subject {
    }

    @SuppressLint("CheckResult")
    private static PublishSubject<Object> getSubject(@Subject int subjectCode) {
        PublishSubject<Object> subject = subjectsMap.get(subjectCode);
        if (subject == null) {
            subject = PublishSubject.create();
            subject.subscribeOn(AndroidSchedulers.mainThread());
            subjectsMap.put(subjectCode, subject);
        }
        return subject;
    }

    private static CompositeDisposable getCompositeDisposable(Object object) {
        CompositeDisposable compositeDisposable = subscriptionsMap.get(object);
        if (compositeDisposable == null) {
            compositeDisposable = new CompositeDisposable();
            subscriptionsMap.put(object, compositeDisposable);
        }

        return compositeDisposable;
    }

    public static void subscribe(@Subject int subject, Object lifecycle, Consumer<Object> action) {
        Disposable disposable = getSubject(subject).subscribe(action);
        getCompositeDisposable(lifecycle).add(disposable);
    }

    public static void unregister(Object lifecycle) {
        CompositeDisposable compositeDisposable = subscriptionsMap.remove(lifecycle);
        if (compositeDisposable != null) {
            compositeDisposable.dispose();
        }
    }

    public static void publish(@Subject int subject, Object message) {
        getSubject(subject).onNext(message);
    }
}

