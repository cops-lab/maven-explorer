/*
 * Copyright 2021 Delft University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.c0ps.mx.runner;

import dev.c0ps.diapper.Runner;
import dev.c0ps.diapper.VmArgs;

public class Main {

    private Main() {
        // don't instantiate
    }

    public static void main(String[] rawArgs) {
        VmArgs.log(rawArgs);
        var runner = new Runner(new LogSettings(), "dev.c0ps.mx");
        runner.run(rawArgs);
    }
}