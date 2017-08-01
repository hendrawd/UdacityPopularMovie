package ganteng.hendrawd.popularmovies.network;

import com.android.volley.Request;
import com.android.volley.error.AuthFailureError;
import com.android.volley.toolbox.HttpStack;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;

import java.io.IOException;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * OkHttp backed {@link com.android.volley.toolbox.HttpStack HttpStack} that does not
 * use okhttp-urlconnection
 */
@SuppressWarnings("deprecation")
public class OkHttp3Stack implements HttpStack {

    private static OkHttpClient sOkHttpClient;

    public OkHttp3Stack() {
        if (sOkHttpClient == null) {
            sOkHttpClient = OkHttpSingleton.getInstance().getOkHttpClient();
        }
    }

    @Override
    public HttpResponse performRequest(com.android.volley.Request<?> request, Map<String, String> additionalHeaders)
            throws IOException, AuthFailureError {

        okhttp3.Request.Builder okHttpRequestBuilder = new okhttp3.Request.Builder().url(request.getUrl());

        if (request.getTag() != null) {
            okHttpRequestBuilder.tag(request.getTag());
        }

        Map<String, String> headers = request.getHeaders();
        for (final String name : headers.keySet()) {
            okHttpRequestBuilder.addHeader(name, headers.get(name));
        }
        for (final String name : additionalHeaders.keySet()) {
            okHttpRequestBuilder.addHeader(name, additionalHeaders.get(name));
        }

        setConnectionParametersForRequest(okHttpRequestBuilder, request);

        if (sOkHttpClient == null) {
            sOkHttpClient = OkHttpSingleton.getInstance().getOkHttpClient();
        }

        okhttp3.Request okHttpRequest = okHttpRequestBuilder.build();
        Call okHttpCall = sOkHttpClient.newCall(okHttpRequest);
        Response okHttpResponse = okHttpCall.execute();

        StatusLine responseStatus = new BasicStatusLine(parseProtocol(okHttpResponse.protocol()), okHttpResponse.code(), okHttpResponse.message());
        BasicHttpResponse response = new BasicHttpResponse(responseStatus);
        response.setEntity(entityFromOkHttpResponse(okHttpResponse));

        Headers responseHeaders = okHttpResponse.headers();
        for (int i = 0, len = responseHeaders.size(); i < len; i++) {
            final String name = responseHeaders.name(i), value = responseHeaders.value(i);
            if (name != null) {
                response.addHeader(new BasicHeader(name, value));
            }
        }

        return response;
    }

    private static HttpEntity entityFromOkHttpResponse(Response r) throws IOException {
        BasicHttpEntity entity = new BasicHttpEntity();
        ResponseBody body = r.body();

        if (body != null) {
            entity.setContent(body.byteStream());
            entity.setContentLength(body.contentLength());
            entity.setContentEncoding(r.header("Content-Encoding"));

            MediaType bodyContentType = body.contentType();
            if (bodyContentType != null) {
                entity.setContentType(bodyContentType.type());
            }
        }
        return entity;
    }

    @SuppressWarnings("deprecation")
    private static void setConnectionParametersForRequest(okhttp3.Request.Builder builder, com.android.volley.Request<?> request)
            throws IOException, AuthFailureError {
        switch (request.getMethod()) {
            case Request.Method.DEPRECATED_GET_OR_POST:
                // Ensure backwards compatibility.  Volley assumes a request with a null body is a GET.
                byte[] postBody = request.getPostBody();
                if (postBody != null) {
                    builder.post(RequestBody.create(MediaType.parse(request.getPostBodyContentType()), postBody));
                }
                break;
            case Request.Method.GET:
                builder.get();
                break;
            case Request.Method.DELETE:
                builder.delete();
                break;
            case Request.Method.POST:
                builder.post(createRequestBody(request));
                break;
            case Request.Method.PUT:
                builder.put(createRequestBody(request));
                break;
            case Request.Method.HEAD:
                builder.head();
                break;
            case Request.Method.OPTIONS:
                builder.method("OPTIONS", null);
                break;
            case Request.Method.TRACE:
                builder.method("TRACE", null);
                break;
            case Request.Method.PATCH:
                builder.patch(createRequestBody(request));
                break;
            default:
                throw new IllegalStateException("Unknown method type.");
        }
    }

    private static ProtocolVersion parseProtocol(final Protocol p) {
        switch (p) {
            case HTTP_1_0:
                return new ProtocolVersion("HTTP", 1, 0);
            case HTTP_1_1:
                return new ProtocolVersion("HTTP", 1, 1);
            case SPDY_3:
                return new ProtocolVersion("SPDY", 3, 1);
            case HTTP_2:
                return new ProtocolVersion("HTTP", 2, 0);
        }

        throw new IllegalAccessError("Unknown protocol");
    }

    private static RequestBody createRequestBody(Request r) throws AuthFailureError {
        final byte[] body = r.getBody();
        if (body == null) {
            //avoid empty body that will throw IllegalArgumentException if the params is empty,
            //so we will return RequestBody.create(MediaType.parse(r.getBodyContentType()), "") instead of null
            return RequestBody.create(MediaType.parse(r.getBodyContentType()), "");
        }

        return RequestBody.create(MediaType.parse(r.getBodyContentType()), body);
    }

    //cancel requests based on tag
    public void cancelRequestOnGoing(Object tag) {
        if (sOkHttpClient != null) {
            for (Call call : sOkHttpClient.dispatcher().queuedCalls()) {
                if (call.request().tag().equals(tag))
                    call.cancel();
            }
            for (Call call : sOkHttpClient.dispatcher().runningCalls()) {
                if (call.request().tag().equals(tag))
                    call.cancel();
            }
        }
    }

}