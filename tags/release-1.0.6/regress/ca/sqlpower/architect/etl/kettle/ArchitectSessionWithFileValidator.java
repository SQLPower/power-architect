/*
 * Copyright (c) 2010, SQL Power Group Inc.
 *
 * This file is part of SQL Power Architect.
 *
 * SQL Power Architect is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * SQL Power Architect is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.architect.etl.kettle;

import ca.sqlpower.architect.ArchitectSessionContext;
import ca.sqlpower.architect.TestingArchitectSession;
import ca.sqlpower.architect.TestingArchitectSessionContext;
import ca.sqlpower.util.UserPrompter;
import ca.sqlpower.util.UserPrompter.UserPromptOptions;
import ca.sqlpower.util.UserPrompter.UserPromptResponse;

/**
 * This implementation of the {@link TestingArchitectSessionContext} is used to allow the file
 * validator return type to be specified by the test. This is for testing how the saving of
 * Kettle jobs reacts if the a file is canceled or selected to not be saved.
 */
class ArchitectSessionWithFileValidator extends TestingArchitectSession {
    
    private UserPromptResponse fvr;

    public ArchitectSessionWithFileValidator(ArchitectSessionContext context) {
        super(context);
    }
    
    public void setReponse(UserPromptResponse fvr) {
        this.fvr = fvr;
    }

    @Override
    public UserPrompter createUserPrompter(String question, UserPromptType responseType, UserPromptOptions optionType, UserPromptResponse defaultResponseType,
            Object defaultResponse, String ... buttonNames) {
        return new UserPrompter() {
            public Object getUserSelectedResponse() {
                if (fvr == UserPromptResponse.OK) {
                    return true;
                } else {
                    return false;
                }
            }

            public UserPromptResponse promptUser(Object... formatArgs) {
                return fvr;
            }
        };
    }
}
