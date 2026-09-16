/*
 * Copyright (c) 2026, SQL Power Group Inc.
 *
 * This file is part of Power*Architect.
 *
 * Power*Architect is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*Architect is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.sqlpower.architect.ddl;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A small, dependency-free parser for {@code CREATE TABLE} statements. It is
 * intentionally lenient about vendor-specific details: it understands the
 * common structure of PostgreSQL, MySQL, SQL Server and Oracle DDL, ignores
 * comments ({@code --} and {@code /* *}{@code /}), quoted identifiers
 * ({@code "x"}, {@code `x`}, {@code [x]}), and reports errors with a line and
 * column.
 *
 * <p>Only {@code CREATE TABLE} is interpreted; any other statement in the
 * input is skipped.
 */
public class DDLParser {

    private final String sql;
    private int pos;
    private int line = 1;
    private int column = 1;

    private List<Token> tokens;
    private int tokenIndex;

    public DDLParser(String sql) {
        this.sql = sql == null ? "" : sql;
    }

    /**
     * Parses the whole input and returns one {@link ParsedTable} per
     * {@code CREATE TABLE} statement found.
     *
     * @throws DDLParseException if a CREATE TABLE statement is malformed.
     */
    public List<ParsedTable> parse() throws DDLParseException {
        tokens = tokenize();
        tokenIndex = 0;
        List<ParsedTable> tables = new ArrayList<ParsedTable>();
        while (!atEnd()) {
            if (peekIsKeyword("CREATE")) {
                tables.add(parseCreateTable());
            } else {
                skipStatement();
            }
        }
        return tables;
    }

    // ------------------------------------------------------------------
    // Tokenizer
    // ------------------------------------------------------------------

    private enum TokenType {
        WORD, QUOTED_IDENTIFIER, STRING, NUMBER, SYMBOL
    }

    private static final class Token {
        final TokenType type;
        final String text;
        final int line;
        final int column;

        Token(TokenType type, String text, int line, int column) {
            this.type = type;
            this.text = text;
            this.line = line;
            this.column = column;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    private List<Token> tokenize() {
        List<Token> result = new ArrayList<Token>();
        int n = sql.length();
        while (pos < n) {
            char c = sql.charAt(pos);

            if (c == '\n') {
                pos++;
                line++;
                column = 1;
                continue;
            }
            if (Character.isWhitespace(c)) {
                advance();
                continue;
            }
            // -- line comment
            if (c == '-' && pos + 1 < n && sql.charAt(pos + 1) == '-') {
                while (pos < n && sql.charAt(pos) != '\n') advance();
                continue;
            }
            // /* block comment */
            if (c == '/' && pos + 1 < n && sql.charAt(pos + 1) == '*') {
                advance();
                advance();
                while (pos < n && !(sql.charAt(pos) == '*' && pos + 1 < n && sql.charAt(pos + 1) == '/')) {
                    if (sql.charAt(pos) == '\n') {
                        pos++;
                        line++;
                        column = 1;
                    } else {
                        advance();
                    }
                }
                if (pos < n) {
                    advance();
                    advance();
                }
                continue;
            }
            int tokenLine = line;
            int tokenCol = column;
            if (c == '\'' || c == '"' || c == '`') {
                result.add(readQuoted(c, tokenLine, tokenCol));
            } else if (c == '[') {
                result.add(readBracketed(tokenLine, tokenCol));
            } else if (Character.isLetter(c) || c == '_' || c == '@' || c == '#' || c == '$') {
                result.add(readWord(tokenLine, tokenCol));
            } else if (Character.isDigit(c) || (c == '.' && pos + 1 < n && Character.isDigit(sql.charAt(pos + 1)))) {
                result.add(readNumber(tokenLine, tokenCol));
            } else {
                result.add(new Token(TokenType.SYMBOL, String.valueOf(c), tokenLine, tokenCol));
                advance();
            }
        }
        return result;
    }

    private void advance() {
        pos++;
        column++;
    }

    private Token readQuoted(char quote, int tokenLine, int tokenCol) {
        advance(); // opening quote
        StringBuilder sb = new StringBuilder();
        int n = sql.length();
        while (pos < n) {
            char c = sql.charAt(pos);
            if (c == quote) {
                // doubled quote is an escaped quote
                if (pos + 1 < n && sql.charAt(pos + 1) == quote) {
                    sb.append(quote);
                    advance();
                    advance();
                    continue;
                }
                advance();
                break;
            }
            if (c == '\n') {
                sb.append(c);
                pos++;
                line++;
                column = 1;
            } else {
                sb.append(c);
                advance();
            }
        }
        TokenType type = quote == '\'' ? TokenType.STRING : TokenType.QUOTED_IDENTIFIER;
        return new Token(type, sb.toString(), tokenLine, tokenCol);
    }

    private Token readBracketed(int tokenLine, int tokenCol) {
        advance(); // [
        StringBuilder sb = new StringBuilder();
        int n = sql.length();
        while (pos < n && sql.charAt(pos) != ']') {
            sb.append(sql.charAt(pos));
            advance();
        }
        if (pos < n) advance(); // ]
        return new Token(TokenType.QUOTED_IDENTIFIER, sb.toString(), tokenLine, tokenCol);
    }

    private Token readWord(int tokenLine, int tokenCol) {
        StringBuilder sb = new StringBuilder();
        int n = sql.length();
        while (pos < n) {
            char c = sql.charAt(pos);
            if (Character.isLetterOrDigit(c) || c == '_' || c == '@' || c == '#' || c == '$') {
                sb.append(c);
                advance();
            } else {
                break;
            }
        }
        return new Token(TokenType.WORD, sb.toString(), tokenLine, tokenCol);
    }

    private Token readNumber(int tokenLine, int tokenCol) {
        StringBuilder sb = new StringBuilder();
        int n = sql.length();
        while (pos < n) {
            char c = sql.charAt(pos);
            if (Character.isDigit(c) || c == '.') {
                sb.append(c);
                advance();
            } else {
                break;
            }
        }
        return new Token(TokenType.NUMBER, sb.toString(), tokenLine, tokenCol);
    }

    // ------------------------------------------------------------------
    // Parser
    // ------------------------------------------------------------------

    private boolean atEnd() {
        return tokenIndex >= tokens.size();
    }

    private Token peek() {
        return atEnd() ? null : tokens.get(tokenIndex);
    }

    private Token next() {
        return atEnd() ? null : tokens.get(tokenIndex++);
    }

    private boolean peekIsKeyword(String keyword) {
        Token t = peek();
        return t != null && t.type == TokenType.WORD && t.text.equalsIgnoreCase(keyword);
    }

    private boolean peekIsSymbol(String symbol) {
        Token t = peek();
        return t != null && t.type == TokenType.SYMBOL && t.text.equals(symbol);
    }

    private void expectSymbol(String symbol) throws DDLParseException {
        if (!peekIsSymbol(symbol)) {
            throw error("Expected '" + symbol + "'");
        }
        next();
    }

    private String readIdentifier() throws DDLParseException {
        Token t = peek();
        if (t == null || (t.type != TokenType.WORD && t.type != TokenType.QUOTED_IDENTIFIER)) {
            throw error("Expected an identifier");
        }
        next();
        return t.text;
    }

    private void skipStatement() {
        while (!atEnd()) {
            if (peekIsSymbol(";")) {
                next();
                return;
            }
            next();
        }
    }

    private ParsedTable parseCreateTable() throws DDLParseException {
        next(); // CREATE
        // optional OR REPLACE / GLOBAL / LOCAL / TEMP / TEMPORARY
        while (peek() != null && peek().type == TokenType.WORD) {
            String w = peek().text.toUpperCase(Locale.ROOT);
            if (w.equals("OR") || w.equals("REPLACE") || w.equals("GLOBAL") || w.equals("LOCAL")
                    || w.equals("TEMP") || w.equals("TEMPORARY")) {
                next();
            } else {
                break;
            }
        }
        if (!peekIsKeyword("TABLE")) {
            throw error("Expected TABLE");
        }
        next();

        // optional IF NOT EXISTS
        if (peekIsKeyword("IF")) {
            next();
            if (peekIsKeyword("NOT")) next();
            if (peekIsKeyword("EXISTS")) next();
        }

        ParsedTable table = new ParsedTable();

        // qualified name: [catalog.]schema.name
        List<String> nameParts = new ArrayList<String>();
        nameParts.add(readIdentifier());
        while (peekIsSymbol(".")) {
            next();
            nameParts.add(readIdentifier());
        }
        if (nameParts.size() >= 3) {
            table.catalog = nameParts.get(nameParts.size() - 3);
            table.schema = nameParts.get(nameParts.size() - 2);
            table.name = nameParts.get(nameParts.size() - 1);
        } else if (nameParts.size() == 2) {
            table.schema = nameParts.get(0);
            table.name = nameParts.get(1);
        } else {
            table.name = nameParts.get(0);
        }

        expectSymbol("(");
        while (!peekIsSymbol(")")) {
            parseTableItem(table);
            if (peekIsSymbol(",")) {
                next();
            } else if (!peekIsSymbol(")")) {
                throw error("Expected ',' or ')'");
            }
        }
        expectSymbol(")");

        // Skip trailing table options up to the statement terminator.
        skipStatement();
        return table;
    }

    private static final String[] TABLE_CONSTRAINT_STARTERS = {
        "CONSTRAINT", "PRIMARY", "UNIQUE", "FOREIGN", "CHECK", "KEY", "INDEX"
    };

    private void parseTableItem(ParsedTable table) throws DDLParseException {
        Token t = peek();
        if (t == null) throw error("Unexpected end of input");

        String upper = t.text.toUpperCase(Locale.ROOT);
        for (String starter : TABLE_CONSTRAINT_STARTERS) {
            if (upper.equals(starter)) {
                parseTableConstraint(table);
                return;
            }
        }
        parseColumnDefinition(table);
    }

    private void parseTableConstraint(ParsedTable table) throws DDLParseException {
        // optional CONSTRAINT name
        if (peekIsKeyword("CONSTRAINT")) {
            next();
            readIdentifier();
        }
        if (peekIsKeyword("PRIMARY")) {
            next();
            if (peekIsKeyword("KEY")) next();
            skipOptionalIndexType();
            List<String> cols = parseIdentifierList();
            table.primaryKeyColumns.addAll(cols);
        } else if (peekIsKeyword("UNIQUE")) {
            next();
            if (peekIsKeyword("KEY")) next();
            skipOptionalIndexType();
            ParsedUniqueConstraint unique = new ParsedUniqueConstraint();
            unique.columns = parseIdentifierList();
            table.uniqueConstraints.add(unique);
        } else if (peekIsKeyword("FOREIGN")) {
            next();
            if (peekIsKeyword("KEY")) next();
            ParsedForeignKey fk = new ParsedForeignKey();
            fk.columns = parseIdentifierList();
            if (peekIsKeyword("REFERENCES")) {
                next();
                parseReferences(fk);
            }
            table.foreignKeys.add(fk);
        } else if (peekIsKeyword("CHECK")) {
            next();
            skipParenthesized();
        } else {
            // KEY / INDEX or anything else: skip to the next comma at depth 0
            skipToNextComma();
        }
    }

    private void skipOptionalIndexType() {
        if (peekIsKeyword("USING")) {
            next();
            if (peek() != null) next();
        }
    }

    private List<String> parseIdentifierList() throws DDLParseException {
        expectSymbol("(");
        List<String> cols = new ArrayList<String>();
        while (!peekIsSymbol(")")) {
            cols.add(readIdentifier());
            // Some dialects allow ASC/DESC and prefix lengths inside the list.
            if (peekIsKeyword("ASC") || peekIsKeyword("DESC")) next();
            if (peekIsSymbol("(")) skipParenthesized();
            if (peekIsSymbol(",")) {
                next();
            } else if (!peekIsSymbol(")")) {
                throw error("Expected ',' or ')' in identifier list");
            }
        }
        expectSymbol(")");
        return cols;
    }

    private void parseReferences(ParsedForeignKey fk) throws DDLParseException {
        List<String> refParts = new ArrayList<String>();
        refParts.add(readIdentifier());
        while (peekIsSymbol(".")) {
            next();
            refParts.add(readIdentifier());
        }
        fk.referencedTable = refParts.get(refParts.size() - 1);
        if (peekIsSymbol("(")) {
            fk.referencedColumns = parseIdentifierList();
        }
        // ON DELETE / ON UPDATE actions are ignored.
    }

    private static final String[] COLUMN_CONSTRAINT_STARTERS = {
        "NOT", "NULL", "DEFAULT", "PRIMARY", "UNIQUE", "CHECK", "REFERENCES",
        "CONSTRAINT", "COLLATE", "COMMENT", "GENERATED", "AUTO_INCREMENT",
        "AUTOINCREMENT", "IDENTITY", "ENCODING"
    };

    private void parseColumnDefinition(ParsedTable table) throws DDLParseException {
        ParsedColumn col = new ParsedColumn();
        col.name = readIdentifier();

        // data type: identifiers, optional (params), optional modifiers such as
        // "WITH TIME ZONE" or "UNSIGNED", until a constraint keyword or comma.
        StringBuilder typeName = new StringBuilder();
        boolean sawParams = false;
        while (!atEnd()) {
            Token t = peek();
            if (t.type == TokenType.SYMBOL && (t.text.equals(",") || t.text.equals(")"))) {
                break;
            }
            if (t.type == TokenType.WORD) {
                String upper = t.text.toUpperCase(Locale.ROOT);
                if (isColumnConstraintKeyword(upper) && typeName.length() > 0) {
                    break;
                }
                if (sawParams) {
                    // e.g. "WITH TIME ZONE" after "TIMESTAMP(6)"
                    typeName.append(' ').append(t.text);
                    next();
                    continue;
                }
                if (typeName.length() > 0) typeName.append(' ');
                typeName.append(t.text);
                next();
            } else if (t.type == TokenType.SYMBOL && t.text.equals("(")) {
                sawParams = true;
                typeName.append(parseTypeParameters());
            } else {
                break;
            }
        }
        col.typeName = typeName.toString().trim();

        // column constraints
        while (!atEnd()) {
            Token t = peek();
            if (t.type == TokenType.SYMBOL && (t.text.equals(",") || t.text.equals(")"))) {
                break;
            }
            if (peekIsKeyword("NOT")) {
                next();
                if (peekIsKeyword("NULL")) next();
                col.nullable = false;
            } else if (peekIsKeyword("NULL")) {
                next();
                col.nullable = true;
            } else if (peekIsKeyword("DEFAULT")) {
                next();
                col.defaultValue = readDefaultExpression();
            } else if (peekIsKeyword("PRIMARY")) {
                next();
                if (peekIsKeyword("KEY")) next();
                col.primaryKey = true;
                col.nullable = false;
            } else if (peekIsKeyword("UNIQUE")) {
                next();
                if (peekIsKeyword("KEY")) next();
                col.unique = true;
            } else if (peekIsKeyword("CHECK")) {
                next();
                col.checkExpression = readParenthesizedText();
            } else if (peekIsKeyword("REFERENCES")) {
                next();
                ParsedForeignKey fk = new ParsedForeignKey();
                fk.columns.add(col.name);
                parseReferences(fk);
                table.foreignKeys.add(fk);
            } else if (peekIsKeyword("CONSTRAINT")) {
                next();
                readIdentifier();
            } else if (peekIsKeyword("COLLATE")) {
                next();
                readIdentifier();
            } else if (peekIsKeyword("COMMENT")) {
                next();
                if (peek() != null && peek().type == TokenType.STRING) next();
            } else if (peekIsKeyword("AUTO_INCREMENT") || peekIsKeyword("AUTOINCREMENT")
                    || peekIsKeyword("IDENTITY")) {
                next();
            } else if (peekIsKeyword("GENERATED")) {
                // GENERATED ALWAYS AS (...) / GENERATED BY DEFAULT AS IDENTITY
                skipToNextCommaOrParen();
            } else {
                // Unknown token: consume it so we don't loop forever.
                next();
            }
        }

        if (col.name != null && !col.name.isEmpty()) {
            table.columns.add(col);
            if (col.primaryKey && !table.primaryKeyColumns.contains(col.name)) {
                table.primaryKeyColumns.add(col.name);
            }
        }
    }

    private boolean isColumnConstraintKeyword(String upper) {
        for (String s : COLUMN_CONSTRAINT_STARTERS) {
            if (upper.equals(s)) return true;
        }
        return false;
    }

    /**
     * Reads a type parameter list like {@code (10)} or {@code (10, 2)} and
     * stores precision/scale on the column via the returned text.
     */
    private String parseTypeParameters() throws DDLParseException {
        // Reuse the text so the caller can keep the full type spelling.
        int startLine = line;
        int startCol = column;
        StringBuilder sb = new StringBuilder();
        expectSymbol("(");
        sb.append('(');
        int depth = 1;
        while (!atEnd() && depth > 0) {
            Token t = next();
            if (t.type == TokenType.SYMBOL && t.text.equals("(")) depth++;
            if (t.type == TokenType.SYMBOL && t.text.equals(")")) depth--;
            if (depth == 0) {
                sb.append(')');
            } else {
                if (t.type == TokenType.SYMBOL && t.text.equals(",")) {
                    sb.append(", ");
                } else {
                    if (sb.length() > 1 && !sb.toString().endsWith("(") && !sb.toString().endsWith(", ")) {
                        sb.append(' ');
                    }
                    sb.append(t.text);
                }
            }
        }
        if (depth != 0) throw new DDLParseException("Unterminated type parameters", startLine, startCol);
        return sb.toString();
    }

    private String readDefaultExpression() {
        StringBuilder sb = new StringBuilder();
        int depth = 0;
        while (!atEnd()) {
            Token t = peek();
            if (t.type == TokenType.SYMBOL) {
                if (t.text.equals("(")) depth++;
                if (t.text.equals(")")) {
                    if (depth == 0) break;
                    depth--;
                }
                if (t.text.equals(",") && depth == 0) break;
            }
            if (t.type == TokenType.WORD && depth == 0 && isColumnConstraintKeyword(t.text.toUpperCase(Locale.ROOT))) {
                break;
            }
            if (sb.length() > 0) sb.append(' ');
            sb.append(t.text);
            next();
        }
        return sb.toString().trim();
    }

    private String readParenthesizedText() throws DDLParseException {
        StringBuilder sb = new StringBuilder();
        expectSymbol("(");
        int depth = 1;
        while (!atEnd() && depth > 0) {
            Token t = next();
            if (t.type == TokenType.SYMBOL && t.text.equals("(")) depth++;
            if (t.type == TokenType.SYMBOL && t.text.equals(")")) depth--;
            if (depth == 0) break;
            if (sb.length() > 0) sb.append(' ');
            sb.append(t.text);
        }
        return sb.toString().trim();
    }

    private void skipParenthesized() {
        int depth = 0;
        while (!atEnd()) {
            Token t = next();
            if (t.type == TokenType.SYMBOL && t.text.equals("(")) depth++;
            if (t.type == TokenType.SYMBOL && t.text.equals(")")) {
                depth--;
                if (depth == 0) return;
            }
        }
    }

    private void skipToNextComma() {
        int depth = 0;
        while (!atEnd()) {
            Token t = peek();
            if (t.type == TokenType.SYMBOL && t.text.equals("(")) depth++;
            if (t.type == TokenType.SYMBOL && t.text.equals(")")) {
                if (depth == 0) return;
                depth--;
            }
            if (t.type == TokenType.SYMBOL && t.text.equals(",") && depth == 0) return;
            next();
        }
    }

    private void skipToNextCommaOrParen() {
        int depth = 0;
        while (!atEnd()) {
            Token t = peek();
            if (t.type == TokenType.SYMBOL && t.text.equals("(")) depth++;
            if (t.type == TokenType.SYMBOL && t.text.equals(")")) {
                if (depth == 0) return;
                depth--;
            }
            if (t.type == TokenType.SYMBOL && t.text.equals(",") && depth == 0) return;
            next();
        }
    }

    private DDLParseException error(String message) {
        Token t = peek();
        if (t != null) {
            return new DDLParseException(message, t.line, t.column);
        }
        return new DDLParseException(message, line, column);
    }

    // ------------------------------------------------------------------
    // Parsed model
    // ------------------------------------------------------------------

    public static class ParsedTable {
        public String catalog;
        public String schema;
        public String name;
        public final List<ParsedColumn> columns = new ArrayList<ParsedColumn>();
        public final List<String> primaryKeyColumns = new ArrayList<String>();
        public final List<ParsedForeignKey> foreignKeys = new ArrayList<ParsedForeignKey>();
        public final List<ParsedUniqueConstraint> uniqueConstraints = new ArrayList<ParsedUniqueConstraint>();

        @Override
        public String toString() {
            return name;
        }
    }

    public static class ParsedColumn {
        public String name;
        public String typeName = "";
        public boolean nullable = true;
        public String defaultValue;
        public boolean primaryKey;
        public boolean unique;
        public String checkExpression;

        @Override
        public String toString() {
            return name + " " + typeName;
        }
    }

    public static class ParsedForeignKey {
        public List<String> columns = new ArrayList<String>();
        public String referencedTable;
        public List<String> referencedColumns = new ArrayList<String>();
    }

    public static class ParsedUniqueConstraint {
        public List<String> columns = new ArrayList<String>();
    }

    /**
     * Signals a syntax error in the input DDL, with the location.
     */
    public static class DDLParseException extends Exception {
        private static final long serialVersionUID = 1L;
        private final int errorLine;
        private final int errorColumn;

        public DDLParseException(String message, int line, int column) {
            super(message + " (line " + line + ", column " + column + ")");
            this.errorLine = line;
            this.errorColumn = column;
        }

        public int getErrorLine() {
            return errorLine;
        }

        public int getErrorColumn() {
            return errorColumn;
        }
    }
}