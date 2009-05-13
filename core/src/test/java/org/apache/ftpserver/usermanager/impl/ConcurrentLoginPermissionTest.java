/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.ftpserver.usermanager.impl;

import junit.framework.TestCase;

import org.apache.ftpserver.usermanager.impl.ConcurrentLoginPermission;
import org.apache.ftpserver.usermanager.impl.ConcurrentLoginRequest;

/**
*
* @author The Apache MINA Project (dev@mina.apache.org)*
*/
public class ConcurrentLoginPermissionTest extends TestCase {

    public void testCanAuthorize() {
        ConcurrentLoginPermission permission = new ConcurrentLoginPermission(4,
                2);
        ConcurrentLoginRequest request = new ConcurrentLoginRequest(1, 1);

        assertTrue(permission.canAuthorize(request));
    }

    public void testAllowBoth() {
        ConcurrentLoginPermission permission = new ConcurrentLoginPermission(4,
                2);
        ConcurrentLoginRequest request = new ConcurrentLoginRequest(1, 1);

        assertNotNull(permission.authorize(request));
    }

    public void testMaxValuesBoth() {
        ConcurrentLoginPermission permission = new ConcurrentLoginPermission(4,
                2);
        ConcurrentLoginRequest request = new ConcurrentLoginRequest(4, 2);

        assertNotNull(permission.authorize(request));
    }

    public void testMaxLoginsTooLarge() {
        ConcurrentLoginPermission permission = new ConcurrentLoginPermission(4,
                2);
        ConcurrentLoginRequest request = new ConcurrentLoginRequest(5, 2);

        assertNull(permission.authorize(request));
    }

    public void testMaxLoginsPerIPTooLarge() {
        ConcurrentLoginPermission permission = new ConcurrentLoginPermission(4,
                2);
        ConcurrentLoginRequest request = new ConcurrentLoginRequest(3, 3);

        assertNull(permission.authorize(request));
    }

    public void testAllowAnyMaxLogins() {
        ConcurrentLoginPermission permission = new ConcurrentLoginPermission(0,
                2);
        ConcurrentLoginRequest request = new ConcurrentLoginRequest(5, 2);

        assertNotNull(permission.authorize(request));
    }

    public void testAllowAnyMaxLoginsPerIP() {
        ConcurrentLoginPermission permission = new ConcurrentLoginPermission(4,
                0);
        ConcurrentLoginRequest request = new ConcurrentLoginRequest(3, 3);

        assertNotNull(permission.authorize(request));
    }

}
