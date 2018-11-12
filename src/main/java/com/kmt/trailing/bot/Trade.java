/*

contact: hlcii@protonmail.com

 */

package com.kmt.trailing.bot;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.constant.BinanceApiConstants;
import com.binance.api.client.domain.account.Account;
import com.binance.api.client.domain.account.AssetBalance;
import com.binance.api.client.domain.market.BookTicker;
import com.binance.api.client.exception.BinanceApiException;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.text.DefaultCaret;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class Trade {

    private JLabel logo;
    private JLabel balance_text;
    public JPanel panel;
    private JComboBox coinList;
    private JButton start_trail;
    private JTextField stop_text;
    private JLabel target_label;
    private JCheckBox target_percent_cb;
    private JCheckBox target_price_cb;
    private JLabel stop_label;
    private JTextArea status_text;
    private JFormattedTextField target_text;
    private JLabel trail_label;
    private JTextField trail_text;
    private JButton stop_trail;
    private JLabel selected_coin_price;
    private JLabel coin_amount;
    private JTextField coin_amount_text;
    private JTextArea info_text;
    private JScrollPane sc;
    private JScrollPane scroll;
    public BinanceApiRestClient client;

    static SwingWorker<Void, Double> w1;


    public Trade(final BinanceApiRestClient client) {

        this.client = client;

        try {

            Account account = client.getAccount(BinanceApiConstants.DEFAULT_RECEIVING_WINDOW, client.getServerTime());

            Boolean acc = (Boolean) account.isCanTrade();

            if (acc) {

                List<BookTicker> allBookTickers = client.getBookTickers();

                List<String> allBookTickers_string = new ArrayList<String>() ;

                for (BookTicker item : allBookTickers) {
                    allBookTickers_string.add(""+item.getSymbol());
                }

                java.util.Collections.sort(allBookTickers_string);

                System.out.println(allBookTickers_string);


                for (String item : allBookTickers_string) {
                    coinList.addItem(new ComboItem(item, "value"));
                }

            }

        } catch (BinanceApiException APIError) {
            APIError.printStackTrace();
            JOptionPane.showMessageDialog(null, "API Error");
        }

        start_trail.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                if ((target_price_cb.isSelected() || target_percent_cb.isSelected()) && w1 == null) {

                        Double stop = Double.parseDouble(stop_text.getText());
                        Double trail = Double.parseDouble(trail_text.getText());
                        String amount = coin_amount_text.getText();

                        int target = 0;

                        if (target_percent_cb.isSelected()) {
                            System.out.println("percent");
                            target = 1;
                        } else if (target_price_cb.isSelected()) {
                            System.out.println("price");
                            target = 2;
                        }

                        Double target_price = .0;

                        if (target == 1) {
                            target_price = Double.parseDouble(target_text.getText());
                        } else if (target == 2) {
                            target_price = Double.parseDouble(target_text.getText());
                        }

                        String coin_name = coinList.getSelectedItem().toString();
                        System.out.println(coin_name);
                        System.out.println(target_price);
                        System.out.println(target_price.getClass().getName());
                        Trail tr = new Trail(client, coin_name, target_price, stop, trail, status_text, target, amount, info_text);
                        w1 = tr.createWorker();
                        w1.execute();

                }
            }
        });

        stop_trail.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                int dialogButton = JOptionPane.YES_NO_OPTION;
                int dialogResult = JOptionPane.showConfirmDialog(null, "Uygulamayı kapatmak istediğinden emin misin?", "Kapat", dialogButton);

                if(dialogResult == 0)
                    System.exit(0);

            }
        });

        target_percent_cb.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                if (target_percent_cb.isSelected())
                    target_price_cb.setSelected(false);
                target_text.setText("1");

            }
        });

        target_price_cb.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                if (target_price_cb.isSelected())
                    target_percent_cb.setSelected(false);

            }
        });

        coinList.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                String selected_coin = "" + coinList.getSelectedItem();
                int dialogButton = JOptionPane.YES_NO_OPTION;
                int dialogResult = JOptionPane.showConfirmDialog(null, selected_coin + " devam?", "Coin Seçimi", dialogButton);
                if(dialogResult == 0) {
                    System.out.println("Yes option");


                    Account account = client.getAccount(BinanceApiConstants.DEFAULT_RECEIVING_WINDOW, client.getServerTime());

                    String coin_price = "" + client.getPrice(selected_coin).getPrice();

                    selected_coin_price.setText(coin_price);
                    target_text.setText(coin_price);

                    String coin_name = "";

                    if (selected_coin.endsWith("BTC")) {
                        coin_name = editCoin(selected_coin, "BTC");
                    } else if (selected_coin.endsWith("USDT")) {
                        coin_name = editCoin(selected_coin, "USDT");
                    } else if (selected_coin.endsWith("ETH")) {
                        coin_name = editCoin(selected_coin, "ETH");
                    } else if (selected_coin.endsWith("BNB")) {
                        coin_name = editCoin(selected_coin, "BNB");
                    }

                    coin_amount_text.setText(client.getAccount().getAssetBalance(coin_name).getFree());

                } else {
                    System.out.println("No Option");
                }

            }
        });
    }

    public String editCoin(String coin, String delete) {

        int index = coin.lastIndexOf(delete);
        coin = coin.substring(0 , index) + coin.substring(index+delete.length());
        return coin;

    }

    private void createUIComponents() {
        status_text = new JTextArea(20, 10);
        DefaultCaret caret = (DefaultCaret)status_text.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
    }

}
