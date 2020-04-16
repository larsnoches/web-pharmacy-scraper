package org.pharmacyparser;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PharmacyProcessor {
    private String pharmacyLabel = null;
    private String rootUrl = null;
    private Connection lastConnection = null;
    private Document doc = null;

    public PharmacyProcessor(String pharmacyLabel, String rootUrl) {
        this.pharmacyLabel = pharmacyLabel;
        this.rootUrl = rootUrl;
    }

    private Connection setupConnection(String url,
                                       boolean isAjax,
                                       boolean isForm) {

        Connection connection = Jsoup.connect(
                (rootUrl != null && !rootUrl.isBlank() && url.startsWith(rootUrl)) ?
                        url :
                        rootUrl + url
        )
                .userAgent(String.format("%1$s %2$s %3$s",
                        "Mozilla/5.0 (X11; Linux x86_64)",
                        "AppleWebKit/537.36 (KHTML, like Gecko)",
                        "Chrome/68.0.3440.106 Safari/537.36"))
                .referrer("https://www.google.com");

        if (isAjax)
            connection = connection.header("X-Requested-With", "XMLHttpRequest");

        if (isForm)
            connection = connection.header("Content-Type", "application/x-www-form-urlencoded");
        else
            connection = connection.header("Content-Type", "text/html; charset=utf8");

        return connection;
    }

    private Map<String, String> extractCookies() {
        if (doc != null && lastConnection != null) {
            var resp = lastConnection.response();
            if (resp != null) {
                return resp.cookies();
            }
        }
        return null;
    }

    public void setRootUrlTo(String rootUrl) {
        this.rootUrl = rootUrl;
    }

    public void requestPost(String url,
                            Map<String, String> data,
                            boolean isAjax,
                            boolean isForm) throws IOException {
        try {
            Connection connection = setupConnection(url, isAjax, isForm);
            if (connection == null)
                throw new IOException("Error setup connection");

            var cookies = extractCookies();

            if (cookies != null) {
                connection = connection.cookies(cookies);
            }

            doc = connection
                    .data(data)
                    .post();

            lastConnection = connection;
        }
        catch (IOException ex) {
            throw ex;
        }
    }

    public void requestGet(String url,
                           Map<String, String> data,
                           boolean isAjax,
                           boolean isForm) throws IOException {
        try {
            Connection connection = setupConnection(url, isAjax, isForm);
            if (connection == null)
                throw new IOException("Error setup connection");

            var cookies = extractCookies();

            if (cookies != null) {
                connection = connection.cookies(cookies);
            }

            doc = connection
                    .data(data)
                    .get();

            lastConnection = connection;
        }
        catch (IOException ex) {
            throw ex;
        }
    }

//    public ArrayList<String> getText(String selector) {
//        ArrayList<String> array = new ArrayList<>();
//        if (doc != null) {
//            System.out.println(doc.html());
//            Elements elements = doc.select(selector);
//            for (Element element : elements) {
//                array.add(element.text());
//            }
//        }
//        return array;
//    }

    public ArrayList<PharmacyProduct> getProducts(String parentSelector,
                                                  String productSelector,
                                                  String priceSelector,
                                                  String paginationSelector) throws IOException {
        ArrayList<PharmacyProduct> array = new ArrayList<>();
        if (doc != null) {
            // read elements
            Elements productRows = doc.select(parentSelector);
            for (Element row : productRows) {
                Element productElement = row.selectFirst(productSelector);
                Element priceElement = row.selectFirst(priceSelector);

                PharmacyProduct pharmacyProduct = new PharmacyProduct();

                // price
                String priceText = "";
                if (priceElement != null) {
                    priceText = priceElement
                            .text()
                            .trim();

                    if (priceText.equals("0,00"))
                        priceText = "";
                    else if (priceText.indexOf("р.") != -1)
                        priceText = priceText.replace("р.", "");
                    else if (priceText.indexOf(".") != -1)
                        priceText = priceText.replace(".", ",");
                }
                pharmacyProduct.setPrice(priceText);

                // name
                if (productElement != null) {
                    pharmacyProduct.setName(productElement.text().trim());
                    pharmacyProduct.setPharmacyLabel(pharmacyLabel); // label

                    array.add(pharmacyProduct);
                }
            }

            // has pagination, recursive get
            if (paginationSelector != null) {
                Element pagination = doc.selectFirst(paginationSelector);
                if (pagination != null) {
                    String nextPage = pagination.attr("href");

                    if (nextPage.isBlank())
                        return array;

                    requestGet(
                            nextPage,
                            new HashMap<String, String>(),
                            false,
                            false
                    );

                    var products = getProducts(
                            parentSelector,
                            productSelector,
                            priceSelector,
                            paginationSelector
                    );
                    if (!products.isEmpty()) {
                        array.addAll(products);
                    }
                }
            }
        }
        return array;
    }

}
