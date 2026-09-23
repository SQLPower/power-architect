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

/**
 * Helpers for the free-form per-table notes that are appended to the generated
 * DDL as SQL comments. The notes are always emitted as {@code --} line
 * comments (one line comment per input line), so they can never terminate or
 * corrupt the surrounding SQL statements.
 */
public final class DDLCommentUtils {

    private DDLCommentUtils() {
    }

    /**
     * Validates free-form notes. Returns {@code null} if the notes are safe to
     * embed, or a human-readable reason otherwise.
     */
    public static String validate(String notes) {
        if (notes == null || notes.isEmpty()) return null;
        for (int i = 0; i < notes.length(); i++) {
            char c = notes.charAt(i);
            if (c == '\n' || c == '\r' || c == '\t') continue;
            if (Character.isISOControl(c)) {
                return "Control character (U+" + Integer.toHexString(c)
                        + ") is not allowed in DDL notes.";
            }
        }
        return null;
    }

    /**
     * Formats notes as a block of {@code --} line comments. Each line is
     * prefixed independently so embedded newlines cannot leak into the SQL.
     *
     * @return the comment block, or an empty string if there is nothing to add.
     */
    public static String toCommentBlock(String notes) {
        if (notes == null || notes.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        String[] lines = notes.split("\\r?\\n", -1);
        for (String line : lines) {
            sb.append("-- ").append(line).append('\n');
        }
        return sb.toString();
    }
}