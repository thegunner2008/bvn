package tamhoang.bvn.akaman;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import java.util.HashMap;
import java.util.Map;

public class InputStreamVolleyRequest extends Request<byte[]> {
    private final Response.Listener<byte[]> mListener;
    private final Map<String, String> mParams;
    public Map<String, String> responseHeaders;

    public InputStreamVolleyRequest(int i, String str, Response.Listener<byte[]> listener, Response.ErrorListener errorListener, HashMap<String, String> hashMap) {
        super(i, str, errorListener);
        setShouldCache(false);
        this.mListener = listener;
        this.mParams = hashMap;
    }

    /* access modifiers changed from: protected */
    public Map<String, String> getParams() throws AuthFailureError {
        return this.mParams;
    }

    /* access modifiers changed from: protected */
    public void deliverResponse(byte[] bArr) {
        this.mListener.onResponse(bArr);
    }

    /* access modifiers changed from: protected */
    public Response<byte[]> parseNetworkResponse(NetworkResponse networkResponse) {
        this.responseHeaders = networkResponse.headers;
        return Response.success(networkResponse.data, HttpHeaderParser.parseCacheHeaders(networkResponse));
    }
}
