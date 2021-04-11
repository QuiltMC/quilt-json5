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

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.json5.api.visitor.JsonVisitor;
import org.quiltmc.json5.api.exception.InternalParserException;
import org.quiltmc.json5.api.exception.InvalidSyntaxException;
import org.quiltmc.json5.impl.wrapper.ArrayWrapper;
import org.quiltmc.json5.impl.wrapper.ObjectWrapper;
import org.quiltmc.json5.impl.wrapper.VisitorWrapper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.logging.Logger;

import static java.lang.Integer.parseInt;

/**
 * JSON 5 parser.
 * Based on https://github.com/json5/json5/blob/master/lib/parse.js
 * You may find the original code here: https://github.com/jimblackler/usejson
 * It has been modified to be fitted under the JsonVisitor
 */
@ApiStatus.Internal
public final class Json5Parser {
	private static final Logger LOG = Logger.getLogger(Json5Parser.class.getName());

	private final StringBuilder buffer = new StringBuilder();
	private final String source;
	private final JsonVisitor vRoot;
	boolean initializedRoot = false;
	private State parseState = State.START;
	private final Deque<VisitorWrapper> vList = new LinkedList<>();
	private int pos = 0;
	private int line = 1;
	private int column = 0;
	private Token token = null;
	private State lexState = null;
	private boolean doubleQuote;
	private int sign;
	private Character c;
	private String key;


	public Json5Parser(String text, JsonVisitor visitor) {
		this.source = text;
		this.vRoot = visitor;
	}

	public void parse() {
		do {
			token = lex();

			parseStates();
		} while (token.getType() != TokenType.EOF);
	}

	static String formatChar(char c) {
		switch (c) {
			case '\'':
				return "\\'";
			case '"':
				return "\\\"";
			case '\\':
				return "\\\\";
			case '\b':
				return "\\b";
			case '\f':
				return "\\f";
			case '\n':
				return "\\n";
			case '\r':
				return "\\r";
			case '\t':
				return "\\t";
			// case '\\v':
			//  return "\\v";
			case '\0':
				return "\\0";
			case '\u2028':
				return "\\u2028";
			case '\u2029':
				return "\u2029";
		}

		if (c < ' ') {
			String hexString = Integer.toString(c, 16);
			return "\\x" + ("00" + hexString).substring(hexString.length());
		}

		return String.valueOf(c);
	}

	private Token lex() {
		lexState = State.DEFAULT;
		buffer.setLength(0);
		doubleQuote = false;
		sign = 1;

		while (true) {
			c = peek();

			token = lexStates(lexState);
			if (token != null) {
				return token;
			}
		}
	}

	private Character peek() {
		try {
			return source.charAt(pos);
		} catch (StringIndexOutOfBoundsException e) {
			return null;
		}
	}

	private Character read() {
		Character c = peek();

		if (c == null) {
			column++;
		} else if (c == '\n') {
			line++;
			column = 0;
		} else {
			column += 1;
		}
		if (c != null) {
			pos += 1;
		}
		return c;
	}

	private Token lexStates(State state) {
		switch (state) {
			case DEFAULT:
				if (c == null) {
					read();
					return new Token(TokenType.EOF);
				}

				switch (c) {
					case '\t':
						// case '\v':
					case '\f':
					case ' ':
					case '\u00A0':
					case '\uFEFF':
					case '\n':
					case '\r':
					case '\u2028':
					case '\u2029':
						read();
						return null;

					case '/':
						read();
						lexState = State.COMMENT;
						return null;
				}
				if (Util.isSpaceSeparator(c)) {
					read();
					return null;
				}

				return lexStates(parseState);

			case COMMENT:
				switch (c) {
					case '*':
						read();
						lexState = State.MULTI_LINE_COMMENT;
						return null;

					case '/':
						read();
						lexState = State.SINGLE_LINE_COMMENT;
						return null;
				}

				throw invalidChar(read());

			case MULTI_LINE_COMMENT:
				if (c == null) {
					throw invalidChar(read());
				}
				if (c == '*') {
					read();
					lexState = State.MULTI_LINE_COMMENT_ASTERISK;
					return null;
				}
				read();
				return null;

			case MULTI_LINE_COMMENT_ASTERISK:
				if (c == null) {
					throw invalidChar(read());
				}
				switch (c) {
					case '*':
						read();
						return null;

					case '/':
						read();
						lexState = State.DEFAULT;
						return null;
				}

				read();
				lexState = State.MULTI_LINE_COMMENT;
				return null;

			case SINGLE_LINE_COMMENT:
				if (c == null) {
					read();
					return new Token(TokenType.EOF);
				}

				switch (c) {
					case '\n':
					case '\r':
					case '\u2028':
					case '\u2029':
						read();
						lexState = State.DEFAULT;
						return null;
				}

				read();
				break;

			case VALUE:
				switch (c) {
					case '{':
					case '[':
						return new Token(TokenType.PUNCTUATOR, read());

					case 'n':
						read();
						literal("ull");
						return new Token(TokenType.NULL, null);

					case 't':
						read();
						literal("rue");
						return new Token(TokenType.BOOLEAN, true);

					case 'f':
						read();
						literal("alse");
						return new Token(TokenType.BOOLEAN, false);

					case '-':
					case '+':
						if (read() == '-') {
							sign = -1;
						}

						lexState = State.SIGN;
						return null;

					case '.':
						buffer.setLength(0);
						buffer.append(read().charValue());
						lexState = State.DECIMAL_POINT_LEADING;
						return null;

					case '0':
						buffer.setLength(0);
						buffer.append(read().charValue());
						lexState = State.ZERO;
						return null;

					case '1':
					case '2':
					case '3':
					case '4':
					case '5':
					case '6':
					case '7':
					case '8':
					case '9':
						buffer.setLength(0);
						buffer.append(read().charValue());
						lexState = State.DECIMAL_INTEGER;
						return null;

					case 'I':
						read();
						literal("nfinity");
						return new Token(TokenType.NUMERIC, Double.POSITIVE_INFINITY);

					case 'N':
						read();
						literal("aN");
						return new Token(TokenType.NUMERIC, Double.NaN);

					case '"':
					case '\'':
						doubleQuote = (read() == '\"');
						buffer.setLength(0);
						lexState = State.STRING;
						return null;
				}
				throw invalidChar(read());

			case IDENTIFIER_NAME_START_ESCAPE:
				if (c != 'u') {
					throw invalidChar(read());
				}

				read();
				char u = unicodeEscape();
				switch (u) {
					case '$':
					case '_':
						break;

					default:
						if (!Util.isIdStartChar(u)) {
							throw invalidIdentifier();
						}

						break;
				}

				buffer.append(u);
				lexState = State.IDENTIFIER_NAME;
				break;

			case IDENTIFIER_NAME:
				switch (c) {
					case '$':
					case '_':
					case '\u200C':
					case '\u200D':
						buffer.append(read().charValue());
						return null;

					case '\\':
						read();
						lexState = State.IDENTIFIER_NAME_ESCAPE;
						return null;
				}

				if (Util.isIdContinueChar(c)) {
					buffer.append(read().charValue());
					return null;
				}

				return new Token(TokenType.IDENTIFIER, buffer.toString());

			case IDENTIFIER_NAME_ESCAPE:
				if (c != 'u') {
					throw invalidChar(read());
				}

				read();
				char u1 = unicodeEscape();
				switch (u1) {
					case '$':
					case '_':
					case '\u200C':
					case '\u200D':
						break;

					default:
						if (!Util.isIdContinueChar(u1)) {
							throw invalidIdentifier();
						}

						break;
				}

				buffer.append(u1);
				lexState = State.IDENTIFIER_NAME;
				break;

			case SIGN:
				switch (c) {
					case '.':
						buffer.setLength(0);
						buffer.append(read().charValue());
						lexState = State.DECIMAL_POINT_LEADING;
						return null;

					case '0':
						buffer.setLength(0);
						buffer.append(read().charValue());
						lexState = State.ZERO;
						return null;

					case '1':
					case '2':
					case '3':
					case '4':
					case '5':
					case '6':
					case '7':
					case '8':
					case '9':
						buffer.setLength(0);
						buffer.append(read().charValue());
						lexState = State.DECIMAL_INTEGER;
						return null;

					case 'I':
						read();
						literal("nfinity");
						return new Token(TokenType.NUMERIC, sign * Double.POSITIVE_INFINITY);

					case 'N':
						read();
						literal("aN");
						return new Token(TokenType.NUMERIC, Double.NaN);
				}

				throw invalidChar(read());

			case ZERO:
				if (c != null) {
					switch (c) {
						case '.':
							buffer.append(read().charValue());
							lexState = State.DECIMAL_POINT;
							return null;

						case 'e':
						case 'E':
							buffer.append(read().charValue());
							lexState = State.DECIMAL_EXPONENT;
							return null;

						case 'x':
						case 'X':
							buffer.append(read().charValue());
							lexState = State.HEXADECIMAL;
							return null;
					}
				}
				return new Token(TokenType.NUMERIC, new BigDecimal(0d * sign));
			case DECIMAL_INTEGER:
				if (c != null) {
					switch (c) {
						case '.':
							buffer.append(read().charValue());
							lexState = State.DECIMAL_POINT;
							return null;
						case 'e':
						case 'E':
							buffer.append(read().charValue());
							lexState = State.DECIMAL_EXPONENT;
							return null;
					}

					if (Util.isDigit(c)) {
						buffer.append(read().charValue());
						return null;
					}
				}
				return new Token(TokenType.NUMERIC,
						new BigInteger(buffer.toString()).multiply(BigInteger.valueOf(sign)));

			case DECIMAL_POINT_LEADING:
				if (c != null) {
					if (Util.isDigit(c)) {
						buffer.append(read().charValue());
						lexState = State.DECIMAL_FRACTION;
						return null;
					}
				}
				throw invalidChar(read());

			case DECIMAL_POINT:
				switch (c) {
					case 'e':
					case 'E':
						buffer.append(read().charValue());
						lexState = State.DECIMAL_EXPONENT;
						return null;
				}

				if (Util.isDigit(c)) {
					buffer.append(read().charValue());
					lexState = State.DECIMAL_FRACTION;
					return null;
				}
				String toParse = buffer.toString();
				if (toParse.endsWith(".")) {
					toParse = toParse.substring(0, toParse.length() - 1);
				}
				// BigDecimal doesn't handle negative 0 properly so we have to special-case that
				if (toParse.equals("0")) {
					return new Token(TokenType.NUMERIC, 0d * sign);
				}
				return new Token(TokenType.NUMERIC,
						new BigDecimal(toParse).multiply(BigDecimal.valueOf(sign)));


			case DECIMAL_FRACTION:
				if (c != null) {
					switch (c) {
						case 'e':
						case 'E':
							buffer.append(read().charValue());
							lexState = State.DECIMAL_EXPONENT;
							return null;
					}

					if (Util.isDigit(c)) {
						buffer.append(read().charValue());
						return null;
					}
				}
				return new Token(TokenType.NUMERIC, sign * Double.parseDouble(buffer.toString()));

			case DECIMAL_EXPONENT:
				if (c != null) {
					switch (c) {
						case '+':
						case '-':
							buffer.append(read().charValue());
							lexState = State.DECIMAL_EXPONENT_SIGN;
							return null;
					}

					if (Util.isDigit(c)) {
						buffer.append(read().charValue());
						lexState = State.DECIMAL_EXPONENT_INTEGER;
						return null;
					}
				}
				throw invalidChar(read());

			case DECIMAL_EXPONENT_SIGN:
				if (Util.isDigit(c)) {
					buffer.append(read().charValue());
					lexState = State.DECIMAL_EXPONENT_INTEGER;
					return null;
				}
				throw invalidChar(read());

			case DECIMAL_EXPONENT_INTEGER:
				if (c != null) {
					if (Util.isDigit(c)) {
						buffer.append(read().charValue());
						return null;
					}
				}
				return new Token(TokenType.NUMERIC,
						BigDecimal.valueOf(sign).multiply(new BigDecimal(buffer.toString())));

			case HEXADECIMAL:
				if (Util.isHexDigit(c)) {
					buffer.append(read().charValue());
					lexState = State.HEXADECIMAL_INTEGER;
					return null;
				}
				throw invalidChar(read());

			case HEXADECIMAL_INTEGER:
				if (c != null) {
					if (Util.isHexDigit(c)) {
						buffer.append(read().charValue());
						return null;
					}
				}
				return new Token(TokenType.NUMERIC, new BigInteger(buffer.substring(2), 16).multiply(BigInteger.valueOf(sign)));

			case STRING:
				if (c == null) {
					throw invalidChar(read());
				}
				switch (c) {
					case '\\':
						read();
						buffer.append(escape());
						return null;

					case '"':
						if (doubleQuote) {
							read();
							return new Token(TokenType.STRING, buffer.toString());
						}

						buffer.append(read().charValue());
						return null;

					case '\'':
						if (!doubleQuote) {
							read();
							return new Token(TokenType.STRING, buffer.toString());
						}

						buffer.append(read().charValue());
						return null;

					case '\n':
					case '\r':
						throw invalidChar(read());

					case '\u2028':
					case '\u2029':
						separatorChar(c);
						break;
				}

				buffer.append(read().charValue());
				break;

			case START:
				switch (c) {
					case '{':
					case '[':
						return new Token(TokenType.PUNCTUATOR, read());
				}

				lexState = State.VALUE;
				break;

			case BEFORE_PROPERTY_NAME:
				switch (c) {
					case '$':
					case '_':
						buffer.setLength(0);
						buffer.append(read().charValue());
						lexState = State.IDENTIFIER_NAME;
						return null;

					case '\\':
						read();
						lexState = State.IDENTIFIER_NAME_START_ESCAPE;
						return null;

					case '}':
						return new Token(TokenType.PUNCTUATOR, read());

					case '"':
					case '\'':
						doubleQuote = (read() == '\"');
						lexState = State.STRING;
						return null;
				}

				if (Util.isIdStartChar(c)) {
					buffer.append(read().charValue());
					lexState = State.IDENTIFIER_NAME;
					return null;
				}

				throw invalidChar(read());

			case AFTER_PROPERTY_NAME:
				if (c == ':') {
					return new Token(TokenType.PUNCTUATOR, read());
				}
				throw invalidChar(read());

			case BEFORE_PROPERTY_VALUE:
				lexState = State.VALUE;
				break;

			case AFTER_PROPERTY_VALUE:
				switch (c) {
					case ',':
					case '}':
						return new Token(TokenType.PUNCTUATOR, read());
				}

				throw invalidChar(read());

			case BEFORE_ARRAY_VALUE:
				if (c == ']') {
					return new Token(TokenType.PUNCTUATOR, read());
				}
				lexState = State.VALUE;
				break;

			case AFTER_ARRAY_VALUE:
				switch (c) {
					case ',':
					case ']':
						return new Token(TokenType.PUNCTUATOR, read());
				}

				throw invalidChar(read());

			case END:
				throw invalidChar(read());
			default:
				throw new InternalParserException("Unknown state: " + state.name());
		}

		return null;
	}

	private void literal(String s) {
		for (char c : s.toCharArray()) {
			Character p = peek();

			if (p != c) {
				throw invalidChar(read());
			}

			read();
		}
	}

	private char escape() {
		Character c = peek();
		if (c == null) {
			throw invalidChar(read());
		}
		switch (c) {
			case 'b':
				read();
				return '\b';

			case 'f':
				read();
				return '\f';

			case 'n':
				read();
				return '\n';

			case 'r':
				read();
				return '\r';

			case 't':
				read();
				return '\t';

			case 'v':
				read();
				return '\013';

			case '0':
				read();
				if (Util.isDigit(peek())) {
					throw invalidChar(read());
				}

				return '\0';

			case 'x':
				read();
				return hexEscape();

			case 'u':
				read();
				return unicodeEscape();

			case '\n':
			case '\u2028':
			case '\u2029':
				read();
				return 0;

			case '\r':
				read();
				if (peek() == '\n') {
					read();
				}

				return 0;

			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9':
				throw invalidChar(read());
		}

		return read();
	}

	char hexEscape() {
		StringBuilder buffer = new StringBuilder();
		Character c = peek();

		if (!Util.isHexDigit(c)) {
			throw invalidChar(read());
		}

		buffer.append(read().charValue());

		c = peek();
		if (!Util.isHexDigit(c)) {
			throw invalidChar(read());
		}

		buffer.append(read().charValue());

		return (char) parseInt(buffer.toString(), 16);
	}

	char unicodeEscape() {
		StringBuilder buffer = new StringBuilder();
		int count = 4;

		while (count-- > 0) {
			Character c = peek();
			if (!Util.isHexDigit(c)) {
				throw invalidChar(read());
			}

			buffer.append(read().charValue());
		}

		return (char) parseInt(buffer.toString(), 16);
	}

	private void parseStates() {
		switch (parseState) {
			case START:
			case BEFORE_PROPERTY_VALUE:
				if (token.getType() == TokenType.EOF) {
					throw invalidEOF();
				}

				push();
				break;

			case BEFORE_PROPERTY_NAME:
				switch (token.getType()) {
					case IDENTIFIER:
					case STRING:
						key = token.getValue().toString();
						parseState = State.AFTER_PROPERTY_NAME;
						return;

					case PUNCTUATOR:
						pop();
						return;

					case EOF:
						throw invalidEOF();
				}
				break;

			case AFTER_PROPERTY_NAME:
				if (token.getType() == TokenType.EOF) {
					throw invalidEOF();
				}
				parseState = State.BEFORE_PROPERTY_VALUE;
				break;

			case BEFORE_ARRAY_VALUE:
				if (token.getType() == TokenType.EOF) {
					throw invalidEOF();
				}

				if (token.getType() == TokenType.PUNCTUATOR && ((Character) token.getValue()) == ']') {
					pop();
					return;
				}

				push();
				break;

			case AFTER_PROPERTY_VALUE:
				if (token.getType() == TokenType.EOF) {
					throw invalidEOF();
				}

				switch ((Character) token.getValue()) {
					case ',':
						parseState = State.BEFORE_PROPERTY_NAME;
						return;

					case '}':
						pop();
				}
				break;

			case AFTER_ARRAY_VALUE:
				if (token.getType() == TokenType.EOF) {
					throw invalidEOF();
				}

				switch ((Character) token.getValue()) {
					case ',':
						parseState = State.BEFORE_ARRAY_VALUE;
						return;

					case ']':
						pop();
				}
				break;

			case END:
				break;
		}
	}

	private void push() {
		Type type;
		Object value;
		switch (token.getType()) {
			case PUNCTUATOR:
				Object token = this.token.getValue();
				switch ((Character) token) {
					case '{':
						type = Type.OBJECT;
						value = null;
						break;

					case '[':
						value = null;
						type = Type.ARRAY;
						break;

					default:
						throw new InternalParserException();
				}
				break;
			case NULL:
				type = Type.NULL;
				value = this.token.getValue();
				break;
			case BOOLEAN:
				type = Type.BOOLEAN;
				value = this.token.getValue();
				break;
			case NUMERIC:
				type = Type.NUMBER;
				value = this.token.getValue();
				break;
			case STRING:
				type = Type.STRING;
				value = this.token.getValue();
				break;
			default:
				throw new InternalParserException();
		}

		if (vList.isEmpty()) {
			if (initializedRoot) {
				throw new InternalParserException("There are somehow two roots? Something is very wrong!");
			}
			switch (type) {
				case NULL:
					vRoot.rootNull();
					break;
				case BOOLEAN:
					vRoot.rootBoolean((Boolean) value);
					break;
				case NUMBER:
					vRoot.rootNumber((Number) value);
					break;
				case STRING:
					vRoot.rootString((String) value);
					break;
				case OBJECT:
					vList.add(VisitorWrapper.create(vRoot.rootObject()));
					break;
				case ARRAY:
					vList.add(VisitorWrapper.create(vRoot.rootArray()));
					break;
			}
			initializedRoot = true;
		} else {
			VisitorWrapper visitor = vList.getLast();
			boolean object = visitor instanceof ObjectWrapper;
			String key = object ? this.key : null;
			switch (type) {
				case NULL:
					visitor.visitNull(key);
					break;
				case BOOLEAN:
					visitor.visitBoolean(key, (Boolean) value);
					break;
				case NUMBER:
					visitor.visitNumber(key, (Number) value);
					break;
				case STRING:
					visitor.visitString(key, (String) value);
					break;
				case OBJECT:
					vList.add(visitor.visitObject(key));
					break;
				case ARRAY:
					vList.add(visitor.visitArray(key));
					break;
			}
		}

		if (type == Type.OBJECT || type == Type.ARRAY) {
			if (type == Type.ARRAY) {
				parseState = State.BEFORE_ARRAY_VALUE;
			} else {
				parseState = State.BEFORE_PROPERTY_NAME;
			}
		} else {
			VisitorWrapper current = vList.peekLast();
			if (current == null) {
				parseState = State.END;
			} else if (current instanceof ArrayWrapper) {
				parseState = State.AFTER_ARRAY_VALUE;
			} else {
				parseState = State.AFTER_PROPERTY_VALUE;
			}
		}

	}

	private void pop() {
		vList.removeLast();

		try {
			VisitorWrapper current = vList.getLast();
			if (current instanceof ArrayWrapper) {
				parseState = State.AFTER_ARRAY_VALUE;
			} else {
				parseState = State.AFTER_PROPERTY_VALUE;
			}
		} catch (NoSuchElementException ex) {
			parseState = State.END;
		}
	}

	private InvalidSyntaxException invalidChar(Character c) {
		if (c == null) {
			return new InvalidSyntaxException("JSON5: invalid end of input at " + line + ":" + column);
		}

		return new InvalidSyntaxException(
				"JSON5: invalid character '" + formatChar(c) + "' at " + line + ":" + column);
	}

	private InvalidSyntaxException invalidEOF() {
		return new InvalidSyntaxException("JSON5: invalid end of input at " + line + ":" + column);
	}

	private InvalidSyntaxException invalidIdentifier() {
		column -= 5;
		return new InvalidSyntaxException("JSON5: invalid identifier character at " + line + ":" + column);
	}

	private void separatorChar(char c) {
		LOG.warning(
				"JSON5: '" + formatChar(c) + "' in strings is not valid ECMAScript; consider escaping");
	}

	private enum TokenType { EOF, PUNCTUATOR, NULL, BOOLEAN, NUMERIC, STRING, IDENTIFIER }

	private enum State {
		DEFAULT,
		COMMENT,
		MULTI_LINE_COMMENT,
		MULTI_LINE_COMMENT_ASTERISK,
		SINGLE_LINE_COMMENT,
		VALUE,
		IDENTIFIER_NAME_START_ESCAPE,
		IDENTIFIER_NAME,
		IDENTIFIER_NAME_ESCAPE,
		SIGN,
		ZERO,
		DECIMAL_INTEGER,
		DECIMAL_POINT_LEADING,
		DECIMAL_POINT,
		DECIMAL_FRACTION,
		DECIMAL_EXPONENT,
		DECIMAL_EXPONENT_SIGN,
		DECIMAL_EXPONENT_INTEGER,
		HEXADECIMAL,
		HEXADECIMAL_INTEGER,
		STRING,
		START,
		BEFORE_PROPERTY_NAME,
		AFTER_PROPERTY_NAME,
		BEFORE_PROPERTY_VALUE,
		AFTER_PROPERTY_VALUE,
		BEFORE_ARRAY_VALUE,
		AFTER_ARRAY_VALUE,
		END
	}

	private static class Token {
		private final TokenType type;
		private final Object value;

		Token(TokenType type) {
			this.type = type;
			value = null;
		}

		Token(TokenType type, Object value) {
			this.type = type;
			this.value = value;
		}

		private TokenType getType() {
			return type;
		}

		public Object getValue() {
			return value;
		}
	}

	private enum Type {
		NULL,
		BOOLEAN,
		NUMBER,
		STRING,
		OBJECT,
		ARRAY
	}
}