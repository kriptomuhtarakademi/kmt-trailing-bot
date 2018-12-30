package com.kmt.trailing.bot;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.account.Account;
import com.binance.api.client.domain.account.NewOrderResponse;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.binance.api.client.domain.account.NewOrder.marketBuy;
import static com.binance.api.client.domain.account.NewOrder.marketSell;

public class Trail {

    public BinanceApiRestClient client;
    String coin;
    Double init_price;
    Double last_price = .0;
    Double current_price = .0;
    Double trigger_price;
    Double stop;
    Double trail_percent;
    Double trail_value;
    Double save_trail_value;
    JTextArea status_text;
    JTextArea info_text;
    int target;
    String amount;

    public Trail(BinanceApiRestClient client,
                 String coin, Double trigger_price, Double stop, Double trail_percent, JTextArea status_text, int target, String amount,
                 JTextArea info_text){
        this.client = client;
        this.coin = coin;
        this.trigger_price = trigger_price;
        this.stop = stop;
        this.trail_percent = trail_percent;
        this.status_text = status_text;
        this.target = target;
        this.amount = amount;
        this.info_text = info_text;

    }

    public String formatDouble(Double d) {
        return String.format(Locale.US,"%.8f", d);
    }

    public String currentTime() {
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date();
        return "" + dateFormat.format(date); // 12:08:43
    }

    /**
     * Create the SwingWorker for trailing
     */
    public SwingWorker createWorker() {
        return new SwingWorker<Void, Double>() {
            @Override
            protected Void doInBackground() throws Exception {

                if(target == 1) {
                    //percent
                    trigger_price = Double.parseDouble(client.getPrice(coin).getPrice()) * ((100+trigger_price)/100);
                }

                info_text.append("\nCOIN: " + coin + "\nTRAIL TETİKLEME FİYATI: " + formatDouble(trigger_price) + "\n");

                while (Double.parseDouble(client.getPrice(coin).getPrice()) < trigger_price) {

                    Double temp_price = Double.parseDouble(client.getPrice(coin).getPrice());
                    String temp_price_formatted = formatDouble(temp_price);

                    if (temp_price <= stop){
                        // stop loss . market sell
                        NewOrderResponse resp = client.newOrder(marketSell(coin, amount));
                        System.out.println(resp);
                        info_text.append("\n-----\n" + currentTime() + " STOP. Anlık fiyat: " + temp_price_formatted);
                        status_text.append("\n-----\n" + currentTime() + " STOP. Anlık fiyat: " + temp_price_formatted);
                        return null;
                    }

                    status_text.append("\n-----\n" + currentTime() + " Hedef bekleniyor. Anlık fiyat: " + temp_price_formatted);
                    waitFor(5000);

                }

                // start trail
                // init trail val
                trail_value = Double.parseDouble(client.getPrice(coin).getPrice()) * ((100-trail_percent) / 100);
                save_trail_value = trail_value;

                String trail_value_formatted = formatDouble(trail_value);

                status_text.append("\n" + currentTime() +" Başlangıç trailing değeri: " + trail_value_formatted);

                while (trail_value <= Double.parseDouble(client.getPrice(coin).getPrice())) {

                    current_price = Double.parseDouble(client.getPrice(coin).getPrice());
                    String current_price_formatted = formatDouble(current_price);
                    status_text.append("\n------\n" + currentTime() + " Trailing etkin / Anlık fiyat: " + current_price_formatted);

                    if (current_price >= last_price) {
                        // update trail

                        if (trail_value < (current_price * ((100-trail_percent) / 100))) {

                            status_text.append("\nTrailing güncellendi: ");
                            trail_value = current_price * ((100-trail_percent) / 100);

                        }

                    }

                    // publish trail val
                    publish(trail_value);

                    trail_value_formatted = formatDouble(trail_value);

                    status_text.append("\nTrailing stop: " + trail_value_formatted + "\n");
                    // update last price
                    last_price = Double.parseDouble(client.getPrice(coin).getPrice());
                    waitFor(5000);
                }

                // market sell order
                try{
                    NewOrderResponse resp = client.newOrder(marketSell(coin, amount));
                } catch (Exception e){
                    e.printStackTrace();
                    status_text.append("\n" + currentTime() + " " + formatDouble(last_price) + " HATA! SATIŞ EMRİ VERİLEMEDİ. ");
                }


                status_text.append("\n" + currentTime() + " " + formatDouble(last_price) + " SATIŞ YAPILDI");
                info_text.append("\n" + currentTime() + " " + formatDouble(last_price) + " SATIŞ YAPILDI");

                // Finished
                return null;
            } // End of Method: doInBackground()

            @Override
            protected void process(List<Double> chunks) {
                // Get Info
                for (Double number : chunks) {
                    //status_text.append("\ntrail: " + number);
                    System.out.println("trail: " + number);
                }
            }

        };
    } // End of Method: createWorker()


    /**
     * Wait the given time in milliseconds
     * @param iMillis
     */
    private void waitFor (int iMillis) {
        try {
            Thread.sleep(iMillis);
        }
        catch (Exception ex) {
            System.err.println(ex);
        }
    } // End of Method: waitFor()



}
