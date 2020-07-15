package com.gunnarro.android.ughme.observable;

import android.util.Log;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

public class RxBus {
    private static RxBus mInstance;
    private PublishSubject<Object> publisher = PublishSubject.create();

    private RxBus() {
    }

    public static RxBus getInstance() {
        if (mInstance == null) {
            mInstance = new RxBus();
        }
        return mInstance;
    }

    public void publish(Object data) {
        if (data != null) {
            Log.d("RxBus", String.format("publish: new data object, type:  %s", data.getClass().getSimpleName()));
            publisher.onNext(data);
        } else {
            Log.w("RxBus", "publish: do not publish data objects which is equal to null");
        }
    }

    // Listen should return an Observable
    public Observable<Object> listen() {
        return publisher;
    }
}
