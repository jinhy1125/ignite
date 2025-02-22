/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ignite.internal.commandline;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Consumer;
import org.apache.ignite.internal.client.GridClient;
import org.apache.ignite.internal.dto.IgniteDataTransferObject;
import org.apache.ignite.internal.management.api.Argument;
import org.apache.ignite.internal.management.api.Command;
import org.apache.ignite.internal.management.api.LocalCommand;
import org.apache.ignite.internal.util.typedef.internal.U;

/**
 * Additional commands provider for control utility.
 */
public class CommandsProviderExtImpl implements CommandsProvider {
    /** */
    public static final Command<?, ?> TEST_COMMAND = new TestCommandCommand();

    /** */
    public static final String TEST_COMMAND_OUTPUT = "Test command executed";

    /** */
    public static final String TEST_COMMAND_USAGE = "Test command usage.";

    /** */
    public static final String TEST_COMMAND_ARG = "--test-print";

    /** {@inheritDoc} */
    @Override public Collection<Command<?, ?>> commands() {
        return Collections.singleton(TEST_COMMAND);
    }

    /** */
    public static class TestCommandCommand implements LocalCommand<TestCommandCommandArg, Void> {
        /** {@inheritDoc} */
        @Override public String description() {
            return TEST_COMMAND_USAGE;
        }

        /** {@inheritDoc} */
        @Override public Class<TestCommandCommandArg> argClass() {
            return TestCommandCommandArg.class;
        }

        /** {@inheritDoc} */
        @Override public Void execute(GridClient cli, TestCommandCommandArg arg, Consumer<String> printer) {
            printer.accept(TEST_COMMAND_OUTPUT + ": " + arg.testPrint);

            return null;
        }
    }

    /** */
    public static class TestCommandCommandArg extends IgniteDataTransferObject {
        /** */
        private static final long serialVersionUID = 0;

        /** */
        @Argument
        private String testPrint;

        /** {@inheritDoc} */
        @Override protected void writeExternalData(ObjectOutput out) throws IOException {
            U.writeString(out, testPrint);
        }

        /** {@inheritDoc} */
        @Override protected void readExternalData(byte protoVer, ObjectInput in) throws IOException {
            testPrint = U.readString(in);
        }

        /** */
        public void testPrint(String testPrint) {
            this.testPrint = testPrint;
        }

        /** */
        public String testPrint() {
            return testPrint;
        }
    }
}
