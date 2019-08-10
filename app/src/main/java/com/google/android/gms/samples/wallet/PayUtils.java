package com.google.android.gms.samples.wallet;

import com.google.android.gms.wallet.WalletConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author Kisarinov Konstantin on 30.04.19.
 */
public class PayUtils {
    public enum TypeEnvironment{
        TEST, PROD;

        public int getEnvironment(){
            return this==TEST ? WalletConstants.ENVIRONMENT_TEST : WalletConstants.ENVIRONMENT_PRODUCTION;
        }
    }

    public enum Cards{
        AMEX, DISCOVER, JCB, MASTERCARD, VISA, INTERAC;

        public int getCardNetwork(){
            switch (this){
                case AMEX: return WalletConstants.CARD_NETWORK_AMEX;
                case DISCOVER: return WalletConstants.CARD_NETWORK_DISCOVER;
                case JCB: return WalletConstants.CARD_NETWORK_JCB;
                case MASTERCARD: return WalletConstants.CARD_NETWORK_MASTERCARD;
                case VISA: return WalletConstants.CARD_NETWORK_VISA;
                case INTERAC: return WalletConstants.CARD_NETWORK_INTERAC;
            }
            return WalletConstants.CARD_NETWORK_VISA;
        }
    }

    public enum PayMethod{
        CARDS, PAN_ONLY, TOKENIZED_CARD, CRYPTOGRAM_3DS;

        public int getPayMethod(){
            switch(this){
                case CARDS:
                case PAN_ONLY: return WalletConstants.PAYMENT_METHOD_CARD;
                case TOKENIZED_CARD:
                case CRYPTOGRAM_3DS: return WalletConstants.PAYMENT_METHOD_TOKENIZED_CARD;
            }
            return WalletConstants.PAYMENT_METHOD_CARD;
        }
    }

    public enum TypeCurrency{
        AED, ALL, AMD, ANG, AOA, ARS, AUD, AWG, AZN,
        BAM, BBD, BDT, BGN, BHD, BMD, BND, BOB, BRL, BSD, BWP, BYN, BZD,
        CAD, CHF, CLP, CNY, COP, CRC, CUP, CVE, CZK,
        DJF, DKK, DOP, DZD,
        EGP, ETB, EUR,
        FJD, FKP,
        GBP, GEL, GHS, GIP, GMD, GNF, GTQ, GYD,
        HKD, HNL, HRK, HTG, HUF,
        IDR, ILS, INR, ISK,
        JMD, JOD, JPY,
        KES, KGS, KHR, KMF, KRW, KWD, KYD, KZT,
        LAK, LBP, LKR, LYD,
        MAD, MDL, MKD, MMK, MNT, MOP, MRU, MUR, MVR, MWK, MXN, MYR, MZN,
        NAD, NGN, NIO, NOK, NPR, NZD,
        OMR,
        PAB, PEN, PGK, PHP, PKR, PLN, PYG,
        QAR,
        RON, RSD, RUB, RWF,
        SAR, SBD, SCR, SEK, SGD, SHP, SLL, SOS, STN, SVC, SZL,
        THB, TND, TOP, TRY, TTD, TWD, TZS,
        UAH, UGX, USD, UYU, UZS,
        VEF, VND, VUV,
        WST,
        XAF, XCD, XOF, XPF,
        YER, ZAR,
        ZMW
    }



    class GPayParams{
        HashMap<String, String> $tokenParameters = new LinkedHashMap<>();
        String $merchantName;
        List<Cards> $cards = new ArrayList<>();
        List<PayMethod> $methods = new ArrayList<>();
        String $price;
        String $currency;

        void addMerchant(String name){
            $merchantName = name;
        }

        void addParameter(String name, String value){
            $tokenParameters.put(name, value);
        }

        void addCard(Cards... card){
            $cards.addAll(Arrays.asList(card));
        }

        void addPayMethod(PayMethod... methods){
            $methods.addAll(Arrays.asList(methods));
        }

        void setTotalPrice(String val, TypeCurrency currency){
            $price = val;
            $currency = currency.name().toUpperCase();
        }

        boolean isEmpty(){
            return $tokenParameters.isEmpty()
                    || $cards.isEmpty()
                    || $methods.isEmpty()
                    || ($price==null || $price.isEmpty())
                    || ($currency==null || $currency.isEmpty());
        }
    }
}
