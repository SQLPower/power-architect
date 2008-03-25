/*
 * Copyright (c) 2008, SQL Power Group Inc.
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
package ca.sqlpower.architect;

import java.text.MessageFormat;

/**
 * The UserPrompter interface is a UI-neutral way for core business objects to
 * solicit a response from the user. When operating in a GUI environment, the
 * UserPrompter implementation could use a modal dialog to prompt the user. When
 * operating in a command line environment, a textual prompt could be used. In a
 * headless environment where prompting is not possible, the responses can be
 * hardwired to a reasonable default. For example, the AlwaysOKUserPrompter
 * always responds as if the user said OK.
 * <p>
 * An example use case is when a file to be created but already exists. The
 * business logic can be implemented to ask the user how to handle the problem,
 * such as overwriting, not overwriting, or canceling the operation completely.
 * The advantage of using this interface is that the business logic remains
 * uncommitted to any particular user interface library.
 */
public interface UserPrompter {

    /**
     * An enumeration of all the possible responses from the user.
     */
    public static enum UserPromptResponse {

        /**
         * Denotes an affirmative response from the user, meaning
         * the current part of the operation should proceed. 
         */
        OK,
        
        /**
         * Denotes a negative response from the user, meaning that
         * the current part of the operation should not proceed.
         */
        NOT_OK,
        
        /**
         * Denotes that the user wants to cancel the entire operation.
         * <p>
         * The difference between {@link #NOT_OK} and this response is
         * that, for instance, if the entire operation consists of writing
         * a series of 7 files, and the 3rd file already exists, if the
         * user response is NOT_OK, the process will skip the 3rd file and
         * continue to the 4th (possibly prompting again for that file if
         * necessary) whereas if the user response is CANCEL, the process
         * will stop completely and there will be no additional prompts.
         * <p>
         * Another example is the typical sequence of events when the user
         * tries to quit the program when their work is not saved: A response
         * of NOT_OK would mean to not save the project but quit the program
         * anyway (thus losing work); a response of CANCEL would mean to
         * not save the work, but also not quit the program.
         */
        CANCEL;
    }

    /**
     * Obtains the decision for how a given operation should proceed. When
     * possible, this will be done by prompting the user. If there is no user
     * available (for example, in a headless environment), a reasonable default
     * response will be provided.
     * 
     * @param formatArgs
     *            The arguments to the insert into the format string in the
     *            question. See {@link MessageFormat} for details.
     */
    public UserPromptResponse promptUser(Object ... formatArgs);
}
