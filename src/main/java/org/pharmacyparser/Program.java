package org.pharmacyparser;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
//import java.util.ArrayList;
import java.util.ArrayList;
import java.util.HashMap;

public class Program {

    // ZhivikaPharmacy
    public static void zhivikaRequest(String query, ArrayList<PharmacyProduct> array) throws
            MalformedURLException, UnsupportedEncodingException, IOException {

        HashMap<String, String> queryData = new HashMap<>();
        queryData.put("searchText", query);
//            data.put("searchText", URLEncoder.encode(query, "cp1251"));

        PharmacyProcessor pharmacyProcessor = new PharmacyProcessor(
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
        array.addAll(products);

        int counter = 0;
        for (var product : products) {
            System.out.format(
                    "%3$d Product %1$s costs %2$s\n",
                    product.getName(),
                    product.getPrice(),
                    ++counter);
        }
    }

    // OtSkladaPharmacy
    public static void skladRequest(String query, ArrayList<PharmacyProduct> array) throws
            MalformedURLException, UnsupportedEncodingException, IOException {

        HashMap<String, String> cityData = new HashMap<>();
        cityData.put("cityid", "136");
        cityData.put("_dc", "1586945000575");
        cityData.put("backref", "/");

        PharmacyProcessor pharmacyProcessor = new PharmacyProcessor(
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
        array.addAll(products);

        int counter = 0;
        for (var product : products) {
            System.out.format(
                    "%3$d Product %1$s costs %2$s\n",
                    product.getName(),
                    product.getPrice(),//.replace(".", ","),
                    ++counter);
        }
    }

    // ClassicPharmacy
    public static void classicRequest(String query, ArrayList<PharmacyProduct> array) throws
            MalformedURLException, UnsupportedEncodingException, IOException {

        HashMap<String, String> cityData = new HashMap<>();
        cityData.put("id", "114356");

        PharmacyProcessor pharmacyProcessor = new PharmacyProcessor(
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
        array.addAll(products);

        int counter = 0;
        for (var product : products) {
            System.out.format(
                    "%3$d Product %1$s costs %2$s\n",
                    product.getName(),
                    product.getPrice(),//.replace("р.", ""),
                    ++counter);
        }
    }

    public static void main(String[] args) {

        try {
            String productsQuery = "гематоген";
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
