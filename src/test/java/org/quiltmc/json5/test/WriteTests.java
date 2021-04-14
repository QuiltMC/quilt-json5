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

import org.junit.jupiter.api.Test;
import org.quiltmc.json5.api.JsonApi;
import org.quiltmc.json5.api.visitor.writer.JsonWriter;

import java.io.IOException;
import java.io.StringWriter;

class WriteTests {
	@Test()
	void write() throws IOException {
		StringWriter w = new StringWriter();

		try (JsonWriter writer = JsonApi.writer(w)) {
			writer.comment("Top comment\nLook mom, multiple lines from one string\nin the input!")
					.comment("This one, however, was a different call to comment().")
					.writeObject()
					.comment("If strict mode was off this would be \"value\" instead.")
					.writeArray("value", "Isn't this pretty printing nice!")
					.comment("This is the maximum value of a signed long")
					.write(Long.MAX_VALUE, "(Long.MAX_VALUE in java)")
					.write(Double.NaN, "NaN is only allowed when strict mode is off")
					.write("a string\nwith multiline", "Strings are sanitized how you would expect.")
					.comment("But unfortunately our array journey has come  to a close :(")
					.pop()
					.comment("Surprise! Another value approaches!")
					.write("another_value", "chicken nuggets" )
					.comment("Wow this sure is a lot of comments huh")
					.pop()
					.comment("Glad that's over.");

			// Yes you should flush your writers, but string writer does not need it.
			// writer.flush();
		}

		System.out.println(w);
	}
}
