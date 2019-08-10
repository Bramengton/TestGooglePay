package com.google.android.gms.samples.wallet;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wallet.AutoResolveHelper;
import com.google.android.gms.wallet.IsReadyToPayRequest;
import com.google.android.gms.wallet.PaymentData;
import com.google.android.gms.wallet.PaymentDataRequest;
import com.google.android.gms.wallet.PaymentsClient;
import com.google.android.gms.wallet.Wallet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Optional;

/**
 * @author Kisarinov Konstantin on 30.04.19.
 */
public class GooglePayments extends PayUtils {

    interface OnPaymentResult{
        void onReadyToPay(boolean ready);
        void onSuccess(PaymentData paymentData);
        void onCancel();
        void onError(String message, int code);
    }

    private OnPaymentResult mOnPaymentResult;

    private GPayParams mGPayParams = new GPayParams();
    private PaymentsClient mPaymentsClient;
    private static final int LOAD_PAYMENT_DATA_REQUEST_CODE = 248;

    public GooglePayments(Activity activity, TypeEnvironment environment){
        Wallet.WalletOptions options = new Wallet.WalletOptions.Builder()
                .setEnvironment(environment.getEnvironment())
                .build();
        mPaymentsClient = Wallet.getPaymentsClient(activity, options);
    }

    public GooglePayments addMerchant(String name){
        mGPayParams.addMerchant(name);
        return this;
    }

    public GooglePayments addParameter(String name, String value){
        mGPayParams.addParameter(name, value);
        return this;
    }

    public GooglePayments addCard(Cards... card){
        mGPayParams.addCard(card);
        return this;
    }

    public GooglePayments addPayMethods(PayMethod... methods){
        mGPayParams.addPayMethod(methods);
        return this;
    }

    public GooglePayments setTotalPrice(String val, TypeCurrency currency){
        mGPayParams.setTotalPrice(val, currency);
        return this;
    }

    private JSONObject getBaseRequest() throws JSONException {
        return new JSONObject().put("apiVersion", 2).put("apiVersionMinor", 0);
    }

    private JSONObject getGatewayTokenizationSpecification() throws JSONException {
        JSONObject addGateway = new JSONObject();
        for(String key : mGPayParams.$tokenParameters.keySet()){
            String val = mGPayParams.$tokenParameters.get(key);
            if(val!=null) addGateway.put(key, val);
        }
        return new JSONObject().put("type", "PAYMENT_GATEWAY").put("parameters", addGateway);
    }

    private JSONArray getAllowedCardNetworks() {
        JSONArray allowed_cards = new JSONArray();
        for(Cards cards : mGPayParams.$cards){
            allowed_cards.put(cards.getCardNetwork());
        }
        return allowed_cards;
    }

    private JSONArray getAllowedCardAuthMethods() {
        JSONArray allowed_pay_methods = new JSONArray();
        for(PayMethod method : mGPayParams.$methods){
            allowed_pay_methods.put(method.getPayMethod());
        }
        return allowed_pay_methods;
    }

    private JSONObject getBaseCardPaymentMethod() throws JSONException {
        JSONObject cardPaymentMethod = new JSONObject();
        cardPaymentMethod.put("type", "CARD");

        JSONObject parameters = new JSONObject();
        parameters.put("allowedAuthMethods", getAllowedCardAuthMethods());
        parameters.put("allowedCardNetworks", getAllowedCardNetworks());
        cardPaymentMethod.put("parameters", parameters);

        return cardPaymentMethod;
    }

    private JSONObject getCardPaymentMethod() throws JSONException {
        JSONObject cardPaymentMethod = getBaseCardPaymentMethod();
        cardPaymentMethod.put("tokenizationSpecification", getGatewayTokenizationSpecification());
        return cardPaymentMethod;
    }

    private JSONObject getTransactionInfo() throws JSONException {
        JSONObject transactionInfo = new JSONObject();
        transactionInfo.put("totalPrice", mGPayParams.$price);
        transactionInfo.put("totalPriceStatus", "FINAL");
        transactionInfo.put("currencyCode", mGPayParams.$currency);
        return transactionInfo;
    }

    private JSONObject getMerchantInfo() throws JSONException {
        return new JSONObject().put("merchantName", mGPayParams.$merchantName);
    }

    private Optional<JSONObject> getPaymentDataRequest() {
        try {
            JSONObject paymentDataRequest = getBaseRequest();
            paymentDataRequest.put("allowedPaymentMethods", new JSONArray().put(getCardPaymentMethod()));
            paymentDataRequest.put("transactionInfo", getTransactionInfo());
            paymentDataRequest.put("merchantInfo", getMerchantInfo());
            paymentDataRequest.put("shippingAddressRequired", false);

            JSONObject shippingAddressParameters = new JSONObject().put("phoneNumberRequired", false);

            paymentDataRequest.put("shippingAddressParameters", shippingAddressParameters);
            return Optional.of(paymentDataRequest);
        } catch (JSONException e) {
            return Optional.empty();
        }
    }

    private Optional<JSONObject> isReadyToPayRequest() {
        try {
            JSONObject isReadyToPayRequest = getBaseRequest();
            isReadyToPayRequest.put("allowedPaymentMethods", new JSONArray().put(getBaseCardPaymentMethod()));
            return Optional.of(isReadyToPayRequest);
        } catch (JSONException e) {
            return Optional.empty();
        }
    }

    public void getIsReadyToPayRequest(final OnPaymentResult listener){
        this.mOnPaymentResult = listener;
        if(mGPayParams.isEmpty()) mOnPaymentResult.onError("Payment info not found!", -1);
        final Optional<JSONObject> isReadyToPayJson = isReadyToPayRequest();
        if (!isReadyToPayJson.isPresent()) return;

        IsReadyToPayRequest request = IsReadyToPayRequest.fromJson(isReadyToPayJson.get().toString());
        if (request == null || mPaymentsClient==null) return;

        Task<Boolean> task = mPaymentsClient.isReadyToPay(request);
        task.addOnCompleteListener(new OnCompleteListener<Boolean>() {
            @Override
            public void onComplete(@NonNull Task<Boolean> task) {
                if(mOnPaymentResult!=null) mOnPaymentResult.onReadyToPay(task.isSuccessful());
            }
        });
    }

    public void getPaymentRequest(Activity activity) throws Exception{
        if(mGPayParams.isEmpty()) throw new Exception("Payment info not found!");
        Optional<JSONObject> paymentDataRequestJson = getPaymentDataRequest();
        if (!paymentDataRequestJson.isPresent()) return;
        PaymentDataRequest request = PaymentDataRequest.fromJson(paymentDataRequestJson.get().toString());
        if (request!= null && mPaymentsClient!=null) {
            Task<PaymentData> task = mPaymentsClient.loadPaymentData(request);
            AutoResolveHelper.resolveTask(task, activity, LOAD_PAYMENT_DATA_REQUEST_CODE);
        }
    }


    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        boolean check = requestCode == LOAD_PAYMENT_DATA_REQUEST_CODE && mOnPaymentResult!=null;
        if (check) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    PaymentData paymentData = PaymentData.getFromIntent(data);
                    mOnPaymentResult.onSuccess(paymentData);
                    break;
                case Activity.RESULT_CANCELED:
                    mOnPaymentResult.onCancel();
                    break;
                case AutoResolveHelper.RESULT_ERROR:
                    String message = "Result status is Error.";
                    Status status = AutoResolveHelper.getStatusFromIntent(data);
                    if(status!=null){
                        message = status.getStatusMessage()!=null ? status.getStatusMessage() : message;
                        mOnPaymentResult.onError(message, status.getStatusCode());
                    }else{
                        mOnPaymentResult.onError(message, -1);
                    }
                    break;
            }
        }
        return check;
    }


}
