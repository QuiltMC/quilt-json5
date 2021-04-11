/*
 * Copyright 2021 QuiltMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.quiltmc.json5.api.stream;

import org.quiltmc.json5.api.JsonToken;

/*
 * This API is meant to be a drop-in replacement for GSON's JsonReader API. The internals are
 * quite different from the original; only the public methods are preserved.
 * Gson is Copyright (C) 2010 Google Inc, under the Apache License Version 2.0 (the same as in the header above).
 * You may view the original, including its license header, here:
 * https://github.com/google/gson/blob/530cb7447089ccc12dc2009c17f468ddf2cd61ca/gson/src/main/java/com/google/gson/stream/JsonReader.java
 */
// TODO: document
public interface JsonStreamReader extends AutoCloseable {
	// we don't have lenient
	void beginArray();
	void endArray();
	void beginObject();
	void endObject();
	boolean hasNext();
	JsonToken peek();
	String nextName();
	String nextString();
	boolean nextBoolean();
	void nextNull();
	double nextDouble();
	long nextLong();
	int nextInt();
	void close();
	void skipValue();
	//String getPath(); // this is a pain to implement, will do it if anybody asks
}
