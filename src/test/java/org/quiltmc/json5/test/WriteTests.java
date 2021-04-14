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

package org.quiltmc.json5.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.quiltmc.json5.api.JsonApi;
import org.quiltmc.json5.api.stream.JsonStreamWriter;

import java.io.IOException;
import java.io.StringWriter;

class WriteTests {
	@Test
	void write() throws IOException {
		StringWriter w = new StringWriter();

		try (JsonStreamWriter writer = JsonApi.streamWriter(w)) {
			sampleWrite(writer);
			// Yes you should flush your writers, but string writer does not need it.
			// writer.flush();
		}

		System.out.println(w);
	}

	@Test
	void writeStrict() {
		// Fails
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			StringWriter w = new StringWriter();

			try (JsonStreamWriter writer = JsonApi.streamWriter(w)) {
				writer.setStrict(true);
				sampleWrite(writer);
				// Yes you should flush your writers, but string writer does not need it.
				// writer.flush();
			}

			System.out.println(w);
		});
	}

	@Test
	void writeCompat() throws IOException {
		StringWriter w = new StringWriter();

		try (JsonStreamWriter writer = JsonApi.streamWriter(w)) {
			writer.setCompact();
			sampleWrite(writer);
			// Yes you should flush your writers, but string writer does not need it.
			// writer.flush();
		}

		// This only works on // but other ones could be added later
		if (w.toString().contains("//")) {
			throw new AssertionError("Comments were found in the output");
		}

		System.out.println(w);
	}

	@Test
	void writeStrictAndCompact() {
		// Fails
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			StringWriter w = new StringWriter();

			try (JsonStreamWriter writer = JsonApi.streamWriter(w)) {
				writer.setStrict(true);
				writer.setCompact();
				sampleWrite(writer);
				// Yes you should flush your writers, but string writer does not need it.
				// writer.flush();
			}

			System.out.println(w);
		});
	}

	static void sampleWrite(JsonStreamWriter writer) throws IOException {
		writer.comment("Top comment\nLook mom, multiple lines from one string\nin the input!")
				.comment("This one, however, was a different call to comment().")
				.beginObject()
					.comment("If strict mode was off this would be \"value\" instead.")
					.name("value").beginArray()
						.blockComment("Isn't this pretty printing nice!")
						.comment("This is the maximum value of a signed long")
						.value(Long.MAX_VALUE).blockComment("(Long.MAX_VALUE in java)")
						.value(Double.NaN).blockComment("NaN is only allowed when strict mode is off")
						.value("a string\nwith multiline").blockComment("Strings are sanitized how you would expect.")
						.comment("But unfortunately our array journey has come to a close :(")
					.endArray()
					.comment("Surprise! Another value approaches!")
					.name("another_value").value("chicken nuggets")
					.comment("Wow this sure is a lot of comments huh")
				.endObject()
				.comment("Glad that's over.");
	}
}
