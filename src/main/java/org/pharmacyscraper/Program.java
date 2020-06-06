package org.pharmacyscraper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Program {

    // zhivika.ru Pharmacy
    public static void zhivikaRequest(String query, ArrayList<PharmacyProduct> array) throws
            MalformedURLException, UnsupportedEncodingException, IOException {

        HashMap<String, String> queryData = new HashMap<>();
        queryData.put("searchText", query);

        PharmacyProcessor pharmacyProcessor = new PharmacyProcessor(
                "Живика",
                "http://zhivika.ru"
        );

        pharmacyProcessor.requestGet(
                "/plugins/catalog/catalog_search",
                queryData,
                false,
                false
        );

        var products = pharmacyProcessor.getProducts(
                "tr#product_in_list_",
                "td > a.one_line_name > span",
                "td.booking > a > span.price1",
                "table.pagination > tbody > tr > td > a.next"
        );

        if (!products.isEmpty()) {
            array.addAll(products);
            printProducts(products);
        }
        else {
            System.out.println("No products.");
        }
    }

    // apteka-ot-sklada.ru Pharmacy
    public static void skladRequest(String query, ArrayList<PharmacyProduct> array) throws
            MalformedURLException, UnsupportedEncodingException, IOException {

        HashMap<String, String> cityData = new HashMap<>();
        cityData.put("cityid", "136");
        cityData.put("_dc", "1586945000575");
        cityData.put("backref", "/");

        PharmacyProcessor pharmacyProcessor = new PharmacyProcessor(
                "От склада",
                "https://apteka-ot-sklada.ru"
        );
        pharmacyProcessor.requestGet(
                "/setglobalcity",
                cityData,
                false,
                false
        );

        pharmacyProcessor.setRootUrlTo("https://apteka-ot-sklada.ru/catalog");

        HashMap<String, String> queryData = new HashMap<>();
        queryData.put("q", query);

        pharmacyProcessor.requestGet(
                "https://apteka-ot-sklada.ru/catalog",
                queryData,
                false,
                false
        );

        var products = pharmacyProcessor.getProducts(
                "li.catalog-list-item.product.product-type.widget > div.product-info",
                "div.product-main > a.product-name",
                "span.product-price",
                "div.paginator > a.paginator-arrow.right"
        );

        if (!products.isEmpty()) {
            array.addAll(products);
            printProducts(products);
        }
        else {
            System.out.println("No products.");
        }
    }

    // klassika-apteka.ru Pharmacy
    public static void classicRequest(String query, ArrayList<PharmacyProduct> array) throws
            MalformedURLException, UnsupportedEncodingException, IOException {

        HashMap<String, String> cityData = new HashMap<>();
        cityData.put("id", "114356");

        PharmacyProcessor pharmacyProcessor = new PharmacyProcessor(
                "Классика",
                "https://klassika-apteka.ru"
        );
        pharmacyProcessor.requestPost(
                "/ajax/set_city.php",
                cityData,
                true,
                true
        );

        HashMap<String, String> queryData = new HashMap<>();
        queryData.put("q", query);

        pharmacyProcessor.requestGet(
                "/search/",
                queryData,
                false,
                false
        );

        var products = pharmacyProcessor.getProducts(
                "div.item-row > div.table_item.item > div.table_item_inner > div > div.item_wrap",
                "div.desc_name > a",
                "div.price_bl > div.price.one_price:first-child",
                "a.arr.next"
        );

        if (!products.isEmpty()) {
            array.addAll(products);
            printProducts(products);
        }
        else {
            System.out.println("No products.");
        }
    }

    public static void printProducts(ArrayList<PharmacyProduct> products) {
        int counter = 0;
        for (var product : products) {
            System.out.format(
                    (product.getPrice().isBlank()) ?
                            "%3$d Product %1$s\n" :
                            "%3$d Product %1$s costs %2$s\n",
                    product.getName(),
                    product.getPrice(),
                    ++counter);
        }
    }

    public static void main(String[] args) {

        try {
//            String productsQuery = (args.length > 0) ? args[0] : "гематоген";
            Scanner inCmd = new  Scanner(System.in);
            System.out.println("Please enter a query for search (scraping)");
            String productsQuery = inCmd.nextLine();

            System.out.format("Web-Scraping for product: %s\n", productsQuery);

            ArrayList<PharmacyProduct> productsArray = new ArrayList<>();

            System.out.println("===== zhivika.ru");
            zhivikaRequest(productsQuery, productsArray);
            System.out.print("\nDone.\n\n");

            System.out.println("===== klassika-apteka.ru");
            classicRequest(productsQuery, productsArray);
            System.out.print("\nDone.\n\n");

            System.out.println("===== apteka-ot-sklada.ru");
            skladRequest(productsQuery, productsArray);
            System.out.print("\nDone.\n\n");

            if (productsArray.isEmpty()) {
                System.out.print("Nothing to export.\n");
                return;
            }

            System.out.print("Exporting to excel.\n");
            PharmacyExporter.writeIntoExcel(productsArray);
        }
        catch (MalformedURLException mex) {
            System.out.println(String.format("URL Error. %1$s\n", mex.getMessage()));
        }
        catch (UnsupportedEncodingException uex) {
            System.out.println(String.format("Encoding Error. %1$s\n", uex.getMessage()));
        }
        catch (IOException ex) {
            System.out.println(String.format("InputOutput Error. %1$s\n", ex.getMessage()));
        }

    }

}
