package com.cedarsoftware.util.io;

import java.io.InputStream;
import java.util.Map;

import com.cedarsoftware.util.ReturnType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Useful utilities for use in unit testing.
 *
 * @author John DeRegnaucourt (jdereg@gmail.com)
 * <br>
 * Copyright (c) Cedar Software LLC
 * <br><br>
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <br><br>
 * <a href="<a href="http://www.apache.org/licenses/LICENSE-2.0">License</a>">...</a>
 * <br><br>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class TestUtil
{
    final static Logger logger = LoggerFactory.getLogger(TestUtil.class);
    
    public static <T> T serializeDeserialize(T initial)
    {
        String json = toJson(initial);
        return toObjects(json, null);
    }

    public static <T> Object serializeDeserializeAsMaps(T initial) {
        String json = toJson(initial, new WriteOptions().showTypeInfoNever());
        return toObjects(json, new ReadOptions().returnType(ReturnType.JSON_VALUES), null);
    }

    private static class TestInfo
    {
        long nanos;
        Throwable t;
        String json;
        Object obj;
    }

    private static TestInfo writeJsonIo(Object obj, WriteOptions writeOptions)
    {
        TestInfo testInfo = new TestInfo();
        try
        {
            long start = System.nanoTime();
            testInfo.json = JsonIo.toJson(obj, writeOptions);
            testInfo.nanos = System.nanoTime() - start;
        }
        catch (Exception e)
        {
            testInfo.t = e;
        }
        return testInfo;
    }

    private static TestInfo writeGSON(Object obj)
    {
        TestInfo testInfo = new TestInfo();
        try
        {
            Gson gson = new Gson();
            long start = System.nanoTime();
            String json = gson.toJson(obj);
            testInfo.nanos = System.nanoTime() - start;
            testInfo.json = json;
        }
        catch (Throwable t)
        {
            testInfo.t = t;
        }
        return testInfo;
    }

    private static TestInfo writeJackson(Object obj)
    {
        TestInfo testInfo = new TestInfo();
        try
        {
            ObjectMapper objectMapper = new ObjectMapper();
            long start = System.nanoTime();
            testInfo.json = objectMapper.writeValueAsString(obj);
            testInfo.nanos = System.nanoTime() - start;
        }
        catch (Throwable t)
        {
            testInfo.t = t;
        }
        return testInfo;
    }

    public static String toJson(Object obj)
    {
        return toJson(obj, new WriteOptions());
    }

    /**
     * Generally, use this API to write JSON.  It will do so using json-io and other serializers, so that
     * timing statistics can be measured.
     */
    public static String toJson(Object obj, WriteOptions writeOptions)
    {
        totalWrites++;
        // json-io
        TestInfo jsonIoTestInfo = writeJsonIo(obj, writeOptions);
        TestInfo gsonTestInfo = writeGSON(obj);
        TestInfo jacksonTestInfo = writeJackson(obj);

        if (jsonIoTestInfo.json != null)
        {
            printLine(jsonIoTestInfo.json);
        }

        if (jsonIoTestInfo.t == null && gsonTestInfo.t == null && jacksonTestInfo.t == null)
        {   // Only add times when all parsers succeeded
            totalJsonWrite += jsonIoTestInfo.nanos;
            totalGsonWrite += gsonTestInfo.nanos;
            totalJacksonWrite += jacksonTestInfo.nanos;
        }
        else
        {
            if (jsonIoTestInfo.t != null)
            {
                jsonIoWriteFails++;
            }
            if (gsonTestInfo.t != null)
            {
                gsonWriteFails++;
            }
            if (jacksonTestInfo.t != null)
            {
                jacksonWriteFails++;
            }
        }

        if (jsonIoTestInfo.t != null)
        {
            try
            {
                throw jsonIoTestInfo.t;
            }
            catch (Throwable t)
            {
                throw (RuntimeException) t;
            }
        }
        return jsonIoTestInfo.json;
    }

    private static TestInfo readJsonIo(String json, ReadOptions options, Class<?> root)
    {
        TestInfo testInfo = new TestInfo();
        try
        {
            long start = System.nanoTime();
            testInfo.obj = JsonIo.toObjects(json, options, root);
            testInfo.nanos = System.nanoTime() - start;
        }
        catch (Exception e)
        {
            testInfo.t = e;
        }
        return testInfo;
    }

    private static TestInfo readGson(String json)
    {
        TestInfo testInfo = new TestInfo();
        try
        {
            Gson gson = new Gson();
            long start = System.nanoTime();
            Map<?, ?> map = gson.fromJson(json, Map.class);
            testInfo.nanos = System.nanoTime() - start;
            testInfo.obj = map;
        }
        catch (Exception e)
        {
            testInfo.t = e;
        }
        return testInfo;
    }

    private static TestInfo readJackson(String json)
    {
        TestInfo testInfo = new TestInfo();
        try
        {
            ObjectMapper mapper = new ObjectMapper();
            long start = System.nanoTime();
            Map<?, ?> map = mapper.readValue(json, Map.class);
            testInfo.nanos = System.nanoTime() - start;
            testInfo.obj = map;
        }
        catch (Exception e)
        {
            testInfo.t = e;
        }
        return testInfo;
    }

    /**
     * Generally, use this API to read JSON.  It will do so using json-io and other serializers, so that
     * timing statistics can be measured.  This version is the simple (no build options version).
     */
    public static <T> T toObjects(String json, Class<T> root)
    {
        return toObjects(json, new ReadOptions(), root);
    }

    /**
     * Generally, use this API to read JSON.  It will do so using json-io and other serializers, so that
     * timing statistics can be measured.  This version is more capable, as it supports build options.
     */
    public static <T> T toObjects(final String json, ReadOptions readOptions, Class<T> root)
    {
        if (null == json || json.trim().isEmpty())
        {
            return null;
        }
        totalReads++;

        TestInfo jsonIoTestInfo = readJsonIo(json, readOptions, root);
        TestInfo gsonTestInfo = readGson(json);
        TestInfo jacksonTestInfo = readJackson(json);

        if (jsonIoTestInfo.t == null && gsonTestInfo.t == null && jacksonTestInfo.t == null)
        {   // only add times when all parsers succeeded
            totalJsonRead += jsonIoTestInfo.nanos;
            totalGsonRead += gsonTestInfo.nanos;
            totalJacksonRead += jacksonTestInfo.nanos;
        }
        else
        {
            if (jsonIoTestInfo.t != null)
            {
                jsonIoReadFails++;
            }
            if (gsonTestInfo.t != null)
            {
                gsonReadFails++;
            }
            if (jacksonTestInfo.t != null)
            {
                jacksonReadFails++;
            }
        }

        if (jsonIoTestInfo.t != null)
        {
            try
            {
                throw jsonIoTestInfo.t;
            }
            catch (Throwable t)
            {
                throw (RuntimeException) t;
            }
        }

        //noinspection unchecked
        return (T) jsonIoTestInfo.obj;
    }

    public static JsonValue toJsonValues(String json, ReadOptions readOptions) {
        return JsonIo.toJsonValues(json, readOptions);
    }

    public static JsonValue toJsonValues(InputStream in, ReadOptions readOptions) {
        return JsonIo.toJsonValues(in, readOptions);
    }

    public static void printLine(String s)
    {
        if (debug)
        {
            System.out.println(s);
        }
    }

    public static void writeTimings()
    {
        logger.info("Write JSON");
        logger.info("  json-io: " + (totalJsonWrite / 1000000.0) + " ms");
        logger.info("  GSON: " + (totalGsonWrite / 1000000.0) + " ms");
        logger.info("  Jackson: " + (totalJacksonWrite / 1000000.0) + " ms");
        logger.info("Read JSON");
        logger.info("  json-io: " + (totalJsonRead / 1000000.0) + " ms");
        logger.info("  GSON: " + (totalGsonRead / 1000000.0) + " ms");
        logger.info("  Jackson: " + (totalJacksonRead / 1000000.0) + " ms");
        logger.info("Write Fails:");
        logger.info("  json-io: " + jsonIoWriteFails + " / " + totalWrites);
        logger.info("  GSON: " + gsonWriteFails + " / " + totalWrites);
        logger.info("  Jackson: " + jacksonWriteFails + " / " + totalWrites);
        logger.info("Read Fails");
        logger.info("  json-io: " + jsonIoReadFails + " / " + totalReads);
        logger.info("  GSON: " + gsonReadFails + " / " + totalReads);
        logger.info("  Jackson: " + jacksonReadFails + " / " + totalReads);
    }

    public static int count(CharSequence content, CharSequence token)
    {
        if (content == null || token == null)
        {
            return 0;
        }

        String source = content.toString();
        if (source.isEmpty())
        {
            return 0;
        }
        String sub = token.toString();
        if (sub.isEmpty())
        {
            return 0;
        }

        int answer = 0;
        int idx = 0;

        while (true)
        {
            idx = source.indexOf(sub, idx);
            if (idx < answer)
            {
                return answer;
            }
            ++answer;
            ++idx;
        }
    }

    private static long totalJsonWrite;
    private static long totalGsonWrite;
    private static long totalJacksonWrite;
    private static long totalJsonRead;
    private static long totalGsonRead;
    private static long totalJacksonRead;
    private static long jsonIoWriteFails;
    private static long gsonWriteFails;
    private static long jacksonWriteFails;
    private static long jsonIoReadFails;
    private static long gsonReadFails;
    private static long jacksonReadFails;
    private static long totalReads;
    private static long totalWrites;
    @Getter
    private static final boolean debug = false;

    /**
     * Ensure that the passed in source contains all the Strings passed in the 'contains' parameter AND
     * that they appear in the order they are passed in.  This is a better check than simply asserting
     * that a particular error message contains a set of tokens...it also ensures the order in which the
     * tokens appear.  If the strings passed in do not appear in the same order within the source string,
     * an assertion failure happens.  Finally, the Strings are NOT compared with case sensitivity.  This is
     * useful for testing exception message text - ensuring that key values are within the message, without
     * copying the exact message into the test.  This allows more freedom for the author of the code being
     * tested, where changes to the error message would be less likely to break the test.
     * @param source String source string to test, for example, an exception error message being tested.
     * @param contains String comma separated list of Strings that must appear in the source string.  Furthermore,
     * the strings in the contains comma separated list must appear in the source string, in the same order as they
     * are passed in.
     */
    public static void assertContainsIgnoreCase(String source, String... contains)
    {
        String lowerSource = source.toLowerCase();
        for (String contain : contains)
        {
            int idx = lowerSource.indexOf(contain.toLowerCase());
            String msg = "'" + contain + "' not found in '" + lowerSource + "'";
            assert idx >=0 : msg;
            lowerSource = lowerSource.substring(idx);
        }
    }

    /**
     * Ensure that the passed in source contains all the Strings passed in the 'contains' parameter AND
     * that they appear in the order they are passed in.  This is a better check than simply asserting
     * that a particular error message contains a set of tokens...it also ensures the order in which the
     * tokens appear.  If the strings passed in do not appear in the same order within the source string,
     * false is returned, otherwise true is returned. Finally, the Strings are NOT compared with case sensitivity.
     * This is useful for testing exception message text - ensuring that key values are within the message, without
     * copying the exact message into the test.  This allows more freedom for the author of the code being
     * tested, where changes to the error message would be less likely to break the test.
     * @param source String source string to test, for example, an exception error message being tested.
     * @param contains String comma separated list of Strings that must appear in the source string.  Furthermore,
     * the strings in the contains comma separated list must appear in the source string, in the same order as they
     * are passed in.
     */
    public static boolean checkContainsIgnoreCase(String source, String... contains)
    {
        String lowerSource = source.toLowerCase();
        for (String contain : contains)
        {
            int idx = lowerSource.indexOf(contain.toLowerCase());
            if (idx == -1)
            {
                return false;
            }
            lowerSource = lowerSource.substring(idx);
        }
        return true;
    }
}
