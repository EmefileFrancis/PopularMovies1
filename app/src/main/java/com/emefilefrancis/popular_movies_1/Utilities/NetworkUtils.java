package com.emefilefrancis.popular_movies_1.Utilities;

import android.net.Uri;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

/**
 * Created by SP on 8/26/2018.
 */

public class NetworkUtils {
    private static final String BASE_URL = "http://api.themoviedb.org/3/movie/";
    private static final String API_KEY = "d92bef7c5e62d869e55e8bc640adda5b";

    private static final String API_KEY_QUERY_NAME = "api_key";

    public static URL buildUrl(String sortByQueryParam) throws MalformedURLException{
        String urlWithSort = BASE_URL + sortByQueryParam;
        Uri moviesUri = Uri.parse(urlWithSort).buildUpon()
                                            .appendQueryParameter(API_KEY_QUERY_NAME, API_KEY)
                                            .build();
        Log.i("URL", moviesUri.toString());
        URL builtUrl = new URL(moviesUri.toString());
        return builtUrl;
    }

    public static String getResponseFromApiCall(URL moviesApiUrl) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) moviesApiUrl.openConnection();
        InputStream is = urlConnection.getInputStream();
        Scanner scanner = new Scanner(is);
        scanner.useDelimiter("\\A");

        if(scanner.hasNext()) {
            return scanner.next();
        }else{
            return null;
        }
    }
}
