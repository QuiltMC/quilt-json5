package org.quiltmc.json5.parser;

import org.jetbrains.annotations.NotNull;
import org.quiltmc.json5.api.JsonArrayVisitor;
import org.quiltmc.json5.api.JsonObjectVisitor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Test {
	public static void main(String[] args) throws IOException {
		new Json5Parser().parseObject(new String(Files.readAllBytes(Paths.get("quilt.mod.json5"))), new QMJVisitor());
	}
}


class QMJVisitor implements JsonObjectVisitor {

	@Override
	public @NotNull JsonObjectVisitor visitObject(String name) {
		System.out.println("object: " + name);
		return new QMJVisitor();
	}

	@Override
	public @NotNull JsonArrayVisitor visitArray(String name) {
		System.out.println("array: " + name);
		return new QMJArrayVisitor();
	}

	@Override
	public void visitString(String name, String value) {
		System.out.println("string: " + name + " " + value);
	}

	@Override
	public void visitBoolean(String name, boolean value) {
		System.out.println("boolean: " + name + " " + value);
	}

	@Override
	public void visitNumber(String name, Number value) {
		System.out.println("number: " + name + " " + value);
	}

	@Override
	public void visitNull(String name) {
		System.out.println("null: " + name);
	}
}

class QMJArrayVisitor implements JsonArrayVisitor {

	@Override
	public @NotNull JsonObjectVisitor visitObject() {
		System.out.println("object");
		return new QMJVisitor();
	}

	@Override
	public @NotNull JsonArrayVisitor visitArray() {
		System.out.println("array");
		return new QMJArrayVisitor();
	}

	@Override
	public void visitString(String string) {
		System.out.println("string: " + string);
	}

	@Override
	public void visitBoolean(boolean bool) {
		System.out.println("boolean: " + bool);
	}

	@Override
	public void visitNumber(Number number) {
		System.out.println("number: " + number);
	}

	@Override
	public void visitNull() {
		System.out.println("null");
	}
}
