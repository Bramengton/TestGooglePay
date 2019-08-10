/*
 * Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.gms.samples.wallet;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.wallet.PaymentData;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Checkout implementation for the app
 */
public class CheckoutActivity extends Activity {
  /**
   * A client for interacting with the Google Pay API.
   *
   * @see <a
   *     href="https://developers.google.com/android/reference/com/google/android/gms/wallet/PaymentsClient">PaymentsClient</a>
   */
  private GooglePayments mPaymentsClient;

  /**
   * A Google Pay payment button presented to the viewer for interaction.
   *
   * @see <a href="https://developers.google.com/pay/api/android/guides/brand-guidelines">Google Pay
   *     payment button brand guidelines</a>
   */
  private View mGooglePayButton;

  /**
   * Arbitrarily-picked constant integer you define to track a request for payment data activity.
   *
   * @value #LOAD_PAYMENT_DATA_REQUEST_CODE
   */
  private static final int LOAD_PAYMENT_DATA_REQUEST_CODE = 991;

  private TextView mGooglePayStatusText;

  private ItemInfo mBikeItem = new ItemInfo("Simple Bike", "30.00", R.drawable.bike);

  /**
   * Initialize the Google Pay API on creation of the activity
   *
   * @see Activity#onCreate(android.os.Bundle)
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_checkout);

      initItemUI();

      mGooglePayButton = findViewById(R.id.googlepay_button);
      mGooglePayStatusText = findViewById(R.id.googlepay_status);

      mPaymentsClient = new GooglePayments(this, PayUtils.TypeEnvironment.TEST);
      mPaymentsClient.addMerchant("TaxiAdminTest");
      mPaymentsClient.addCard(PayUtils.Cards.MASTERCARD, PayUtils.Cards.VISA);
      mPaymentsClient.addPayMethods(PayUtils.PayMethod.PAN_ONLY, PayUtils.PayMethod.CRYPTOGRAM_3DS);
      mPaymentsClient.addParameter("gateway", "portmonecom");
      mPaymentsClient.addParameter("gatewayMerchantId", "1185");

      mPaymentsClient.getIsReadyToPayRequest(new GooglePayments.OnPaymentResult() {
          @Override
          public void onReadyToPay(boolean ready) {
              setGooglePayAvailable(ready);
          }

          @Override
          public void onSuccess(PaymentData paymentData) {
              handlePaymentSuccess(paymentData);
          }

          @Override
          public void onCancel() {

          }

          @Override
          public void onError(String message, int code) {

          }
      });

      mGooglePayButton.setOnClickListener( new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              mGooglePayButton.setClickable(false);
              try {
                  mPaymentsClient.getPaymentRequest(CheckoutActivity.this);
              } catch (Exception e) {
                  e.printStackTrace();
              }
          }
      });
  }


  private void setGooglePayAvailable(boolean available) {
      if (available) {
          mGooglePayStatusText.setVisibility(View.GONE);
          mGooglePayButton.setVisibility(View.VISIBLE);
      } else {
          mGooglePayStatusText.setText(R.string.googlepay_status_unavailable);
      }
  }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(mPaymentsClient.onActivityResult(requestCode, resultCode, data))
            mGooglePayButton.setClickable(true);
    }


  private void handlePaymentSuccess(PaymentData paymentData) {
    String paymentInformation = paymentData.toJson();

    // Token will be null if PaymentDataRequest was not constructed using fromJson(String).
    if (paymentInformation == null) {
      return;
    }
    JSONObject paymentMethodData;

    try {
      paymentMethodData = new JSONObject(paymentInformation).getJSONObject("paymentMethodData");
      Log.d("GooglePaymentToken", paymentMethodData.getJSONObject("tokenizationData").getString("token"));
    } catch (JSONException e) {
      Log.e("handlePaymentSuccess", "Error: " + e.toString());
    }
  }

  private void initItemUI() {
    TextView itemName = findViewById(R.id.text_item_name);
    ImageView itemImage = findViewById(R.id.image_item_image);
    TextView itemPrice = findViewById(R.id.text_item_price);

    itemName.setText(mBikeItem.getName());
    itemImage.setImageResource(mBikeItem.getImageResourceId());
    itemPrice.setText(mBikeItem.getPrice());
  }
}
