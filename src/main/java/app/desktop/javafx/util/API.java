package app.desktop.javafx.util;

import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class API {
    private final URI url;
    private final HttpClient.Version version;
    private final Processor.Methods method;
    private final String[] headers;
    private final HttpRequest.BodyPublisher bodyPublisher;

    public API(URI url, HttpClient.Version version, Processor.Methods method, String[] headers, HttpRequest.BodyPublisher bodyPublisher) {
        this.url = url;
        this.version = version;
        this.method = method;
        this.headers = headers;
        this.bodyPublisher = bodyPublisher;
    }

     public HttpRequest createHttpRequest(){
        HttpRequest request;
        if (headers.length!=0){
           request = createHttpRequestWithHeaders();
        }else{
            request = createHttpRequestWithoutHeaders();
        }
        return request;
    }
    public HttpClient createHttpClient(){
        return HttpClient.newBuilder()
                .proxy(ProxySelector.getDefault())
                .version(version)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .cookieHandler(CookieHandler.getDefault())
                .build();
    }

    private HttpRequest createHttpRequestWithHeaders() {
        return HttpRequest.newBuilder(url)
                .method(method.toString(), bodyPublisher)
                .headers(headers)
                .build();
    }

    private HttpRequest createHttpRequestWithoutHeaders() {
        return HttpRequest.newBuilder(url)
                .method(method.toString(), bodyPublisher)
                .build();
    }

    public CompletableFuture<HttpResponse<byte[]>> sendRequest() throws Exception {
        var request = createHttpRequest();
        var client = createHttpClient();
        try {
            return client.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray());
        } catch (Exception e){
            throw new Exception();
        }
    }
}
