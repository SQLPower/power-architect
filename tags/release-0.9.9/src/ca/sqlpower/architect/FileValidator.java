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

/**
 *  This interface is used for handling when a file is to be created
 *   but already exists. This class can be implemented to handle
 *   file overwrites in different ways, such as overwriting or not. 
 *   One example is the AlwaysAcceptFileValidator which always
 *   returns a WRITE_OK response.
 *   
 *   This class is also used to handle the desired overwriting when
 *   saving to a repository.
 */
public interface FileValidator {

    public static enum FileValidationResponse {WRITE_OK                 //This response denotes that the file should be overwritten
        
                                               , WRITE_NOT_OK           //This response denotes that the file should not be overwritten
                                               
                                               , CANCEL                 //This response denotes that the file should not be overwritten
                                                                        //and no more files should be written to
                                               
                                               , WRITE_OK_ALWAYS        //This response denotes that the file and all remaining files
                                                                        //should be overwritten
                                               
                                               , WRITE_NOT_OK_ALWAYS    //This response denotes that no further files should be overwritten
                                                                        //including the current one
                                               }

    /**
     * This method decides if a given file should be overwritten or not
     * based on the string name and its path.
     */
    public FileValidationResponse acceptFile (String name, String path);
}
