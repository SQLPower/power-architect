/*
 * Copyright (c) 2007, SQL Power Group Inc.
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *     * Neither the name of SQL Power Group Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
