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

package org.quiltmc.json5.impl.parser.json5;
/**
 * Based on https://github.com/json5/json5/blob/master/lib/util.js
 * You may find the original code here: https://github.com/jimblackler/usejson
 */
final class Util {
	static boolean isSpaceSeparator(char c) {
		return Unicode.SPACE_SEPARATOR.matcher(String.valueOf(c)).matches();
	}

	public static boolean isIdStartChar(char c) {
		return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c == '$') || (c == '_')
				|| Unicode.ID_START.matcher(String.valueOf(c)).matches();
	}

	public static boolean isIdContinueChar(char c) {
		return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || (c == '$')
				|| (c == '_') || (c == '\u200C') || (c == '\u200D')
				|| Unicode.ID_CONTINUE.matcher(String.valueOf(c)).matches();
	}

	public static boolean isDigit(char c) {
		return c >= '0' && c <= '9';
	}

	public static boolean isHexDigit(char c) {
		return isDigit(c) || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
	}
}