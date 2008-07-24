/*
// $Id: //open/util/resgen/example/source/Birthday.java#2 $
// package org.eigenbase.resgen is an i18n resource generator
// Copyright (C) 2005-2005 The Eigenbase Project
// Copyright (C) 2005-2005 Disruptive Tech
// Copyright (C) 2005-2005 Red Square, Inc.
// Portions Copyright (C) 2002-2005 Kana Software, Inc. and others.
// All Rights Reserved.
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful, but
// WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
// or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
// License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with this library; if not, write to the Free Software Foundation, Inc.,
// 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
//
// ResGen example code.
*/
import happy.BirthdayResource;

import java.util.Locale;

public class Birthday {
    static void wishHappyBirthday(String name, int age) {
        if (age < 0) {
            throw BirthdayResource.instance().newTooYoung(name);
        }
        System.out.println(BirthdayResource.instance().getHappyBirthday(name, new Integer(age)));
    }
    public static void main(String[] args) {
        wishHappyBirthday("Fred", 33);
        try {
            wishHappyBirthday("Wilma", -3);
        } catch (Throwable e) {
            System.out.println("Received " + e);
        }
        BirthdayResource.setThreadLocale(Locale.FRANCE);
        wishHappyBirthday("Pierre", 22);
    }
}
