package com.dhl.demp.mydmac.api.listener;

import androidx.annotation.Nullable;

import com.dhl.demp.mydmac.api.ApiFactory;
import com.dhl.demp.mydmac.api.response.RegistrationErrorResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class RegistrationCallback<T extends Statefull> implements Callback<T> {
    private final int state;

    public RegistrationCallback(int state) {
        this.state = state;
    }

    @Override
    public void onResponse(Call<T> call, Response<T> response) {
        if (response.isSuccessful()) {
            T registrationResponse = response.body();
            if (registrationResponse != null && (!registrationResponse.checkState() || state == registrationResponse.getState())) {
                onSuccess(registrationResponse);
            }
        } else {
            try {
                RegistrationErrorResponse errorResponse = ApiFactory.gson.fromJson(response.errorBody().string(), RegistrationErrorResponse.class);
                onError(errorResponse);
            } catch (Exception e) {
                onError(null);
            }
        }
    }

    protected abstract void onSuccess(T response);
    protected abstract void onError(@Nullable RegistrationErrorResponse error);
}
