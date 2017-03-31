/**
 * Copyright 2017 The OpenTracing Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.opentracing.contrib.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Helpers to create Map in a single line of code.
 */
public class MapMaker {
    /**
     *
     * @param keyAndValue
     */
    public static Map<String, Object> fields(Object... keyAndValue){
        int lg = keyAndValue.length / 2;
        HashMap<String, Object> m = new HashMap<>();
        for(int i = 0; i < lg; i++) {
            m.put(String.valueOf(keyAndValue[i*2]), keyAndValue[i*2+1]);
        }
        return m;
    }
}
