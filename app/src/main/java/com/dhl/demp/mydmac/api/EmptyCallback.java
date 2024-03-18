package com.dhl.demp.mydmac.api;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmptyCallback<T> implements Callback<T> {
    @Override
    public void onResponse(Call<T> call, Response<T> response) {
        //Do nothing
    }

    @Override
    public void onFailure(Call<T> call, Throwable t) {
        //Do nothing
    }
}
