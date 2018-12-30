package com.kmt.trailing.bot;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.constant.BinanceApiConstants;
import com.binance.api.client.domain.account.Account;
import com.binance.api.client.domain.market.BookTicker;
import com.binance.api.client.exception.BinanceApiException;

public class App {

    private JButton login_button;
    private JPanel panel1;
    private JLabel api_key;
    private JTextField APIKeyTextField;
    private JTextField APISecretTextField;
    private JLabel api_secret;


    public App() {

        String path = "C:" + File.separator + "kmt" + File.separator + "login.txt";
        File file = new File(path);
        long file_length = file.length();

        DB db = new DB();

        String check_db = db.checkDB();

        if (!(file_length > 0)) {

            db.createFile();
        } else if(!check_db.equals("kmt")) {

            try {
                ArrayList<String> alist = db.readDB();
                APIKeyTextField.setText(alist.get(0));
                APISecretTextField.setText(alist.get(1));

            } catch (Exception exc){
                exc.printStackTrace();
            }

        }


        login_button.addActionListener(new ActionListener() {


            public void actionPerformed(ActionEvent e) {

                String getAPIKey = APIKeyTextField.getText();
                String getAPISecret = APISecretTextField.getText();

                BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance(getAPIKey, getAPISecret);
                final BinanceApiRestClient client = factory.newRestClient();

                try {
                    long start = System.currentTimeMillis();
                    long end = System.currentTimeMillis();
                    long gap = end - start;
                    Boolean acc = client.getAccount(BinanceApiConstants.DEFAULT_RECEIVING_WINDOW, client.getServerTime() + gap).isCanTrade();

                    if (acc) {
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                JFrame login_frame = new JFrame("App");
                                login_frame.setContentPane(new Trade(client).panel);
                                login_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                                login_frame.pack();
                                login_frame.setVisible(true);
                            }
                        });

                        String path = "C:" + File.separator + "kmt" + File.separator + "login.txt";
                        try {
                            PrintWriter writer = new PrintWriter(path, "UTF-8");
                            writer.print("");
                            writer.close();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }

                        try {
                            PrintWriter writer = new PrintWriter(path, "UTF-8");
                            writer.println(APIKeyTextField.getText());
                            writer.println(APISecretTextField.getText());
                            writer.close();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }

                    }

                } catch (BinanceApiException APIError) {
                    APIError.printStackTrace();
                    JOptionPane.showMessageDialog(null, "API Error");
                }

            }
        });

    }


    public static void main(String[] args) {

        JFrame login_frame = new JFrame("Kripto Muhtar Trailing Stop Bot");
        login_frame.setContentPane(new App().panel1);
        login_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        login_frame.pack();
        login_frame.setVisible(true);

    }

}