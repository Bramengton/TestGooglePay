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

import com.google.android.gms.wallet.PaymentsClient;
import com.google.android.gms.wallet.Wallet;
import com.google.android.gms.wallet.WalletConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Contains helper static methods for dealing with the Payments API.
 *
 * <p>Many of the parameters used in the code are optional and are set here merely to call out their
 * existence. Please consult the documentation to learn more and feel free to remove ones not
 * relevant to your implementation.
 */
public class PaymentsUtil {
  private PaymentsUtil() {}

    public static final List<String> SUPPORTED_NETWORKS = Arrays.asList( "AMEX", "DISCOVER", "JCB", "MASTERCARD", "VISA");
    public static final List<String> SUPPORTED_METHODS = Arrays.asList("PAN_ONLY", "CRYPTOGRAM_3DS");

    public static final String CURRENCY_CODE = "USD";

  /**
   * Create a Google Pay API base request object with properties used in all requests.
   *
   * @return Google Pay API base request object.
   * @throws JSONException
   */
  private static JSONObject getBaseRequest() throws JSONException {
    return new JSONObject().put("apiVersion", 2).put("apiVersionMinor", 0);
  }

  public static PaymentsClient createPaymentsClient(Activity activity) {
    Wallet.WalletOptions walletOptions = new Wallet.WalletOptions.Builder().setEnvironment(WalletConstants.ENVIRONMENT_TEST).build();
    return Wallet.getPaymentsClient(activity, walletOptions);
  }

  /**
   * Gateway Integration: Identify your gateway and your app's gateway merchant identifier.
   *
   * <p>The Google Pay API response will return an encrypted payment method capable of being charged
   * by a supported gateway after payer authorization.
   *
   * <p>TODO: Check with your gateway on the parameters to pass and modify them in Constants.java.
   *
   * @return Payment data tokenization for the CARD payment method.
   * @throws JSONException
   * @see <a href=
   *     "https://developers.google.com/pay/api/android/reference/object#PaymentMethodTokenizationSpecification">PaymentMethodTokenizationSpecification</a>
   */
  private static JSONObject getGatewayTokenizationSpecification() throws JSONException {
      JSONObject addGateway = new JSONObject().put("gateway", "example").put("gatewayMerchantId", "exampleGatewayMerchantId");
      return new JSONObject().put("type", "PAYMENT_GATEWAY").put("parameters", addGateway);
  }

  /**
   * Card networks supported by your app and your gateway.
   *
   * <p>TODO: Confirm card networks supported by your app and gateway & update in Constants.java.
   *
   * @return Allowed card networks
   * @see <a
   *     href="https://developers.google.com/pay/api/android/reference/object#CardParameters">CardParameters</a>
   */
  private static JSONArray getAllowedCardNetworks() {
    return new JSONArray(SUPPORTED_NETWORKS);
  }

  /**
   * Card authentication methods supported by your app and your gateway.
   *
   * <p>TODO: Confirm your processor supports Android device tokens on your supported card networks
   * and make updates in Constants.java.
   *
   * @return Allowed card authentication methods.
   * @see <a
   *     href="https://developers.google.com/pay/api/android/reference/object#CardParameters">CardParameters</a>
   */
  private static JSONArray getAllowedCardAuthMethods() {
    return new JSONArray(SUPPORTED_METHODS);
  }

  /**
   * Describe your app's support for the CARD payment method.
   *
   * <p>The provided properties are applicable to both an IsReadyToPayRequest and a
   * PaymentDataRequest.
   *
   * @return A CARD PaymentMethod object describing accepted cards.
   * @throws JSONException
   * @see <a
   *     href="https://developers.google.com/pay/api/android/reference/object#PaymentMethod">PaymentMethod</a>
   */
  private static JSONObject getBaseCardPaymentMethod() throws JSONException {
    JSONObject cardPaymentMethod = new JSONObject();
    cardPaymentMethod.put("type", "CARD");

    JSONObject parameters = new JSONObject();
    parameters.put("allowedAuthMethods", getAllowedCardAuthMethods());
    parameters.put("allowedCardNetworks", getAllowedCardNetworks());
    cardPaymentMethod.put("parameters", parameters);

    return cardPaymentMethod;
  }

  /**
   * Describe the expected returned payment data for the CARD payment method
   *
   * @return A CARD PaymentMethod describing accepted cards and optional fields.
   * @throws JSONException
   * @see <a
   *     href="https://developers.google.com/pay/api/android/reference/object#PaymentMethod">PaymentMethod</a>
   */
  private JSONObject getCardPaymentMethod() throws JSONException {
    JSONObject cardPaymentMethod = getBaseCardPaymentMethod();
    cardPaymentMethod.put("tokenizationSpecification", getGatewayTokenizationSpecification());
    return cardPaymentMethod;
  }

  /**
   * An object describing accepted forms of payment by your app, used to determine a viewer's
   * readiness to pay.
   *
   * @return API version and payment methods supported by the app.
   * @see <a
   *     href="https://developers.google.com/pay/api/android/reference/object#IsReadyToPayRequest">IsReadyToPayRequest</a>
   */
  public static Optional<JSONObject> getIsReadyToPayRequest() {
    try {
      JSONObject isReadyToPayRequest = getBaseRequest();
      isReadyToPayRequest.put("allowedPaymentMethods", new JSONArray().put(getBaseCardPaymentMethod()));

      return Optional.of(isReadyToPayRequest);
    } catch (JSONException e) {
      return Optional.empty();
    }
  }

  /**
   * Provide Google Pay API with a payment amount, currency, and amount status.
   *
   * @return information about the requested payment.
   * @throws JSONException
   * @see <a
   *     href="https://developers.google.com/pay/api/android/reference/object#TransactionInfo">TransactionInfo</a>
   */
  private JSONObject getTransactionInfo(String price) throws JSONException {
    JSONObject transactionInfo = new JSONObject();
    transactionInfo.put("totalPrice", price);
    transactionInfo.put("totalPriceStatus", "FINAL");
    transactionInfo.put("currencyCode", CURRENCY_CODE);

    return transactionInfo;
  }

  /**
   * Information about the merchant requesting payment information
   *
   * @return Information about the merchant.
   * @throws JSONException
   * @see <a
   *     href="https://developers.google.com/pay/api/android/reference/object#MerchantInfo">MerchantInfo</a>
   */
  private JSONObject getMerchantInfo() throws JSONException {
    return new JSONObject().put("merchantName", "Example Merchant");
  }

  /**
   * An object describing information requested in a Google Pay payment sheet
   *
   * @return Payment data expected by your app.
   * @see <a
   *     href="https://developers.google.com/pay/api/android/reference/object#PaymentDataRequest">PaymentDataRequest</a>
   */
  public Optional<JSONObject> getPaymentDataRequest(String price) {
    try {
      JSONObject paymentDataRequest = PaymentsUtil.getBaseRequest();
      paymentDataRequest.put("allowedPaymentMethods", new JSONArray().put(getCardPaymentMethod()));
      paymentDataRequest.put("transactionInfo", getTransactionInfo(price));
      paymentDataRequest.put("merchantInfo", getMerchantInfo());

      /* An optional shipping address requirement is a top-level property of the PaymentDataRequest
      JSON object. */
      paymentDataRequest.put("shippingAddressRequired", true);

      JSONObject shippingAddressParameters = new JSONObject().put("phoneNumberRequired", false);

      paymentDataRequest.put("shippingAddressParameters", shippingAddressParameters);
      return Optional.of(paymentDataRequest);
    } catch (JSONException e) {
      return Optional.empty();
    }
  }
}
