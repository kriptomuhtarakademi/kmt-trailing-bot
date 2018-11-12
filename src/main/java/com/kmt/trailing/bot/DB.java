/*

contact: hlcii@protonmail.com

 */

package com.kmt.trailing.bot;


import java.io.*;
import java.util.ArrayList;

public class DB {

    String path = "C:" + File.separator + "kmt" + File.separator + "login.txt";

    public void createFile() {

        // Use relative path for Unix systems
        File f = new File(path);
        f.getParentFile().mkdirs();

        Boolean res = false;

        try {
            res = f.createNewFile();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (res) {

            try {
                PrintWriter writer = new PrintWriter(path, "UTF-8");
                writer.println("kmt");
                writer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }


    public String checkDB() {
        String s = "";
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(path));
            String line = reader.readLine();
            while (line != null) {
                s = line;
                break;
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return s;
    }


    public ArrayList<String> readDB() {

        ArrayList<String> alist=new ArrayList<String>();
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(path));
            String line = reader.readLine();
            while (line != null) {
                alist.add(line);
                // read next line
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return alist;



    }




}
