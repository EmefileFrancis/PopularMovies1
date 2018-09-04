package com.emefilefrancis.popular_movies_1.Utilities;

import android.net.Uri;

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
    private static final String BASE_URL = "https://api.themoviedb.org/3/discover/movie";
    private static final String API_KEY = "";
    private static final String LANGUAGE = "en-US";

    private static final String API_KEY_QUERY_NAME = "api_key";
    private static final String LANGUAGE_QUERY_NAME = "language";
    private static final String SORT_BY_QUERY_NAME = "sort_by";

    public static URL buildUrl(String sortByQueryParam) throws MalformedURLException{
        Uri moviesUri = Uri.parse(BASE_URL).buildUpon()
                                            .appendQueryParameter(API_KEY_QUERY_NAME, API_KEY)
                                            .appendQueryParameter(LANGUAGE_QUERY_NAME, LANGUAGE)
                                            .appendQueryParameter(SORT_BY_QUERY_NAME, sortByQueryParam)
                                            .build();
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
