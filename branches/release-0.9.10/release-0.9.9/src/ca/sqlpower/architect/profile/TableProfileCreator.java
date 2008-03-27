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

package ca.sqlpower.architect.profile;

import ca.sqlpower.util.Monitorable;

/**
 * Service Provider Interface for creating profile results related to a
 * particular database table.  Different implementations can use different
 * strategies that perform better under certain circumstances, sacrifice
 * accuracy for performance, and so on.
 */
public interface TableProfileCreator {

    /**
     * Populates the given {@link TableProfileResult} (which must not be
     * populated already) and all of its {@link ColumnProfileResult} children.
     * <p>
     * This may be a long-running operation.  Its progress can be monitored and
     * canceled via the TableProfileResult itself, which is {@link Monitorable}.
     * 
     * @param tpr The unpopulated profile result to populate.
     * @return true if the profiling of the table and its columns has completed
     * normally; false if the profiling operation was canceled before it had a
     * chance to complete.
     * @throws RuntimeException If the profiling fails for some reason other than
     * a cancel request, an appropriate exception will be thrown.
     */
    public boolean doProfile(TableProfileResult tpr);
}
