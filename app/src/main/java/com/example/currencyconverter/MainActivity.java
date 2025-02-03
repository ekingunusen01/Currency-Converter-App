
package com.example.currencyconverter;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private Spinner spinnerFromCurrency, spinnerToCurrency;
    private EditText conversionAmount;
    private TextView resultView;
    private Button buttonConvert;
    private Map<String, Double> currencyRates = new HashMap<>();
    private String apiURLstring =
            "https://api.currencyfreaks.com/v2.0/rates/latest?apikey=" +
                    "4db5e4e927844d82b786c6bbff01040b&format=xml";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        setupUI();
        new FetchCurrencyRatesTask().execute(apiURLstring);
    }

    // method to initialize the ui elements
    private void setupUI(){
        spinnerFromCurrency = findViewById(R.id.spinnerFromCurrency);
        spinnerToCurrency = findViewById(R.id.spinnerToCurrency);
        conversionAmount = findViewById(R.id.inputText);
        resultView = findViewById(R.id.outputView);
        buttonConvert = findViewById(R.id.convertButton);

        buttonConvert.setOnClickListener(v -> performConversion());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // async task to fetch currency rates from the API
    private class FetchCurrencyRatesTask extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... urls){
            try{
                return handleAPI(urls[0]);      // fetch data from API
            } catch (IOException e){
                return "abc";
            }
        }

        // after fetching the data, parse the xml response
        protected void onPostExecute(String result){
            try {
                parseXML(result);
                setupSpinners();
            } catch (XmlPullParserException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // method to handle API request to fetch data
        private String handleAPI(String apiUrl) throws IOException {
            InputStream inputStream = null;
            String result = "";

            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                inputStream = conn.getInputStream();
                result = convertInputStreamToString(inputStream);
            } else {
                result = null;
            }
            return result;
        }

        // method to convert inputStream data to string
        private String convertInputStreamToString(InputStream inputStream) throws IOException{
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line = "";
            String result = "";
            while ((line = bufferedReader.readLine()) != null){
                result += line;
            }
            inputStream.close();
            return result;
        }
    }

    // method to parse the XML data to extract currency rates
    public void parseXML(String xmlData) throws XmlPullParserException, IOException{
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser xpp = factory.newPullParser();
        xpp.setInput(new StringReader(xmlData));

        String currentTag = null;
        String currency = null;
        double rate = 0;

        try {
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        currentTag = xpp.getName();
                        // check if current tag equals to desired currency
                        if (       "USD".equals(currentTag)
                                || "EUR".equals(currentTag)
                                || "TRY".equals(currentTag)
                                || "GBP".equals(currentTag)
                                || "CHF".equals(currentTag)
                                || "NOK".equals(currentTag)
                                || "SAR".equals(currentTag)
                                || "JPY".equals(currentTag)
                                || "RUB".equals(currentTag)
                                || "AED".equals(currentTag)
                                || "AUD".equals(currentTag)
                                || "CAD".equals(currentTag)
                                || "BTC".equals(currentTag)
                                || "ETH".equals(currentTag)
                        ) {
                            currency = currentTag.toString();
                        }
                        break;

                    case XmlPullParser.TEXT:
                        if (currency != null && (
                                           "USD".equals(currentTag)
                                        || "EUR".equals(currentTag)
                                        || "TRY".equals(currentTag)
                                        || "GBP".equals(currentTag)
                                        || "CHF".equals(currentTag)
                                        || "NOK".equals(currentTag)
                                        || "SAR".equals(currentTag)
                                        || "JPY".equals(currentTag)
                                        || "RUB".equals(currentTag)
                                        || "AED".equals(currentTag)
                                        || "AUD".equals(currentTag)
                                        || "CAD".equals(currentTag)
                                        || "BTC".equals(currentTag)
                                        || "ETH".equals(currentTag)
                        )) {
                            rate = Double.parseDouble(xpp.getText());
                            currencyRates.put(currency, rate);    // store the currency and its rate
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                }
                eventType = xpp.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // method to setup the spinners with the currencyList and images
    private void setupSpinners() {
        List<String> currencyList = new ArrayList<>(currencyRates.keySet());

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_item, R.id.currencyName, currencyList) {
            private Map<String, Integer> currencyIcons = new HashMap<>();

            {
                currencyIcons.put("USD", R.drawable.usd);
                currencyIcons.put("EUR", R.drawable.eur);
                currencyIcons.put("TRY", R.drawable.tr);
                currencyIcons.put("GBP", R.drawable.gbp);
                currencyIcons.put("CHF", R.drawable.chf);
                currencyIcons.put("NOK", R.drawable.nok);
                currencyIcons.put("SAR", R.drawable.sar);
                currencyIcons.put("JPY", R.drawable.jpy);
                currencyIcons.put("RUB", R.drawable.rub);
                currencyIcons.put("AED", R.drawable.aed);
                currencyIcons.put("AUD", R.drawable.aud);
                currencyIcons.put("CAD", R.drawable.cad);
                currencyIcons.put("BTC", R.drawable.btc);
                currencyIcons.put("ETH", R.drawable.eth);
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                return createCustomView(position, convertView, parent);
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                return createCustomView(position, convertView, parent);
            }

            private View createCustomView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.spinner_item, parent, false);
                }

                ImageView icon = convertView.findViewById(R.id.currencyIcon);
                TextView name = convertView.findViewById(R.id.currencyName);

                String currency = getItem(position);
                name.setText(currency);
                icon.setImageResource(currencyIcons.getOrDefault(currency, R.drawable.default_icon));

                return convertView;
            }
        };

        spinnerFromCurrency.setAdapter(adapter);
        spinnerToCurrency.setAdapter(adapter);
    }

    private void performConversion() {
        String fromCurrency = (String) spinnerFromCurrency.getSelectedItem();
        String toCurrency = (String) spinnerToCurrency.getSelectedItem();
        String amountOfConversion = conversionAmount.getText().toString();

        try {
            double amount = Double.parseDouble(amountOfConversion);
            Double fromRate = currencyRates.get(fromCurrency);
            Double toRate = currencyRates.get(toCurrency);

            double conversionResult = amount * (toRate / fromRate);

            resultView.setText(String.format("%.2f", conversionResult));
        } catch (NumberFormatException e) {
            throw new RuntimeException(e);
        }
    }
}

