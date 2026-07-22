package com.example.javaeight.fileutils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.stream.Collectors;

public class QueryBuilder {

    private static final Logger logger = LoggerFactory.getLogger(QueryBuilder.class);

    public static void main(String[] args) throws Exception {
        try {


            ArrayList<String> orderArry = new ArrayList<>();
            Scanner input = new Scanner(new File("/Users/gramaraju/Downloads/RMS_Weekly_Report_Generation/orders_list.txt"));
            FileWriter fw = new FileWriter("/Users/gramaraju/Downloads/RMS_Weekly_Report_Generation/updated_orders_list.txt", true);
            BufferedWriter bw = new BufferedWriter(fw);
            int counter = 0;

            while (input.hasNextLine()) {
                orderArry.add(input.nextLine());
                counter++;

                if (counter == 1000 || !input.hasNextLine()) {


                    String inClause = orderArry.stream().map(s -> "'" + s + "'")
                            .collect(Collectors.joining(","));

                   String query = "select ord_nbr, lnk_ord_nbr, mo.ord_dt, ord_pur, PAR_ORD_LN_NBR, RTN_RSN_CD from order_ca.marc_ord_assn_t sn, order_ca.marc_ord_t mo, order_ca.marc_ord_ln_t ln where sn.lnk_ord_key = mo.marc_ord_key and mo.marc_ord_key = ln.marc_ord_key and LNK_ORD_ASSN_TYP = 2 and ORD_PUR in ('CSR_CREDIT') and ord_nbr in (" + inClause + ")" +
                           "\nUNION";

//                    String query = "select ord_nbr, lnk_ord_nbr, mo.ord_dt, ord_pur, PAR_ORD_LN_NBR, RTN_RSN_CD from order_ca.marc_ord_assn_t sn, order_ca.marc_ord_t mo, order_ca.marc_ord_ln_t ln where sn.lnk_ord_key = mo.marc_ord_key and mo.marc_ord_key = ln.marc_ord_key and LNK_ORD_ASSN_TYP = 2 and ORD_PUR in ('CSR_CREDIT') and ord_nbr in (" + inClause + ")" +
//                            "\nUNION";


                    //logger.info(query);
                    System.out.println(query);

//                    bw.write(query);
//                    bw.newLine();

                    orderArry.clear();
                    counter = 0;
                }
            }

        } catch (Exception e) {
            logger.error("Caught exception:", e);
        } 
    }
}
