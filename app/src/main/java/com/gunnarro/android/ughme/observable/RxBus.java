package com.gunnarro.android.ughme.observable;

import android.util.Log;

import com.gunnarro.android.ughme.utility.Utility;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

/**
 * used to communicate between fragments
 */
public class RxBus {
    private static RxBus instance;
    private final PublishSubject<Object> publisher = PublishSubject.create();

    private RxBus() {
    }

    public static RxBus getInstance() {
        if (instance == null) {
            Log.d(Utility.buildTag(RxBus.class, "getInstance"), "init RxBus instance");
            instance = new RxBus();
        }
        return instance;
    }

    public void publish(Object data) {
        if (data != null) {
            Log.d(Utility.buildTag(getClass(), "publish"), String.format("new data object, type:  %s", data.getClass().getSimpleName()));
            publisher.onNext(data);
        } else {
            Log.w(Utility.buildTag(getClass(), "publish"), "do not publish data objects which is equal to null");
        }
    }

    // Listen should return an Observable
    public Observable<Object> listen() {
        return publisher;
    }
}
