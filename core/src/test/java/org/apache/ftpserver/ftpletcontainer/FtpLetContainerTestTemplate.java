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

package org.apache.ftpserver.ftpletcontainer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.ftpserver.FtpSessionImpl;
import org.apache.ftpserver.ftplet.FileSystemFactory;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.ftplet.FtpSession;
import org.apache.ftpserver.ftplet.FtpStatistics;
import org.apache.ftpserver.ftplet.Ftplet;
import org.apache.ftpserver.ftplet.FtpletContext;
import org.apache.ftpserver.ftplet.FtpletResult;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.impl.FtpRequestImpl;

public abstract class FtpLetContainerTestTemplate extends TestCase {

    private FtpletContainer container = createFtpletContainer();

    private final List<String> calls = new ArrayList<String>();

    protected void setUp() throws Exception {
        MockFtplet.callback = new MockFtpletCallback();
        MockFtpletCallback.returnValue = FtpletResult.DEFAULT;
    }

    protected abstract FtpletContainer createFtpletContainer();

    private static class MockFtpletContext implements FtpletContext {
        public FileSystemFactory getFileSystemManager() {
            return null;
        }

        public FtpStatistics getFtpStatistics() {
            return null;
        }

        public Ftplet getFtplet(String name) {
            return null;
        }

        public UserManager getUserManager() {
            return null;
        }
    }
    
    public void testAddAndGetFtplet() throws FtpException {
        MockFtplet ftplet1 = new MockFtplet();
        MockFtplet ftplet2 = new MockFtplet();

        assertNull(container.getFtplet("ftplet1"));
        assertNull(container.getFtplet("ftplet2"));

        container.addFtplet("ftplet1", ftplet1);
        container.addFtplet("ftplet2", ftplet2);
        
        assertSame(ftplet1, container.getFtplet("ftplet1"));
        assertSame(ftplet2, container.getFtplet("ftplet2"));
    }
    
    public void testFtpletLifecyclePreContainerInit() throws FtpException {
        MockFtplet ftplet = new MockFtplet();

        container.addFtplet("ftplet1", ftplet);
        
        // ftplet should be initialized before the container is
        assertNull(ftplet.context);
        container.init(new MockFtpletContext());
        assertNotNull(ftplet.context);
        
        // make sure ftplets get's destroyed
        assertFalse(ftplet.destroyed);
        
        container.destroy();

        assertTrue(ftplet.destroyed);
        
    }

    public void testFtpletLifecyclePostContainerInit() throws FtpException {
        MockFtplet ftplet = new MockFtplet();

        assertNull(ftplet.context);
        container.init(new MockFtpletContext());

        container.addFtplet("ftplet1", ftplet);
        
        assertNotNull(ftplet.context);
        
        // make sure ftplets get's destroyed
        assertFalse(ftplet.destroyed);
        
        container.destroy();

        assertTrue(ftplet.destroyed);
        
    }

    
    public void testAddFtpletWithDuplicateName() throws FtpException {
        MockFtplet ftplet1 = new MockFtplet();
        MockFtplet ftplet2 = new MockFtplet();

        assertNull(container.getFtplet("ftplet1"));

        container.addFtplet("ftplet1", ftplet1);

        try {
            container.addFtplet("ftplet1", ftplet2);
            fail("IllegalArgumentException must be thrown");
        } catch (IllegalArgumentException e) {
            // ok
        }

        assertSame(ftplet1, container.getFtplet("ftplet1"));
    }

    public void testRemoveFtplet() throws FtpException {
        MockFtplet ftplet1 = new MockFtplet();
        MockFtplet ftplet2 = new MockFtplet();

        container.addFtplet("ftplet1", ftplet1);
        container.addFtplet("ftplet2", ftplet2);

        assertSame(ftplet1, container.getFtplet("ftplet1"));
        assertSame(ftplet2, container.getFtplet("ftplet2"));

        assertSame(ftplet1, container.removeFtplet("ftplet1"));

        assertNull(container.getFtplet("ftplet1"));
        assertSame(ftplet2, container.getFtplet("ftplet2"));

        assertNull(container.removeFtplet("ftplet1"));
    }

    public void testOnConnect() throws FtpException, IOException {
        MockFtplet ftplet1 = new MockFtplet() {
            public FtpletResult onConnect(FtpSession session)
                    throws FtpException, IOException {
                calls.add("ftplet1");
                return super.onConnect(session);
            }
        };
        MockFtplet ftplet2 = new MockFtplet() {
            public FtpletResult onConnect(FtpSession session)
                    throws FtpException, IOException {
                calls.add("ftplet2");
                return super.onConnect(session);
            }
        };

        container.addFtplet("ftplet1", ftplet1);
        container.addFtplet("ftplet2", ftplet2);

        container.onConnect(new FtpSessionImpl(null));

        assertEquals(2, calls.size());
        assertEquals("ftplet1", calls.get(0));
        assertEquals("ftplet2", calls.get(1));
    }

    public void testOnDisconnect() throws FtpException, IOException {
        MockFtplet ftplet1 = new MockFtplet() {
            public FtpletResult onDisconnect(FtpSession session)
                    throws FtpException, IOException {
                calls.add("ftplet1");
                return super.onDisconnect(session);
            }
        };
        MockFtplet ftplet2 = new MockFtplet() {
            public FtpletResult onDisconnect(FtpSession session)
                    throws FtpException, IOException {
                calls.add("ftplet2");
                return super.onDisconnect(session);
            }
        };

        container.addFtplet("ftplet1", ftplet1);
        container.addFtplet("ftplet2", ftplet2);

        container.onDisconnect(new FtpSessionImpl(null));

        assertEquals(2, calls.size());
        assertEquals("ftplet1", calls.get(0));
        assertEquals("ftplet2", calls.get(1));
    }

    public void testOnLogin() throws FtpException, IOException {
        MockFtplet ftplet1 = new MockFtplet() {
            public FtpletResult onLogin(FtpSession session, FtpRequest request)
                    throws FtpException, IOException {
                calls.add("ftplet1");
                return super.onLogin(session, request);
            }
        };
        MockFtplet ftplet2 = new MockFtplet() {
            public FtpletResult onLogin(FtpSession session, FtpRequest request)
                    throws FtpException, IOException {
                calls.add("ftplet2");
                return super.onLogin(session, request);
            }
        };

        container.addFtplet("ftplet1", ftplet1);
        container.addFtplet("ftplet2", ftplet2);

        container.afterCommand(new FtpSessionImpl(null), new FtpRequestImpl(
                "PASS"));

        assertEquals(2, calls.size());
        assertEquals("ftplet1", calls.get(0));
        assertEquals("ftplet2", calls.get(1));
    }

    public void testOnDeleteStart() throws FtpException, IOException {
        MockFtplet ftplet1 = new MockFtplet() {
            public FtpletResult onDeleteStart(FtpSession session,
                    FtpRequest request) throws FtpException, IOException {
                calls.add("ftplet1");
                return super.onDeleteStart(session, request);
            }
        };
        MockFtplet ftplet2 = new MockFtplet() {
            public FtpletResult onDeleteStart(FtpSession session,
                    FtpRequest request) throws FtpException, IOException {
                calls.add("ftplet2");
                return super.onDeleteStart(session, request);
            }
        };

        container.addFtplet("ftplet1", ftplet1);
        container.addFtplet("ftplet2", ftplet2);

        container.beforeCommand(new FtpSessionImpl(null), new FtpRequestImpl(
                "DELE"));

        assertEquals(2, calls.size());
        assertEquals("ftplet1", calls.get(0));
        assertEquals("ftplet2", calls.get(1));
    }

    public void testOnDeleteEnd() throws FtpException, IOException {
        MockFtplet ftplet1 = new MockFtplet() {
            public FtpletResult onDeleteEnd(FtpSession session, FtpRequest request)
                    throws FtpException, IOException {
                calls.add("ftplet1");
                return super.onDeleteEnd(session, request);
            }
        };
        MockFtplet ftplet2 = new MockFtplet() {
            public FtpletResult onDeleteEnd(FtpSession session, FtpRequest request)
                    throws FtpException, IOException {
                calls.add("ftplet2");
                return super.onDeleteEnd(session, request);
            }
        };

        container.addFtplet("ftplet1", ftplet1);
        container.addFtplet("ftplet2", ftplet2);

        container.afterCommand(new FtpSessionImpl(null), new FtpRequestImpl(
                "DELE"));

        assertEquals(2, calls.size());
        assertEquals("ftplet1", calls.get(0));
        assertEquals("ftplet2", calls.get(1));
    }

    public void testOnUploadStart() throws FtpException, IOException {
        MockFtplet ftplet1 = new MockFtplet() {
            public FtpletResult onUploadStart(FtpSession session,
                    FtpRequest request) throws FtpException, IOException {
                calls.add("ftplet1");
                return super.onUploadStart(session, request);
            }
        };
        MockFtplet ftplet2 = new MockFtplet() {
            public FtpletResult onUploadStart(FtpSession session,
                    FtpRequest request) throws FtpException, IOException {
                calls.add("ftplet2");
                return super.onUploadStart(session, request);
            }
        };

        container.addFtplet("ftplet1", ftplet1);
        container.addFtplet("ftplet2", ftplet2);

        container.beforeCommand(new FtpSessionImpl(null), new FtpRequestImpl(
                "STOR"));

        assertEquals(2, calls.size());
        assertEquals("ftplet1", calls.get(0));
        assertEquals("ftplet2", calls.get(1));
    }

    public void testOnUploadEnd() throws FtpException, IOException {
        MockFtplet ftplet1 = new MockFtplet() {
            public FtpletResult onUploadEnd(FtpSession session, FtpRequest request)
                    throws FtpException, IOException {
                calls.add("ftplet1");
                return super.onUploadEnd(session, request);
            }
        };
        MockFtplet ftplet2 = new MockFtplet() {
            public FtpletResult onUploadEnd(FtpSession session, FtpRequest request)
                    throws FtpException, IOException {
                calls.add("ftplet2");
                return super.onUploadEnd(session, request);
            }
        };

        container.addFtplet("ftplet1", ftplet1);
        container.addFtplet("ftplet2", ftplet2);

        container.afterCommand(new FtpSessionImpl(null), new FtpRequestImpl(
                "STOR"));

        assertEquals(2, calls.size());
        assertEquals("ftplet1", calls.get(0));
        assertEquals("ftplet2", calls.get(1));
    }

    public void testOnDownloadStart() throws FtpException, IOException {
        MockFtplet ftplet1 = new MockFtplet() {
            public FtpletResult onDownloadStart(FtpSession session,
                    FtpRequest request) throws FtpException, IOException {
                calls.add("ftplet1");
                return super.onDownloadStart(session, request);
            }
        };
        MockFtplet ftplet2 = new MockFtplet() {
            public FtpletResult onDownloadStart(FtpSession session,
                    FtpRequest request) throws FtpException, IOException {
                calls.add("ftplet2");
                return super.onDownloadStart(session, request);
            }
        };

        container.addFtplet("ftplet1", ftplet1);
        container.addFtplet("ftplet2", ftplet2);

        container.beforeCommand(new FtpSessionImpl(null), new FtpRequestImpl(
                "RETR"));

        assertEquals(2, calls.size());
        assertEquals("ftplet1", calls.get(0));
        assertEquals("ftplet2", calls.get(1));
    }

    public void testOnDownloadEnd() throws FtpException, IOException {
        MockFtplet ftplet1 = new MockFtplet() {
            public FtpletResult onDownloadEnd(FtpSession session,
                    FtpRequest request) throws FtpException, IOException {
                calls.add("ftplet1");
                return super.onDownloadEnd(session, request);
            }
        };
        MockFtplet ftplet2 = new MockFtplet() {
            public FtpletResult onDownloadEnd(FtpSession session,
                    FtpRequest request) throws FtpException, IOException {
                calls.add("ftplet2");
                return super.onDownloadEnd(session, request);
            }
        };

        container.addFtplet("ftplet1", ftplet1);
        container.addFtplet("ftplet2", ftplet2);

        container.afterCommand(new FtpSessionImpl(null), new FtpRequestImpl(
                "RETR"));

        assertEquals(2, calls.size());
        assertEquals("ftplet1", calls.get(0));
        assertEquals("ftplet2", calls.get(1));
    }

    public void testOnRmdirStart() throws FtpException, IOException {
        MockFtplet ftplet1 = new MockFtplet() {
            public FtpletResult onRmdirStart(FtpSession session,
                    FtpRequest request) throws FtpException, IOException {
                calls.add("ftplet1");
                return super.onRmdirStart(session, request);
            }
        };
        MockFtplet ftplet2 = new MockFtplet() {
            public FtpletResult onRmdirStart(FtpSession session,
                    FtpRequest request) throws FtpException, IOException {
                calls.add("ftplet2");
                return super.onRmdirStart(session, request);
            }
        };

        container.addFtplet("ftplet1", ftplet1);
        container.addFtplet("ftplet2", ftplet2);

        container.beforeCommand(new FtpSessionImpl(null), new FtpRequestImpl(
                "RMD"));

        assertEquals(2, calls.size());
        assertEquals("ftplet1", calls.get(0));
        assertEquals("ftplet2", calls.get(1));
    }

    public void testOnRmdirEnd() throws FtpException, IOException {
        MockFtplet ftplet1 = new MockFtplet() {
            public FtpletResult onRmdirEnd(FtpSession session, FtpRequest request)
                    throws FtpException, IOException {
                calls.add("ftplet1");
                return super.onRmdirEnd(session, request);
            }
        };
        MockFtplet ftplet2 = new MockFtplet() {
            public FtpletResult onRmdirEnd(FtpSession session, FtpRequest request)
                    throws FtpException, IOException {
                calls.add("ftplet2");
                return super.onRmdirEnd(session, request);
            }
        };

        container.addFtplet("ftplet1", ftplet1);
        container.addFtplet("ftplet2", ftplet2);

        container.afterCommand(new FtpSessionImpl(null), new FtpRequestImpl(
                "RMD"));

        assertEquals(2, calls.size());
        assertEquals("ftplet1", calls.get(0));
        assertEquals("ftplet2", calls.get(1));
    }

    public void testOnMkdirStart() throws FtpException, IOException {
        MockFtplet ftplet1 = new MockFtplet() {
            public FtpletResult onMkdirStart(FtpSession session,
                    FtpRequest request) throws FtpException, IOException {
                calls.add("ftplet1");
                return super.onMkdirStart(session, request);
            }
        };
        MockFtplet ftplet2 = new MockFtplet() {
            public FtpletResult onMkdirStart(FtpSession session,
                    FtpRequest request) throws FtpException, IOException {
                calls.add("ftplet2");
                return super.onMkdirStart(session, request);
            }
        };

        container.addFtplet("ftplet1", ftplet1);
        container.addFtplet("ftplet2", ftplet2);

        container.beforeCommand(new FtpSessionImpl(null), new FtpRequestImpl(
                "MKD"));

        assertEquals(2, calls.size());
        assertEquals("ftplet1", calls.get(0));
        assertEquals("ftplet2", calls.get(1));
    }

    public void testOnMkdirEnd() throws FtpException, IOException {
        MockFtplet ftplet1 = new MockFtplet() {
            public FtpletResult onMkdirEnd(FtpSession session, FtpRequest request)
                    throws FtpException, IOException {
                calls.add("ftplet1");
                return super.onMkdirEnd(session, request);
            }
        };
        MockFtplet ftplet2 = new MockFtplet() {
            public FtpletResult onMkdirEnd(FtpSession session, FtpRequest request)
                    throws FtpException, IOException {
                calls.add("ftplet2");
                return super.onMkdirEnd(session, request);
            }
        };

        container.addFtplet("ftplet1", ftplet1);
        container.addFtplet("ftplet2", ftplet2);

        container.afterCommand(new FtpSessionImpl(null), new FtpRequestImpl(
                "MKD"));

        assertEquals(2, calls.size());
        assertEquals("ftplet1", calls.get(0));
        assertEquals("ftplet2", calls.get(1));
    }

    public void testOnAppendStart() throws FtpException, IOException {
        MockFtplet ftplet1 = new MockFtplet() {
            public FtpletResult onAppendStart(FtpSession session,
                    FtpRequest request) throws FtpException, IOException {
                calls.add("ftplet1");
                return super.onAppendStart(session, request);
            }
        };
        MockFtplet ftplet2 = new MockFtplet() {
            public FtpletResult onAppendStart(FtpSession session,
                    FtpRequest request) throws FtpException, IOException {
                calls.add("ftplet2");
                return super.onAppendStart(session, request);
            }
        };

        container.addFtplet("ftplet1", ftplet1);
        container.addFtplet("ftplet2", ftplet2);

        container.beforeCommand(new FtpSessionImpl(null), new FtpRequestImpl(
                "APPE"));

        assertEquals(2, calls.size());
        assertEquals("ftplet1", calls.get(0));
        assertEquals("ftplet2", calls.get(1));
    }

    public void testOnAppendEnd() throws FtpException, IOException {
        MockFtplet ftplet1 = new MockFtplet() {
            public FtpletResult onAppendEnd(FtpSession session, FtpRequest request)
                    throws FtpException, IOException {
                calls.add("ftplet1");
                return super.onAppendEnd(session, request);
            }
        };
        MockFtplet ftplet2 = new MockFtplet() {
            public FtpletResult onAppendEnd(FtpSession session, FtpRequest request)
                    throws FtpException, IOException {
                calls.add("ftplet2");
                return super.onAppendEnd(session, request);
            }
        };

        container.addFtplet("ftplet1", ftplet1);
        container.addFtplet("ftplet2", ftplet2);

        container.afterCommand(new FtpSessionImpl(null), new FtpRequestImpl(
                "APPE"));

        assertEquals(2, calls.size());
        assertEquals("ftplet1", calls.get(0));
        assertEquals("ftplet2", calls.get(1));
    }

    public void testOnUploadUniqueStart() throws FtpException, IOException {
        MockFtplet ftplet1 = new MockFtplet() {
            public FtpletResult onUploadUniqueStart(FtpSession session,
                    FtpRequest request) throws FtpException, IOException {
                calls.add("ftplet1");
                return super.onUploadUniqueStart(session, request);
            }
        };
        MockFtplet ftplet2 = new MockFtplet() {
            public FtpletResult onUploadUniqueStart(FtpSession session,
                    FtpRequest request) throws FtpException, IOException {
                calls.add("ftplet2");
                return super.onUploadUniqueStart(session, request);
            }
        };

        container.addFtplet("ftplet1", ftplet1);
        container.addFtplet("ftplet2", ftplet2);

        container.beforeCommand(new FtpSessionImpl(null), new FtpRequestImpl(
                "STOU"));

        assertEquals(2, calls.size());
        assertEquals("ftplet1", calls.get(0));
        assertEquals("ftplet2", calls.get(1));
    }

    public void testOnUploadUniqueEnd() throws FtpException, IOException {
        MockFtplet ftplet1 = new MockFtplet() {
            public FtpletResult onUploadUniqueEnd(FtpSession session,
                    FtpRequest request) throws FtpException, IOException {
                calls.add("ftplet1");
                return super.onUploadUniqueEnd(session, request);
            }
        };
        MockFtplet ftplet2 = new MockFtplet() {
            public FtpletResult onUploadUniqueEnd(FtpSession session,
                    FtpRequest request) throws FtpException, IOException {
                calls.add("ftplet2");
                return super.onUploadUniqueEnd(session, request);
            }
        };

        container.addFtplet("ftplet1", ftplet1);
        container.addFtplet("ftplet2", ftplet2);

        container.afterCommand(new FtpSessionImpl(null), new FtpRequestImpl(
                "STOU"));

        assertEquals(2, calls.size());
        assertEquals("ftplet1", calls.get(0));
        assertEquals("ftplet2", calls.get(1));
    }

    public void testOnRenameStart() throws FtpException, IOException {
        MockFtplet ftplet1 = new MockFtplet() {
            public FtpletResult onRenameStart(FtpSession session,
                    FtpRequest request) throws FtpException, IOException {
                calls.add("ftplet1");
                return super.onRenameStart(session, request);
            }
        };
        MockFtplet ftplet2 = new MockFtplet() {
            public FtpletResult onRenameStart(FtpSession session,
                    FtpRequest request) throws FtpException, IOException {
                calls.add("ftplet2");
                return super.onRenameStart(session, request);
            }
        };

        container.addFtplet("ftplet1", ftplet1);
        container.addFtplet("ftplet2", ftplet2);

        container.beforeCommand(new FtpSessionImpl(null), new FtpRequestImpl(
                "RNTO"));

        assertEquals(2, calls.size());
        assertEquals("ftplet1", calls.get(0));
        assertEquals("ftplet2", calls.get(1));
    }

    public void testOnRenameEnd() throws FtpException, IOException {
        MockFtplet ftplet1 = new MockFtplet() {
            public FtpletResult onRenameEnd(FtpSession session, FtpRequest request)
                    throws FtpException, IOException {
                calls.add("ftplet1");
                return super.onRenameEnd(session, request);
            }
        };
        MockFtplet ftplet2 = new MockFtplet() {
            public FtpletResult onRenameEnd(FtpSession session, FtpRequest request)
                    throws FtpException, IOException {
                calls.add("ftplet2");
                return super.onRenameEnd(session, request);
            }
        };

        container.addFtplet("ftplet1", ftplet1);
        container.addFtplet("ftplet2", ftplet2);

        container.afterCommand(new FtpSessionImpl(null), new FtpRequestImpl(
                "RNTO"));

        assertEquals(2, calls.size());
        assertEquals("ftplet1", calls.get(0));
        assertEquals("ftplet2", calls.get(1));
    }

    public void testOnSite() throws FtpException, IOException {
        MockFtplet ftplet1 = new MockFtplet() {
            public FtpletResult onSite(FtpSession session, FtpRequest request)
                    throws FtpException, IOException {
                calls.add("ftplet1");
                return super.onSite(session, request);
            }
        };
        MockFtplet ftplet2 = new MockFtplet() {
            public FtpletResult onSite(FtpSession session, FtpRequest request)
                    throws FtpException, IOException {
                calls.add("ftplet2");
                return super.onSite(session, request);
            }
        };

        container.addFtplet("ftplet1", ftplet1);
        container.addFtplet("ftplet2", ftplet2);

        container.afterCommand(new FtpSessionImpl(null), new FtpRequestImpl(
                "SITE"));

        assertEquals(2, calls.size());
        assertEquals("ftplet1", calls.get(0));
        assertEquals("ftplet2", calls.get(1));
    }
}