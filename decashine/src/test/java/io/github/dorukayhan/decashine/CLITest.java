/*
 * Decashine
 * Copyright (C) 2023  Doruk Ayhan
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package io.github.dorukayhan.decashine;

import org.junit.jupiter.api.Test;

import net.sourceforge.argparse4j.inf.Namespace;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;

class CLITest {
    @Test void testParseArgs() {
        var cli = new CLI();
        String systemFile   = "system.json",
               inlineTarget = "{\"okay egg\":1}",
               targetFile   = "target.json",
               runCount     = "727000";
        
        Namespace SFIT = assertDoesNotThrow(() -> cli.parseArgs(new String[]{"-sf", systemFile, "-t", inlineTarget}));
        assertAll("system file inline target",
            () -> assertEquals(SFIT.get(cli.systemFileDest), systemFile),
            () -> assertNull(SFIT.get(cli.systemDest)),
            () -> assertEquals(SFIT.get(cli.targetDest), inlineTarget),
            () -> assertNull(SFIT.get(cli.targetFileDest)),
            () -> assertEquals(SFIT.get(cli.runCountDest), cli.defaultRunCount)
        );

        Namespace SFTF = assertDoesNotThrow(() -> cli.parseArgs(new String[]{"-tf", targetFile, "-n", runCount, "-sf", systemFile}));
        assertAll("system file target file (-n too)",
            () -> assertEquals(SFTF.get(cli.systemFileDest), systemFile),
            () -> assertNull(SFTF.get(cli.systemDest)),
            () -> assertEquals(SFTF.get(cli.targetFileDest), targetFile),
            () -> assertNull(SFTF.get(cli.targetDest)),
            () -> assertEquals(SFTF.get(cli.runCountDest), Integer.valueOf(runCount))
        );
    }

    @Test void testReadJson() throws IOException {
        var cli = new CLI();
        var inlineObj = "{\"7\":\"27\"}";
        var fileObj = "{\"when\":\"you\"}";
        var file = File.createTempFile("trj", ".tmp");
        try (var fw = new FileWriter(file, Charset.forName("UTF-8"), false)) {
            fw.write(fileObj);
        }
        // inline with null filename
        var inlineWNullFile = cli.readJson(inlineObj, null);
        assertAll("inline json",
            () -> assertEquals(inlineWNullFile.getString("7"), "27"),
            () -> assertNull(inlineWNullFile.getJsonString("when"))
        );
        // file with null inline
        var fileWNullInline = cli.readJson(null, file.getAbsolutePath());
        assertAll("json from file",
            () -> assertNull(fileWNullInline.getJsonString("7")),
            () -> assertEquals(fileWNullInline.getString("when"), "you")
        );
        // undefined behavior if both are non-null, no need to test that
        file.delete();
    }
}
